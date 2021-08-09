package com.example.audiomedia;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import java.util.ArrayList;
import java.util.Objects;

public class PlayerFragment extends Fragment implements AudioService.MediaCallback {




    private AudioService mService;
    private AudioViewModel mViewModel;

    private ArrayList<AudioMusic> mMusicList;
    private int mSelectedPosition;

    private MediaPlayer mMediaPlayer;
    private boolean mIsPlaying = false;
    private boolean mIsPaused;
    private int mIndex = 0;


    private CheckableView mPlayPauseBtn;
    private CheckableView mPrevBtn;
    private CheckableView mNextBtn;

    private TextView mCurrentTime;
    private TextView mEndTime;
    private SeekBar mSeekBar;

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable;

    private View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.player_fragment, container, false);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //disallow children touch in main layout
            }
        });

        mViewModel = ViewModelProviders.of(this).get(AudioViewModel.class);

        mPlayPauseBtn = view.findViewById(R.id.play_pause_btn);
        mPrevBtn = view.findViewById(R.id.prev_btn);
        mNextBtn = view.findViewById(R.id.next_btn);
        mCurrentTime = view.findViewById(R.id.current_time);
        mEndTime = view.findViewById(R.id.end_time);
        mSeekBar = view.findViewById(R.id.seekBar);

        setListeners();
        setObservers();
        startService();

        return view;
    }


    public void setSelectedItem(int position) {
        mSelectedPosition = position;
    }

    private void setListeners() {
        mPlayPauseBtn.setChecked(false);
        mPlayPauseBtn.setOnCheckedChangeListener(new CheckableView.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(View buttonView, boolean isChecked) {
                mService.playMusic(mSelectedPosition);
            }
        });
        mPrevBtn.setOnCheckedChangeListener(new CheckableView.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(View buttonView, boolean isChecked) {
                mSelectedPosition--;
                mSelectedPosition = mSelectedPosition < 0 ? mMusicList.size()-1 : mSelectedPosition;
                resetProgress();
                mService.playMusic(mSelectedPosition);

            }
        });
        mNextBtn.setOnCheckedChangeListener(new CheckableView.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(View buttonView, boolean isChecked) {
                mSelectedPosition++;
                mSelectedPosition = mSelectedPosition > mMusicList.size()-1 ? 0 : mSelectedPosition;
                resetProgress();
                mService.playMusic(mSelectedPosition);
            }
        });
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mService.getPlayer().seekTo(seekBar.getProgress());
            }
        });
    }
    private void setObservers() {
        mViewModel.getBinder().observe(this, new Observer<AudioService.MyBinder>() {
            @Override
            public void onChanged(@Nullable AudioService.MyBinder myBinder) {
                if (myBinder == null) {
                    Log.d("TAG", "onChanged: unbound from service");
                } else {
                    Log.d("TAG", "onChanged: bound to service.");
                    mService = myBinder.getService();
                    mService.setMediaCallback(PlayerFragment.this);
                    mService.setList(mMusicList);
                }
            }
        });
    }


    @Override
    public void onStop() {
        super.onStop();
//        if(mViewModel.getBinder() != null){
//            Objects.requireNonNull(getActivity()).unbindService(mViewModel.getServiceConnection());
//        }
    }

    private void startService(){
        Intent serviceIntent = new Intent(getActivity(), AudioService.class);
        Objects.requireNonNull(getActivity()).startService(serviceIntent);

        bindService();
    }

    private void bindService(){
        Intent serviceBindIntent = new Intent(getActivity(), AudioService.class);
        Objects.requireNonNull(getActivity()).bindService(serviceBindIntent, mViewModel.getServiceConnection(), Context.BIND_AUTO_CREATE);
    }

    public void openPlayer() {
        view.setVisibility(View.VISIBLE);
        mService.playMusic(mSelectedPosition);
        mPlayPauseBtn.setChecked(true);
    }

    public void closePlayer() {
        view.setVisibility(View.GONE);
    }

    public boolean isOpen() {
        return view.getVisibility() == View.VISIBLE;
    }

    public void setListData(ArrayList<AudioMusic> list) {
        mMusicList = list;
    }

    @Override
    public void startTime() {
        MediaPlayer mediaPlayer = mService.getPlayer();
        mSeekBar.setMax((int) mediaPlayer.getDuration());
        mPlayPauseBtn.setChecked(true);
        mEndTime.setText(timerConversion((long)mediaPlayer.getDuration() - 1000));
        runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    mCurrentTime.setText(timerConversion((long)mediaPlayer.getCurrentPosition()));
                    mSeekBar.setProgress((int) mediaPlayer.getCurrentPosition());
                    handler.postDelayed(this, 1000);
                }catch (IllegalStateException e) {
                    e.printStackTrace();
                }
            }
        };
        handler.postDelayed(runnable, 1000);
        ImageView musicIcon = view.findViewById(R.id.music_icon);

        RotateAnimation rotate = new RotateAnimation(
                0, 360,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        long duration = (long)mediaPlayer.getDuration();
        rotate.setDuration(duration);
        rotate.setRepeatCount(Animation.INFINITE);
        musicIcon.startAnimation(rotate);
    }

    @Override
    public void resetProgress() {
        handler.removeCallbacks(runnable);
        mSeekBar.setProgress(0);
        mCurrentTime.setText("00:00");
        mPlayPauseBtn.setChecked(false);

        ImageView musicIcon = view.findViewById(R.id.music_icon);
        musicIcon.clearAnimation();
    }

    @Override
    public void onStateChanged(AudioService.PlayerState state) {
        mPlayPauseBtn.setChecked(state == AudioService.PlayerState.ON_PLAY);
        ImageView musicIcon = view.findViewById(R.id.music_icon);
       if (state == AudioService.PlayerState.ON_PAUSE) {
           musicIcon.clearAnimation();
       }
    }

    @Override
    public void onComplete(int position) {
        mSelectedPosition = position;
    }

    //time conversion
    public String timerConversion(long value) {
        String audioTime;
        int dur = (int) value;
        int hrs = (dur / 3600000);
        int mns = (dur / 60000) % 60000;
        int scs = dur % 60000 / 1000;

        if (hrs > 0) {
            audioTime = String.format("%02d:%02d:%02d", hrs, mns, scs);
        } else {
            audioTime = String.format("%02d:%02d", mns, scs);
        }
        return audioTime;
    }
}
