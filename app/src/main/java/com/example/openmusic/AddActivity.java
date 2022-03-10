package com.example.openmusic;


import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.github.kiulian.downloader.Config;
import com.github.kiulian.downloader.YoutubeDownloader;
import com.github.kiulian.downloader.downloader.YoutubeCallback;
import com.github.kiulian.downloader.downloader.YoutubeProgressCallback;
import com.github.kiulian.downloader.downloader.request.Request;
import com.github.kiulian.downloader.downloader.request.RequestVideoFileDownload;
import com.github.kiulian.downloader.downloader.request.RequestVideoInfo;
import com.github.kiulian.downloader.downloader.request.RequestVideoStreamDownload;
import com.github.kiulian.downloader.downloader.response.Response;
import com.github.kiulian.downloader.model.*;
import com.github.kiulian.downloader.model.videos.VideoDetails;
import com.github.kiulian.downloader.model.videos.VideoInfo;
import com.github.kiulian.downloader.model.videos.formats.AudioFormat;
import com.github.kiulian.downloader.model.videos.formats.Format;
import com.github.kiulian.downloader.model.videos.formats.VideoFormat;
import com.github.kiulian.downloader.model.videos.formats.VideoWithAudioFormat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class AddActivity extends AppCompatActivity {

    EditText edxLink;
    Button btnDownload;
    ProgressBar progressBar;
    private static final int REQUEST_CODE_WRITE_FILES = 2;
    private static boolean WRITE_FILES_GRANTED = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        edxLink = findViewById(R.id.edxLink);
        btnDownload = findViewById(R.id.btnDownload);
        progressBar = findViewById(R.id.progressBar);

        btnDownload.setOnClickListener(this::download);


    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void download(View view){
        test2();
        setPermission();


    }


    public void setPermission(){
        // получаем разрешения
        int hasReadFilesPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        // если устройство до API 23, устанавливаем разрешение
        if (hasReadFilesPermission == PackageManager.PERMISSION_GRANTED) {
            WRITE_FILES_GRANTED = true;
        } else {
            // вызываем диалоговое окно для установки разрешений
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_WRITE_FILES);
        }
        // если разрешение установлено, загружаем контакты
        if (WRITE_FILES_GRANTED) {
            test2();

        }
    }


    public void test2(){
        YoutubeDownloader downloader = new YoutubeDownloader();
        Config config = downloader.getConfig();
        config.setMaxRetries(0);

        String videoId = "NlAeFb3j2aw";

        File outputDir = new File(Environment.getExternalStoragePublicDirectory("Music").getPath());
        //File outputDir = new File(getExternalCacheDir().getPath());



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
        List<AudioFormat> audioFormats = video.audioFormats();

        // get all videos formats (may contain better quality but without audio)
        List<VideoFormat> videoFormats = video.videoFormats();

        Format format = audioFormats.get(0);


        // async downloading with callback
        RequestVideoFileDownload request = new RequestVideoFileDownload(format)
                .callback(new YoutubeProgressCallback<File>() {
                    @Override
                    public void onDownloading(int progress) {
                        System.out.printf("Downloaded %d%%\n", progress);
                        progressBar.setMax(100);
                        progressBar.setProgress(progress);
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
        Response<File> response = downloader.downloadVideoFile(request);
        File data = response.data(); // will block current thread
    }
}