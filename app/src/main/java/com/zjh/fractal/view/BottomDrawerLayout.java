package com.zjh.fractal.view;
import android.content.Context;
//import android.support.v4.view.ViewCompat;
//import android.support.v4.widget.ViewDragHelper;
import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.customview.widget.ViewDragHelper;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.zjh.fractal.R;

//轮子：BottomDrawerLayout 来自github
//https://github.com/wangpeiming110/BottomDrawer
//已经被删得不成样子了，将就用吧qwq

public class BottomDrawerLayout extends ViewGroup {
    private View mDrawerView;
    private View mBottomView;
    private View mRotateView;

    private ViewDragHelper mDragHelper;

    private int mCurTop=-1;
    private int mBottomHeight;
    private int mDrawerHeight;
    private int mParentHeight;

    private boolean flag_is_maximize = false;
    public BottomDrawerLayout(Context context) {
        super(context);
        init();
    }

    public BottomDrawerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BottomDrawerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mDragHelper = ViewDragHelper.create(this, 1.0f,new DragCallBack());
    }

    private class DragCallBack extends  ViewDragHelper.Callback{
        //从底部到顶部的顺序遍历子view
        @Override
        public int getOrderedChildIndex(int index) {
            int childCount = BottomDrawerLayout.this.getChildCount();
            return childCount - index -1;
        }

        @Override
        public boolean tryCaptureView(@NonNull View child, int pointerId) {
            return false;
        }

        @Override
        public void onViewPositionChanged(@NonNull View changedView, int left, int top, int dx, int dy) {
            mCurTop = top;
            float mDragOffset1 = ((float) top - (mParentHeight - mDrawerHeight - mBottomHeight)) / mDrawerHeight;

            //旋转与透明跟随效果
            mDrawerView.setAlpha(1 - mDragOffset1);
            mRotateView.setRotation((1 - mDragOffset1) * 180);
            requestLayout();
        }
    }

    public void maximize() {
        smoothSlideTo(0.0f);
        flag_is_maximize = true;
    }

    public void minimize() {
        smoothSlideTo(1.0f);
        flag_is_maximize =false;
    }

    private void smoothSlideTo( float slideOffset) {
        final int topBound = mParentHeight - mDrawerHeight - mBottomHeight;
        int y = (int) (topBound + slideOffset * mDrawerHeight);

        if (mDragHelper.smoothSlideViewTo(mDrawerView, mDrawerView.getLeft(), y)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDragHelper.processTouchEvent(event);
        //down事件返回false，让其底部的平行层级的view能够接收到点击事件
        return false;
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mDrawerView = findViewById(R.id.fractal_ConstraintLayout2_LinearLayout);
        mBottomView = findViewById(R.id.fractal_ConstraintLayout1);

        mDrawerView.setOnClickListener(v -> {
        });
        mBottomView.setOnClickListener(v -> {
        });
        //上面这俩不能删，否则点一下drawer动画会卡住

        mRotateView = findViewById(R.id.hide);
        mRotateView.setOnClickListener(v -> {
            if(flag_is_maximize){
                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    if(!flag_is_maximize) mDrawerView.setVisibility(GONE);
                }, 500);//延时0.5s后设置为gone
                minimize();}
            else{
                mDrawerView.setVisibility(VISIBLE);
                maximize();}
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureChildren(widthMeasureSpec,heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mParentHeight = this.getHeight();
        mBottomHeight = mBottomView.getMeasuredHeight();
        mDrawerHeight = mDrawerView.getMeasuredHeight();

        mBottomView.layout(l, mParentHeight - mBottomHeight, r, b);
        if (mCurTop == -1)
            mCurTop = mParentHeight - mBottomHeight;

        mDrawerView.layout(l, mCurTop, r, mCurTop + mDrawerHeight);
    }

    @Override
    public void computeScroll() {
        if(mDragHelper.continueSettling(true))
            ViewCompat.postInvalidateOnAnimation(this);
    }
}
