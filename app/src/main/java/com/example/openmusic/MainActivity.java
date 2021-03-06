package com.example.openmusic;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.File;


import com.example.openmusic.adpters.SongAdapter;
import com.example.openmusic.fragments.DownloadSongFragment;
import com.example.openmusic.fragments.PlaylistsFragment;
import com.example.openmusic.fragments.SongControlFragment;
import com.example.openmusic.fragments.SongListFragment;
import com.example.openmusic.models.Song;
import com.example.openmusic.service.MusicRepository;
import com.example.openmusic.service.PlayerService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity implements
        SongAdapter.OnCardClickListener,
        SongListFragment.SongListFragmentListener,
        SongControlFragment.SongControlFragmentListener,
        DownloadSongFragment.DownloadSongFragmentListener,
        PlaylistsFragment.PlaylistsFragmentListener
{

    SongAdapter adapter;

    private SongListFragment songListFragment;
    private SongControlFragment songControlFragment;
    private DownloadSongFragment downloadSongFragment;
    private PlaylistsFragment playlistsFragment;

    private static final int REQUEST_CODE_READ_FILES = 1;
    private static boolean READ_FILES_GRANTED = false;

    Handler permissionHandler = new Handler();

    BottomNavigationView bottomNavigationView;

    private PlayerService.PlayerServiceBinder playerServiceBinder;
    private MediaControllerCompat mediaController;
    private MediaControllerCompat.Callback callback;
    private ServiceConnection serviceConnection;
    private MusicRepository musicRepository;

    private final String SIMPLE_SONG_CONTROL_FRAGMENT_TAG = "SIMPLE_SONG_CONTROL_FRAGMENT_TAG";
    private final String SIMPLE_SONG_LIST_FRAGMENT_TAG = "SIMPLE_SONG_LIST_FRAGMENT_TAG";
    private final String SIMPLE_DOWNLOAD_SONG_FRAGMENT_TAG = "SIMPLE_DOWNLOAD_SONG_FRAGMENT_TAG";
    private final String SIMPLE_PLAYLIST_FRAGMENT_TAG = "SIMPLE_PLAYLIST_FRAGMENT_TAG";


    private String current_fragment = SIMPLE_SONG_LIST_FRAGMENT_TAG;



    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("current_fragment", current_fragment);
        super.onSaveInstanceState(outState);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
/*        Player player = PlayerController.getPlayer();
        if(player.getPlayer().isPlaying())
            mediaController.getTransportControls().pause();*/
        playerServiceBinder = null;
        if (mediaController != null) {
            mediaController.unregisterCallback(callback);
            mediaController = null;
        }
        unbindService(serviceConnection);
    }

    // ???????????????????? ?????????? ??????, ?????? ???????????????????? ???????????????????? "??????????????".
    @Override
    public void onRestart(){
        super.onRestart();
        setPermission();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        callback = new MediaControllerCompat.Callback() {
            @Override
            public void onPlaybackStateChanged(PlaybackStateCompat state) {
                if (state == null)
                    return;
            }

        };

        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                playerServiceBinder = (PlayerService.PlayerServiceBinder) service;
                if(playerServiceBinder == null){
                    mediaController = Instances.getMediaSessionCompat(MainActivity.this,
                            playerServiceBinder.getMediaSessionToken());

                    mediaController.registerCallback(callback);
                    callback.onPlaybackStateChanged(mediaController.getPlaybackState());
                }

            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                playerServiceBinder = null;
                if (mediaController != null) {
                    mediaController.unregisterCallback(callback);
                    mediaController = null;
                }
            }
        };

        bindService(new Intent(this, PlayerService.class), serviceConnection, BIND_AUTO_CREATE);

        if (savedInstanceState != null) { // saved instance state, fragment may exist
            // look up the instance that already exists by tag
            songControlFragment = (SongControlFragment)
                    getSupportFragmentManager().findFragmentByTag(SIMPLE_SONG_CONTROL_FRAGMENT_TAG);

            songListFragment = (SongListFragment)
                    getSupportFragmentManager().findFragmentByTag(SIMPLE_SONG_LIST_FRAGMENT_TAG);

            downloadSongFragment = (DownloadSongFragment)
                    getSupportFragmentManager().findFragmentByTag(SIMPLE_DOWNLOAD_SONG_FRAGMENT_TAG);

            playlistsFragment = (PlaylistsFragment)
                    getSupportFragmentManager().findFragmentByTag(SIMPLE_PLAYLIST_FRAGMENT_TAG);

            current_fragment = savedInstanceState.getString("current_fragment");

        }
        // only create fragment if they haven't been instantiated already
        if (songControlFragment == null) {
            songControlFragment = new SongControlFragment();
        }
        if (songListFragment == null) {
            songListFragment = new SongListFragment();
        }
        if (downloadSongFragment == null) {
            downloadSongFragment = new DownloadSongFragment();
        }
        if(playlistsFragment == null){
            playlistsFragment = new PlaylistsFragment();
        }

        downloadSongFragment.setSongListFragmentListener(this);
        songControlFragment.setSongControlFragmentListener(this);
        songListFragment.setSongListFragmentListener(this);
        playlistsFragment.setPlaylistsFragmentListener(this);

        musicRepository = MusicRepository.getMusicRepository();
        musicRepository.updateSongList(this);



        bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.page_1:{
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.fragment, songListFragment, SIMPLE_SONG_LIST_FRAGMENT_TAG);
                        fragmentTransaction.commit();
                        current_fragment = SIMPLE_SONG_LIST_FRAGMENT_TAG;
                        return true;
                    }
                    case R.id.page_2:{
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.fragment, downloadSongFragment, SIMPLE_DOWNLOAD_SONG_FRAGMENT_TAG);
                        fragmentTransaction.commit();
                        current_fragment = SIMPLE_DOWNLOAD_SONG_FRAGMENT_TAG;
                        return true;
                    }
                    case R.id.page_3:{
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.fragment, playlistsFragment, SIMPLE_PLAYLIST_FRAGMENT_TAG);
                        fragmentTransaction.commit();
                        current_fragment = SIMPLE_PLAYLIST_FRAGMENT_TAG;
                        return true;
                    }
                }

                return false;
            }
        });

        FragmentManager fragmentManager = getSupportFragmentManager();


        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if(current_fragment.equals(SIMPLE_SONG_LIST_FRAGMENT_TAG)){
            fragmentTransaction.replace(R.id.fragment, songListFragment, SIMPLE_SONG_LIST_FRAGMENT_TAG);
        }else if(current_fragment.equals(SIMPLE_DOWNLOAD_SONG_FRAGMENT_TAG)){
            fragmentTransaction.replace(R.id.fragment, downloadSongFragment, SIMPLE_DOWNLOAD_SONG_FRAGMENT_TAG);
        }
        fragmentTransaction.commit();

        FragmentTransaction fragmentTransactionSongControl = fragmentManager.beginTransaction();
        fragmentTransactionSongControl.replace(R.id.fragmentControlSong, songControlFragment, SIMPLE_SONG_CONTROL_FRAGMENT_TAG);
        fragmentTransactionSongControl.commit();

        adapter = new SongAdapter(this, musicRepository.getSongs());
        adapter.setOnCardClickListener(this);

        setPermission();
    }



    private Runnable permissionGranted = new Runnable() {
        public void run() {
            musicRepository.updateSongList(getApplicationContext());
            adapter.notifyDataSetChanged();
        }
    };


    public void setPermission(){
        // ???????????????? ????????????????????
        int hasReadFilesPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        // ???????? ???????????????????? ???? API 23, ?????????????????????????? ????????????????????
        if (hasReadFilesPermission == PackageManager.PERMISSION_GRANTED) {
            READ_FILES_GRANTED = true;
        } else {
            // ???????????????? ???????????????????? ???????? ?????? ?????????????????? ????????????????????
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_CODE_READ_FILES);
        }
        // ???????? ???????????????????? ??????????????????????
        if (READ_FILES_GRANTED) {
            permissionHandler.postDelayed(permissionGranted, 15);
        }
    }

    // ??????????, ?????????????? ?????????????? ?????????????? ???? ???????????? ??????????????
    @Override
    public void onDeleteClick(View view,final int pos) {
        Song song = musicRepository.getSongs().get(pos);
        String path = Environment.getExternalStoragePublicDirectory(
                song.getPath() + song.getDisplayName()).getPath();
        File file = new File(path);
        try{
            boolean isDel = file.delete();
            if(isDel){
                musicRepository.getSongs().remove(pos);
                if(pos == musicRepository.getCurrentItemIndex()){
                    if(pos == musicRepository.getSongs().size())
                        onSongClick(view, pos - 1);
                    else
                        onSongClick(view, pos);
                }
                Toast.makeText(MainActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                adapter.notifyDataSetChanged();
            }else
                Toast.makeText(MainActivity.this, "Deleted", Toast.LENGTH_SHORT).show();

        }catch (Exception e){
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSongClick(View view, int position) {
        musicRepository.setCurrentItemIndex(position);
        if (mediaController != null)
            mediaController.getTransportControls().play();
    }

    //-----------------------------------LIST SONGS FRAGMENT----------------------------------

    @Override
    public void setAdapter(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(this,
                        RecyclerView.VERTICAL, false));
        recyclerView.setAdapter(adapter);

    }

    @Override
    public void updateSongsList(String dir) {
        musicRepository.updateSongList(this, dir);
    }

    @Override
    public void updateSongsList() {
        musicRepository.updateSongList(this);
    }

    @Override
    public void search(String search) {
        adapter.getFilter().filter(search);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void updateList() {
        setPermission();
    }

    //-----------------------------------SONG CONTROL FRAGMENT----------------------------------

    @Override
    public void clickBack() {
        if (mediaController != null)
            mediaController.getTransportControls().skipToPrevious();
    }



    @Override
    public void clickPausePlay(boolean setPause) {
        if (mediaController != null){
            if(setPause){
                mediaController.getTransportControls().pause();
            }else{
                mediaController.getTransportControls().play();
            }
        }

    }

    @Override
    public void clickNext() {
        if (mediaController != null)
            mediaController.getTransportControls().skipToNext();
    }

    @Override
    public void seekTo(int progress) {
        if (mediaController != null)
            mediaController.getTransportControls().seekTo(progress);
    }
}


