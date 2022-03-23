package com.example.openmusic.fragments;

import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.openmusic.MainActivity;
import com.example.openmusic.models.Player;
import com.example.openmusic.PlayerController;
import com.example.openmusic.R;
import com.example.openmusic.models.Song;
import com.example.openmusic.service.PlayerService;


public class SongControlFragment extends Fragment implements Player.OnPlayerListener , PlayerService.MainActivityListener {

    ImageButton btnBack, btnPause, btnNext;
    SeekBar seekBar;
    TextView elapsed, txtSongName, txtSongAuthor;
    Player player;
    Handler seekHandler = new Handler();

    PlayerService service;
    Animation animScale;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_song_control, container, false);

        player = PlayerController.getPlayer();
        service = new PlayerService();
        service.setMainActivityListener(this);

        player.setOnPlayerListener(this);

        btnBack = v.findViewById(R.id.btnBack);
        btnPause = v.findViewById(R.id.btnPause);
        btnNext = v.findViewById(R.id.btnNext);
        seekBar = v.findViewById(R.id.seekBar);
        elapsed = v.findViewById(R.id.elapsed);
        txtSongName = v.findViewById(R.id.txtSongName);
        txtSongAuthor = v.findViewById(R.id.txtSongAuthor);

        animScale = AnimationUtils.loadAnimation(getContext(), R.anim.scale);

        btnBack.setOnClickListener(this::clickBack);
        btnPause.setOnClickListener(this::clickPause);
        btnNext.setOnClickListener(this::clickNext);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
               mListener.seekTo(seekBar.getProgress());
            }
        });

        return v;
    }


    public void clickBack(View view){
        view.startAnimation(animScale);
        mListener.clickBack();
    }

    public void clickPause(View view){
        view.startAnimation(animScale);
        mListener.clickPause();
    }

    public void clickNext(View view){
        view.startAnimation(animScale);
        mListener.clickNext();
    }

    // создаем поле объекта-колбэка
    private static SongControlFragmentListener mListener;

    @Override
    public void OnBufferingUpdateListener(int percent) {
         seekBar.setSecondaryProgress(percent);

    }

    @Override
    public void OnCompletionListener(MediaPlayer mp) {
         mListener.clickNext();
         seekBar.setProgress(mp.getCurrentPosition());

    }

    @Override
    public void setSongData(Song song, int duration) {
         seekBar.setProgress(0);
         seekBar.setMax(duration);
        // Updating progress bar
        seekHandler.postDelayed(updateSeekBar, 15);

        txtSongName.setText(song.getTitle());
        if(song.getArtist() != null){
            if(song.getArtist().length() > 30){
                txtSongAuthor.setText(song.getArtist().substring(0,30) + "...");
            }else{
                txtSongAuthor.setText(song.getArtist());
            }
        }else
            txtSongAuthor.setText(song.getArtist());


    }

    @Override
    public void changeImageResourceBtnPause() {
        if(player.getPlayer().isPlaying()){
            btnPause.setImageResource(R.drawable.ic_baseline_pause_24);
        }else{
            btnPause.setImageResource(R.drawable.ic_baseline_play_arrow_24);
        }
    }

    // создаем сам интерфейс и указываем метод и передаваемые им аргументы
    // View на котором произошло событие и позиция этого View
    public interface SongControlFragmentListener {
        void clickBack();
        void clickPause();
        void clickNext();
        void seekTo(int progress);
    }



    // метод-сеттер для привязки колбэка к получателю событий
    public void setSongControlFragmentListener(SongControlFragmentListener listener) {
        mListener = listener;
    }

    private Runnable updateSeekBar = new Runnable() {
        public void run() {
            //long totalDuration = PlayerController.getPlayer().getPlayer().getDuration();
            long currentDuration = PlayerController.getPlayer().getPlayer().getCurrentPosition();

            // Displaying Total Duration time
           //  remaining.setText(""+ milliSecondsToTimer(totalDuration-currentDuration));
            // Displaying time completed playing
            elapsed.setText(""+ milliSecondsToTimer(currentDuration));

            // Updating progress bar
             seekBar.setProgress((int)currentDuration);

            // Call this thread again after 15 milliseconds => ~ 1000/60fps
            seekHandler.postDelayed(this, 15);
        }
    };

    public String milliSecondsToTimer(long milliseconds){
        String finalTimerString = "";
        String secondsString = "";

        // Convert total duration into time
        int hours = (int)( milliseconds / (1000*60*60));
        int minutes = (int)(milliseconds % (1000*60*60)) / (1000*60);
        int seconds = (int) ((milliseconds % (1000*60*60)) % (1000*60) / 1000);
        // Add hours if there
        if(hours > 0){
            finalTimerString = hours + ":";
        }

        // Prepending 0 to seconds if it is one digit
        if(seconds < 10) {
            secondsString = "0" + seconds;
        }else {
            secondsString = "" + seconds;
        }

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        // return timer string
        return finalTimerString;
    }
}