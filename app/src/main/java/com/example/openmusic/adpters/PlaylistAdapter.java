package com.example.openmusic.adpters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.example.openmusic.R;
import com.example.openmusic.models.Playlist;
import com.example.openmusic.models.Song;

import java.util.ArrayList;
import java.util.Locale;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.ViewHolder>{

    private final LayoutInflater inflater;
    private final ArrayList<Playlist> playlists;


    // создаем поле объекта-колбэка
    private static OnPlaylistAdapterListener mListener;

    // метод-сеттер для привязки колбэка к получателю событий
    public void setOnPlaylistAdapterListener(OnPlaylistAdapterListener listener) {
        mListener = listener;
    }

    // создаем сам интерфейс и указываем метод и передаваемые им аргументы
    // View на котором произошло событие и позиция этого View
    public interface OnPlaylistAdapterListener {
        void onLayoutClick(int position);
    }

    public PlaylistAdapter(LayoutInflater inflater, ArrayList<Playlist> playlists) {
        this.inflater = inflater;
        this.playlists = playlists;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.playlist_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Playlist playlist = playlists.get(position);
        holder.txtPlaylistName.setText(playlist.getName());
    }

    @Override
    public int getItemCount() {
        return playlists.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        ConstraintLayout layout;
        final TextView txtPlaylistName;
        //final ImageView imgPlaylistName;

        ViewHolder(View view){
            super(view);
            layout = view.findViewById(R.id.layout);
            txtPlaylistName = view.findViewById(R.id.txtPlaylistName);
            //imgPlaylistName = view.findViewById(R.id.imgPlaylistName);

            layout.setOnClickListener(v -> {
                int position = getAdapterPosition();
                mListener.onLayoutClick(position);
            });
        }
    }
}
