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
import android.media.MediaPlayer;
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
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity{

    public int createdAudioSessionId ;

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
                }
            }
        });
        callModeToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    musicModeToggle.setChecked(false);
                }
            }
        });


        final Button buttonPlay = findViewById(R.id.button_play);
        buttonPlay.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                new Thread(new Runnable() {
                    public void run() {
                        int generatedAudioSessionId = audioManager.generateAudioSessionId();
                        Log.d("1234", "generated audio session id "+ generatedAudioSessionId);
                        int minBuffSize = AudioTrack.getMinBufferSize(44100, 4, 2);
                        //  Log.d("1234", "minnBuffsize is "+minBuffSize);
                        AudioTrack audioTrack = new AudioTrack.Builder()
                                .setAudioAttributes(new AudioAttributes.Builder()
                                     //   .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                                        .setUsage(AudioAttributes.USAGE_MEDIA)
                                    //    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                        .build())
                                .setAudioFormat(new AudioFormat.Builder()
                                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                        .setSampleRate(16000)
                                        .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                                        .build())
                                .setSessionId(generatedAudioSessionId)
                                .setTransferMode(AudioTrack.MODE_STREAM)
                                //   .setBufferSizeInBytes(minBuffSize)
                                .build();
                        createdAudioSessionId = audioTrack.getAudioSessionId();
                        // InputStream in1=getResources().openRawResource(R.raw.greenday);
                        AssetManager am = getApplicationContext().getAssets();
                        try {
                            InputStream in1 = am.open("piano.wav");
                            //InputStream in1=getResources().openRawResource(R.raw.piano);
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
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });

        final Button buttonPlay2 = findViewById(R.id.button_play2);
        buttonPlay2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.greenday);
//                mediaPlayer.setAudioAttributes(
//                        new AudioAttributes.Builder()
//                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
//                                .setUsage(AudioAttributes.USAGE_MEDIA)
//                                .build()
//                );

                mediaPlayer.start();
                createdAudioSessionId = mediaPlayer.getAudioSessionId();
                Log.d("1234", "id of session created is " + createdAudioSessionId);
            }
        });
        final Button buttonPlus = findViewById(R.id.button_plus);
        buttonPlus.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
           //     audioManager.adjustVolume(AudioManager.ADJUST_RAISE, 0);
                audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, 5, 0);
             //   audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 5, 0);

            }
        });

        final Button buttonMinus = findViewById(R.id.button_minus);
        buttonMinus.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
            //    audioManager.adjustVolume(AudioManager.ADJUST_LOWER, 0);
                audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, 1, 0);
            //    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 1, 0);

            }
        });

        final Button buttonPermissions = findViewById(R.id.button_permissions);
        buttonPermissions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ContextCompat.checkSelfPermission(
                        getApplicationContext(), Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE) ==
                        PackageManager.PERMISSION_GRANTED) {
                    // You can use the API that requires the permission.
                    Log.d("1234", "PERMISSIONS OK");
                } else {
                    Log.d("1234", "PERMISSIONS missing");
                    Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                    startActivity(intent);
                }
                // ComponentName cn = new ComponentName(getApplicationContext(), NotificationListener.class);
               // String flat = Settings.Secure.getString(getApplicationContext().getContentResolver(), "enabled_notification_listeners");
               // final boolean enabled = flat != null && flat.contains(cn.flattenToString());

            }
        });

        final Button buttonSessions = findViewById(R.id.button_sessions);
        buttonSessions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("1234", "sessions button ");

                ComponentName myNotificationListenerComponent = new ComponentName(getApplicationContext(), NotificationListener.class);
                MediaSessionManager mediaSessionManager = ((MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE));
                List<MediaController> activeSessions = mediaSessionManager.getActiveSessions(myNotificationListenerComponent);
                Log.d("1234", "sessions " +activeSessions);

                for(int i = 0;  i<activeSessions.size(); i++){
                    MediaController currentController = activeSessions.get(i);
                    Log.d("1234", "found session id " + i + " is " + currentController.getPackageName());
                //    currentController.setVolumeTo(0, 0);
                }
                Equalizer eq = new Equalizer(1, createdAudioSessionId);
                eq.setEnabled(true);
                short numBands = eq.getNumberOfBands();
                short minLevel = eq.getBandLevelRange()[0];
                short maxLevel = eq.getBandLevelRange()[1];
                Log.d("1234", "audio session id" + createdAudioSessionId + " number of bands is " + numBands + " min level is " + minLevel + " max level is " + maxLevel);
                for (short i = 0; i<numBands; i++){
                    Log.d("1234", "setting  " + maxLevel + " to " + i);
                    eq.setBandLevel(i, maxLevel);
                }



            }
        });
    }
/*

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 100:
                if(grantResults.length>0  &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Log.d("1234", "media control permission granted");
                    MediaSessionManager mediaSessionManager = ((MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE));
                    ComponentName myNotificationListenerComponent = new ComponentName(this, NotificationListener.class);
                    List<MediaController> activeSessions = mediaSessionManager.getActiveSessions(myNotificationListenerComponent);
                    for(int i = 0;  i<activeSessions.size()-1; i++){
                        MediaController currentController = activeSessions.get(i);
                        Log.d("1234", "found session id " + i + " is " + currentController.getPackageName());
                    }
                } else {
                    Log.d("1234", "media control permission NOT GRANTED");
                }
                return;
            default:
                Log.d("1234", "what are u even doing here");
        }
    }
*/

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
/*
    @Override
    public void onAudioFocusChange(int focusChange){
        Log.d("1234", "audiofocus change!");
    }*/

}

