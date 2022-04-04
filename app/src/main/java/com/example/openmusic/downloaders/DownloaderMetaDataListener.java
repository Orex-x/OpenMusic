package com.example.openmusic.downloaders;

import com.example.openmusic.models.DownloadItemViewModel;

public interface DownloaderMetaDataListener {
    void addMetaData(DownloadItemViewModel model);
    void setProgressCompleted(int key);
    void onFailure(String message);
}
