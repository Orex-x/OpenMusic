package com.example.openmusic;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;


public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder>{

    private final LayoutInflater inflater;
    private final ArrayList<Song> songs;

    // создаем сам интерфейс и указываем метод и передаваемые им аргументы
    // View на котором произошло событие и позиция этого View
    interface OnCardClickListener {
        void onDeleteClick(View view, int position);
        void onSongClick(View view, int position);
    }

    // создаем поле объекта-колбэка
    private static OnCardClickListener mListener;


    SongAdapter(Context context, ArrayList<Song> states) {
        this.songs = states;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public SongAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Song song = songs.get(position);
        holder.txtListItem.setText(song.getTitle());
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    // метод-сеттер для привязки колбэка к получателю событий
    public void setOnCardClickListener(OnCardClickListener listener) {
        mListener = listener;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder{

        final TextView txtListItem;
        final Button btnDelete;

        ViewHolder(View view){
            super(view);
            txtListItem = view.findViewById(R.id.txtListItem);
            txtListItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    mListener.onSongClick(v, position);
                }
            });
            btnDelete = view.findViewById(R.id.btnDelete);
            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    mListener.onDeleteClick(v, position);
                }
            });
        }
    }
}
