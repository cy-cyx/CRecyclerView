package com.example.myapplication;

import android.content.Context;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.myapplication.cyRecyclerView.BaseRefreshHeader;

/**
 * create by cy
 * time : 2019/6/4
 * version : 1.0
 * Features :
 */
public class TextHeader extends BaseRefreshHeader {
    private TextView textView;

    public TextHeader(@NonNull Context context) {
        super(context);
    }

    @Override
    public void initView(FrameLayout container) {
        textView = new TextView(getContext());
        textView.setText("头布局");
        LinearLayout.LayoutParams fl = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        container.addView(textView, fl);
    }

    @Override
    public void setViewByState(int LastState, int CurState) {
        if (CurState == state.STATE_RELEASE_TO_REFRESH) {
            textView.setText("准备刷新");
        }
        if (CurState == state.STATE_REFRESHING) {
            textView.setText("正在刷新");
        }
        if (CurState == state.STATE_NORMAL) {
            textView.setText("头布局");
        }
    }

    @Override
    public int getCriticalViewHeight() {
        return 200;
    }
}
