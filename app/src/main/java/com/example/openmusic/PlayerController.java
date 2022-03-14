package com.example.openmusic;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.view.View;

import java.io.IOException;
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
