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

import android.os.Environment;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import com.example.openmusic.LinkParse;
import com.example.openmusic.adpters.ProgressAdapter;
import com.example.openmusic.adpters.YDownloadItemAdapter;
import com.example.openmusic.downloaders.DownloaderListener;
import com.example.openmusic.downloaders.YandexDownloader;
import com.example.openmusic.downloaders.YoutubeDownloader;
import com.example.openmusic.models.DownloadItemViewModel;
import com.example.openmusic.R;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;


public class DownloadSongFragment extends Fragment implements
        AdapterView.OnItemClickListener,
        ProgressAdapter.OnYDownloadItemAdapter,
        DownloaderListener {

    EditText edxLink;
    ImageButton btnDownload, btnClear_link;
    TextView txtProgress;
    RecyclerView list_song_queue;
    private static final int REQUEST_CODE_WRITE_FILES = 2;
    private static boolean WRITE_FILES_GRANTED = false;

    ArrayList<DownloadItemViewModel> downloadItemViewModels = new ArrayList<>();
    private int numSimultaneousDownloads = 0, numberDownloads = 0;

   // YDownloadItemAdapter adapter;
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
        edxLink = v.findViewById(R.id.edxLink);
        txtProgress = v.findViewById(R.id.txtProgress);
        btnDownload = v.findViewById(R.id.btnDownload);
        btnClear_link = v.findViewById(R.id.btnClear_link);
        list_song_queue = v.findViewById(R.id.list_song_queue);
        list_song_queue.setLayoutManager(new LinearLayoutManager(getContext(),
                RecyclerView.VERTICAL, false));

/*        youtubeDownloader = new YoutubeDownloader();
        youtubeDownloader.setDownloaderListener(this);*/
        yandexDownloader = new YandexDownloader();
        yandexDownloader.setDownloaderListener(this);

       /* adapter = new YDownloadItemAdapter(getContext(), downloadItemViewModels);
        adapter.setOnYDownloadItemAdapter(this);
        list_song_queue.setAdapter(adapter);*/

        progressAdapter = new ProgressAdapter();
        list_song_queue.setAdapter(progressAdapter);
        progressAdapter.updateProgressObjects(downloadItemViewModels);

        animScale = AnimationUtils.loadAnimation(getContext(), R.anim.scale);
        animScaleReverse = AnimationUtils.loadAnimation(getContext(), R.anim.scale_reverse);

        btnClear_link.setOnClickListener(v1 -> {
            edxLink.setText("");
        });


        edxLink.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String link = s.toString();
                LinkParse linkParse = new LinkParse();
                linkParse.parse(link);
                switch (linkParse.getLinkType()){
                    case YANDEX_TRACK:
                        yandexDownloader.loadMetaDataByTrackId(linkParse.getId());
                        break;
                    case YANDEX_ALBUM:
                        yandexDownloader.loadMetaDataByAlbumId(linkParse.getId());
                        break;
                    case YOUTUBE:

                        break;
                    case YOUTUBE_MUSIC:

                        break;

                }
            }

            public void afterTextChanged(Editable s) {

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
        }
    }



    private static DownloadSongFragmentListener mListener;

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //positionAudioFormat = position;
    }



    @Override
    public void onDeleteClick(int position) {
        downloadItemViewModels.remove(position);
    }

    @Override
    public void onDownloadClick(int position) {


    }



    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void addMetaData(DownloadItemViewModel model) {
        downloadItemViewModels.add(model);
        progressAdapter.updateProgressObjects(downloadItemViewModels);
       // adapter.notifyDataSetChanged();
    }


    @Override
    public void setProgress(int progress) {
        /*downloadItemViewModels.get(position).setProgress(progress);
        adapter.notifyItemChanged(position);*/
    }

    @Override
    public void setProgressCompleted(int position) {
       /* DownloadItemViewModel model = downloadItemViewModels.get(position);
        if(model != null){
            //downloadItemViewModels.remove(model);

        }*/
        numSimultaneousDownloads--;
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