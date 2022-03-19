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
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
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
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity implements
        SongAdapter.OnCardClickListener,
        SongListFragment.SongListFragmentListener,
        SongControlFragment.SongControlFragmentListener,
        DownloadSongFragment.DownloadSongFragmentListener

{

    SongAdapter adapter;

    Player player;

    private SongListFragment songListFragment;
    private SongControlFragment songControlFragment;
    private DownloadSongFragment downloadSongFragment;

    private static final int REQUEST_CODE_READ_FILES = 1;
    private static boolean READ_FILES_GRANTED = false;

    Handler permissionHandler = new Handler();

    BottomNavigationView bottomNavigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        player = PlayerController.getPlayer();

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
                        fragmentTransaction.replace(R.id.fragment, songListFragment);
                        fragmentTransaction.commit();
                        return true;
                    }
                    case R.id.page_2:{
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
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

        FragmentTransaction fragmentTransactionSongControl = fragmentManager.beginTransaction();
        fragmentTransactionSongControl.add(R.id.fragmentControlSong, songControlFragment);
        fragmentTransactionSongControl.commit();


        adapter = new SongAdapter(this, player.getSongs());
        adapter.setOnCardClickListener(this);

        setPermission();
    }

    public void setHomeFragment(View view){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment, songListFragment);
        fragmentTransaction.commit();
    }

    public void setAddFragment(View view){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment, downloadSongFragment);
        fragmentTransaction.commit();
    }

    public void setPlaylistsFragment(View view){

    }


    private Runnable permissionGranted = new Runnable() {
        public void run() {
            getSongList();
            //сортировка в алфавитном порядке
            Collections.sort(player.getSongs(), new Comparator<Song>(){
                public int compare(Song a, Song b){
                    return a.getTitle().compareTo(b.getTitle());
                }
            });
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


