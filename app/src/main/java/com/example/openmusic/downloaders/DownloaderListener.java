package com.example.openmusic.downloaders;

public interface DownloaderListener {
    void setProgress(int progress);
    void setProgressCompleted();
    void updateList();
}
