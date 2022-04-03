package com.example.openmusic.models;

import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.openmusic.LinkParse;
import com.example.openmusic.downloaders.DownloaderListener;

public class DownloadItemViewModel {

    private int id, progress;
    private String SongName, link;
    private LinkParse.LinkType linkType;
    private boolean isDownloading;

    public DownloadItemViewModel(int id, String songName, String link, LinkParse.LinkType linkType) {
        this.id = id;
        this.progress = 0;
        SongName = songName;
        this.link = link;
        this.linkType = linkType;
        this.isDownloading = false;
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
