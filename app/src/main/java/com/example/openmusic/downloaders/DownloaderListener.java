package com.example.openmusic.downloaders;

import com.example.openmusic.models.DownloadItemViewModel;

public interface DownloaderListener {
    void setProgress(int progress);
    void setProgressCompleted(int position);
    void addMetaData(DownloadItemViewModel model);
}
