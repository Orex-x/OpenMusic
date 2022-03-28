package com.example.openmusic.models;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;

import com.example.openmusic.models.Song;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import io.netty.internal.tcnative.AsyncTask;

public class Player {
    private MediaPlayer player;

    private Timer mTimer;
    private MyTimerTask mMyTimerTask;

    public Player(MediaPlayer player) {
        this.player = player;


        this.player.setOnBufferingUpdateListener((mp, percent) ->
                mListener.OnBufferingUpdateListener(percent));
        this.player.setOnCompletionListener(mp ->
                mListener.OnCompletionListener(mp));
        this.player.setOnPreparedListener(mp -> {
            if (mTimer != null) {
                mTimer.cancel();
            }
            mTimer = new Timer();
            mMyTimerTask = new MyTimerTask();
            mTimer.schedule(mMyTimerTask, 500);

        });
    }

    class MyTimerTask extends TimerTask {

        @Override
        public void run() {
            player.start();
            mListener.changeImageResourceBtnPause();
        }
    }


    public MediaPlayer getPlayer() {
        return player;
    }


    public void seekTo(int progress) {
        player.seekTo(progress);
    }


    public void playSong(Song song, Context context){
        String path = Environment.getExternalStoragePublicDirectory(
                song.getPath() + song.getDisplayName()).getPath();
        Uri uri = Uri.parse(path);
        try {
            player.reset();
            mListener.changeImageResourceBtnPause();
            player.setDataSource(context, uri);
            player.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public void pause(){
        if (mTimer != null) {
            mTimer.cancel();
        }
        player.pause();
        mListener.changeImageResourceBtnPause();
    }

    public void start(){
        player.start();
        mListener.changeImageResourceBtnPause();
    }

    public void stop(){
        if (mTimer != null) {
            mTimer.cancel();
        }
       player.stop();
       mListener.changeImageResourceBtnPause();
    }

    // создаем сам интерфейс и указываем метод и передаваемые им аргументы
    // View на котором произошло событие и позиция этого View
    public interface OnPlayerListener {
        void OnBufferingUpdateListener(int percent);
        void OnCompletionListener(MediaPlayer mp);
        void changeImageResourceBtnPause();
    }

    // создаем поле объекта-колбэка
    private static OnPlayerListener mListener;


    // метод-сеттер для привязки колбэка к получателю событий
    public void setOnPlayerListener(OnPlayerListener listener) {
        mListener = listener;
    }
}
