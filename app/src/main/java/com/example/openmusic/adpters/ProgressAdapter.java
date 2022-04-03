package com.example.openmusic.adpters;

import android.os.Build;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.example.openmusic.R;
import com.example.openmusic.downloaders.DownloaderListener;
import com.example.openmusic.downloaders.YandexDownloader;
import com.example.openmusic.models.DownloadItemViewModel;


import java.util.ArrayList;

public class ProgressAdapter extends RecyclerView.Adapter<ProgressAdapter.ProgressViewHolder> {

    private ArrayList<DownloadItemViewModel> mDownloadItemViewModelList = new ArrayList<>();
    private SparseArray<YandexDownloader> mWaitingTaskSparseArray = new SparseArray<>();



    public void updateProgressObjects(@NonNull ArrayList<DownloadItemViewModel> mDownloadItemViewModelList) {
        this.mDownloadItemViewModelList = mDownloadItemViewModelList;
        notifyDataSetChanged();
    }

    public void addMWaitingTaskSparseArray(DownloadItemViewModel model){
        mWaitingTaskSparseArray.put(model.getId(), new YandexDownloader(model));
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void downloadAllProgressObjects() {
        for(DownloadItemViewModel model : mDownloadItemViewModelList){
            YandexDownloader task = mWaitingTaskSparseArray.get(model.getId());
            task.downloadUsingByteArray();
            model.setDownloading(true);
        }
        notifyDataSetChanged();
    }


    @Override
    public ProgressViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View root = LayoutInflater.from(parent.getContext()).inflate(R.layout.download_item, parent, false);
        return new ProgressViewHolder(root);
    }

    @Override
    public void onBindViewHolder(ProgressViewHolder holder, int position) {
        holder.bind(mDownloadItemViewModelList.get(position));
    }


    @Override
    public void onViewRecycled(ProgressViewHolder holder) {
        YandexDownloader task = mWaitingTaskSparseArray.get(holder.getId());
        if (task != null) {
            task.updateListener(null);
        }
        super.onViewRecycled(holder);
    }

    @Override
    public int getItemCount() {
        return mDownloadItemViewModelList.size();
    }

    public class ProgressViewHolder extends RecyclerView.ViewHolder implements DownloaderListener {

        private ProgressBar mProgressBar;
        private TextView txtProgress, txtSongName;
        private ImageButton btnDownload, btnDelete;
        private int mId;


        ProgressViewHolder(View itemView) {
            super(itemView);
            mProgressBar = itemView.findViewById(R.id.progressBar);
            txtProgress = itemView.findViewById(R.id.txtProgress);
            txtSongName = itemView.findViewById(R.id.txtSongName);
            btnDownload = itemView.findViewById(R.id.btnDownload);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }


        void bind(final DownloadItemViewModel progressObject) {
            mId = progressObject.getId();
            txtSongName.setText(progressObject.getSongName());
            txtProgress.setText(progressObject.getProgress() + "%");
            mProgressBar.setProgress(progressObject.getProgress());

            YandexDownloader task = mWaitingTaskSparseArray.get(mId);
            if (task != null) {
                task.updateListener(this);
            }

            btnDownload.setOnClickListener(view -> {
                download();
                progressObject.setDownloading(true);
            });
        }

        public void download(){
            // Create the task, set the listener, add to the task controller, and run
            YandexDownloader task = mWaitingTaskSparseArray.get(mId);
            task.updateListener(ProgressViewHolder.this);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                task.downloadUsingByteArray();
            }
        }


        public int getId() {
            return mId;
        }

        @Override
        public void setProgress(int progress) {
            mProgressBar.setProgress(progress);
            txtProgress.setText(progress + "%");
        }

    }
}
