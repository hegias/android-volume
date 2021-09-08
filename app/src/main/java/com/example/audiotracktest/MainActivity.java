package com.example.audiotracktest;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.database.ContentObserver;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity implements AudioManager.OnAudioFocusChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();

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
         audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);

//        AudioFocusRequest response = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
//                .setAudioAttributes(new AudioAttributes.Builder()
//                        .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
//                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
//                        .build())
//                .setAcceptsDelayedFocusGain(true)
//                .setWillPauseWhenDucked(true)
//                .setOnAudioFocusChangeListener(this)
//                .build();
//        audioManager.requestAudioFocus(response);

        Log.d("1234", "stream control BEFORE "+ getVolumeControlStream());

        Log.d("1234", "stream control AFTER "+ getVolumeControlStream());

        Log.d("1234", "volume fixed "+ audioManager.isVolumeFixed());
        Log.d("1234", "mode "+ audioManager.getMode());




        final Button buttonPlay = findViewById(R.id.button_play);
        buttonPlay.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                new Thread(new Runnable() {
                    public void run() {
                        int minBuffSize = AudioTrack.getMinBufferSize(44100, 4, 2);
                        //  Log.d("1234", "minnBuffsize is "+minBuffSize);
                        AudioTrack audioTrack = new AudioTrack.Builder()
                                .setAudioAttributes(new AudioAttributes.Builder()
                                        .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                                        .build())
                                .setAudioFormat(new AudioFormat.Builder()
                                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                        .setSampleRate(16000)
                                        .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                                        .build())
                                .setTransferMode(AudioTrack.MODE_STREAM)
                                //   .setBufferSizeInBytes(minBuffSize)
                                .build();
                        // InputStream in1=getResources().openRawResource(R.raw.greenday);
                        InputStream in1=getResources().openRawResource(R.raw.piano);



                        byte[] music1 = null;
                        try {
                            music1= new byte[in1.available()];
                            music1=convertStreamToByteArray(in1);
                            in1.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        audioTrack.play();
                        Log.d("1234", "mode "+ audioManager.getMode());
                        audioTrack.write(music1, 0, music1.length);


                    }
                }).start();




            }
        });



        final Button buttonPlus = findViewById(R.id.button_plus);
        buttonPlus.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
           //     audioManager.adjustVolume(AudioManager.ADJUST_RAISE, 0);
                audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, 5, 0);
            }
        });

        final Button buttonMinus = findViewById(R.id.button_minus);
        buttonMinus.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
            //    audioManager.adjustVolume(AudioManager.ADJUST_LOWER, 0);
                audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, 1, 0);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
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

    @Override
    public void onAudioFocusChange(int focusChange){
        Log.d("1234", "audiofocus change!");
    }

}

