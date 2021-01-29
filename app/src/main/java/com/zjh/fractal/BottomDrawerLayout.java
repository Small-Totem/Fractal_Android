package com.zjh.fractal;
import android.content.Context;
//import android.support.v4.view.ViewCompat;
//import android.support.v4.widget.ViewDragHelper;
import androidx.core.view.ViewCompat;
import androidx.customview.widget.ViewDragHelper;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

//轮子：BottomDrawerLayout 来自github

public class BottomDrawerLayout extends ViewGroup {

    private static final String TAG = "BottomDrawerLayout";

    private View mDrawerView1;
    private View mBottomView1;
    private View mRotateView1;

    private ViewDragHelper mDragHelper;
    private float mInitialX;
    private float mInitialY;
    private int mTouchSlop;

    private int mCurTop1=-1;
    private int mBottomHeight1;
    private int mDrawerHeight1;
    private int mParentHeight1;
    private int mCurTop2=-1;
    private float mDragOffset1 = 1;

    private boolean isUnderBottomView = false;
    private boolean isUnderDrawerView = false;

    private boolean flag_is_maximize1 = false;
    private OnDrawerStatusChanged onDrawerStatusChanged;
    public BottomDrawerLayout(Context context) {
        super(context);
        init(context);
    }

    public BottomDrawerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public BottomDrawerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mDragHelper = ViewDragHelper.create(this, 1.0f,new DragerCallBack());
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }


/*
    public void setOnDrawerStatusChanged(OnDrawerStatusChanged onDrawerStatusChanged) {
        this.onDrawerStatusChanged = onDrawerStatusChanged;
    }

    public void switchDrawer() {
        if(mDragOffset1 <1){
            minimize();
        }else{
            maximize();
        }
    }


 */
    private class DragerCallBack extends  ViewDragHelper.Callback{

        //从底部到顶部的顺序遍历子view
        @Override
        public int getOrderedChildIndex(int index) {
            int childCount = com.zjh.fractal.BottomDrawerLayout.this.getChildCount();
            int newIndex = childCount - index -1;

            return  newIndex;
        }

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return false;//child == mDrawerView1;
            //这里可能是拖拽相关的事情，注释掉能正常用
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
//            Log.d(TAG, "clampViewPositionHorizontal " + left + "," + dx);
//            final int leftBound  = getPaddingLeft();
//            final int rightBound = getWidth() - mBottomView.getWidth() - leftBound;
//            //坐标系三种情况
//            final int newLeft = Math.min(Math.max(left, leftBound), rightBound);
//
//            return newLeft;

            return super.clampViewPositionHorizontal(child, left, dx);
        }

        //要想上下拖动必须重写此方法
        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
//            Log.d(TAG, "clampViewPositionVertical " + top + "," + dy);

            //final int topBound = getHeight() - mDrawerView1.getMeasuredHeight() - mBottomView1.getHeight();
            //final int bottomBound = getHeight()  - mBottomView1.getHeight();
            //final int newTop = Math.min(Math.max(top, topBound), bottomBound);

            return 0;//newTop;
            //同tryCaptureView(),这里注释掉不影响使用
        }

        @Override
        public void onViewCaptured(View capturedChild, int activePointerId) {
            super.onViewCaptured(capturedChild, activePointerId);
        }


        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
//            Log.i(TAG, "onViewReleased:" + "xvel:" + xvel + ",yvel:" + yvel);
            //yvel Fling产生的值，yvel > 0 则是快速往下Fling || yvel < 0 则是快速往上Fling

            //int top = mParentHeight1 - mDrawerHeight1 - mBottomHeight1;
            //if (yvel > 0 || (yvel == 0 && mDragOffset1 > 0.5f)/* 后面这个小括号里判断处理拖动之后停下来但是未松手的情况 */) {
            //     top += mDrawerHeight1;
            //}
            // mDragHelper.settleCapturedViewAt(releasedChild.getLeft(), top);
            //invalidate();//important 不加，就不会刷新View的位置

            //同tryCaptureView(),这里注释掉不影响使用
        }

        @Override
        public void onViewDragStateChanged(int state) {
            super.onViewDragStateChanged(state);
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            mCurTop1 = top;
            mDragOffset1 = ((float) top - (mParentHeight1 - mDrawerHeight1 - mBottomHeight1)) / mDrawerHeight1;

            //旋转与透明跟随效果
            mDrawerView1.setAlpha(1 - mDragOffset1);
            mRotateView1.setRotation((1 - mDragOffset1) * 180);
            requestLayout();


            if (onDrawerStatusChanged != null) {
                onDrawerStatusChanged.onChanged(mParentHeight1,top);
            }


        }
    }

    public interface OnDrawerStatusChanged{
        void onChanged(int parentHeight, int drawerTop);
    }

    public void maximize(int id)
    {
        smoothSlideTo(id,0.0f);
        if(id==1) {
            flag_is_maximize1 = true;
        }
    }

    public void minimize(int id)
    {
        smoothSlideTo(id,1.0f);
        if(id==1) {
            flag_is_maximize1 =false;
        }
    }

    private boolean smoothSlideTo(int id,float slideOffset) {
        if(id==1) {
            final int topBound = mParentHeight1 - mDrawerHeight1 - mBottomHeight1;
            int y = (int) (topBound + slideOffset * mDrawerHeight1);

            if (mDragHelper.smoothSlideViewTo(mDrawerView1, mDrawerView1.getLeft(), y)) {
                ViewCompat.postInvalidateOnAnimation(this);
                return true;
            }
        }
        return false;
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);

    }
