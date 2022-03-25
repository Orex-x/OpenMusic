package com.example.openmusic.models;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;

import com.example.openmusic.models.Song;

import java.io.IOException;
import java.util.ArrayList;

import io.netty.internal.tcnative.AsyncTask;

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


    public MediaPlayer getPlayer() {
        return player;
    }


    public void seekTo(int progress) {
        player.seekTo(progress);
    }


    public Song playSong(Song song, Context context){
        String path = Environment.getExternalStoragePublicDirectory(
                song.getPath() + song.getDisplayName()).getPath();
        Uri uri = Uri.parse(path);
        try {
            //mPlayer.stop();
            //тут надо во второй поток
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


    public void pause(){
        player.pause();
    }

    public void start(){
        player.start();
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
