package com.example.openmusic.service;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;

import com.example.openmusic.models.Song;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;

public class MusicRepository {

    private ArrayList<Song> songs = new ArrayList<>();
    private int currentItemIndex = 0;

    private static MusicRepository musicRepository;

    public static MusicRepository getMusicRepository(){
        if(musicRepository == null){
            musicRepository = new MusicRepository();
        }
        return musicRepository;
    }

    Song getNext() {
        if (currentItemIndex == songs.size()-1)
            currentItemIndex = 0;
        else
            currentItemIndex++;
        return getCurrent();
    }

    Song getPrevious() {
        if (currentItemIndex == 0)
            currentItemIndex = songs.size()-1;
        else
            currentItemIndex--;
        return getCurrent();
    }

    Song getCurrent() {
        return songs.get(currentItemIndex);
    }

    public ArrayList<Song> getSongs() {
        return songs;
    }

    public void setCurrentItemIndex(int currentItemIndex) {
        this.currentItemIndex = currentItemIndex;
    }

    public int getCurrentItemIndex() {
        return currentItemIndex;
    }

    public void update(Context context) {
        songs.clear();
        ContentResolver musicResolver = context.getContentResolver();
        Uri musicUri =  MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);


        if(musicCursor!=null && musicCursor.moveToFirst()){
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.TITLE);
            int artistColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ARTIST);
            int album = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM);
            int displayNameColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.DISPLAY_NAME);
            int relativePathColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.RELATIVE_PATH);
            int duration = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.DURATION);

            //add songs to list
            do {
                String thisAlbum = musicCursor.getString(album);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                String thisDisplayName = musicCursor.getString(displayNameColumn);
                String thisRelativePath = musicCursor.getString(relativePathColumn);
                int thisDuration = musicCursor.getInt(duration);
                songs.add(new Song(thisTitle, thisArtist, thisDisplayName,
                        thisRelativePath, thisDuration, thisAlbum));
            }
            while (musicCursor.moveToNext());
        }

        //сортировка в алфавитном порядке
        Collections.sort(songs, new Comparator<Song>(){
            public int compare(Song a, Song b){
                return a.getTitle().compareTo(b.getTitle());
            }
        });
    }

    public void add(Context context, Song song){
        // Add a specific media item.
        ContentResolver resolver = context
                .getContentResolver();

        Uri audioCollection;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            audioCollection = MediaStore.Audio.Media
                    .getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        } else {
            audioCollection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }

        ContentValues newSongDetails = new ContentValues();
        newSongDetails.put(MediaStore.Audio.Media.TITLE, song.getTitle());
        newSongDetails.put(MediaStore.Audio.Media.ARTIST, song.getArtist());

        newSongDetails.put(MediaStore.Audio.Media.DISPLAY_NAME, song.getDisplayName());
        newSongDetails.put(MediaStore.Audio.Media.RELATIVE_PATH, song.getPath());
        newSongDetails.put(MediaStore.Audio.Media.DURATION, song.getDuration());

        try{
            newSongDetails.put(MediaStore.Audio.Media.ALBUM, song.getAlbum());
        }catch (NullPointerException e){}

        resolver.insert(audioCollection, newSongDetails);
    }
}
