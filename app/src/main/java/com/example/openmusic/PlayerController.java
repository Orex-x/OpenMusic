package com.example.openmusic;

import android.media.MediaPlayer;

import com.example.openmusic.models.Player;

import java.util.ArrayList;

public class PlayerController {
    private static Player mPlayer;

    public static Player getPlayer(){
        if(mPlayer == null){
            mPlayer = new Player(new MediaPlayer(), 0, new ArrayList<>());
        }
        return mPlayer;
    }
}
