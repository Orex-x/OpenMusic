package com.example.openmusic.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.openmusic.R;
import com.example.openmusic.adpters.PlaylistAdapter;
import com.example.openmusic.adpters.SongAdapter;
import com.example.openmusic.models.Playlist;
import com.example.openmusic.service.MusicRepository;

import java.util.ArrayList;


public class PlaylistsFragment extends Fragment
        implements PlaylistAdapter.OnPlaylistAdapterListener {


    RecyclerView list_playlist;
    RecyclerView list_songs_in_playlist;
    MusicRepository musicRepository;
    PlaylistAdapter playlistAdapter;
    SongAdapter songAdapter;
    ImageButton btnBack;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_playlists, container, false);

        list_playlist = v.findViewById(R.id.list_playlist);
        list_songs_in_playlist = v.findViewById(R.id.list_songs_in_playlist);
        btnBack = v.findViewById(R.id.btnBack);

        list_playlist.setLayoutManager(new LinearLayoutManager(getContext(),
                RecyclerView.VERTICAL, false));
        list_songs_in_playlist.setLayoutManager(new LinearLayoutManager(getContext(),
                RecyclerView.VERTICAL, false));


        musicRepository = MusicRepository.getMusicRepository();
        musicRepository.updatePlayLists();
        playlistAdapter = new PlaylistAdapter(inflater, musicRepository.getPlaylists());
        playlistAdapter.setOnPlaylistAdapterListener(this);
        list_playlist.setAdapter(playlistAdapter);

        btnBack.setOnClickListener(this::btnBackClick);

        if(mListener != null)
            mListener.setAdapter(list_songs_in_playlist);
        return v;
    }

    @Override
    public void onLayoutClick(int position) {
        String dir = musicRepository.getPlaylists().get(position).getName();
        mListener.updateSongsList(dir);
        list_playlist.setVisibility(View.GONE);
        btnBack.setVisibility(View.VISIBLE);
        list_songs_in_playlist.setVisibility(View.VISIBLE);

    }

    public void btnBackClick(View view){
        mListener.updateSongsList();
        list_playlist.setVisibility(View.VISIBLE);
        btnBack.setVisibility(View.GONE);
        list_songs_in_playlist.setVisibility(View.GONE);
    }



    // создаем поле объекта-колбэка
    private static PlaylistsFragmentListener mListener;

    // создаем сам интерфейс и указываем метод и передаваемые им аргументы
    // View на котором произошло событие и позиция этого View
    public interface PlaylistsFragmentListener {
        void setAdapter(RecyclerView recyclerView);
        void updateSongsList(String dir);
        void updateSongsList();
    }

    // метод-сеттер для привязки колбэка к получателю событий
    public void setPlaylistsFragmentListener(PlaylistsFragmentListener listener) {
        mListener = listener;
    }
}