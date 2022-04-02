package com.example.openmusic.service;

import static android.service.controls.ControlsProviderService.TAG;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.media.session.MediaButtonReceiver;
import androidx.recyclerview.widget.RecyclerView;

import com.example.openmusic.MainActivity;
import com.example.openmusic.PlayerController;
import com.example.openmusic.R;
import com.example.openmusic.fragments.SongControlFragment;
import com.example.openmusic.fragments.SongListFragment;
import com.example.openmusic.models.Player;
import com.example.openmusic.models.Song;

import java.io.IOException;

public class PlayerService extends Service {


    private final int NOTIFICATION_ID = 404;
    private final String NOTIFICATION_DEFAULT_CHANNEL_ID = "default_channel";

    private MediaSessionCompat mediaSession;
    private AudioManager audioManager;
    private boolean audioFocusRequested = false;
    private AudioFocusRequest audioFocusRequest;
    private MusicRepository musicRepository;

    private final MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder();

    private final PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder().setActions(
            PlaybackStateCompat.ACTION_PLAY
                    | PlaybackStateCompat.ACTION_STOP
                    | PlaybackStateCompat.ACTION_PAUSE
                    | PlaybackStateCompat.ACTION_PLAY_PAUSE
                    | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                    | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
    );


    Player player;
    private int current_song = 0;


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            @SuppressLint("WrongConstant") NotificationChannel notificationChannel =
                    new NotificationChannel(NOTIFICATION_DEFAULT_CHANNEL_ID,
                            getString(R.string.notification_channel_name),
                            NotificationManagerCompat.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = (NotificationManager)
                    getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);

            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();
            audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setOnAudioFocusChangeListener(audioFocusChangeListener)
                    .setAcceptsDelayedFocusGain(false)
                    .setWillPauseWhenDucked(true)
                    .setAudioAttributes(audioAttributes)
                    .build();
        }

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);



        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                0, mediaButtonIntent,
                PendingIntent.FLAG_IMMUTABLE
        );
        mediaSession = new MediaSessionCompat(this, "PlayerService",
                null, pendingIntent);
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
                | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setCallback(mediaSessionCallback);


        //Context appContext = getApplicationContext();

        Intent activityIntent = new Intent(this, MainActivity.class);
        mediaSession.setSessionActivity(PendingIntent.getActivity(this,
                0, activityIntent, PendingIntent.FLAG_IMMUTABLE));

        player = PlayerController.getPlayer();
        musicRepository = MusicRepository.getMusicRepository();


  /*      AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();
        audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setOnAudioFocusChangeListener(audioFocusChangeListener)
                .setAcceptsDelayedFocusGain(false)
                .setWillPauseWhenDucked(true)
                .setAudioAttributes(audioAttributes)
                .build();*/
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MediaButtonReceiver.handleIntent(mediaSession, intent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaSession.release();
        player.getPlayer().release();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new PlayerServiceBinder();
    }

    public class PlayerServiceBinder extends Binder {
        public MediaSessionCompat.Token getMediaSessionToken() {
            return mediaSession.getSessionToken();
        }
    }

    public MediaSessionCompat.Callback mediaSessionCallback = new MediaSessionCompat.Callback() {

        int currentState = PlaybackStateCompat.STATE_STOPPED;

        @Override
        public void onPlay() {
            if (!player.getPlayer().isPlaying()) {
                startService(new Intent(getApplicationContext(), PlayerService.class));
                setStatePlay();
            }

            if(currentState == PlaybackStateCompat.STATE_PAUSED
                    && current_song == musicRepository.getCurrentItemIndex()){
                player.start();
            }else{
                Song song = musicRepository.getCurrent();
                prepareToPlay(song);
                updateMetadataFromTrack(song);
            }


            mediaSession.setPlaybackState(stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,
                    PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1).build());
            currentState = PlaybackStateCompat.STATE_PLAYING;

            refreshNotificationAndForegroundStatus(currentState);

        }

        @Override
        public void onPause() {
            if (player.getPlayer().isPlaying()) {
                player.pause();
                unregisterReceiver(becomingNoisyReceiver);
            }

            currentState = PlaybackStateCompat.STATE_PAUSED;
            mediaSession.setPlaybackState(stateBuilder.setState(PlaybackStateCompat.STATE_PAUSED,
                    PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1).build());
            refreshNotificationAndForegroundStatus(currentState);
        }

        @Override
        public void onSeekTo(long pos) {
            super.onSeekTo(pos);
            player.seekTo((int) pos);
        }

        @Override
        public void onStop() {
            if (player.getPlayer().isPlaying()) {
                player.stop();
                unregisterReceiver(becomingNoisyReceiver);
            }

            Log.i("MyTAG", "onStop audioFocusRequested = " + audioFocusRequested);

            if (audioFocusRequested) {
                audioFocusRequested = false;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    audioManager.abandonAudioFocusRequest(audioFocusRequest);
                } else {
                    audioManager.abandonAudioFocus(audioFocusChangeListener);
                }
            }

            mediaSession.setActive(false);

            mediaSession.setPlaybackState(stateBuilder.setState(PlaybackStateCompat.STATE_STOPPED,
                    PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1).build());
            currentState = PlaybackStateCompat.STATE_STOPPED;

            refreshNotificationAndForegroundStatus(currentState);

            stopSelf();
        }

        @Override
        public void onSkipToNext() {
            Song song = musicRepository.getNext();
            setStatePlay();
            refreshNotificationAndForegroundStatus(currentState);
            updateMetadataFromTrack(song);
            prepareToPlay(song);

        }

        @Override
        public void onSkipToPrevious() {
            Song song = musicRepository.getPrevious();
            setStatePlay();
            refreshNotificationAndForegroundStatus(currentState);
            updateMetadataFromTrack(song);
            prepareToPlay(song);
        }


        private void prepareToPlay(Song song) {
            mListener.setSongData(song);
            //mListener.setDuration(song.getDuration());
            current_song = musicRepository.getSongs().indexOf(song);

            player.playSong(song, getApplicationContext());

        }

        private void updateMetadataFromTrack(Song song) {
            metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.getTitle());
            metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, song.getArtist());
            metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.getArtist());
           // metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, player.getPlayer().getDuration());
            mediaSession.setMetadata(metadataBuilder.build());
        }

        public void setStatePlay(){

            Log.i("MyTAG", "setStatePlay audioFocusRequested = " + audioFocusRequested);
            if (!audioFocusRequested) {
                audioFocusRequested = true;

                int audioFocusResult;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    audioFocusResult = audioManager.requestAudioFocus(audioFocusRequest);
                } else {
                    audioFocusResult = audioManager.requestAudioFocus(audioFocusChangeListener,
                            AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
                }
                if (audioFocusResult != AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
                    return;
            }

            mediaSession.setActive(true); // Сразу после получения фокуса

            registerReceiver(becomingNoisyReceiver,
                    new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
        }
    };



    private final BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Disconnecting headphones - stop playback
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                mediaSessionCallback.onPause();
            }
        }
    };

    private AudioManager.OnAudioFocusChangeListener audioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                /*case AudioManager.AUDIOFOCUS_GAIN:
                    mediaSessionCallback.onPlay(); // Не очень красиво
                    break;*/
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    mediaSessionCallback.onPause();
                    break;
                default:
                    mediaSessionCallback.onPause();
                    break;
            }
        }
    };

    private void refreshNotificationAndForegroundStatus(int playbackState) {
        switch (playbackState) {
            case PlaybackStateCompat.STATE_PLAYING: {
                final NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                try{
                    final Notification notification = getNotification(playbackState);
                    // аля так
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        NotificationChannel nc = new NotificationChannel(NOTIFICATION_DEFAULT_CHANNEL_ID,
                                "PlayerService", NotificationManager.IMPORTANCE_DEFAULT);
                        nm.createNotificationChannel(nc);
                    }

                    nm.notify(NOTIFICATION_ID, notification);
                    startForeground(NOTIFICATION_ID, notification);
                }catch (NullPointerException e){

                }

                break;
            }
            case PlaybackStateCompat.STATE_PAUSED: {
                try{
                    NotificationManagerCompat.from(PlayerService.this)
                            .notify(NOTIFICATION_ID, getNotification(playbackState));
                    stopForeground(false);
                }catch (NullPointerException e){

                }

                break;
            }
            default: {
                stopForeground(true);
                break;
            }
        }
    }


    public static PendingIntent myBuildMediaButtonPendingIntent(Context context,
                                                         @PlaybackStateCompat.MediaKeyAction long action){
        @SuppressLint("RestrictedApi")
        ComponentName mbrComponent = MediaButtonReceiver.getMediaButtonReceiverComponent(context);
        if (mbrComponent == null) {
            Log.w(TAG, "A unique media button receiver could not be found in the given context, so "
                    + "couldn't build a pending intent.");
            return null;
        }
        return myBuildMediaButtonPendingIntent(context, mbrComponent, action);
    }
    public static PendingIntent myBuildMediaButtonPendingIntent(Context context, ComponentName mbrComponent,
                                                         @PlaybackStateCompat.MediaKeyAction long action){
        if (mbrComponent == null) {
            Log.w(TAG, "The component name of media button receiver should be provided.");
            return null;
        }
        int keyCode = PlaybackStateCompat.toKeyCode(action);
        if (keyCode == KeyEvent.KEYCODE_UNKNOWN) {
            Log.w(TAG,
                    "Cannot build a media button pending intent with the given action: " + action);
            return null;
        }
        Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        intent.setComponent(mbrComponent);
        intent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
        return PendingIntent.getBroadcast(context, keyCode, intent, PendingIntent.FLAG_IMMUTABLE);
    }

    private Notification getNotification(int playbackState) {
        try{
            NotificationCompat.Builder builder = MediaStyleHelper.from(this, mediaSession);
            builder.addAction(new NotificationCompat.Action(android.R.drawable.ic_media_previous, "previous",
                    myBuildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)));

            if (playbackState == PlaybackStateCompat.STATE_PLAYING)
                builder.addAction(new NotificationCompat.Action(android.R.drawable.ic_media_pause, "pause",
                        myBuildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PLAY_PAUSE)));
            else
                builder.addAction(new NotificationCompat.Action(android.R.drawable.ic_media_play, "play",
                        myBuildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PLAY_PAUSE)));

            builder.addAction(new NotificationCompat.Action(android.R.drawable.ic_media_next, "next",
                    myBuildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_SKIP_TO_NEXT)));


            builder.setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(1)
                    .setShowCancelButton(true)
                    .setCancelButtonIntent(myBuildMediaButtonPendingIntent(this,
                            PlaybackStateCompat.ACTION_STOP))
                    .setMediaSession(mediaSession.getSessionToken())); // setMediaSession требуется для Android Wear

            builder.setSmallIcon(R.drawable.ic_group);
            builder.setColor(ContextCompat.getColor(this, R.color.black)); // The whole background (in MediaStyle), not just icon background
            builder.setShowWhen(false);

            builder.setPriority(NotificationCompat.PRIORITY_HIGH);
            builder.setOnlyAlertOnce(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder.setChannelId(NOTIFICATION_DEFAULT_CHANNEL_ID);
            }


            return builder.build();
        }catch (NullPointerException e){

        }
        return null;
    }


    // создаем поле объекта-колбэка
    private static MainActivityListener mListener;

    // создаем сам интерфейс и указываем метод и передаваемые им аргументы
    // View на котором произошло событие и позиция этого View
    public interface MainActivityListener {
        void setSongData(Song song);
        void setDuration(int duration);

    }

    // метод-сеттер для привязки колбэка к получателю событий
    public void setMainActivityListener(MainActivityListener listener) {
        mListener = listener;
    }

}
