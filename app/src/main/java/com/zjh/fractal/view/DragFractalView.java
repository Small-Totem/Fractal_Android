//参考自 https://www.cnblogs.com/linjzong/p/4211661.html
package com.zjh.fractal.view;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import androidx.appcompat.widget.AppCompatImageView;
import com.zjh.fractal.Definition;
import com.zjh.fractal.MainActivity;

import static com.zjh.fractal.Definition.center_im;
import static com.zjh.fractal.Definition.center_re;
import static com.zjh.fractal.Definition.flag_if_generate_now;
import static com.zjh.fractal.Definition.flag_use_transition_animation;
import static com.zjh.fractal.Definition.generate_now_quality;
import static com.zjh.fractal.Definition.scale_times;

public class DragFractalView extends AppCompatImageView{
    private final GestureDetector gesture_detector;
    public MainActivity m;

    public DragFractalView(Context context, AttributeSet attrs) {
        super(context, attrs);
        m = (MainActivity)context;
        setOnTouchListener(new DragTouchListener());
        gesture_detector = new GestureDetector(getContext(), new SimpleOnGestureListener(){
            @Override
            public boolean onDown(MotionEvent e) {
            return true;
            }
        });
        //setScaleType(ScaleType.FIT_CENTER);
    }

    private void update_re_im_scale(){
        //拖动、缩放时即时更新显示在屏幕下方之数据
        String str_re = "Re=" + center_re;
        String str_im = "Im=" + center_im;
        String str_scale = "缩放=" + scale_times;
        m.TextView_re.setText(str_re);
        m.TextView_im.setText(str_im);
        m.TextView_scale.setText(str_scale);
    }

    public class DragTouchListener implements OnTouchListener{
        private static final int MODE_DRAG = 1;
        private static final int MODE_ZOOM = 2;
        private int mode = 0;
        private float start_distance;/*缩放开始时的手指间距*/
        private final PointF start_point = new PointF();/*用于记录开始时候的坐标位置*/

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    //设置拖动模式
                    mode =MODE_DRAG;
                    start_point.set(event.getX(), event.getY());
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (flag_if_generate_now)
                        m.generate(generate_now_quality, flag_use_transition_animation);
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mode == MODE_ZOOM) {
                        exec_zoom(event);
                    }else if (mode ==MODE_DRAG) {
                        exec_drag(event);
                    }
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    mode =MODE_ZOOM;
                    start_distance = get_distance(event);
                    break;
                default:
                    break;
            }
            return gesture_detector.onTouchEvent(event);
        }

        public void exec_drag(MotionEvent event) {
            float dx = event.getX() - start_point.x; // 得到x轴的移动距离
            float dy = event.getY() - start_point.y; // 得到y轴的移动距离

            //fixme 猜测0.001953125这个数仅在类1920x1080的分辨率有效，如果
            // 是2K或者4K屏可能会出现拖拽后与实际位移不符合
            // 俺又没2K屏的玩意，懒的搞了
            center_re -= dx*0.001953125/Definition.scale_times;
            center_im -= dy*0.001953125/Definition.scale_times;
            update_re_im_scale();

            start_point.set(event.getX(), event.getY());
            //在当前基础上移动
        }
        private void exec_zoom(MotionEvent event) {
            //只有同时触屏两个点的时候才执行
            if(event.getPointerCount()<2) return;
            float endDis = get_distance(event);// 结束距离
            if (endDis > 10f) { // 两个手指并拢在一起的时候像素大于10
                float curr_scale = endDis / start_distance;// 得到缩放倍数

                scale_times*=curr_scale;
                update_re_im_scale();

                start_distance =endDis;//重置距离
            }
        }

        //计算两个触摸点的距离
        private float get_distance(MotionEvent event) {
            float dx = event.getX(1) - event.getX(0);
            float dy = event.getY(1) - event.getY(0);
            /* 使用勾股定理返回两点之间的距离 */
            return (float) Math.sqrt(dx * dx + dy * dy);
        }
    }
}