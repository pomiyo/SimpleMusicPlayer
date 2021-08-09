package com.example.audiomedia;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class SongsAdapter extends BaseAdapter {


    private ArrayList<AudioMusic> mMusicLists;
    private Activity mActivity;
    private LayoutInflater mInflater;

    public SongsAdapter(Activity activity) {
        mActivity = activity;
        mInflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setSongList(ArrayList<AudioMusic> list) {
        mMusicLists = list;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (mMusicLists == null) return 0;
        return mMusicLists.size();
    }

    @Override
    public Object getItem(int position) {
        return mMusicLists.get(position) == null ? null : mMusicLists.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position > mMusicLists.size() ? -1 : position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.song_list_item, parent, false);
            holder.mSongTitle = convertView.findViewById(R.id.tv_song_title);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }


        holder.mSongTitle.setText(mMusicLists.get(position).getTitle().isEmpty() ? "Test Music" : mMusicLists.get(position).getTitle());
        return convertView;
    }


    private class ViewHolder {
        TextView mSongTitle;
    }
}
