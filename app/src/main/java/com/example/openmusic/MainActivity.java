package com.example.openmusic;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
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
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;

import java.util.Collections;
import java.util.Comparator;

import android.net.Uri;
import android.content.ContentResolver;
import android.database.Cursor;

import com.example.openmusic.adpters.SongAdapter;
import com.example.openmusic.fragments.DownloadSongFragment;
import com.example.openmusic.fragments.SongControlFragment;
import com.example.openmusic.fragments.SongListFragment;
import com.example.openmusic.models.Player;
import com.example.openmusic.models.Song;
import com.example.openmusic.service.MusicRepository;
import com.example.openmusic.service.PlayerService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity implements
        SongAdapter.OnCardClickListener,
        SongListFragment.SongListFragmentListener,
        SongControlFragment.SongControlFragmentListener,
        DownloadSongFragment.DownloadSongFragmentListener
{

    SongAdapter adapter;

    private SongListFragment songListFragment;
    private SongControlFragment songControlFragment;
    private DownloadSongFragment downloadSongFragment;

    private static final int REQUEST_CODE_READ_FILES = 1;
    private static boolean READ_FILES_GRANTED = false;

    Handler permissionHandler = new Handler();

    BottomNavigationView bottomNavigationView;

    private PlayerService.PlayerServiceBinder playerServiceBinder;
    private MediaControllerCompat mediaController;
    private MediaControllerCompat.Callback callback;
    private ServiceConnection serviceConnection;
    private MusicRepository musicRepository;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        musicRepository = MusicRepository.getMusicRepository();
        musicRepository.update(this);

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
                try {
                    mediaController = new MediaControllerCompat(MainActivity.this,
                            playerServiceBinder.getMediaSessionToken());
                    mediaController.registerCallback(callback);
                    callback.onPlaybackStateChanged(mediaController.getPlaybackState());
                }
                catch (RemoteException e) {
                    mediaController = null;
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

        songListFragment = new SongListFragment();
        songListFragment.setSongListFragmentListener(this);
        songControlFragment = new SongControlFragment();
        songControlFragment.setSongControlFragmentListener(this);
        downloadSongFragment = new DownloadSongFragment();
        downloadSongFragment.setSongListFragmentListener(this);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.page_1:{
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.hide(downloadSongFragment);
                        fragmentTransaction.replace(R.id.fragment, songListFragment);
                        fragmentTransaction.commit();
                        return true;
                    }
                    case R.id.page_2:{
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.hide(songListFragment);
                        fragmentTransaction.replace(R.id.fragment, downloadSongFragment);
                        fragmentTransaction.commit();
                        return true;
                    }
                }

                return false;
            }
        });

        FragmentManager fragmentManager = getSupportFragmentManager();



        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment, songListFragment);
        fragmentTransaction.commit();

/*        FragmentTransaction fragmentTransactionSongControl = fragmentManager.beginTransaction();
        fragmentTransactionSongControl.add(R.id.fragmentControlSong, songControlFragment);
        fragmentTransactionSongControl.commit();*/

        adapter = new SongAdapter(this, musicRepository.getSongs());
        adapter.setOnCardClickListener(this);

        setPermission();
    }



    private Runnable permissionGranted = new Runnable() {
        public void run() {
            musicRepository.update(getApplicationContext());
            adapter.notifyDataSetChanged();
        }
    };


    public void setPermission(){
        // получаем разрешения
        int hasReadFilesPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        // если устройство до API 23, устанавливаем разрешение
        if (hasReadFilesPermission == PackageManager.PERMISSION_GRANTED) {
            READ_FILES_GRANTED = true;
        } else {
            // вызываем диалоговое окно для установки разрешений
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_CODE_READ_FILES);
        }
        // если разрешение установлено
        if (READ_FILES_GRANTED) {
            permissionHandler.postDelayed(permissionGranted, 15);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        playerServiceBinder = null;
        if (mediaController != null) {
            mediaController.unregisterCallback(callback);
            mediaController = null;
        }
        unbindService(serviceConnection);
    }

    // Вызывается перед тем, как Активность становится "видимой".
    @Override
    public void onRestart(){
        super.onRestart();
        setPermission();
    }

    // метод, который получит события из нашего колбэка
    @Override
    public void onDeleteClick(View view,final int pos) {
        Song song = musicRepository.getSongs().get(pos);
        String path = Environment.getExternalStoragePublicDirectory(
                song.getPath() + song.getDisplayName()).getPath();
        File file = new File(path);
        try{
            file.delete();
            musicRepository.getSongs().remove(pos);
            if(pos == musicRepository.getCurrentItemIndex()){
                if(pos == musicRepository.getSongs().size())
                    onSongClick(view, pos - 1);
                else
                    onSongClick(view, pos);
            }
            Toast.makeText(MainActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
            adapter.notifyDataSetChanged();
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
    public void search(String search) {
        musicRepository.search(search, this);
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


