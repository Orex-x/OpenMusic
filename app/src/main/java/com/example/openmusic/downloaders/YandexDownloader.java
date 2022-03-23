package com.example.openmusic.downloaders;

import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.alibaba.fastjson.JSONException;
import com.example.openmusic.api.ApiClient;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import cz.msebera.android.httpclient.Header;

public class YandexDownloader {


    public void getDownloadLinkByTrackId(final String trackId) throws JSONException {
        ApiClient.get("GetYSongById?trackId="+trackId, null, new JsonHttpResponseHandler() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    String link = response.getString("link");
                    String name = response.getString("name");

                    File outputDir = new File(Environment.getExternalStoragePublicDirectory("Music").getPath());
                    Path path = Paths.get(outputDir.getPath(), name+".mp3");

                    downloadUsingByteArray(path, link);

                } catch (org.json.JSONException | IOException e) {
                    e.printStackTrace();
                }
            }
        });
        //Toast.makeText(getContext(), "Good", Toast.LENGTH_SHORT).show();
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void downloadUsingByteArray(Path destination, String link)
            throws IOException {

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(link, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                try {
                    Files.write(destination, response);
                    if(mListener != null)
                        mListener.updateList();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onProgress(long bytesWritten, long totalSize) {
                int progress = (int)((bytesWritten*100)/totalSize);
                Log.i("MyLOG", "bytesWritten: " + bytesWritten
                        + " totalSize: " + totalSize + " Progress " + progress + "%");
                mListener.setProgress(progress);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {

            }

            @Override
            public void onRetry(int retryNo) {

            }
            @Override
            public void onFinish() {
                mListener.setProgressCompleted();
            }

        });
    }

    private static DownloaderListener mListener;

    // метод-сеттер для привязки колбэка к получателю событий
    public void setDownloaderListener(DownloaderListener listener) {
        mListener = listener;
    }
}
