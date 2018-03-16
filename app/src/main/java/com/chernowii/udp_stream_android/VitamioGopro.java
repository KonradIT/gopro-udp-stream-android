package com.chernowii.udp_stream_android;

import android.app.Dialog;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;


import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class VitamioGopro extends AppCompatActivity {
    private VideoView mVideoView;
    private final OkHttpClient client = new OkHttpClient();
    Integer count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vitamio_gopro);

        Stream();
    }
    void callHTTP(){
        final Request startpreview = new Request.Builder()
                .url(HttpUrl.get(URI.create("http://10.5.5.9/gp/gpControl/execute?p1=gpStream&a1=proto_v2&c1=restart")))
                .build();

        client.newCall(startpreview).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()){
                    Toast.makeText(getApplicationContext(),"Camera not connected!",Toast.LENGTH_SHORT).show();
                }


            }
        });
    }
    void Stream(){

        //Call http://10.5.5.9/gp/gpControl/execute?p1=gpStream&a1=proto_v2&c1=restart

        utils.callHTTP(getApplicationContext());
        try {
            String[] cmd = {"-f", "mpegts", "-i", "udp://:8554", "-f", "mpegts","udp://127.0.0.1:8555/gopro?pkt_size=64"};
            FFmpeg ffmpeg = FFmpeg.getInstance(getApplicationContext());

            ffmpeg.execute(cmd, new ExecuteBinaryResponseHandler() {

                @Override
                public void onStart() {
                    count += 1;
                    if(count == 7){
                        count = 0;
                        utils.sendMagicPacket();
                    }
                }

                @Override
                public void onProgress(String message) {
                    Log.d("FFmpeg",message);
                    utils.callHTTP(getApplicationContext());

                }

                @Override
                public void onFailure(String message) {
                    Toast.makeText(getApplicationContext(),"Stream fail",Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onSuccess(String message) {}

                @Override
                public void onFinish() {}
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            // Handle if FFmpeg is already running
        }
        //Preview();
        createPlayer();
    }

    void createPlayer(){

        mVideoView = (VideoView) findViewById(R.id.vitamio_videoView);
        mVideoView.setVideoURI(Uri.parse("udp://@127.0.0.1:10000/gopro"));
        mVideoView.setMediaController(new MediaController(this));
        mVideoView.requestFocus();
        mVideoView.start();
    }

}
