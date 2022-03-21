package com.example.openmusic.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONException;
import com.example.openmusic.LinkParse;
import com.example.openmusic.api.ApiClient;
import com.example.openmusic.models.MyAudioFormat;
import com.example.openmusic.R;
import com.example.openmusic.adpters.RecyclerAdapterTest;
import com.github.kiulian.downloader.Config;
import com.github.kiulian.downloader.YoutubeDownloader;
import com.github.kiulian.downloader.downloader.YoutubeCallback;
import com.github.kiulian.downloader.downloader.YoutubeProgressCallback;
import com.github.kiulian.downloader.downloader.request.RequestVideoFileDownload;
import com.github.kiulian.downloader.downloader.request.RequestVideoInfo;
import com.github.kiulian.downloader.downloader.response.Response;
import com.github.kiulian.downloader.model.videos.VideoInfo;
import com.github.kiulian.downloader.model.videos.formats.AudioFormat;
import com.github.kiulian.downloader.model.videos.formats.Format;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;


public class DownloadSongFragment extends Fragment implements AdapterView.OnItemClickListener{

    EditText edxLink, edxName;
    ImageButton btnDownload, btnClear_link, btnClear_name;
    ProgressBar progressBar;
    TextView txtProgress;
    RecyclerView list_format;
    private static final int REQUEST_CODE_WRITE_FILES = 2;
    private static boolean WRITE_FILES_GRANTED = false;
    List<MyAudioFormat> audioFormats = new ArrayList<>();
    RecyclerAdapterTest adapter;
    private int positionAudioFormat = 0;

    Animation animScale;
    final Handler h = new Handler();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_download_song, container, false);
        edxLink = v.findViewById(R.id.edxLink);
        edxName = v.findViewById(R.id.edxName);
        txtProgress = v.findViewById(R.id.txtProgress);
        btnDownload = v.findViewById(R.id.btnDownload);
        btnClear_name = v.findViewById(R.id.btnClear_name);
        btnClear_link = v.findViewById(R.id.btnClear_link);
        progressBar = v.findViewById(R.id.progressBar);
        list_format = v.findViewById(R.id.list_format);
        list_format.setLayoutManager(new LinearLayoutManager(getContext(),
                RecyclerView.VERTICAL, false));
        adapter = new RecyclerAdapterTest(getContext(), audioFormats);
        adapter.setOnItemClickListener(this);
        list_format.setAdapter(adapter);

        animScale = AnimationUtils.loadAnimation(getContext(), R.anim.scale);

        btnClear_link.setOnClickListener(v1 -> {
            edxLink.setText("");
        });
        btnClear_name.setOnClickListener(v1 -> {
            edxName.setText("");
        });

        edxLink.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String link = s.toString();
                if(link.contains("youtu.be") && link.length() == 28){ //28 - link length
                    try {
                        requestVideoInfo(LinkParse.parseYoutubeLink(link));
                        list_format.setVisibility(View.VISIBLE);
                    }catch (Exception e){

                    }

                }else if(link.contains("music.youtube") && link.length() == 45){
                    //https://music.youtube.com/watch?v=aymh4trStM8
                   try {
                       requestVideoInfo(LinkParse.parseYoutubeMusicLink(link));
                       list_format.setVisibility(View.VISIBLE);
                   }catch (Exception e){

                   }
                }
                else{
                    list_format.setVisibility(View.GONE);
                }
            }

            public void afterTextChanged(Editable s) {

            }
        });

        progressBar.setMax(100);
        btnDownload.setOnClickListener(this::ButtonClickDownload);


        return v;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void ButtonClickDownload(View view){
        view.startAnimation(animScale);
        setPermission();
    }




    @RequiresApi(api = Build.VERSION_CODES.O)
    public void downloadUsingByteArray(Path destination, String link)
            throws IOException {

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(link, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                try {
                    Files.write(destination, response);
                    if(mListener != null)
                        mListener.updateList();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {

            }

            @Override
            public void onRetry(int retryNo) {

            }
        });
    }


    public void getDownloadLinkByTrackId(final String trackId) throws JSONException {
        ApiClient.get("GetYSongById?trackId="+trackId, null, new JsonHttpResponseHandler() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    String link = response.getString("link");
                    String name = response.getString("name");

                    File outputDir = new File(Environment.getExternalStoragePublicDirectory("Music").getPath());
                    Path path = Paths.get(outputDir.getPath(), name+".mp3");

                    downloadUsingByteArray(path, link);

                } catch (org.json.JSONException | IOException e) {
                    e.printStackTrace();
                }
            }
        });
        Toast.makeText(getContext(), "Good", Toast.LENGTH_SHORT).show();
    }

    public void requestVideoInfo(String videoId){
        YoutubeDownloader downloader = new YoutubeDownloader();
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
        List<AudioFormat> audioFormats = video.audioFormats();
        this.audioFormats.clear();
        for (AudioFormat af :
                audioFormats) {
            if(!af.extension().value().equals("weba")){
                this.audioFormats.add(new MyAudioFormat(af, false));
            }

        }
        adapter.notifyDataSetChanged();
    }

    public void downloadYoutubeAsync(){
        YoutubeDownloader downloader = new YoutubeDownloader();

        Format format = audioFormats.get(positionAudioFormat).getAudioFormat();

        File outputDir = new File(Environment.getExternalStoragePublicDirectory("Music").getPath());

        // async downloading with callback
        RequestVideoFileDownload request = new RequestVideoFileDownload(format)
                .callback(new YoutubeProgressCallback<File>() {
                    @Override
                    public void onDownloading(int progress) {
                        System.out.printf("Downloaded %d%%\n", progress);
                        txtProgress.setText("Downloaded " + progress + "%");
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
        String name = edxName.getText().toString();
        if(name.length() > 0){
            request.renameTo(name);
        }

        Response<File> response = downloader.downloadVideoFile(request);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                File data = response.data();
                Looper.prepare();
               // Toast.makeText (getContext(), "Good", Toast.LENGTH_LONG).show();
                if(mListener != null)
                    mListener.updateList();
                Looper.loop();
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void setPermission(){
        // получаем разрешения
        int hasReadFilesPermission = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        // если устройство до API 23, устанавливаем разрешение
        if (hasReadFilesPermission == PackageManager.PERMISSION_GRANTED) {
            WRITE_FILES_GRANTED = true;
        } else {
            // вызываем диалоговое окно для установки разрешений
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_WRITE_FILES);
        }
        // если разрешение установлено, загружаем контакты
        if (WRITE_FILES_GRANTED) {
            String link = edxLink.getText().toString();
            if(link.length() > 0) {
                if(link.contains("music.yandex")){
                    getDownloadLinkByTrackId(LinkParse.parseYandexLink(link));
                }
                if(link.contains("youtu.be") || link.contains("music.youtube")){
                    downloadYoutubeAsync();
                }
            }
        }
    }

    private static DownloadSongFragmentListener mListener;

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        positionAudioFormat = position;
    }

    // создаем сам интерфейс и указываем метод и передаваемые им аргументы
    // View на котором произошло событие и позиция этого View
    public interface DownloadSongFragmentListener {
        void updateList();
    }

    // метод-сеттер для привязки колбэка к получателю событий
    public void setSongListFragmentListener(DownloadSongFragmentListener listener) {
        mListener = listener;
    }


}