/*
//edited_by_totem:删去拖动打开drawer功能


    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {

        int act = MotionEventCompat.getActionMasked(event);
//        final int action = event.getAction();

        switch (act) {
            //由于很多情况不能拦截事件，这种时候系统不会调用onTouchEvent()
            // 手动把事件传递给mDragHelper.processTouchEvent
            case MotionEvent.ACTION_DOWN:

                mInitialX = event.getX();
                mInitialY = event.getY();
                //Feed the down event to the detector so it has
                // context when/if dragging begins
//                mDetector.onTouchEvent(event);
                mDragHelper.processTouchEvent(event);
                isUnderBottomView = mDragHelper.isViewUnder(mBottomView, (int)mInitialX, (int)mInitialY);
                isUnderDrawerView = mDragHelper.isViewUnder(mDrawerView, (int)mInitialX, (int)mInitialY);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                mDragHelper.processTouchEvent(event) ;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                mDragHelper.processTouchEvent(event) ;
                break;
            case MotionEvent.ACTION_MOVE:

                final float x = event.getX();
                final float y = event.getY();
                final int yDiff = (int) Math.abs(y - mInitialY);
                final int xDiff = (int) Math.abs(x - mInitialX);
                //Verify that either difference is enough to be a drag
                if ((yDiff > mTouchSlop || xDiff > mTouchSlop) && (isUnderBottomView || isUnderDrawerView) ){
                    //Start capturing events
                    return true;
                }
                break;
        }

        //父类是viewgroup，返回的false
        return super.onInterceptTouchEvent(event);

    }
*/
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        mDragHelper.processTouchEvent(event);
        //down事件返回false，让其底部的平行层级的view能够接收到点击事件
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                return false;
            case MotionEvent.ACTION_UP:
                return false;
            //只有当手指达到拖动阈值时this才确定消耗此系列事件
            //若未达到阈值也返回true，则与其平行的view不会收到click事件

            case MotionEvent.ACTION_MOVE:
                final float x = event.getX();
                final float y = event.getY();
                final int yDiff = (int) Math.abs(y - mInitialY);
                final int xDiff = (int) Math.abs(x - mInitialX);
                //Verify that either difference is enough to be a drag
                if ((yDiff > mTouchSlop || xDiff > mTouchSlop) && (isUnderBottomView || isUnderDrawerView) ){
                    //Start capturing events
                    return true;
                }
                break;
        }
        return false;
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mDrawerView1 = findViewById(R.id.fractal_ConstraintLayout2_LinearLayout);
        mBottomView1 = findViewById(R.id.fractal_ConstraintLayout1);

        mDrawerView1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        mBottomView1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        //上面这俩不能删，否则点一下drawer动画会卡住

        mRotateView1 = findViewById(R.id.hide);
        mRotateView1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                View a=findViewById(R.id.fractal_ConstraintLayout2_LinearLayout);

                if(flag_is_maximize1){
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(!flag_is_maximize1) a.setVisibility(GONE);
                        }
                    }, 500);//延时0.5s后设置为gone
                    minimize(1);}
                else{
                    a.setVisibility(VISIBLE);
                    maximize(1);}
            }
        });

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureChildren(widthMeasureSpec,heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mParentHeight1 = this.getHeight();
        mBottomHeight1 = mBottomView1.getMeasuredHeight();

        mDrawerHeight1 = mDrawerView1.getMeasuredHeight();

        mBottomView1.layout(l, mParentHeight1 - mBottomHeight1, r, b);
        if (mCurTop1 == -1) {
            mCurTop1 = mParentHeight1 - mBottomHeight1;
        }

        mDrawerView1.layout(l, mCurTop1, r, mCurTop1 + mDrawerHeight1);
    }



    @Override
    public void computeScroll() {
        if(mDragHelper.continueSettling(true))
        {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

}
