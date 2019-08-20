package com.example.myapplication.cyRecyclerView;

import android.view.View;

import com.example.myapplication.cyRecyclerView.CyRecyclerView;

/**
 * create by cy
 * time : 2019/6/3
 * version : 1.0
 * Features : {@link CyRecyclerView}的下拉刷新接口
 */
public interface IRefreshHeader {

    public @interface state {
        public final int STATE_NORMAL = 0;                 //
        public final int STATE_RELEASE_TO_REFRESH = 1;     // 释放准备刷新（过了准备刷新的高度）
        public final int STATE_REFRESHING = 2;             // 正在刷新
        public final int STATE_DONE = 3;                   // 刷新结束(有结束动画)
    }

    void onMove(float delta);      // 滑动了多少

    boolean releaseAction();       // 结束触摸动作

    void refreshComplete();        // 刷新完成

    int getVisibleHeight();         // 获得当前View的高度

    View getView();
}
