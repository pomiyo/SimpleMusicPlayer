package com.example.audiomedia;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class CheckableView extends androidx.appcompat.widget.AppCompatImageView implements Checkable {

    public interface OnCheckedChangeListener{
        void onCheckedChanged(View buttonView, boolean isChecked);
    }
    private static final int[] CHECKED_STATE_SET = {android.R.attr.state_checked};
    private boolean mChecked = false;
    private boolean mCanToggle = false;

    private OnCheckedChangeListener mListener;

    public CheckableView(@NonNull  Context context) {
        super(context);
        updateToggle();
    }

    public CheckableView(@NonNull  Context context, @Nullable  AttributeSet attrs) {
        super(context, attrs);
        updateToggle();
    }

    public CheckableView(@NonNull Context context, @Nullable  AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        updateToggle();
    }


    @Override
    public int[] onCreateDrawableState(final int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked())
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        return drawableState;
    }

    @Override
    public void setChecked(boolean checked) {
        mChecked = checked;
        refreshDrawableState();
    }

    @Override
    public boolean isChecked() {
        return mChecked;
    }

    @Override
    public void toggle() {
        mChecked = !mChecked;
        refreshDrawableState();
        if(mListener != null){
            mListener.onCheckedChanged(this, mChecked);
        }
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener listener){
        mListener = listener;
    }

    public void setCanToggle(boolean toggle){
        mCanToggle = toggle;
        updateToggle();
    }

    private void updateToggle(){
        mCanToggle = true;
        if(mCanToggle){
            this.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggle();
                }
            });
        } else {
            this.setOnClickListener(null);
        }
    }
}
