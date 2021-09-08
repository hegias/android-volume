package com.hegias.audiotestmodule;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.ContentObserver;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class AudioTest {

    private Context context;
    private AudioManager audioManager;

    public AudioTest(Context context) {
        this.context = context;
        ContentObserver observer = new ContentObserver(new Handler(Looper.myLooper())) {
            @Override
            public void onChange(boolean selfChange, @Nullable Uri uri) {
                super.onChange(selfChange, uri);

//                AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                if (audioManager == null)
                    return;

                Log.d("1234", "new ONCHANGE system settings " + uri.toString() + " current mode " + audioManager.getMode());
                // Log.d("1234", "ONCHANGE stream MUSIC max " + audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) );
                Log.d("1234", "ONCHANGE stream MUSIC current " + audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
                // Log.d("1234", "ONCHANGE stream VOICe CALL max " + audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL) );
                Log.d("1234", "ONCHANGE stream VOICe CALL current " + audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL));
                Log.d("1234", "ONCHANGE stream STREAM_ACCESSIBILITY current " + audioManager.getStreamVolume(AudioManager.STREAM_ACCESSIBILITY));
                Log.d("1234", "ONCHANGE stream STREAM_ALARM current " + audioManager.getStreamVolume(AudioManager.STREAM_ALARM));
                Log.d("1234", "ONCHANGE stream STREAM_DTMF current " + audioManager.getStreamVolume(AudioManager.STREAM_DTMF));
                Log.d("1234", "ONCHANGE stream STREAM_NOTIFICATION current " + audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION));
                Log.d("1234", "ONCHANGE stream STREAM_RING current " + audioManager.getStreamVolume(AudioManager.STREAM_RING));
                Log.d("1234", "ONCHANGE stream STREAM_SYSTEM current " + audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM));
            }
        };
        context.getContentResolver().registerContentObserver(Settings.System.CONTENT_URI, true, observer);

        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

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

//        Log.d("1234", "stream control BEFORE " + context.getVolumeControlStream());
//
//        Log.d("1234", "stream control AFTER " + context.getVolumeControlStream());

        Log.d("1234", "volume fixed " + audioManager.isVolumeFixed());
        Log.d("1234", "mode " + audioManager.getMode());
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

    public void raiseVolume()
    {
        audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, 5, 0);
    }

    public void lowerVolume()
    {
        audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, 1, 0);
    }

    public void playMusic() {
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
//                InputStream in1=getResources().openRawResource(R.raw.piano);
                AssetManager am = context.getAssets();
                InputStream in1 = null;
                try {
                    in1 = am.open("piano.wav");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                byte[] music1 = null;
                try {
                    music1 = new byte[in1.available()];
                    music1 = convertStreamToByteArray(in1);
                    in1.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                audioTrack.play();
                Log.d("1234", "mode " + audioManager.getMode());
                audioTrack.write(music1, 0, music1.length);
            }
        }).start();
    }
}
