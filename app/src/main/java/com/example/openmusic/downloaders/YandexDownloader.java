package com.example.openmusic.downloaders;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.alibaba.fastjson.JSONException;
import com.example.openmusic.LinkParse;
import com.example.openmusic.MainActivity;
import com.example.openmusic.api.ApiClient;
import com.example.openmusic.downloaders.DownloaderListener;
import com.example.openmusic.downloaders.DownloaderMetaDataListener;
import com.example.openmusic.models.DownloadItemViewModel;
import com.example.openmusic.models.Player;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import cz.msebera.android.httpclient.Header;

public class YandexDownloader {

    private final String CLIENT_ID = "23cabbbdc6cd418abb4b39c32c41195d";
    private final String CLIENT_SECRET = "53bc75238f0c4d08a118e51fe9203300";

    private DownloadItemViewModel mDownloadItemViewModel;
    private WeakReference<DownloaderListener> mWaitingListenerWeakReference;

    public YandexDownloader(){

    }



    public YandexDownloader(DownloadItemViewModel progressObject) {
        mDownloadItemViewModel = progressObject;
    }

    public void updateListener(@Nullable DownloaderListener waitingListener) {
        mWaitingListenerWeakReference = new WeakReference<>(waitingListener);
    }



    public void authorize(String login, String password){
        RequestParams params = new RequestParams();
        params.put("grant_type", "password");
        params.put("client_id", CLIENT_ID);
        params.put("client_secret", CLIENT_SECRET);
        params.put("username", login);
        params.put("password", password);

        ApiClient.post("https://oauth.yandex.ru/token/", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    String access_token = response.getString("access_token");
                    String uid = response.getString("uid");

                } catch (org.json.JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void loadMetaDataByTrackId(final String trackId) throws JSONException {
        ApiClient.get("GetYSongById?trackId="+trackId, null, new JsonHttpResponseHandler() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    String link = response.getString("link");
                    String name = response.getString("name");
                    String id = response.getString("id");
                    if(downloaderMetaDataListener != null)
                        downloaderMetaDataListener.addMetaData(
                                new DownloadItemViewModel(Integer.parseInt(id), name, link, LinkParse.LinkType.YANDEX_TRACK));
                } catch (org.json.JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onProgress(long bytesWritten, long totalSize) {
                int progress = (int)((bytesWritten*100)/totalSize);
                Log.i("MyLOG", "bytesWritten: " + bytesWritten
                        + " totalSize: " + totalSize + " Progress " + progress + "%");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
            }
        });
    }

    public void loadMetaDataByAlbumId(final String albumId) throws JSONException {
        ApiClient.get("GetYSongsByAlbumId?albumId="+albumId, null, new JsonHttpResponseHandler() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                for(int i = 0; i < response.length(); i++){
                    try {
                        JSONObject o = (JSONObject) response.get(i);
                        String link = o.getString("link");
                        String name = o.getString("name");
                        String id = o.getString("id");
                        if(downloaderMetaDataListener != null)
                            downloaderMetaDataListener.addMetaData(
                                    new DownloadItemViewModel(Integer.parseInt(id) ,name, link, LinkParse.LinkType.YANDEX_TRACK));
                    } catch (org.json.JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            @Override
            public void onProgress(long bytesWritten, long totalSize) {
                int progress = (int)((bytesWritten*100)/totalSize);
                Log.i("MyLOG", "bytesWritten: " + bytesWritten
                        + " totalSize: " + totalSize + " Progress " + progress + "%");
            }
        });
    }

    class WaitingTask extends AsyncTask<Void, Integer, Void> {
        @Override
        protected Void doInBackground(Void... voids) {

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                File outputDir = new File(Environment.getExternalStoragePublicDirectory("Music").getPath());
                Path path = Paths.get(outputDir.getPath(), mDownloadItemViewModel.getSongName()+".mp3");

                SyncHttpClient client = new SyncHttpClient();
                client.get(mDownloadItemViewModel.getLink(), new AsyncHttpResponseHandler() {

                    @Override
                    public void onStart() {
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                        try {
                            Files.write(path, response);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onProgress(long bytesWritten, long totalSize) {
                        int progress = (int)((bytesWritten*100)/totalSize);
                        Log.i("MyLOG", "bytesWritten: " + bytesWritten
                                + " totalSize: " + totalSize + " Progress " + progress + "%");


                        mDownloadItemViewModel.setProgress(progress);	// Update data set
                        if (mWaitingListenerWeakReference != null) {
                            DownloaderListener listener = mWaitingListenerWeakReference.get();
                            if (listener != null) {
                                listener.setProgress(progress);
                            }
                        }

                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {

                    }

                    @Override
                    public void onRetry(int retryNo) {

                    }
                    @Override
                    public void onFinish() {
                        downloaderMetaDataListener.setProgressCompleted(mDownloadItemViewModel.getId());
                    }

                });
            }
            return null;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public WaitingTask downloadUsingByteArray(){
       WaitingTask task = new WaitingTask();
       task.execute();
       return task;
    }

    private static DownloaderMetaDataListener downloaderMetaDataListener;

    // метод-сеттер для привязки колбэка к получателю событий
    public void setDownloaderListener(DownloaderMetaDataListener listener) {
        downloaderMetaDataListener = listener;
    }
}



