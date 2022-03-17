package com.example.openmusic;

import com.github.kiulian.downloader.model.videos.formats.AudioFormat;

public class MyAudioFormat {
    private AudioFormat audioFormat;
    private boolean isSelected;

    public MyAudioFormat(AudioFormat audioFormat, boolean isSelected) {
        this.audioFormat = audioFormat;
        this.isSelected = isSelected;
    }

    public AudioFormat getAudioFormat() {
        return audioFormat;
    }

    public void setAudioFormat(AudioFormat audioFormat) {
        this.audioFormat = audioFormat;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
