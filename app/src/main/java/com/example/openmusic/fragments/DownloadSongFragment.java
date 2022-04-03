package com.example.openmusic.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.example.openmusic.LinkParse;
import com.example.openmusic.adpters.ProgressAdapter;
import com.example.openmusic.downloaders.DownloaderMetaDataListener;
import com.example.openmusic.downloaders.YandexDownloader;
import com.example.openmusic.downloaders.YoutubeDownloader;
import com.example.openmusic.models.DownloadItemViewModel;
import com.example.openmusic.R;

import java.util.ArrayList;


public class DownloadSongFragment extends Fragment implements DownloaderMetaDataListener {

    ProgressBar progressBarMetaData;
    EditText edxLink;
    ImageButton btnDownload, btnSearch;
    TextView txtProgress;
    RecyclerView list_song_queue;
    private static final int REQUEST_CODE_WRITE_FILES = 2;
    private static boolean WRITE_FILES_GRANTED = false;


    ArrayList<DownloadItemViewModel> downloadItemViewModels = new ArrayList<>();
    private SparseArray<YandexDownloader> mWaitingTaskYandexDownloaders = new SparseArray<>();
    private SparseArray<YoutubeDownloader> mWaitingTaskYoutubeDownloaders = new SparseArray<>();

    ProgressAdapter progressAdapter;

    Animation animScale, animScaleReverse;

    YandexDownloader yandexDownloader;
    YoutubeDownloader youtubeDownloader;

    //for saving
    private String link;


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("link", link);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_download_song, container, false);
        progressBarMetaData = v.findViewById(R.id.progressBarMetaData);
        edxLink = v.findViewById(R.id.edxLink);
        txtProgress = v.findViewById(R.id.txtProgress);
        btnDownload = v.findViewById(R.id.btnDownload);
        if(downloadItemViewModels.size() == 0)
            btnDownload.setEnabled(false);
        btnSearch = v.findViewById(R.id.btnSearch);
        list_song_queue = v.findViewById(R.id.list_song_queue);
        list_song_queue.setLayoutManager(new LinearLayoutManager(getContext(),
                RecyclerView.VERTICAL, false));

        youtubeDownloader = new YoutubeDownloader();
        youtubeDownloader.setDownloaderListener(this);
        yandexDownloader = new YandexDownloader();
        yandexDownloader.setDownloaderListener(this);


        progressAdapter = new ProgressAdapter();
        list_song_queue.setAdapter(progressAdapter);
        progressAdapter.updateProgressObjects(
                downloadItemViewModels,
                mWaitingTaskYandexDownloaders,
                mWaitingTaskYoutubeDownloaders);

        animScale = AnimationUtils.loadAnimation(getContext(), R.anim.scale);
        animScaleReverse = AnimationUtils.loadAnimation(getContext(), R.anim.scale_reverse);

        btnSearch.setOnClickListener(v1 -> {
            v1.startAnimation(animScale);
            v1.startAnimation(animScaleReverse);
            String link = edxLink.getText().toString();
            LinkParse linkParse = new LinkParse();
            linkParse.parse(link);
            if(linkParse.getLinkType() != LinkParse.LinkType.NONE){
                btnSearch.setVisibility(View.GONE);
                progressBarMetaData.setVisibility(View.VISIBLE);


                switch (linkParse.getLinkType()){
                    case YANDEX_TRACK:
                        yandexDownloader.loadMetaDataByTrackId(linkParse.getId());
                        break;
                    case YANDEX_ALBUM:
                        yandexDownloader.loadMetaDataByAlbumId(linkParse.getId());
                        break;
                    case YOUTUBE:
                        youtubeDownloader.loadMetaDataByVideoId(linkParse.getId());
                        break;
                    case YOUTUBE_MUSIC:

                        break;

                }
            }
        });

        btnDownload.setOnClickListener(this::ButtonClickDownload);

        if(savedInstanceState != null) {
            link = savedInstanceState.getString("link");
            edxLink.setText(link);
        }

        return v;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void ButtonClickDownload(View view){
        view.startAnimation(animScale);
        view.startAnimation(animScaleReverse);
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
           progressAdapter.downloadAllProgressObjects();
           btnDownload.setEnabled(false);
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void addMetaData(DownloadItemViewModel model) {
        downloadItemViewModels.add(model);
        if(model.getLinkType() == LinkParse.LinkType.YOUTUBE){
            int id = mWaitingTaskYoutubeDownloaders.size() + 1;
            model.setId(id);
            mWaitingTaskYoutubeDownloaders.put(id, new YoutubeDownloader(model));
        }
        if(model.getLinkType() == LinkParse.LinkType.YANDEX_TRACK){
            int id = mWaitingTaskYandexDownloaders.size() + 1;
            model.setId(id);
            mWaitingTaskYandexDownloaders.put(id, new YandexDownloader(model));
        }

        progressAdapter.updateProgressObjects(downloadItemViewModels,
                mWaitingTaskYandexDownloaders,
                mWaitingTaskYoutubeDownloaders);
        progressBarMetaData.setVisibility(View.GONE);
        btnSearch.setVisibility(View.VISIBLE);
        btnSearch.startAnimation(animScale);
        btnSearch.startAnimation(animScaleReverse);
        btnDownload.setEnabled(true);
        edxLink.setText("");
    }

    Handler handler = new Handler();
    @Override
    public void setProgressCompleted(int key) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                mListener.updateList();

                DownloadItemViewModel model = findDownloadItemViewModelById(key);
                if(model != null){
                    downloadItemViewModels.remove(model);
                    if(downloadItemViewModels.size() == 0)
                        btnDownload.setEnabled(false);
                    progressAdapter.updateProgressObjects(
                            downloadItemViewModels,
                            mWaitingTaskYandexDownloaders,
                            mWaitingTaskYoutubeDownloaders);
                }
            }
        });
    }

    public DownloadItemViewModel findDownloadItemViewModelById(int key){
        for (DownloadItemViewModel model : downloadItemViewModels){
            if(model.getId() == key)
                return model;
        }
        return null;
    }

    //MainActivity
    private static DownloadSongFragmentListener mListener;

    public interface DownloadSongFragmentListener {
        void updateList();
    };

    public void setSongListFragmentListener(DownloadSongFragmentListener listener) {
        mListener = listener;
    }
}