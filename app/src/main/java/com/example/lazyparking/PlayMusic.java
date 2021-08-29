package com.example.lazyparking;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;

public class PlayMusic{

    private Context context;
    private SoundPool soundPool;
    private int sound;

    public PlayMusic(Context context){
        this.context = context;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            this.soundPool = new SoundPool.Builder().setAudioAttributes(audioAttributes).build();

        }
        else
            this.soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);

        this.sound = soundPool.load(this.context, R.raw.sound, 1);

    }

    public void StarSound(){
        this.soundPool.play(this.sound, 1, 1, 1, 0, 1);
    }

    public void release(){
        this.soundPool.release();
    }
}