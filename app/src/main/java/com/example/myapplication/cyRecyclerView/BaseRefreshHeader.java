package com.example.myapplication.cyRecyclerView;

import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

/**
 * create by cy
 * time : 2019/6/3
 * version : 1.0
 * Features :{@link CyRecyclerView}的下拉刷新基类
 */
public abstract class BaseRefreshHeader extends FrameLayout implements IRefreshHeader {

    private int mState = state.STATE_NORMAL;

    private FrameLayout mContainer;

    public BaseRefreshHeader(@NonNull Context context) {
        super(context);
        // 根布局设置高度为0
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
        // 留给外部的接口
        mContainer = new FrameLayout(context);
        FrameLayout.LayoutParams fl = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 100);
        fl.gravity = Gravity.BOTTOM;
        addView(mContainer, fl);
        initView(mContainer);
    }

    public abstract void initView(FrameLayout container);

    /**
     * @param LastState 上一个状态
     * @param CurState  当前状态
     */
    public abstract void setViewByState(@state int LastState, @state int CurState);

    /**
     * 是否到达下拉刷新的高度
     *
     * @return
     */
    public abstract int getCriticalViewHeight();

    @Override
    public void onMove(float delta) {
        if (getVisibleHeight() > 0 || delta > 0) {
            setVisibleHeight((int) (getVisibleHeight() + delta));
            // 如果在非更新状态
            if (mState < state.STATE_REFRESHING) {
                if (getVisibleHeight() < getCriticalViewHeight()) {
                    setState(state.STATE_NORMAL);
                } else {
                    setState(state.STATE_RELEASE_TO_REFRESH);
                }
            }
        }
    }

    @Override
    public boolean releaseAction() {
        if (mState == state.STATE_NORMAL) {
            smoothScrollTo(0);
            return false;
        }
        if (mState == state.STATE_RELEASE_TO_REFRESH) {
            smoothScrollTo(getCriticalViewHeight());
            setState(state.STATE_REFRESHING);
            return true;
        }
        if (mState == state.STATE_REFRESHING) {
            smoothScrollTo(getCriticalViewHeight());
            return false;
        }
        if (mState == state.STATE_DONE) {
            return false;
        }

        return false;
    }

    @Override
    public void refreshComplete() {
        setState(state.STATE_DONE);
        smoothScrollTo(0);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setState(state.STATE_NORMAL);
            }
        }, 400);
    }

    @Override
    public View getView() {
        return this;
    }

    /**
     * 分配状态
     *
     * @param state
     */
    public void setState(@state int state) {
        if (state == mState) return;

        switch (state) {
            case IRefreshHeader.state.STATE_NORMAL:
                // 刷新完成 - 》初始
                if (mState == IRefreshHeader.state.STATE_DONE) {
                    setViewByState(mState, state);
                }
                // 准备刷新 -》初始
                if (mState == IRefreshHeader.state.STATE_RELEASE_TO_REFRESH) {
                    setViewByState(mState, state);
                }
                break;
            case IRefreshHeader.state.STATE_RELEASE_TO_REFRESH:
                // 初始 -》 准备刷新
                if (mState == IRefreshHeader.state.STATE_NORMAL) {
                    setViewByState(mState, state);
                }
                break;
            case IRefreshHeader.state.STATE_REFRESHING:
                // 准备刷新 -》 刷新
                if (mState == IRefreshHeader.state.STATE_RELEASE_TO_REFRESH) {
                    setViewByState(mState, state);
                }
                break;
            case IRefreshHeader.state.STATE_DONE:
                // 刷新 - 》 刷新结束
                if (mState == IRefreshHeader.state.STATE_REFRESHING) {
                    setViewByState(mState, state);
                }
                break;
        }
        mState = state;
    }


    /**
     * 用于从当前状态滑动都destHeight
     *
     * @param destHeight
     */
    private void smoothScrollTo(int destHeight) {
        ValueAnimator animator = ValueAnimator.ofInt(getVisibleHeight(), destHeight);
        animator.setDuration(300).start();
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                //最后地往回收view
                setVisibleHeight((int) animation.getAnimatedValue());
            }
        });
        animator.start();
    }

    public void setVisibleHeight(int height) {
        if (height < 0) height = 0;
        ViewGroup.LayoutParams lp = getLayoutParams();
        lp.height = height;
        setLayoutParams(lp);
    }

    @Override
    public int getVisibleHeight() {
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        return layoutParams.height;
    }
}
