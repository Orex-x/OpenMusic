package com.example.openmusic.models;

import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.openmusic.LinkParse;
import com.example.openmusic.downloaders.DownloaderListener;
import com.github.kiulian.downloader.model.videos.formats.AudioFormat;

public class DownloadItemViewModel {

    private int id, progress;
    private String SongName, link;
    private LinkParse.LinkType linkType;
    private boolean isDownloading;
    private AudioFormat audioFormat;


    public DownloadItemViewModel(String songName, String link, LinkParse.LinkType linkType) {
        this.progress = 0;
        SongName = songName;
        this.link = link;
        this.linkType = linkType;
        this.isDownloading = false;
    }

    public DownloadItemViewModel(String songName, AudioFormat audioFormat, LinkParse.LinkType linkType) {
        this.progress = 0;
        SongName = songName;
        this.audioFormat = audioFormat;
        this.linkType = linkType;
        this.isDownloading = false;
    }

    public void setId(int id) {
        this.id = id;
    }

    public AudioFormat getAudioFormat() {
        return audioFormat;
    }

    public void setAudioFormat(AudioFormat audioFormat) {
        this.audioFormat = audioFormat;
    }

    public boolean isDownloading() {
        return isDownloading;
    }

    public void setDownloading(boolean downloading) {
        isDownloading = downloading;
    }

    public int getId() {
        return id;
    }

    public LinkParse.LinkType getLinkType() {
        return linkType;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public String getSongName() {
        return SongName;
    }

    public String getLink() {
        return link;
    }

}
