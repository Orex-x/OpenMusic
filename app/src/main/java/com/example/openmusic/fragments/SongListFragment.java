package com.example.openmusic.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.openmusic.models.Player;
import com.example.openmusic.PlayerController;
import com.example.openmusic.R;


public class SongListFragment extends Fragment {


    RecyclerView lvMusics;
    Button btnUpdateList;
    Player player;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.fragment_song_list, container, false);

        player = PlayerController.getPlayer();
        lvMusics = v.findViewById(R.id.lvMusics);
        btnUpdateList = v.findViewById(R.id.btnUpdateList);

        if(mListener != null)
            mListener.setAdapter(lvMusics);

        btnUpdateList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mListener != null)
                    mListener.updateList();
            }
        });
        return v;
    }

    // создаем поле объекта-колбэка
    private static SongListFragmentListener mListener;

    // создаем сам интерфейс и указываем метод и передаваемые им аргументы
    // View на котором произошло событие и позиция этого View
    public interface SongListFragmentListener {
       void setAdapter(RecyclerView recyclerView);
       void updateList();
    }

    // метод-сеттер для привязки колбэка к получателю событий
    public void setSongListFragmentListener(SongListFragmentListener listener) {
        mListener = listener;
    }


}