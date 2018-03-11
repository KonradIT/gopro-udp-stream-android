package com.chernowii.udp_stream_android;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MethodSelector extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_method_selector);
        Button openVLC = (Button)findViewById(R.id.stream_vlc);
        openVLC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openVLCIntent= new Intent(MethodSelector.this, GoProPreview.class);
                startActivity(openVLCIntent);
            }
        });

        Button openExoPlayer = (Button)findViewById(R.id.stream_exp);
        openExoPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openEXPIntent= new Intent(MethodSelector.this, ExoPlayerGoPro.class);
                startActivity(openEXPIntent);
            }
        });
    }

}
