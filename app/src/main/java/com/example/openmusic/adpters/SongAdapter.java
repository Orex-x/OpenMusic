package com.example.openmusic.adpters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.chauthai.swipereveallayout.ViewBinderHelper;
import com.example.openmusic.R;
import com.example.openmusic.downloaders.DownloaderListener;
import com.example.openmusic.models.Song;

import java.util.ArrayList;
import java.util.Locale;


public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder>{

    private final LayoutInflater inflater;
    private final ArrayList<Song> songs;
    private ArrayList<Song> filterSongs = new ArrayList<>();
    private String search;

    private final ViewBinderHelper viewBinderHelper = new ViewBinderHelper();

    public void setSearch(String search) {
        this.search = search;
    }



    // создаем поле объекта-колбэка
    private static OnCardClickListener mListener;

    // метод-сеттер для привязки колбэка к получателю событий
    public void setOnCardClickListener(OnCardClickListener listener) {
        mListener = listener;
    }



    // создаем сам интерфейс и указываем метод и передаваемые им аргументы
    // View на котором произошло событие и позиция этого View
    public interface OnCardClickListener {
        void onDeleteClick(View view, int position);
        void onSongClick(View view, int position);
    }


    public SongAdapter(Context context, ArrayList<Song> states) {
        this.songs = states;
        this.inflater = LayoutInflater.from(context);
        this.search = "";
        // uncomment the line below if you want to open only one row at a time
         viewBinderHelper.setOpenOnlyOne(true);
    }

    @Override
    public SongAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.music_item_swipe, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Song song = songs.get(position);
        viewBinderHelper.bind(holder.swipeLayout, String.valueOf(position));
        if(search.length() != 0){
            if(!song.getDisplayName().toLowerCase(Locale.ROOT).contains(search.toLowerCase(Locale.ROOT))
                    || !song.getTitle().toLowerCase(Locale.ROOT).contains(search.toLowerCase(Locale.ROOT))){
                return;
            }
        }

        if(song.getTitle().length() > 30){
            holder.txtListItem.setText(song.getTitle().substring(0,30) + "...");
        }else
            holder.txtListItem.setText(song.getTitle());

    }

    @Override
    public int getItemCount() {
        return songs.size();
    }


    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    filterSongs = songs;
                } else {
                    ArrayList<Song> filteredList = new ArrayList<>();
                    for (Song row : songs) {
                        //тут прописываешь условие по которому ты отбираешь данные
                        if (row.getDisplayName().toLowerCase(Locale.ROOT).contains(search.toLowerCase(Locale.ROOT))
                                || row.getTitle().toLowerCase(Locale.ROOT).contains(search.toLowerCase(Locale.ROOT))){
                            filteredList.add(row);
                        }
                    }

                    //присваиваем нашему листу, который отвечает за фильтр, рузельтат
                    filterSongs = filteredList;
                }

                //Отправляем результат фильтрации
                FilterResults filterResults = new FilterResults();
                filterResults.values = filterSongs;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                filterSongs = (ArrayList<Song>) filterResults.values;
                notifyDataSetChanged();
                //говорим адаптеру что данные изменились
            }
        };
    }





    public static class ViewHolder extends RecyclerView.ViewHolder{

        SwipeRevealLayout swipeLayout;
        final TextView txtListItem;
        final Button btnDelete;
        final ImageView imageView;
        final ConstraintLayout layout;

        ViewHolder(View view){
            super(view);
            swipeLayout = view.findViewById(R.id.swipe_layout);
            txtListItem = view.findViewById(R.id.txtListItem);
            btnDelete = view.findViewById(R.id.btnDelete);
            imageView = view.findViewById(R.id.imageView);
            layout = view.findViewById(R.id.layout_song_item);
            layout.setOnClickListener(v -> {
                int position = getAdapterPosition();
                mListener.onSongClick(v, position);
            });
            swipeLayout.setOnClickListener(v -> {
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
