package com.zjh.fractal.view;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import static com.zjh.fractal.util.AnimationForView.close_view;
import static com.zjh.fractal.util.AnimationForView.load_view;

public class ZLogView {
    public final static short info_status_normal=0;
    public final static short info_status_hint =1;
    public final static short info_status_warning=2;
    public final static short info_status_error=3;

    public boolean flag_if_using_log=false;

    private final ForegroundColorSpan span_light_blue;
    private final ForegroundColorSpan span_red;
    private final ForegroundColorSpan span_light_red;
    private final ForegroundColorSpan span_yellow;
    private final ForegroundColorSpan span_white;

    private final ViewTreeObserver.OnGlobalLayoutListener listener;
    private boolean OnGlobalLayoutListener_enabled=false;//标识现在到底有没有启用 只能通过scroll_down_enabled改

    private final AppCompatActivity activity;

    public final LinearLayout linearLayout;
    public final ScrollView scrollView;
    public final ProgressBar doing_task_ProgressBar;

    //ProgressBar可为null
    public ZLogView(AppCompatActivity a, ScrollView sv, ProgressBar pb){
        activity=a;
        span_light_blue = new ForegroundColorSpan(Color.rgb(15,129,218));
        span_red = new ForegroundColorSpan(Color.RED);
        span_light_red = new ForegroundColorSpan(Color.rgb(255,94,94));
        span_yellow = new ForegroundColorSpan(Color.YELLOW);
        span_white = new ForegroundColorSpan(Color.WHITE);
        linearLayout = new LinearLayout(a);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        scrollView = sv;
        scrollView.addView(linearLayout);
        doing_task_ProgressBar =pb;

        //这里是两个lambda
        listener= () -> scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));

        scroll_down_enabled(true);
    }


    //使用start_task和close_task来避免同时开两个任务的情况
    //name 可为null
    public boolean start_task(boolean clear_log,String name,boolean using_scroll_down){
        //返回值是 是否改取消掉这个请求
        if(flag_if_using_log) {
            info_add(ZLogView.info_status_warning, "由于有正在进行的任务，取消了一次请求");
            return true;
        }

        if(clear_log)
            clear();

        if(doing_task_ProgressBar !=null)
            load_view(doing_task_ProgressBar,300,1);

        if(using_scroll_down)
            scroll_down_enabled(true);
        if(name!=null)
            info_add(Color.GREEN,name);
        flag_if_using_log=true;
        return false;
    }

    public void close_task(/*boolean success*/){
        //if(success)
        //    info_add(Color.GREEN,"完成");

        if(OnGlobalLayoutListener_enabled){
            scroll_down();//不然的话直接关掉scroll_down_enabled他最后一下不会滑下去
        }
        scroll_down_enabled(false);

        if(doing_task_ProgressBar !=null)
            close_view(doing_task_ProgressBar,300,1,View.INVISIBLE);

        flag_if_using_log=false;
    }

    public void scroll_down(){
        //*这里其实还挺复杂的
        //关于这里为什么要runOnUiThread:
        //由于info_add的实现用到了runOnUiThread 实际的view添加过程是在UI线程中完成的而非此处
        //所以这里也要runOnUiThread 保证下滑操作在log成功添加后执行
        //然后 不能直接scrollview.fullScroll(ScrollView.FOCUS_DOWN) 还要带个post 具体可参考https://blog.csdn.net/hanjieson/article/details/10312861
        activity.runOnUiThread(()-> scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN)));
    }

    public void scroll_down_enabled(boolean enabled){
        if(OnGlobalLayoutListener_enabled&&!enabled){
            OnGlobalLayoutListener_enabled=false;
            scrollView.getViewTreeObserver().removeOnGlobalLayoutListener(listener);
        }
        else if(!OnGlobalLayoutListener_enabled&&enabled){
            OnGlobalLayoutListener_enabled=true;
            scrollView.getViewTreeObserver().addOnGlobalLayoutListener(listener);
        }
    }

    public void clear(){
        //不知道为啥可以这样写。。
        //原:activity.runOnUiThread(() -> linearLayout.removeAllViewsInLayout());
        activity.runOnUiThread(linearLayout::removeAllViews);
    }


    //普通text
    public void info_add(int text_color, String text){
        final ForegroundColorSpan span_text_color = new ForegroundColorSpan(text_color);
        SpannableStringBuilder builder = new SpannableStringBuilder("> ");
        builder.setSpan(span_light_blue, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        int start=builder.length();
        builder.append(text);
        builder.setSpan(span_text_color, start ,start+text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        final TextView t = new TextView(activity);
        t.setTextIsSelectable(true);
        t.setVisibility(View.INVISIBLE);
        //参数含义:宽,高
        t.setLayoutParams(new LinearLayout.LayoutParams
                (ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        activity.runOnUiThread(() -> {
            t.setText(builder);
            linearLayout.addView(t);
            load_view(t,300,1);
        });
    }


    //带状态的text
    public void info_add(short mode, String text) {
        //改变textview的部分颜色
        //参考自https://blog.csdn.net/qq_21036939/article/details/50239543
        //其中的span颜色在initial_span_colors中初始化
        SpannableStringBuilder builder = new SpannableStringBuilder("> ");
        builder.setSpan(span_light_blue,0,1,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        switch (mode){
            /*case info_status_normal:
                break;*/
            case info_status_hint:
                builder.append("提示:");
                builder.setSpan(span_yellow, 2, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;
            case info_status_warning:
                builder.append("警告:");
                builder.setSpan(span_light_red, 2, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;
            case info_status_error:
                builder.append("错误:");
                builder.setSpan(span_red, 2, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;
            default:
                break;
        }

        int start=builder.length();
        builder.append(text);
        builder.setSpan(span_white, start ,start+text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        final TextView t = new TextView(activity);
        t.setTextIsSelectable(true);
        t.setVisibility(View.INVISIBLE);
        t.setLayoutParams(new LinearLayout.LayoutParams
                (ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        activity.runOnUiThread(() -> {
            t.setText(builder);
            linearLayout.addView(t);
            load_view(t,300,1);
        });
    }
}
