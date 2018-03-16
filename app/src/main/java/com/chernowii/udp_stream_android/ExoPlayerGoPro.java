package com.chernowii.udp_stream_android;

import android.app.Dialog;
import android.content.res.Configuration;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.decoder.DecoderCounters;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.LoopingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.upstream.UdpDataSource;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoRendererEventListener;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ExoPlayerGoPro extends AppCompatActivity implements VideoRendererEventListener {


    private static final String TAG = "ExoPlayer GP Demo";
    private SimpleExoPlayerView simpleExoPlayerView;
    private SimpleExoPlayer player;
    private String mFilePath;

    private final OkHttpClient client = new OkHttpClient();
    Integer count = 0;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exoplayer_preview);
        Button restart = (Button)findViewById(R.id.startPreview);
        restart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Stream();
                createPlayer(mFilePath);
            }
        });
        Button stop = (Button)findViewById(R.id.stoppreviewbtn);
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FFmpeg ffmpeg = FFmpeg.getInstance(getApplicationContext());
                if(ffmpeg.isFFmpegCommandRunning()){
                    ffmpeg.killRunningProcesses();
                }
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        });
        Button setHQ = (Button)findViewById(R.id.setQuality);
        setHQ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(ExoPlayerGoPro.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.radiobutton_dialog);
                List<String> stringList=new ArrayList<>();  // here is list
                final String[] resolutions =  {"720p","480p","240p"};
                for(int i=0;i<resolutions.length;i++) {
                    stringList.add(resolutions[i]);
                }
                RadioGroup rg = (RadioGroup) dialog.findViewById(R.id.radio_group);

                for(int i=0;i<stringList.size();i++){
                    RadioButton rb=new RadioButton(ExoPlayerGoPro.this); // dynamically creating RadioButton and adding to RadioGroup.
                    rb.setText(stringList.get(i));
                    rg.addView(rb);
                }

                dialog.show();
                rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        int childCount = group.getChildCount();
                        for (int x = 0; x < childCount; x++) {
                            RadioButton btn = (RadioButton) group.getChildAt(x);
                            if (btn.getId() == checkedId) {
                                switch (btn.getText().toString()){
                                    case "720p":
                                        GoProSet("64", "7");
                                        setBitrate();
                                    case "480p":
                                        GoProSet("64", "4");
                                        setBitrate();
                                    case "240p":
                                        GoProSet("64", "1");
                                        setBitrate();
                                }

                            }
                        }
                    }
                });
            }
        });
        Stream();
        mFilePath = "udp://@:8555/gopro";
        Log.d(TAG, "Playing: " + mFilePath);

    }
    void setBitrate(){
        final Dialog dialog = new Dialog(ExoPlayerGoPro.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.radiobutton_dialog);
        List<String> stringList=new ArrayList<>();  // here is list
        final String[] bitrates =  {"4 Mbps","2 Mbps","1 Mbps","600 Kbps","250 Kbps"};
        for(int i=0;i<bitrates.length;i++) {
            stringList.add(bitrates[i]);
        }
        RadioGroup rg = (RadioGroup) dialog.findViewById(R.id.radio_group);

        for(int i=0;i<stringList.size();i++){
            RadioButton rb=new RadioButton(ExoPlayerGoPro.this); // dynamically creating RadioButton and adding to RadioGroup.
            rb.setText(stringList.get(i));
            rg.addView(rb);
        }

        dialog.show();
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int childCount = group.getChildCount();
                for (int x = 0; x < childCount; x++) {
                    RadioButton btn = (RadioButton) group.getChildAt(x);
                    if (btn.getId() == checkedId) {
                        switch (btn.getText().toString()){
                            case "4 Mbps":
                                GoProSet("62", "4000000");
                            case "2 Mbps":
                                GoProSet("64", "2000000");
                            case "1 Mbps":
                                GoProSet("64", "1000000");
                            case "600 Kbps":
                                GoProSet("64", "600000");
                            case "250 Kbps":
                                GoProSet("64", "250000");
                        }

                        Stream();

                    }
                }
            }
        });


    }


    void GoProSet(String param, String value){
        final Request startpreview = new Request.Builder()
                .url(HttpUrl.get(URI.create("http://10.5.5.9/gp/gpControl/setting/" + param + "/" + value)))
                .build();

        client.newCall(startpreview).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()){
                    Log.d("GoPro","Camera not connected");
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
        createPlayer(mFilePath);
    }


    void createPlayer(String streampath){
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        LoadControl loadControl = new DefaultLoadControl();
        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector, loadControl);
        simpleExoPlayerView = new SimpleExoPlayerView(this);
        simpleExoPlayerView = (SimpleExoPlayerView) findViewById(R.id.exoplayer_gopro_stream);
        simpleExoPlayerView.setUseController(true);
        simpleExoPlayerView.requestFocus();
        simpleExoPlayerView.setPlayer(player);
        final Uri gpSource = Uri.parse("udp://@127.0.0.1:8555/gopro");
        DefaultBandwidthMeter bandwidthMeterA = new DefaultBandwidthMeter();
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "udp_stream_android"), bandwidthMeterA);
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        final MediaSource udpListener = new ExtractorMediaSource(gpSource, dataSourceFactory, extractorsFactory, null, null);
        UdpDataSource udpDataSource = new UdpDataSource(new TransferListener<UdpDataSource>() {
            @Override
            public void onTransferStart(UdpDataSource source, DataSpec dataSpec) {
                try {
                    source.open(new DataSpec(gpSource));
                } catch (UdpDataSource.UdpDataSourceException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onBytesTransferred(UdpDataSource source, int bytesTransferred) {
                try {
                    source.open(new DataSpec(gpSource));
                } catch (UdpDataSource.UdpDataSourceException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onTransferEnd(UdpDataSource source) {
                try {
                    source.open(new DataSpec(gpSource));
                } catch (UdpDataSource.UdpDataSourceException e) {
                    e.printStackTrace();
                }
            }
        });
        //MediaSource videoSource = new HlsMediaSource(gpSource, dataSourceFactory, 1, null, null);
        //UdpDataSource gpStreamSource = new UdpDataSource((TransferListener<? super UdpDataSource>) udpListener);
        player.prepare(udpListener);
        player.addListener(new ExoPlayer.EventListener() {
            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest) {
                Log.v(TAG, "Listener-onTimelineChanged...");
            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
                Log.v(TAG, "Listener-onTracksChanged...");
            }

            @Override
            public void onLoadingChanged(boolean isLoading) {
                Log.v(TAG, "Listener-onLoadingChanged...isLoading:"+isLoading);
            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                Log.v(TAG, "Listener-onPlayerStateChanged..." + playbackState);
            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {
                Log.v(TAG, "Listener-onRepeatModeChanged...");
            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
                Log.v(TAG, "Listener-onPlayerError...");
                player.stop();
                player.prepare(udpListener);
                player.setPlayWhenReady(true);
            }

            @Override
            public void onPositionDiscontinuity() {
                Log.v(TAG, "Listener-onPositionDiscontinuity...");
            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
                Log.v(TAG, "Listener-onPlaybackParametersChanged...");
            }
        });

        player.setPlayWhenReady(true);
        player.setVideoDebugListener(this);
    }


    @Override
    public void onVideoEnabled(DecoderCounters counters) {

    }

    @Override
    public void onVideoDecoderInitialized(String decoderName, long initializedTimestampMs, long initializationDurationMs) {

    }

    @Override
    public void onVideoInputFormatChanged(Format format) {

    }

    @Override
    public void onDroppedFrames(int count, long elapsedMs) {

    }

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {

    }

    @Override
    public void onRenderedFirstFrame(Surface surface) {

    }

    @Override
    public void onVideoDisabled(DecoderCounters counters) {

    }
}