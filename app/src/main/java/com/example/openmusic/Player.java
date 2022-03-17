package com.example.openmusic;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.view.View;

import java.io.IOException;
import java.util.ArrayList;

public class Player {
    private MediaPlayer player;
    private int currentSong;
    private ArrayList<Song> songs = new ArrayList<>();

    public Player(MediaPlayer player, int currentSong, ArrayList<Song> songs) {
        this.player = player;
        this.currentSong = currentSong;
        this.songs = songs;

        this.player.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mp, int percent) {
                mListener.OnBufferingUpdateListener(percent);
            }
        });
        this.player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mListener.OnCompletionListener(mp);
            }
        });
    }

    public void setSongs(ArrayList<Song> songs) {
        this.songs = songs;
    }

    public MediaPlayer getPlayer() {
        return player;
    }

    public int getCurrentSong() {
        return currentSong;
    }

    public void seekTo(int progress) {
        player.seekTo(progress);
    }

    public ArrayList<Song> getSongs() {
        return songs;
    }

    public Song playSong(int position, Context context){
        Song song = songs.get(position);

        currentSong = position;
        String path = Environment.getExternalStoragePublicDirectory(song.getPath() + song.getDisplayName()).getPath();
        Uri uri = Uri.parse(path);
        try {
            //mPlayer.stop();
            player.reset();
            player.setDataSource(context, uri);
            player.prepare();
            player.start();

            mListener.setSongData(song, player.getDuration());

            return song;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void pauseSong(){
        if (player.isPlaying())
            player.pause();
        else{
            player.start();
        }
    }

    public void nextSong(Context context){
        currentSong++;
        if(currentSong == songs.size())
            currentSong = 0;
        playSong(currentSong, context);
    }

    public void backSong(Context context){
        currentSong--;
        if(currentSong == -1)
            currentSong = songs.size() - 1;
        playSong(currentSong, context);
    }

    public void stop(){
       player.stop();
    }

    // создаем сам интерфейс и указываем метод и передаваемые им аргументы
    // View на котором произошло событие и позиция этого View
    public interface OnPlayerListener {
        void OnBufferingUpdateListener(int percent);
        void OnCompletionListener(MediaPlayer mp);
        void setSongData(Song song, int duration);
    }

    // создаем поле объекта-колбэка
    private static OnPlayerListener mListener;


    // метод-сеттер для привязки колбэка к получателю событий
    public void setOnPlayerListener(OnPlayerListener listener) {
        mListener = listener;
    }
}