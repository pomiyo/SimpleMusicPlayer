package com.example.audiomedia;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.view.Gravity;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.ArrayList;

public class AudioService extends Service {

    public enum PlayerState {
        ON_PAUSE, ON_PLAY
    }

    interface MediaCallback {
        void startTime();
        void resetProgress();
        void onStateChanged(PlayerState state);
        void onComplete(int position);
    }

    private final IBinder mBinder = new MyBinder();
    private MediaCallback mediaCallback;


    private ArrayList<AudioMusic> mMusicList;
    private int mSelectedPosition;

    private MediaPlayer mMediaPlayer;
    private boolean mIsPlaying = false;
    private boolean mIsPaused;
    private int mIndex = 0;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void setList(ArrayList<AudioMusic> list) {
        mMusicList = list;
    }

    public void setSelected(int index) {
        mIndex = index;
    }

    public int getPosition() {
        return mSelectedPosition;
    }

    public void playMusic(final int position) {
        if (mMusicList != null) {

            mSelectedPosition = position;
            if (mMediaPlayer != null) {
                if (position != mIndex && (mMediaPlayer.isPlaying() || mIsPaused)) {
                    //other song
                    mMediaPlayer.reset();
                    mMediaPlayer.release();
                    mMediaPlayer = null;
                    mIsPlaying = false;
                    mediaCallback.onStateChanged(PlayerState.ON_PLAY);
                } else if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                    mIsPlaying = false;
                    mIsPaused = true;
                    mediaCallback.onStateChanged(PlayerState.ON_PAUSE);
                    return;
                } else if (!mMediaPlayer.isPlaying() && mIsPaused) {
                    //resume music
                    mMediaPlayer.start();
                    mIsPlaying = true;
                    mIsPaused = false;
                    mediaCallback.onStateChanged(PlayerState.ON_PLAY);
                    return;
                }
            }

            try {
                String path = mMusicList.get(mSelectedPosition).getUrl();
                Uri myUri = Uri.parse(path); // initialize Uri here
                mMediaPlayer = new MediaPlayer();
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mMediaPlayer.setDataSource(this, myUri);
                mMediaPlayer.prepare(); // must call prepare first


                mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mMediaPlayer.start(); // then start
                        mIsPlaying = true;
                        mIndex = mSelectedPosition;
                        Toast toast = Toast.makeText(AudioService.this, ""+mMusicList.get(mSelectedPosition).getUrl(), Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.TOP,0,0);
                        toast.show();

                        mediaCallback.startTime();
                    }
                });
                mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mIsPlaying = false;
                        mediaCallback.resetProgress();

                        ++mSelectedPosition;
                        mSelectedPosition = mSelectedPosition < mMusicList.size() ? mSelectedPosition : 0;
                        playMusic(mSelectedPosition);
                        mediaCallback.onComplete(mSelectedPosition);
                    }
                });


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public MediaPlayer getPlayer() {
        return mMediaPlayer;
    }

    public class MyBinder extends Binder {

        AudioService getService(){
            return AudioService.this;
        }

    }

    public void setMediaCallback(MediaCallback callback) {
        mediaCallback = callback;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
