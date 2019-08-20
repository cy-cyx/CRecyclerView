package com.example.myapplication.cyRecyclerView;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import java.util.List;

/**
 * create by cy
 * time : 2019/6/3
 * version : 1.0
 * Features :
 * 1、上拉刷新
 * 2、下拉刷新（支持只剩几个item时响应）
 * 3、空页面
 */
public class CyRecyclerView extends RecyclerView {

    private final int TYPE_REFRESH_HEADER = 10000;//下拉刷新的ViewType的码
    private final int TYPE_REFRESH_FOOTER = 10001;//上拉刷新的ViewType的码

    private IRefreshHeader mHeader;
    private ILoadingMoreFooter mFooter;
    private View mEmpty;
    private RecyclerView.AdapterDataObserver mDataObserver = new DataObserver();

    private WrapAdapter mWrapAdapter;
    private LoadingListener mLoadingListener;

    /**
     * 标志位
     */
    private boolean mPullRefreshEnabled = false;
    private boolean mLoadingMoreEnabled = false;

    public CyRecyclerView(@NonNull Context context) {
        super(context, null);
    }

    public CyRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public CyRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    private class WrapAdapter extends RecyclerView.Adapter<ViewHolder> {

        private RecyclerView.Adapter mAdapter;

        public WrapAdapter(Adapter adapter) {
            this.mAdapter = adapter;
        }

        @Override
        public int getItemViewType(int position) {
            if (isHeader(position)) {
                return TYPE_REFRESH_HEADER;
            }
            if (isFooter(position)) {
                return TYPE_REFRESH_FOOTER;
            }

            int mRealPosition;
            if (mPullRefreshEnabled) {
                mRealPosition = position - 1;
            } else {
                mRealPosition = position;
            }
            if (mAdapter != null) {
                if (mRealPosition <= mAdapter.getItemCount()) {
                    int itemViewType = mAdapter.getItemViewType(mRealPosition);
                    // 保护机制，防止和头尾布局的ViewType相同
                    if (isReservedItemViewType(itemViewType)) {
                        throw new RuntimeException("ViewType中10000和10001已被占用");
                    }
                    return itemViewType;
                }
            }
            return 0;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == TYPE_REFRESH_HEADER) {
                return new SimpleViewHolder(mHeader.getView());
            } else if (viewType == TYPE_REFRESH_FOOTER) {
                return new SimpleViewHolder(mFooter.getView());
            }
            return mAdapter.onCreateViewHolder(parent, viewType);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            if (isHeader(position)) {
                return;
            }

            int mRealPosition;
            if (mPullRefreshEnabled) {
                mRealPosition = position - 1;
            } else {
                mRealPosition = position;
            }
            if (mAdapter != null) {
                if (mRealPosition < mAdapter.getItemCount()) {
                    mAdapter.onBindViewHolder(holder, mRealPosition);
                }
            }
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
            if (isHeader(position)) {
                return;
            }

            int mRealPosition;
            if (mPullRefreshEnabled) {
                mRealPosition = position - 1;
            } else {
                mRealPosition = position;
            }
            if (mAdapter != null) {
                if (mRealPosition < mAdapter.getItemCount()) {
                    if (payloads.isEmpty()) {
                        mAdapter.onBindViewHolder(holder, mRealPosition);
                    } else {
                        mAdapter.onBindViewHolder(holder, mRealPosition, payloads);
                    }
                }
            }
        }

        @Override
        public int getItemCount() {
            int count = 0;
            if (mLoadingMoreEnabled) {
                count++;
            }
            if (mPullRefreshEnabled) {
                count++;
            }
            if (mAdapter != null) {
                return mAdapter.getItemCount() + count;
            }
            return count;
        }

