package com.example.openmusic;

import android.content.Context;
import android.os.RemoteException;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;

import com.example.openmusic.service.*;

public class Instances {
    private static MediaControllerCompat mediaController;

    public static MediaControllerCompat getMediaSessionCompat(Context context,
                                                           MediaSessionCompat.Token token){
        if(mediaController == null) {
            try {
                mediaController = new MediaControllerCompat(context, token);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return mediaController;
    }
}
