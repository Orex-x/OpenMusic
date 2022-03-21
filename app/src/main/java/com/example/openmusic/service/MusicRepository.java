package com.example.openmusic.service;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.example.openmusic.models.Song;

import java.util.ArrayList;
import java.util.Locale;

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
                songs.add(new Song(thisId, thisTitle, thisArtist, thisDisplayName, thisRelativePath));
            }
            while (musicCursor.moveToNext());
        }
    }

    public void search(String search, Context context) {
        if(search.length() == 0){
            update(context);
        }
       ArrayList<Song> result_search = new ArrayList<>();
        for (Song song :
                songs) {
            if(song.getDisplayName().toLowerCase(Locale.ROOT).contains(search.toLowerCase(Locale.ROOT))
            || song.getTitle().toLowerCase(Locale.ROOT).contains(search.toLowerCase(Locale.ROOT)))
                result_search.add(song);
        }
        songs.clear();
        for (Song song : result_search) {
            songs.add(song);
        }
    }
}
