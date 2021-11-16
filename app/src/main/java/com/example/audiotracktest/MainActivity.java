package com.example.audiotracktest;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.ContentObserver;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity{

    public AudioManager audioManager ;
    public AudioTrack audioTrack = null;
    public int targetStreamForVolumeChange = 3;
    public String curUsage, curContent, curURI;
    public Boolean isPlaying = false;
    public Thread playThread;
    public Button buttonPlayAudioTrack;
    public TextView modeText, usageText, contentText, targetStreamText, streamMusicText, streamVoiceText, uriText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // INIT TEXT fields
        modeText = findViewById(R.id.modeText);
        usageText = findViewById(R.id.usageText);
        contentText = findViewById(R.id.contentText);
        targetStreamText = findViewById(R.id.targetStreamText);
        streamMusicText = findViewById(R.id.streamMusicText);
        streamVoiceText = findViewById(R.id.streamVoiceText);
        uriText = findViewById(R.id.uriText);

        // SETUP OBSERVER FOR VOLUME CHANGES
        ContentObserver observer = new ContentObserver(new Handler(Looper.myLooper())) {
            @Override
            public void onChange(boolean selfChange, @Nullable Uri uri) {
                super.onChange(selfChange, uri);

                AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
                curURI = uri.toString();
                Log.d("1234", "new ONCHANGE system settings " + uri.toString() + " current mode " + audioManager.getMode());
                Log.d("1234", "ONCHANGE stream MUSIC current " + audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) );
                Log.d("1234", "ONCHANGE stream VOICe CALL current " + audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL) );
                Log.d("1234", "ONCHANGE stream STREAM_ACCESSIBILITY current " + audioManager.getStreamVolume(AudioManager.STREAM_ACCESSIBILITY) );
                Log.d("1234", "ONCHANGE stream STREAM_ALARM current " + audioManager.getStreamVolume(AudioManager.STREAM_ALARM) );
                Log.d("1234", "ONCHANGE stream STREAM_DTMF current " + audioManager.getStreamVolume(AudioManager.STREAM_DTMF) );
                Log.d("1234", "ONCHANGE stream STREAM_NOTIFICATION current " + audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION) );
                Log.d("1234", "ONCHANGE stream STREAM_RING current " + audioManager.getStreamVolume(AudioManager.STREAM_RING) );
                Log.d("1234", "ONCHANGE stream STREAM_SYSTEM current " + audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM) );
                updateReport();
            }
        };
        getApplicationContext().getContentResolver().registerContentObserver(Settings.System.CONTENT_URI, true, observer);

        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        // setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
        // audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        // audioManager.setMode(AudioManager.MODE_NORMAL);

        Log.d("1234", "stream control "+ getVolumeControlStream());
        Log.d("1234", "volume fixed "+ audioManager.isVolumeFixed());
        Log.d("1234", "starting mode "+ audioManager.getMode());
        Log.d("1234", "please select MUSIC/CALL to setup audioManager and audioTrack. Then press play");

        final ToggleButton musicModeToggle = findViewById(R.id.musicMode_toggle);
        final ToggleButton callModeToggle = findViewById(R.id.callMode_toggle);
        final ToggleButton musicStreamToggle = findViewById(R.id.selectMusicStream_toggle);
        final ToggleButton callStreamToggle = findViewById(R.id.selectCallStream_toggle);

        musicModeToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    callModeToggle.setChecked(false);
                    if(isPlaying){
                        audioTrack.stop();
                        isPlaying = false;
                        playThread.interrupt();
                        buttonPlayAudioTrack.setText("PLAY\nAUDIOTRACK");
                    }
                    audioTrack = createAudioTrack(AudioAttributes.USAGE_MEDIA, AudioAttributes.CONTENT_TYPE_MUSIC);
                    audioManager.setMode(AudioManager.MODE_NORMAL);
                    targetStreamForVolumeChange = AudioManager.STREAM_MUSIC;
                    musicStreamToggle.setChecked(true);
                    updateReport();
                }
            }
        });
        callModeToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    musicModeToggle.setChecked(false);
                    if(isPlaying){
                        audioTrack.stop();
                        isPlaying = false;
                        playThread.interrupt();
                        buttonPlayAudioTrack.setText("PLAY\nAUDIOTRACK");
                    }
                    audioTrack = createAudioTrack(AudioAttributes.USAGE_VOICE_COMMUNICATION, AudioAttributes.CONTENT_TYPE_SPEECH);
                    audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                    targetStreamForVolumeChange = AudioManager.STREAM_VOICE_CALL;
                    callStreamToggle.setChecked(true);
                    updateReport();
                }
            }
        });

        musicStreamToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    callStreamToggle.setChecked(false);
                    targetStreamForVolumeChange = AudioManager.STREAM_MUSIC;
                    updateReport();
                }
            }
        });
        callStreamToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    musicStreamToggle.setChecked(false);
                    targetStreamForVolumeChange = AudioManager.STREAM_VOICE_CALL;
                    updateReport();
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
                Log.d("1234", "current stream for volume change is " + targetStreamForVolumeChange +  " max is " +  audioManager.getStreamMaxVolume(targetStreamForVolumeChange));
                int currentVolume = audioManager.getStreamVolume(targetStreamForVolumeChange);
                int finalVolume;
                if (currentVolume < audioManager.getStreamMaxVolume(targetStreamForVolumeChange)) {
                    finalVolume = currentVolume + 1;
                } else {
                    finalVolume = audioManager.getStreamVolume(targetStreamForVolumeChange);
                }
                Log.d("1234", "setting volume for stream " + targetStreamForVolumeChange + " to " + finalVolume);
                audioManager.setStreamVolume(targetStreamForVolumeChange, finalVolume, 0);
                updateReport();
            }
        });

        final Button buttonMinus = findViewById(R.id.button_minus);
        buttonMinus.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //    audioManager.adjustVolume(AudioManager.ADJUST_LOWER, 0);
                //    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 1, 0);
                Log.d("1234", "current stream for volume change is " + targetStreamForVolumeChange +  " min is " +  audioManager.getStreamMinVolume(targetStreamForVolumeChange));
                int currentVolume = audioManager.getStreamVolume(targetStreamForVolumeChange);
                int finalVolume;
                if(currentVolume > audioManager.getStreamMinVolume(targetStreamForVolumeChange) ) {
                    finalVolume = currentVolume - 1;
                } else {
                    finalVolume = audioManager.getStreamMinVolume(targetStreamForVolumeChange);
                }
                Log.d("1234", "setting volume for stream " + targetStreamForVolumeChange + " to " + finalVolume);
                audioManager.setStreamVolume(targetStreamForVolumeChange, finalVolume, 0);
                updateReport();
            }
        });

    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    public AudioTrack createAudioTrack(int usage, int contentType){
//        int minBuffSize = AudioTrack.getMinBufferSize(44100, 4, 2);
        if(usage == AudioAttributes.USAGE_MEDIA){
            curUsage = "media";
        } else if (usage == AudioAttributes.USAGE_VOICE_COMMUNICATION){
            curUsage = "communication";
        }
        if(contentType == AudioAttributes.CONTENT_TYPE_MUSIC){
            curContent = "music";
        } else if (contentType == AudioAttributes.CONTENT_TYPE_SPEECH){
            curContent = "speech";
        }
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

    public void updateReport(){
        modeText.setText(((Integer) audioManager.getMode()).toString());
        usageText.setText(curUsage);
        contentText.setText(curContent);
        targetStreamText.setText(((Integer) targetStreamForVolumeChange).toString());
        streamMusicText.setText(((Integer) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)).toString());
        streamVoiceText.setText(((Integer) audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL)).toString());
        uriText.setText(curURI);

        return;
    }

}

