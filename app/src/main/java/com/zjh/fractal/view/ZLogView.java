package com.zjh.fractal.view;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.zjh.fractal.R;


public class ZLogView {
    public final static short info_status_normal=0;
    public final static short info_status_hint =1;
    public final static short info_status_warning=2;
    public final static short info_status_error=3;

    private final ForegroundColorSpan span_light_blue;
    private final ForegroundColorSpan span_red;
    private final ForegroundColorSpan span_light_red;
    private final ForegroundColorSpan span_yellow;
    private final ForegroundColorSpan span_white;
    private final LinearLayout web_info_LinearLayout;
    private final AppCompatActivity activity;
    private final ScrollView scrollview;

    public ZLogView(AppCompatActivity a, LinearLayout ll, ScrollView sv){
        activity=a;
        span_light_blue = new ForegroundColorSpan(activity.getResources().getColor(R.color.light_blue));
        span_red = new ForegroundColorSpan(activity.getResources().getColor(R.color.red));
        span_light_red = new ForegroundColorSpan(activity.getResources().getColor(R.color.light_red));
        span_yellow = new ForegroundColorSpan(activity.getResources().getColor(R.color.yellow));
        span_white = new ForegroundColorSpan(activity.getResources().getColor(R.color.white));
        web_info_LinearLayout=ll;
        scrollview=sv;
        scrollview.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                scrollview.post(new Runnable() {
                    @Override
                    public void run() {
                        scrollview.fullScroll(View.FOCUS_DOWN);
                    }
                });
            }
        });
    }

    public void info_add(String text){
        info_add(info_status_normal,text);
    }
    public void info_add(int R_color, String text){
        final ForegroundColorSpan temp_color_span = new ForegroundColorSpan(R_color);
        SpannableStringBuilder builder = new SpannableStringBuilder("> ");
        builder.setSpan(span_light_blue, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        int start=builder.length();
        builder.append(text);
        builder.setSpan(temp_color_span, start ,start+text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        final TextView t = new TextView(activity);
        t.setTextIsSelectable(true);
        t.setText(builder);

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                web_info_LinearLayout.addView(t);
            }
        });
    }
    public void info_add(short mode, String text) {
        //改变textview的部分颜色
        //参考自https://blog.csdn.net/qq_21036939/article/details/50239543
        //其中的span颜色在initial_span_colors中初始化
        SpannableStringBuilder builder = new SpannableStringBuilder("> ");
        builder.setSpan(span_light_blue, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        switch (mode){
            case info_status_normal:
                break;
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
        t.setText(builder);

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                web_info_LinearLayout.addView(t);
            }
        });
    }
    public void clear_screen(){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                web_info_LinearLayout.removeAllViewsInLayout();
            }
        });
    }
}
