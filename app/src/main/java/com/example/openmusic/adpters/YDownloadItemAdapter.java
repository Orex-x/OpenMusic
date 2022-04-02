package com.example.openmusic.adpters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.openmusic.R;
import com.example.openmusic.models.DownloadItemViewModel;

import java.util.ArrayList;

public class YDownloadItemAdapter extends RecyclerView.Adapter<YDownloadItemAdapter.ViewHolder>{

    /*private static OnYDownloadItemAdapter mListener;

    public void setOnYDownloadItemAdapter(OnYDownloadItemAdapter listener) {
        mListener = listener;
    }

    public interface OnYDownloadItemAdapter {
        void onDeleteClick(int position);
        void onDownloadClick(int position);
    }*/


    private final LayoutInflater inflater;
    private final ArrayList<DownloadItemViewModel> downloadItemViewModels;

    public YDownloadItemAdapter(Context context,
                                ArrayList<DownloadItemViewModel> downloadItemViewModel) {
        this.inflater = LayoutInflater.from(context);
        this.downloadItemViewModels = downloadItemViewModel;
    }

    @Override
    public YDownloadItemAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.download_item, parent, false);
        return new YDownloadItemAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(YDownloadItemAdapter.ViewHolder holder, int position) {
        DownloadItemViewModel downloadItemViewModel = downloadItemViewModels.get(position);
        holder.txtProgress.setText(downloadItemViewModel.getProgress() + "%");
        holder.txtSongName.setText(downloadItemViewModel.getSongName());
        holder.progressBar.setProgress(downloadItemViewModel.getProgress());
    }

    @Override
    public int getItemCount() {
        return downloadItemViewModels.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder  {

        final ProgressBar progressBar;
        final TextView txtProgress, txtSongName;
        final ImageButton btnDownload, btnDelete;
       //final YandexDownloader yandexDownloader;

        ViewHolder(View view){
            super(view);

           // yandexDownloader = new YandexDownloader();
            //yandexDownloader.setDownloaderListener(this);

            progressBar = view.findViewById(R.id.progressBar);
            progressBar.setMax(100);
            txtProgress = view.findViewById(R.id.txtProgress);
            txtSongName = view.findViewById(R.id.txtSongName);
            btnDownload = view.findViewById(R.id.btnDownload);
            btnDelete = view.findViewById(R.id.btnDelete);
/*            btnDownload.setOnClickListener(v -> mListener.onDownloadClick(getAdapterPosition()));
            btnDelete.setOnClickListener(v -> mListener.onDeleteClick(getAdapterPosition()));*/
        }

       /* @SuppressLint("SetTextI18n")
        @Override
        public void setProgress(int progress) {


            progressBar.setProgress(progress);
            txtProgress.setText(progress + "%");
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void setProgressCompleted() {
            txtProgress.setText("Completed");
        }*/
    }
}
