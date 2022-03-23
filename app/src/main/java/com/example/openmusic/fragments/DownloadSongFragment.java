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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONException;
import com.example.openmusic.LinkParse;
import com.example.openmusic.api.ApiClient;
import com.example.openmusic.downloaders.DownloaderListener;
import com.example.openmusic.downloaders.YandexDownloader;
import com.example.openmusic.downloaders.YoutubeDownloader;
import com.example.openmusic.models.MyAudioFormat;
import com.example.openmusic.R;
import com.example.openmusic.adpters.MusicFormatAdapter;
import com.github.kiulian.downloader.Config;
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


public class DownloadSongFragment extends Fragment implements
        AdapterView.OnItemClickListener,
        DownloaderListener {

    EditText edxLink, edxName;
    ImageButton btnDownload, btnClear_link, btnClear_name;
    ProgressBar progressBar;
    TextView txtProgress;
    RecyclerView list_format;
    private static final int REQUEST_CODE_WRITE_FILES = 2;
    private static boolean WRITE_FILES_GRANTED = false;

    List<MyAudioFormat> audioFormats = new ArrayList<>();
    private int positionAudioFormat = 0;

    MusicFormatAdapter adapter;

    Animation animScale;

    YandexDownloader yandexDownloader;
    YoutubeDownloader youtubeDownloader;

    //for saving
    private String name, link;


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("name", name);
        outState.putString("link", link);
        super.onSaveInstanceState(outState);
    }

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

        youtubeDownloader = new YoutubeDownloader();
        youtubeDownloader.setDownloaderListener(this);
        yandexDownloader = new YandexDownloader();
        yandexDownloader.setDownloaderListener(this);

        adapter = new MusicFormatAdapter(getContext(), audioFormats);
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
                        changeListMyAudioFormat(LinkParse.parseYoutubeLink(link));
                        list_format.setVisibility(View.VISIBLE);
                    }catch (Exception e){

                    }

                }else if(link.contains("music.youtube") && link.length() == 45){
                    //https://music.youtube.com/watch?v=aymh4trStM8
                   try {
                       changeListMyAudioFormat(LinkParse.parseYoutubeMusicLink(link));
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

        if(savedInstanceState != null) {
            name = savedInstanceState.getString("name");
            link = savedInstanceState.getString("link");
            edxName.setText(name);
            edxLink.setText(link);
        }

        return v;
    }

    public void changeListMyAudioFormat(String link){
        List<AudioFormat> list = youtubeDownloader.requestVideoInfo(link);
        audioFormats.clear();
        for (AudioFormat af : list) {
            if(!af.extension().value().equals("weba")){
                audioFormats.add(new MyAudioFormat(af, false));
            }

        }
        adapter.notifyDataSetChanged();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void ButtonClickDownload(View view){
        view.startAnimation(animScale);
        setPermission();
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
                    yandexDownloader.getDownloadLinkByTrackId(LinkParse.parseYandexLink(link));
                }
                if(link.contains("youtu.be") || link.contains("music.youtube")){
                    String name = edxName.getText().toString();
                    youtubeDownloader.downloadYoutubeAsync(name, audioFormats.get(positionAudioFormat).getAudioFormat());
                }
            }
        }
    }

    private static DownloadSongFragmentListener mListener;

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        positionAudioFormat = position;
    }



    @Override
    public void setProgress(int progress) {
         txtProgress.setText("Downloaded " + progress + "%");
         progressBar.setProgress(progress);
    }

    @Override
    public void setProgressCompleted() {
        progressBar.setProgress(0);
        txtProgress.setText("Completed");
    }

    @Override
    public void updateList() {
        mListener.updateList();
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