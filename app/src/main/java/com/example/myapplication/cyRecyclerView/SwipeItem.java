package com.example.myapplication.cyRecyclerView;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.myapplication.R;

public class SwipeItem extends FrameLayout {

    int swipeWidth = 0;   // 该值为正值，实应为负

    public SwipeItem(@NonNull Context context) {
        super(context);
    }

    public SwipeItem(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeItem(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray array = context.getTheme().obtainStyledAttributes(attrs
                , R.styleable.SwipeItem
                , defStyleAttr, 0);

        swipeWidth = array.getDimensionPixelSize(R.styleable.SwipeItem_swipe_size, 0);
        array.recycle();
    }

    float eventX = 0; // 记录手指位置
    static boolean intercept = false; // 拦截（有一个被拦截，所有都被拦截）
    float translationX = 0;  // 当前抽屉的打开程度


    public void setSwipeWidth(int width) {
        this.swipeWidth = width;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // 当有抽屉打开时，不是当前item，拦截事件
        if (CyRecyclerView.curSwipeItem != null && CyRecyclerView.curSwipeItem != this) {
            return true;
        } else {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    eventX = ev.getX();
                case MotionEvent.ACTION_MOVE:
                    float v = eventX - ev.getX();
                    if (Math.abs(v) > 20) {
                        intercept = true;
                    }
                    eventX = ev.getX();
                    break;
            }
            return intercept;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                intercept = true;
                return true;
            case MotionEvent.ACTION_MOVE:
                if (intercept && (CyRecyclerView.curSwipeItem == null || CyRecyclerView.curSwipeItem == this)) {
                    requestDisallowInterceptTouchEvent(true);
                    CyRecyclerView.curSwipeItem = this;
                    float temp = event.getX() - eventX;
                    translationX += temp;
                    if (translationX > 0) {
                        translationX = 0;
                    } else if (translationX < -swipeWidth) {
                        translationX = -swipeWidth;
                    }
                    String s = (String) getChildAt(0).getTag();
                    Log.d("xx", "onTouchEvent: " + s);

                    getChildAt(0).scrollTo((int) -translationX, 0);
                    eventX = event.getX();
                    return true;
                } else {
                    return true;
                }

            case MotionEvent.ACTION_UP:
                if (CyRecyclerView.curSwipeItem == this) {
                    endSwipe();
                }
                break;
        }

        return super.onTouchEvent(event);
    }

    /**
     * 松手时如果超过一半，自动展开，否则关上
     */
    private void endSwipe() {
        if (translationX < -swipeWidth / 2) {
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(translationX, -swipeWidth);
            valueAnimator.setDuration(100);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float animatedValue = (float) animation.getAnimatedValue();
                    getChildAt(0).scrollTo((int) -animatedValue, 0);
                    translationX = animatedValue;
                }
            });
            valueAnimator.start();
            intercept = false;
        } else {
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(translationX, 0);
            valueAnimator.setDuration(100);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float animatedValue = (float) animation.getAnimatedValue();
                    getChildAt(0).scrollTo((int) -animatedValue, 0);
                    translationX = animatedValue;
                }
            });
            valueAnimator.start();
            intercept = false;
            CyRecyclerView.curSwipeItem = null;
        }
    }


    public void closeSwipe() {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(translationX, 0);
        valueAnimator.setDuration(100);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animatedValue = (float) animation.getAnimatedValue();
                getChildAt(0).scrollTo((int) -animatedValue, 0);
            }
        });
        valueAnimator.start();
        intercept = false;
        CyRecyclerView.curSwipeItem = null;
    }

    public int dp2px(float dpValue) {
        float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
