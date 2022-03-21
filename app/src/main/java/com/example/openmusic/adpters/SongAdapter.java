package com.example.openmusic.adpters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.openmusic.R;
import com.example.openmusic.models.Song;

import java.util.ArrayList;


public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder>{

    private final LayoutInflater inflater;
    private final ArrayList<Song> songs;

    // создаем сам интерфейс и указываем метод и передаваемые им аргументы
    // View на котором произошло событие и позиция этого View
    public interface OnCardClickListener {
        void onDeleteClick(View view, int position);
        void onSongClick(View view, int position);
    }

    // создаем поле объекта-колбэка
    private static OnCardClickListener mListener;


    public SongAdapter(Context context, ArrayList<Song> states) {
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
        if(song.getTitle().length() > 30){
            holder.txtListItem.setText(song.getTitle().substring(0,30) + "...");
        }else
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
        final ImageView imageView;
        final ConstraintLayout layout;

        ViewHolder(View view){
            super(view);
            txtListItem = view.findViewById(R.id.txtListItem);
            btnDelete = view.findViewById(R.id.btnDelete);
            imageView = view.findViewById(R.id.imageView);
            layout = view.findViewById(R.id.layout_song_item);
            layout.setOnClickListener(v -> {
                int position = getAdapterPosition();
                mListener.onSongClick(v, position);
            });
            btnDelete.setOnClickListener(v -> {
                int position = getAdapterPosition();
                mListener.onDeleteClick(v, position);
            });
        }
    }
}
