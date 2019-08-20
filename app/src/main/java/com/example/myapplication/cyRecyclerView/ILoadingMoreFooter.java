package com.example.myapplication.cyRecyclerView;

import android.view.View;

/**
 * create by cy
 * time : 2019/6/4
 * version : 1.0
 * Features : 上拉更多接口
 */
public interface ILoadingMoreFooter {
    @interface state {
        public final static int STATE_NORMAL = 0;
        public final static int STATE_LOADING = 1;
        public final static int STATE_COMPLETE = 2;
    }

    public void startLoadingMore();

    public void loadingMoreCompete();

    public boolean inLoading();

    public View getView();
}
