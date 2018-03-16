package com.chernowii.udp_stream_android;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.URI;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by konrad on 3/14/18.
 */

public class utils {
    static void callHTTP(final Context context){
        final OkHttpClient client = new OkHttpClient();

        final Request startpreview = new Request.Builder()
                .url(HttpUrl.get(URI.create("http://10.5.5.9/gp/gpControl/execute?p1=gpStream&a1=proto_v2&c1=restart")))
                .build();

        client.newCall(startpreview).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()){
                    Toast.makeText(context,"Camera not connected!",Toast.LENGTH_SHORT).show();
                }


            }
        });
    }
    static void loadFFmpeg(Context con){
        FFmpeg ffmpeg = FFmpeg.getInstance(con);
        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {

                @Override
                public void onStart() {
                    Log.d("LoadBinary","Start");

                }

                @Override
                public void onFailure() {
                    Log.d("LoadBinary","Fail");

                }

                @Override
                public void onSuccess() {
                    Log.d("LoadBinary","Success");

                }

                @Override
                public void onFinish() {
                    Log.d("LoadBinary","Finish");

                }
            });
        } catch (FFmpegNotSupportedException e) {
            // Handle if FFmpeg is not supported by device
        }
    }
    static void sendMagicPacket() {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            String sendMessage = "GPHD:0:0:2:0.000000\n";
            byte[] sendData = sendMessage.getBytes();
            InetAddress IPAddress = InetAddress.getByName("10.5.5.9");
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 8554);
            socket.send(sendPacket);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    static class sendAsyncMagicPacket extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            DatagramSocket socket = null;
            try {
                socket = new DatagramSocket();
                String sendMessage = "GPHD:0:0:2:0.000000\n";
                byte[] sendData = sendMessage.getBytes();
                InetAddress IPAddress = InetAddress.getByName("10.5.5.9");
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 8554);
                socket.send(sendPacket);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (socket != null) {
                        socket.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }
    }

}
