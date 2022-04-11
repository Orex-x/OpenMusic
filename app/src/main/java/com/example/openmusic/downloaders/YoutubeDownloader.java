package com.example.openmusic.downloaders;

import android.os.AsyncTask;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.openmusic.LinkParse;
import com.example.openmusic.downloaders.DownloaderListener;
import com.example.openmusic.models.DownloadItemViewModel;
import com.github.kiulian.downloader.Config;
import com.github.kiulian.downloader.downloader.YoutubeCallback;
import com.github.kiulian.downloader.downloader.YoutubeProgressCallback;
import com.github.kiulian.downloader.downloader.request.RequestVideoFileDownload;
import com.github.kiulian.downloader.downloader.request.RequestVideoInfo;
import com.github.kiulian.downloader.downloader.response.Response;
import com.github.kiulian.downloader.model.videos.VideoDetails;
import com.github.kiulian.downloader.model.videos.VideoInfo;
import com.github.kiulian.downloader.model.videos.formats.AudioFormat;
import com.github.kiulian.downloader.model.videos.formats.Format;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class YoutubeDownloader {

    private DownloadItemViewModel mDownloadItemViewModel;
    private WeakReference<DownloaderListener> mWaitingListenerWeakReference;

    public YoutubeDownloader(){

    }

    public YoutubeDownloader(DownloadItemViewModel progressObject) {
        mDownloadItemViewModel = progressObject;
    }

    public void updateListener(@Nullable DownloaderListener waitingListener) {
        mWaitingListenerWeakReference = new WeakReference<>(waitingListener);
    }

    public void loadMetaDataByVideoId(String videoId){
        com.github.kiulian.downloader.YoutubeDownloader downloader =
                new com.github.kiulian.downloader.YoutubeDownloader();
        Config config = downloader.getConfig();
        config.setMaxRetries(0);
        // async parsing
        RequestVideoInfo requestVideoInfo = new RequestVideoInfo(videoId)
                .callback(new YoutubeCallback<VideoInfo>() {
                    @Override
                    public void onFinished(VideoInfo videoInfo) {
                        Log.d("TAG", "Finished parsing");
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.d("TAG", "Error: " + throwable.getMessage());
                    }
                })
                .async();
        Response<VideoInfo> responseVideoInfo = downloader.getVideoInfo(requestVideoInfo);
        VideoInfo video = responseVideoInfo.data(); // will block thread
        VideoDetails s = video.details();
        String title = s.title();

        if(downloaderMetaDataListener != null)
            downloaderMetaDataListener.addMetaData(
                    new DownloadItemViewModel(title,
                            video.audioFormats().get(0), LinkParse.LinkType.YOUTUBE));
    }



    class WaitingTask extends AsyncTask<Void, Integer, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            com.github.kiulian.downloader.YoutubeDownloader downloader =
                    new com.github.kiulian.downloader.YoutubeDownloader();

            File outputDir = new File(
                    Environment.getExternalStoragePublicDirectory("Music").getPath());

            RequestVideoFileDownload request = new
                    RequestVideoFileDownload(mDownloadItemViewModel.getAudioFormat())
                    .callback(new YoutubeProgressCallback<File>() {
                        @Override
                        public void onDownloading(int progress) {
                            System.out.printf("Downloaded %d%%\n", progress);
                            mDownloadItemViewModel.setProgress(progress);	// Update data set
                            if (mWaitingListenerWeakReference != null) {
                                DownloaderListener listener = mWaitingListenerWeakReference.get();
                                if (listener != null) {
                                    listener.setProgress(progress);
                                }
                            }
                        }

                        @Override
                        public void onFinished(File videoInfo) {
                            System.out.println("Finished file: " + videoInfo);

                        }

                        @Override
                        public void onError(Throwable throwable) {
                            System.out.println("Error: " + throwable.getLocalizedMessage());
                        }
                    })
                    .renameTo(mDownloadItemViewModel.getSongName())
                    .saveTo(outputDir)
                    .async();


            Response<File> response = downloader.downloadVideoFile(request);
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try{
                        File data = response.data();
                        Looper.prepare();
                        downloaderMetaDataListener.setProgressCompleted(mDownloadItemViewModel.getId());
                        Looper.loop();
                    }catch(Exception e){

                    }
                }
            };
            Thread thread = new Thread(runnable);
            thread.start();
            return null;
        }
    }

    public void downloadYoutubeAsync(){
        WaitingTask task = new WaitingTask();
        task.execute();
    }


    private static DownloaderMetaDataListener downloaderMetaDataListener;

    // метод-сеттер для привязки колбэка к получателю событий
    public void setDownloaderListener(DownloaderMetaDataListener listener) {
        downloaderMetaDataListener = listener;
    }
}
