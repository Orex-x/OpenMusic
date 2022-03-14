package com.example.openmusic;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

import android.net.Uri;
import android.content.ContentResolver;
import android.database.Cursor;
import android.widget.ListView;

import com.example.openmusic.fragments.SongControlFragment;
import com.example.openmusic.fragments.SongListFragment;

public class MainActivity extends AppCompatActivity implements
        SongAdapter.OnCardClickListener,
        SongListFragment.SongListFragmentListener,
        SongControlFragment.SongControlFragmentListener

{

    SongAdapter adapter;

    Player player;

    private SongListFragment songListFragment;
    private SongControlFragment songControlFragment;

    private static final int REQUEST_CODE_READ_FILES = 1;
    private static boolean READ_FILES_GRANTED = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        player = PlayerController.getPlayer();

        songListFragment = new SongListFragment();
        songListFragment.setSongListFragmentListener(this);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment, songListFragment);
        fragmentTransaction.commit();

        FragmentTransaction fragmentTransactionSongControl = fragmentManager.beginTransaction();
        songControlFragment = new SongControlFragment();
        songControlFragment.setSongControlFragmentListener(this);
        fragmentTransactionSongControl.add(R.id.fragmentControlSong, songControlFragment);
        fragmentTransactionSongControl.commit();


        adapter = new SongAdapter(this, player.getSongs());
        adapter.setOnCardClickListener(this);



        setPermission();
    }



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
            getSongList();
            //сортировка в алфавитном порядке
            Collections.sort(player.getSongs(), new Comparator<Song>(){
                public int compare(Song a, Song b){
                    return a.getTitle().compareTo(b.getTitle());
                }
            });
            adapter.notifyDataSetChanged();
        }
    }

    public void playSong(int position){
        player.playSong(position, this);
    }

    public void getSongList() {
        player.getSongs().clear();
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri =  MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
        if(musicCursor!=null && musicCursor.moveToFirst()){
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ARTIST);
            int displayNameColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME);
            int relativePathColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.RELATIVE_PATH);

            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                String thisDisplayName = musicCursor.getString(displayNameColumn);
                String thisRelativePath = musicCursor.getString(relativePathColumn);
                player.getSongs().add(new Song(thisId, thisTitle, thisArtist, thisDisplayName, thisRelativePath));
            }
            while (musicCursor.moveToNext());
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        player.stop();
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
        Song song = player.getSongs().get(pos);
        String path = Environment.getExternalStoragePublicDirectory(
                song.getPath() + song.getDisplayName()).getPath();
        File file = new File(path);
        try{
            file.delete();
            player.getSongs().remove(pos);
            if(pos == player.getCurrentSong()){
                player.nextSong(this);
            }
            Toast.makeText(MainActivity.this, "Deleted", Toast.LENGTH_SHORT).show();

            adapter.notifyDataSetChanged();
        }catch (Exception e){
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSongClick(View view, int position) {
        playSong(position);
    }

    //-----------------------------------LIST SONGS FRAGMENT----------------------------------

    @Override
    public void setAdapter(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(this,
                        RecyclerView.VERTICAL, false));
        recyclerView.setAdapter(adapter);

    }

    @Override
    public void updateList() {
        setPermission();
    }

    //-----------------------------------SONG CONTROL FRAGMENT----------------------------------

    @Override
    public void clickBack() {
        player.backSong(this);
    }

    @Override
    public void clickPause() {
        player.pauseSong();
    }

    @Override
    public void clickNext() {
        player.nextSong(this);
    }

    @Override
    public void seekTo(int progress) {
        player.seekTo(progress);
    }
}


