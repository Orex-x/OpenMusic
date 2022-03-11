package com.example.openmusic;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import java.util.concurrent.atomic.AtomicBoolean;

import android.net.Uri;
import android.content.ContentResolver;
import android.database.Cursor;
import android.widget.ListView;

public class MainActivity extends AppCompatActivity {

    MediaPlayer mPlayer = new MediaPlayer();;
    Button btnBack, btnPause, btnNext, btnAdd, btnUpdateList;
    ListView lvMusics;
    SimpleAdapter adapter;
    Stack<String> globalPath = new Stack<>();
    private ArrayList<Song> songList = new ArrayList<Song>();
    private int currentSong = 0;
    SeekBar seekBar;
    TextView elapsed, remaining, txtSongName, txtSongAuthor;
    Handler seekHandler = new Handler();


    private static final int REQUEST_CODE_READ_FILES = 1;
    private static boolean READ_FILES_GRANTED = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnBack = findViewById(R.id.btnBack);
        btnPause = findViewById(R.id.btnPause);
        btnNext = findViewById(R.id.btnNext);
        btnAdd = findViewById(R.id.btnAdd);
        btnUpdateList = findViewById(R.id.btnUpdateList);
        seekBar = findViewById(R.id.seekBar);
        lvMusics = findViewById(R.id.lvMusics);
        elapsed = findViewById(R.id.elapsed);
        remaining = findViewById(R.id.remaining);
        txtSongName = findViewById(R.id.txtSongName);
        txtSongAuthor = findViewById(R.id.txtSongAuthor);


        adapter = new SimpleAdapter(this, songList);
        lvMusics.setAdapter(adapter);


        btnPause.setOnClickListener(this::pauseSong);

        btnNext.setOnClickListener(this::nextSong);

        btnBack.setOnClickListener(this::backSong);

        btnAdd.setOnClickListener(this::goAddActivity);

        btnUpdateList.setOnClickListener(this::updateList);

        lvMusics.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                currentSong = position;
                playSong(currentSong);
            }
        });

        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                nextSong(null);
                seekBar.setProgress(mp.getCurrentPosition());
            }
        });

        mPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mp, int percent) {
                seekBar.setSecondaryProgress(percent);
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekTo(seekBar.getProgress());
            }


        });

        setPermission();
    }

    public void goAddActivity(View view){
        Intent intent = new Intent(MainActivity.this, AddActivity.class);
        startActivity(intent);

    }

    public void updateList(View view){
        setPermission();
    }

    public void seekTo(int progress) {
        mPlayer.seekTo(progress);
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
            Collections.sort(songList, new Comparator<Song>(){
                public int compare(Song a, Song b){
                    return a.getTitle().compareTo(b.getTitle());
                }
            });
            adapter.notifyDataSetChanged();
        }
    }

    public void playSong(int position){
        Song song = songList.get(position);
        String dir = Environment.getExternalStoragePublicDirectory(song.getPath() + song.getDisplayName()).getPath();
        Uri uri = Uri.parse(dir);
        try {
            //mPlayer.stop();
            mPlayer.reset();
            mPlayer.setDataSource(MainActivity.this, uri);
            mPlayer.prepare();
            mPlayer.start();

            seekBar.setProgress(0);
            seekBar.setMax(mPlayer.getDuration());
            // Updating progress bar
            seekHandler.postDelayed(updateSeekBar, 15);

            txtSongName.setText(song.getTitle());
            txtSongAuthor.setText(song.getArtist());


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void pauseSong(View view){
        if (mPlayer.isPlaying())
            mPlayer.pause();
        else{
            mPlayer.start();
        }
    }

    public void nextSong(View view){
        currentSong++;
        if(currentSong == songList.size())
            currentSong = 0;
        playSong(currentSong);
    }

    public void backSong(View view){
        currentSong--;
        if(currentSong == -1)
            currentSong = songList.size() - 1;
        playSong(currentSong);
    }

    public void getSongList() {
        songList.clear();
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
                songList.add(new Song(thisId, thisTitle, thisArtist, thisDisplayName, thisRelativePath));
            }
            while (musicCursor.moveToNext());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPlayer.stop();
    }

    // Вызывается перед тем, как Активность становится "видимой".
    @Override
    public void onRestart(){
        super.onRestart();
        setPermission();
    }

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
    private Runnable updateSeekBar = new Runnable() {
        public void run() {
            long totalDuration = mPlayer.getDuration();
            long currentDuration = mPlayer.getCurrentPosition();

            // Displaying Total Duration time
            remaining.setText(""+ milliSecondsToTimer(totalDuration-currentDuration));
            // Displaying time completed playing
            elapsed.setText(""+ milliSecondsToTimer(currentDuration));

            // Updating progress bar
            seekBar.setProgress((int)currentDuration);

            // Call this thread again after 15 milliseconds => ~ 1000/60fps
            seekHandler.postDelayed(this, 15);
        }
    };
}


