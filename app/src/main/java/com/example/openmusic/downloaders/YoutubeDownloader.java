package com.example.openmusic.downloaders;

import android.os.Environment;
import android.os.Looper;
import android.util.Log;

import com.example.openmusic.downloaders.DownloaderListener;
import com.github.kiulian.downloader.Config;
import com.github.kiulian.downloader.downloader.YoutubeCallback;
import com.github.kiulian.downloader.downloader.YoutubeProgressCallback;
import com.github.kiulian.downloader.downloader.request.RequestVideoFileDownload;
import com.github.kiulian.downloader.downloader.request.RequestVideoInfo;
import com.github.kiulian.downloader.downloader.response.Response;
import com.github.kiulian.downloader.model.videos.VideoInfo;
import com.github.kiulian.downloader.model.videos.formats.AudioFormat;
import com.github.kiulian.downloader.model.videos.formats.Format;

import java.io.File;
import java.util.List;

public class YoutubeDownloader {



    public void downloadYoutubeAsync(Format format, int key){
        com.github.kiulian.downloader.YoutubeDownloader downloader =
                new com.github.kiulian.downloader.YoutubeDownloader();

        File outputDir = new File(Environment.getExternalStoragePublicDirectory("Music").getPath());

        // async downloading with callback
        RequestVideoFileDownload request = new RequestVideoFileDownload(format)
                .callback(new YoutubeProgressCallback<File>() {
                    @Override
                    public void onDownloading(int progress) {
                        System.out.printf("Downloaded %d%%\n", progress);
                        //mListener.setProgress(progress, position);
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
                .saveTo(outputDir)
                .async();

       /* if(name.length() > 0){
            request.renameTo(name);
        }*/

        Response<File> response = downloader.downloadVideoFile(request);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try{
                    File data = response.data();
                    Looper.prepare();
                    mListener.setProgressCompleted(key);
                    Looper.loop();
                }catch(Exception e){

                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    public List<AudioFormat> requestVideoInfo(String videoId){
        com.github.kiulian.downloader.YoutubeDownloader downloader = new com.github.kiulian.downloader.YoutubeDownloader();
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

        // get audio formats
        return video.audioFormats();
    }




    private static DownloaderMetaDataListener mListener;

    // метод-сеттер для привязки колбэка к получателю событий
    public void setDownloaderListener(DownloaderMetaDataListener listener) {
        mListener = listener;
    }
}
