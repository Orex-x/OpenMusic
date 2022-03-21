package com.example.openmusic.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.example.openmusic.models.Player;
import com.example.openmusic.PlayerController;
import com.example.openmusic.R;


public class SongListFragment extends Fragment {


    RecyclerView lvMusics;
    ImageButton btnUpdateList;
    Player player;
    EditText edtSearch;
    Animation animScale;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.fragment_song_list, container, false);

        player = PlayerController.getPlayer();
        lvMusics = v.findViewById(R.id.lvMusics);
        btnUpdateList = v.findViewById(R.id.btnUpdateList);
        edtSearch = v.findViewById(R.id.edtSearch);

        animScale = AnimationUtils.loadAnimation(getContext(), R.anim.scale);

        if(mListener != null)
            mListener.setAdapter(lvMusics);

        btnUpdateList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mListener != null){
                    v.startAnimation(animScale);
                    String search = edtSearch.getText().toString();
                    mListener.search(search);

                }
            }
        });

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(count == 0)
                    mListener.search("");
            }

            @Override
            public void afterTextChanged(Editable s) {

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
       void search(String search);
    }

    // метод-сеттер для привязки колбэка к получателю событий
    public void setSongListFragmentListener(SongListFragmentListener listener) {
        mListener = listener;
    }
}