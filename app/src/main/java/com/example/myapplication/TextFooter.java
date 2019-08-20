package com.example.myapplication;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.myapplication.cyRecyclerView.BaseLoadingMoreFooter;

/**
 * create by cy
 * time : 2019/6/4
 * version : 1.0
 * Features :
 */
public class TextFooter extends BaseLoadingMoreFooter {

    TextView textView;

    public TextFooter(@NonNull Context context) {
        super(context);
    }

    @Override
    public void initView(FrameLayout container) {
        textView = new TextView(getContext());
        textView.setText("尾布局");
        ViewGroup.LayoutParams vl = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 200);
        container.addView(textView, vl);

    }

    @Override
    public void setViewByState(int LastState, int CurState) {
        if (CurState == state.STATE_LOADING) {
            textView.setText("正在加载");
        }
        if (CurState == state.STATE_NORMAL) {
            textView.setText("尾布局");
        }
    }
}
