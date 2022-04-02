package com.example.openmusic.adpters;

import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.DownloadListener;
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


import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

public class ProgressAdapter extends RecyclerView.Adapter<ProgressAdapter.ProgressViewHolder> {

    private static OnYDownloadItemAdapter mListener;

    public void setOnYDownloadItemAdapter(OnYDownloadItemAdapter listener) {
        mListener = listener;
    }

    public interface OnYDownloadItemAdapter {
        void onDeleteClick(int position);
        void onDownloadClick(int position);
    }

    private ArrayList<DownloadItemViewModel> mDownloadItemViewModelList = new ArrayList<>();
    private SparseArray<YandexDownloader> mWaitingTaskSparseArray = new SparseArray<>();
    private int numQuery = 0;
    private Queue<YandexDownloader> QueryStack = new LinkedList<>();

    public void updateProgressObjects(@NonNull ArrayList<DownloadItemViewModel> mDownloadItemViewModelList) {
        this.mDownloadItemViewModelList = mDownloadItemViewModelList;
        notifyDataSetChanged();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void downloadAllProgressObjects() {
        class Query extends Thread{
            @Override
            public void run() {
                while (!QueryStack.isEmpty() && numQuery < 2){
                    YandexDownloader downloader = QueryStack.remove();
                    downloader.downloadUsingByteArray();
                }
            }
        }
        new Query().start();
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
            mProgressBar.setProgress(progressObject.getProgress());

            YandexDownloader task = mWaitingTaskSparseArray.get(mId);
            if (task != null) {
                task.updateListener(this);
            }

            btnDownload.setOnClickListener(view -> {
                download(progressObject);
            });

            if(!progressObject.isQueue()){
                progressObject.setQueue(true);
                YandexDownloader task2 = new YandexDownloader(progressObject, ProgressViewHolder.this);
                mWaitingTaskSparseArray.put(mId, task2);
                QueryStack.offer(task2);
            }
        }

        public void download(final DownloadItemViewModel progressObject){
            // Create the task, set the listener, add to the task controller, and run
            YandexDownloader task = new YandexDownloader(progressObject, ProgressViewHolder.this);
            mWaitingTaskSparseArray.put(mId, task);
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
        }

        @Override
        public void setProgressCompleted(int position) {
            numQuery--;
        }

        @Override
        public void addMetaData(DownloadItemViewModel model) {

        }
    }
}
