package com.example.audiomedia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {


    public static final int PERMISSION_READ = 0;


    private ListView mListView;
    private SongsAdapter mAdapter;
    private PlayerFragment fragment;
    private AlertDialog  mDialog;

    private ArrayList<AudioMusic> mMusicList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final String TAG = "WACHUSAY";

        Log.d(TAG, "Environment.getStorageDirectory() = " + Environment.getStorageDirectory().getAbsolutePath());
        Log.d(TAG, "Environment.getRootDirectory() = " + Environment.getRootDirectory().getAbsolutePath());
        Log.d(TAG, "Environment.getDataDirectory() = " + Environment.getDataDirectory().getAbsolutePath());
        Log.d(TAG, "Environment.getDownloadCacheDirectory() = " + Environment.getDownloadCacheDirectory().getAbsolutePath());
        Log.d(TAG, "Environment.getExternalStorageDirectory() = " + Environment.getExternalStorageDirectory().getAbsolutePath());

        fragment = (PlayerFragment) getSupportFragmentManager().findFragmentById(R.id.player_fragment);

        mListView = findViewById(R.id.list_view);
        mAdapter = new SongsAdapter(this);
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position < mMusicList.size()) {
                    //playMusic(position);
                    fragment.setSelectedItem(position);
                    fragment.openPlayer();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mDialog != null && mDialog.isShowing()) {
            return;
        }
        if (checkPermission()) {
            getAudioFiles();
        }
    }

    private void getAudioFiles() {
        ContentResolver contentResolver = getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = contentResolver.query(uri, null, null, null, null);
        //looping through all rows and adding to list
        if (cursor != null && cursor.moveToFirst()) {
            do {

                AudioMusic audioMusic = new AudioMusic();

                audioMusic.setTitle(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
                audioMusic.setArtist(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
                audioMusic.setDuration(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)));
                audioMusic.setUrl(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));

                mMusicList.add(audioMusic);
            } while (cursor.moveToNext());
        }

        if (mMusicList != null && !mMusicList.isEmpty() && mAdapter != null) {
            mAdapter.setSongList(mMusicList);

            fragment.setListData(mMusicList);
        }
        assert cursor != null;
        cursor.close();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int result : grantResults) {
            if (result == PackageManager.PERMISSION_DENIED) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setTitle("Access Denied")
                        .setMessage("Go to settings and allow permission.")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getPackageName(), null);
                                intent.setData(uri);
                                startActivity(intent);
                            }
                        })
                        .setOnCancelListener(DialogInterface::dismiss);
                mDialog = dialog.show();


                return;
            } else if (grantResults[grantResults.length-1] == result && result == PackageManager.PERMISSION_GRANTED) {
                getAudioFiles();
            }
        }
    }

    //runtime storage permission
    public boolean checkPermission() {
        int READ_EXTERNAL_PERMISSION = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if((READ_EXTERNAL_PERMISSION != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_READ);
            return false;
        }
        return true;
    }


    @Override
    public void onBackPressed() {
        if (fragment.isOpen()) {
            fragment.closePlayer();
            return;
        }
        super.onBackPressed();
    }
}