        @Override
        public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
            RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
            // 设置头部居和尾布局独立占用一行
            if (manager instanceof GridLayoutManager) {
                final GridLayoutManager gridManager = ((GridLayoutManager) manager);
                gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        return (isHeader(position) || isFooter(position))
                                ? gridManager.getSpanCount() : 1;
                    }
                });
            }
            if (mAdapter != null)
                mAdapter.onAttachedToRecyclerView(recyclerView);
        }

        @Override
        public void onViewAttachedToWindow(@NonNull ViewHolder holder) {
            super.onViewAttachedToWindow(holder);
            ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
            // 设置头布局和尾布局独立占用一行
            if (lp instanceof StaggeredGridLayoutManager.LayoutParams
                    && (isHeader(holder.getLayoutPosition()) || isFooter(holder.getLayoutPosition()))) {
                StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) lp;
                p.setFullSpan(true);
            }
            if (mAdapter != null)
                mAdapter.onViewAttachedToWindow(holder);
        }

        @Override
        public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
            if (mAdapter != null)
                mAdapter.onDetachedFromRecyclerView(recyclerView);
        }

        @Override
        public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder holder) {
            if (mAdapter != null)
                mAdapter.onViewDetachedFromWindow(holder);
        }

        @Override
        public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
            if (mAdapter != null)
                mAdapter.onViewRecycled(holder);
        }

        @Override
        public boolean onFailedToRecycleView(@NonNull RecyclerView.ViewHolder holder) {
            if (mAdapter != null) {
                return mAdapter.onFailedToRecycleView(holder);
            }
            return false;
        }

        @Override
        public void unregisterAdapterDataObserver(AdapterDataObserver observer) {
            if (mAdapter != null)
                mAdapter.unregisterAdapterDataObserver(observer);
        }

        @Override
        public void registerAdapterDataObserver(AdapterDataObserver observer) {
            if (mAdapter != null)
                mAdapter.registerAdapterDataObserver(observer);
        }

        public RecyclerView.Adapter getOriginalAdapter() {
            return this.mAdapter;
        }

        public boolean isFooter(int position) {
            if (mLoadingMoreEnabled) {
                return position == getItemCount() - 1;
            } else {
                return false;
            }
        }

        public boolean isHeader(int position) {
            if (mPullRefreshEnabled) {
                return position == 0;
            } else {
                return false;
            }
        }

        private class SimpleViewHolder extends RecyclerView.ViewHolder {
            SimpleViewHolder(View itemView) {
                super(itemView);
            }
        }
    }

    /**
     * 是否保留的adapter的ViewType
     */
    private boolean isReservedItemViewType(int itemViewType) {
        return itemViewType == TYPE_REFRESH_HEADER || itemViewType == TYPE_REFRESH_FOOTER;
    }

    /* ***核心实现***/

    private float mLastY = -1; // 记录拖动的
    private static final float DRAG_RATE = 3;  // 下拉的程度和手指移动的程度的比例

    /**
     * 在这里响应下拉事件
     *
     * @param e
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // 监听为空
        if (mLoadingListener == null) return super.onTouchEvent(e);

        if (mLastY == -1) {
            mLastY = e.getRawY();
        }
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastY = e.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                final float deltaY = e.getRawY() - mLastY;
                mLastY = e.getRawY();
                if (isOnTop() && mPullRefreshEnabled) {
                    //符合条件就开始滑动
                    mHeader.onMove(deltaY / DRAG_RATE);
                    // 如果头布局出现的情况下就不传递事件给recyclerView
                    if (mHeader.getVisibleHeight() > 0) {
                        return false;
                    }
                }
                break;
            default:
                mLastY = -1; // reset
                if (isOnTop() && mPullRefreshEnabled) {
                    if (mHeader.releaseAction()) {
                        if (mLoadingListener != null) {
                            mLoadingListener.onRefresh();
                        }
                    }
                }
        }
        return super.onTouchEvent(e);
    }

    /**
     * 判断是否处于列表头（如果没有下拉刷新的头当作不处于列表头处理）
     *
     * @return
     */
    private boolean isOnTop() {
        if (mHeader == null)
            return false;
        return mHeader.getView().getParent() != null;
    }

    private boolean isNoMore = false; // 没有更多了

    private int limitNumberToCallLoadMore = 1; // 剩余多少条时拉取更多

    @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);
        // 监听为空
        if (mLoadingListener == null) return;

        if (state == RecyclerView.SCROLL_STATE_IDLE
                && mFooter != null && !mFooter.inLoading() && mLoadingMoreEnabled && !isNoMore) {
            LayoutManager layoutManager = getLayoutManager();//获得相关的布局

            int lastVisibleItemPosition;//最后一个可见
            if (layoutManager instanceof GridLayoutManager) {
                lastVisibleItemPosition = ((GridLayoutManager) layoutManager).findLastVisibleItemPosition();//获得Grid布局最后一个可见的子view的值
            } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                int[] into = new int[((StaggeredGridLayoutManager) layoutManager).getSpanCount()];//获得有多少的跨度就是多少列
                ((StaggeredGridLayoutManager) layoutManager).findLastVisibleItemPositions(into);//获得最后一个可见的字view的值
                lastVisibleItemPosition = findMax(into);//获得瀑布流最后一个可见的子view的值
            } else {
                lastVisibleItemPosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();//获得linear布局的最后一个可见的子view的值
            }

            int itemCount = mWrapAdapter.getItemCount() - 1; // 不算上尾布局

            // 满足刷新条件
            if (itemCount - lastVisibleItemPosition < limitNumberToCallLoadMore) {
                mFooter.startLoadingMore();
                if (mLoadingListener != null)
                    mLoadingListener.onLoadMore();
            }
        }
    }

    /**
     * 用瀑布流里面的获取最大的
     */
    private int findMax(int[] lastPositions) {
        int max = lastPositions[0];
        for (int value : lastPositions) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }

    private class DataObserver extends RecyclerView.AdapterDataObserver {
        @Override
        public void onChanged() {
            if (mWrapAdapter != null) {
                mWrapAdapter.notifyDataSetChanged();
            }
            if (mEmpty != null && mWrapAdapter != null) {
                if (mWrapAdapter.getOriginalAdapter().getItemCount() == 0) {
                    setVisibility(GONE);
                    mEmpty.setVisibility(VISIBLE);
                } else {
                    setVisibility(VISIBLE);
                    mEmpty.setVisibility(GONE);
                }
            }
        }
    }

    /* ****外部get方法****/

    /**
     * 避免用户自己调用getAdapter() 引起的ClassCastException
     */
    @Override
    public Adapter getAdapter() {
        if (mWrapAdapter != null)
            return mWrapAdapter.getOriginalAdapter();
        else
            return null;
    }

    /* ****外部配置方法****/
    @Override
    public void setAdapter(@Nullable Adapter adapter) {
        if (adapter == null) return;
        mWrapAdapter = new WrapAdapter(adapter);
        super.setAdapter(mWrapAdapter);
        adapter.registerAdapterDataObserver(mDataObserver);
        mDataObserver.onChanged();
    }

    @Override
    public void setLayoutManager(LayoutManager layout) {
        super.setLayoutManager(layout);
        if (mWrapAdapter != null) {
            // 设置头部居和尾布局独立占用一行
            if (layout instanceof GridLayoutManager) {
                final GridLayoutManager gridManager = ((GridLayoutManager) layout);
                gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        return (mWrapAdapter.isHeader(position) || mWrapAdapter.isFooter(position))
                                ? gridManager.getSpanCount() : 1;
                    }
                });
            }
        } else {
            throw new RuntimeException("必须在setAdapter()之后调用");
        }
    }

    /**
     * 设置能否上拉刷新
     *
     * @param pullRefreshEnabled
     */
    public void setPullRefreshEnabled(boolean pullRefreshEnabled) {
        if (mHeader == null) return;
        this.mPullRefreshEnabled = pullRefreshEnabled;
    }

    /**
     * 设置头布局
     *
     * @param view
     * @param pullRefreshEnabled
     */
    public void setHeaderView(BaseRefreshHeader view, boolean pullRefreshEnabled) {
        if (view != null) {
            mHeader = view;
        }
        setPullRefreshEnabled(pullRefreshEnabled);
    }

    /**
     * 下拉刷新完成
     */
    public void refreshComplete() {
        if (mHeader != null)
            mHeader.refreshComplete();
        isNoMore = false; // 下拉刷新完成后就可以重置上拉刷新了
    }

    /**
     * 可以上啦刷新
     *
     * @param loadingMoreEnabled
     */
    public void setLoadingMoreEnabled(boolean loadingMoreEnabled) {
        if (mFooter == null) return;
        this.mLoadingMoreEnabled = loadingMoreEnabled;
    }

    /**
     * 设置尾布局
     *
     * @param view
     * @param loadingMoreEnabled
     */
    public void setFooterView(BaseLoadingMoreFooter view, boolean loadingMoreEnabled) {
        if (view != null) {
            mFooter = view;
        }
        setLoadingMoreEnabled(loadingMoreEnabled);
    }

    /**
     * 加载更多完成
     */
    public void loadingMoreComplete() {
        if (mFooter != null)
            mFooter.loadingMoreCompete();
    }

    /**
     * 没有更多了
     */
    public void setNoMore() {
        isNoMore = true;
    }

    /**
     * 设置还剩多少条拉取更多(至少为1)
     *
     * @param limitNumberToCallLoadMore
     */
    public void setLimitNumberToCallLoadMore(int limitNumberToCallLoadMore) {
        if (limitNumberToCallLoadMore < 1) {
            limitNumberToCallLoadMore = 1;
        }
        this.limitNumberToCallLoadMore = limitNumberToCallLoadMore;
    }

    /**
     * 设置空页面(只是拿到外部布局的空页面引用而已，注意！！)
     *
     * @param empty
     */
    public void setEmpty(View empty) {
        this.mEmpty = empty;
        mDataObserver.onChanged();
    }

    /**
     * 引导的回调
     */
    public void setLoadingListener(LoadingListener loadingListener) {
        this.mLoadingListener = loadingListener;
    }

    public interface LoadingListener {

        void onRefresh();

        void onLoadMore();
    }
}
