package com.example.audiotracktest;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.ContentObserver;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.audiofx.Equalizer;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity{

    public int createdAudioSessionId ;
    public AudioTrack audioTrack = null;
    public int targetStreamForVolumeChange = 3;
    public Boolean isPlaying = false;
    public Thread playThread;
    public Button buttonPlayAudioTrack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // SETUP OBSERVER FOR VOLUME CHANGES
        ContentObserver observer = new ContentObserver(new Handler(Looper.myLooper())) {
            @Override
            public void onChange(boolean selfChange, @Nullable Uri uri) {
                super.onChange(selfChange, uri);

                AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

                Log.d("1234", "new ONCHANGE system settings " + uri.toString() + " current mode " + audioManager.getMode());
                // Log.d("1234", "ONCHANGE stream MUSIC max " + audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) );
                Log.d("1234", "ONCHANGE stream MUSIC current " + audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) );
                // Log.d("1234", "ONCHANGE stream VOICe CALL max " + audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL) );
                Log.d("1234", "ONCHANGE stream VOICe CALL current " + audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL) );
                Log.d("1234", "ONCHANGE stream STREAM_ACCESSIBILITY current " + audioManager.getStreamVolume(AudioManager.STREAM_ACCESSIBILITY) );
                Log.d("1234", "ONCHANGE stream STREAM_ALARM current " + audioManager.getStreamVolume(AudioManager.STREAM_ALARM) );
                Log.d("1234", "ONCHANGE stream STREAM_DTMF current " + audioManager.getStreamVolume(AudioManager.STREAM_DTMF) );
                Log.d("1234", "ONCHANGE stream STREAM_NOTIFICATION current " + audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION) );
                Log.d("1234", "ONCHANGE stream STREAM_RING current " + audioManager.getStreamVolume(AudioManager.STREAM_RING) );
                Log.d("1234", "ONCHANGE stream STREAM_SYSTEM current " + audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM) );
            }
        };
        getApplicationContext().getContentResolver().registerContentObserver(Settings.System.CONTENT_URI, true, observer);

        AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
       // setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
       // audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        audioManager.setMode(AudioManager.MODE_NORMAL);

        Log.d("1234", "stream control BEFORE "+ getVolumeControlStream());

        Log.d("1234", "stream control AFTER "+ getVolumeControlStream());

        Log.d("1234", "volume fixed "+ audioManager.isVolumeFixed());
        Log.d("1234", "mode "+ audioManager.getMode());

        final ToggleButton musicModeToggle = findViewById(R.id.musicMode_toggle);
        final ToggleButton callModeToggle = findViewById(R.id.callMode_toggle);

        musicModeToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    callModeToggle.setChecked(false);
                    audioTrack = createAudioTrack(AudioAttributes.USAGE_MEDIA, AudioAttributes.CONTENT_TYPE_MUSIC);
                    audioManager.setMode(AudioManager.MODE_NORMAL);
                    targetStreamForVolumeChange = AudioManager.STREAM_MUSIC;
                }
            }
        });
        callModeToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    musicModeToggle.setChecked(false);
                    audioTrack = createAudioTrack(AudioAttributes.USAGE_VOICE_COMMUNICATION, AudioAttributes.CONTENT_TYPE_SPEECH);
                    audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                    targetStreamForVolumeChange = AudioManager.STREAM_VOICE_CALL;
                }
            }
        });


        buttonPlayAudioTrack = findViewById(R.id.button_play);
        buttonPlayAudioTrack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(audioTrack == null) {
                    return;
                }
                if(isPlaying) {
                    audioTrack.stop();
                    isPlaying = false;
                    playThread.interrupt();
                    buttonPlayAudioTrack.setText("PLAY\nAUDIOTRACK");
                    return;
                }
                playThread = new Thread(new Runnable() {
                    public void run() {
                        AssetManager am = getApplicationContext().getAssets();
                        try {
                            InputStream in1 = am.open("piano.wav");
                            byte[] music1 = null;
                            try {
                                music1= new byte[in1.available()];
                                music1=convertStreamToByteArray(in1);
                                in1.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            isPlaying = true;
                            audioTrack.play();
                            audioTrack.write(music1, 0, music1.length);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                playThread.start();
                buttonPlayAudioTrack.setText("STOP\nAUDIOTRACK");
            }
        });

        final Button buttonPlus = findViewById(R.id.button_plus);
        buttonPlus.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //   audioManager.adjustVolume(AudioManager.ADJUST_RAISE, 0);
                //   audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 5, 0);
                int currentVolume = audioManager.getStreamVolume(targetStreamForVolumeChange);
                int finalVolume;
                if (currentVolume < audioManager.getStreamMaxVolume(targetStreamForVolumeChange)) {
                    finalVolume = currentVolume + 1;
                } else {
                    finalVolume = audioManager.getStreamVolume(targetStreamForVolumeChange);
                }
                audioManager.setStreamVolume(targetStreamForVolumeChange, finalVolume, 0);

            }
        });

        final Button buttonMinus = findViewById(R.id.button_minus);
        buttonMinus.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //    audioManager.adjustVolume(AudioManager.ADJUST_LOWER, 0);
                //    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 1, 0);
                int currentVolume = audioManager.getStreamVolume(targetStreamForVolumeChange);
                int finalVolume;
                if(currentVolume > audioManager.getStreamMinVolume(targetStreamForVolumeChange) ) {
                    finalVolume = currentVolume - 1;
                } else {
                    finalVolume = audioManager.getStreamMinVolume(targetStreamForVolumeChange);
                }
                audioManager.setStreamVolume(targetStreamForVolumeChange, finalVolume, 0);
            }
        });

    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    public AudioTrack createAudioTrack(int usage, int contentType){
//        int minBuffSize = AudioTrack.getMinBufferSize(44100, 4, 2);

        return new AudioTrack.Builder()
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(usage)
                        .setContentType(contentType)
                        .build())
                .setAudioFormat(new AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(16000)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                        .build())
                .setTransferMode(AudioTrack.MODE_STREAM)
                //   .setBufferSizeInBytes(minBuffSize)
                .build();
    }

    public static byte[] convertStreamToByteArray(InputStream is) throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buff = new byte[10240];
        int i = Integer.MAX_VALUE;
        while ((i = is.read(buff, 0, buff.length)) > 0) {
            baos.write(buff, 0, i);
        }

        return baos.toByteArray(); // be sure to close InputStream in calling function
    }

}

