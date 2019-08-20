package com.example.myapplication.cyRecyclerView;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

/**
 * create by cy
 * time : 2019/6/4
 * version : 1.0
 * Features :{@link CyRecyclerView}的上拉刷新基类
 */
public abstract class BaseLoadingMoreFooter extends FrameLayout implements ILoadingMoreFooter {

    private int mState = state.STATE_NORMAL;

    public BaseLoadingMoreFooter(@NonNull Context context) {
        super(context);
        initView(this);
    }

    public abstract void initView(FrameLayout container);

    /**
     * @param LastState 上一个状态
     * @param CurState  当前状态
     */
    public abstract void setViewByState(@IRefreshHeader.state int LastState, @IRefreshHeader.state int CurState);

    @Override
    public void startLoadingMore() {
        setState(state.STATE_LOADING);
    }

    @Override
    public void loadingMoreCompete() {
        setState(state.STATE_COMPLETE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setState(state.STATE_NORMAL);
            }
        }, 200);
    }

    @Override
    public boolean inLoading() {
        return mState == state.STATE_LOADING;
    }

    @Override
    public View getView() {
        return this;
    }

    public void setState(@state int state) {
        switch (state) {
            case ILoadingMoreFooter.state.STATE_NORMAL:
                // 完成 - 》 初始
                if (mState == ILoadingMoreFooter.state.STATE_COMPLETE) {
                    setViewByState(mState, state);
                }
                break;
            case ILoadingMoreFooter.state.STATE_LOADING:
                // 初始 -》 loading
                if (mState == ILoadingMoreFooter.state.STATE_NORMAL) {
                    setViewByState(mState, state);
                }
                break;
            case ILoadingMoreFooter.state.STATE_COMPLETE:
                // loading -》 完整
                if (mState == ILoadingMoreFooter.state.STATE_LOADING) {
                    setViewByState(mState, state);
                }
                break;
        }
        mState = state;
    }
}
