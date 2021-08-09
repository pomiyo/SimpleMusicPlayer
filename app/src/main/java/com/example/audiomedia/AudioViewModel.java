package com.example.audiomedia;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AudioViewModel extends ViewModel {

    private static final String TAG = "MainActivityViewModel";

    private MutableLiveData<Boolean> mIsProgressBarUpdating = new MutableLiveData<>();
    private MutableLiveData<AudioService.MyBinder> mMutableBinder = new MutableLiveData<>();



    // Keeping this in here because it doesn't require a context
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder iBinder) {
            Log.d(TAG, "ServiceConnection: connected to service.");

            AudioService.MyBinder binder = (AudioService.MyBinder) iBinder;
            mMutableBinder.postValue(binder);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.d(TAG, "ServiceConnection: disconnected from service.");
            mMutableBinder.postValue(null);
        }
    };


    public ServiceConnection getServiceConnection(){
        return serviceConnection;
    }

    public LiveData<AudioService.MyBinder> getBinder(){
        return mMutableBinder;
    }
}
