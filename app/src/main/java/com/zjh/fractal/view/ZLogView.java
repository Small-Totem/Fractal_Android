package com.zjh.fractal.view;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.zjh.fractal.R;

import static com.zjh.fractal.util.AnimationForView.load_view;

public class ZLogView {
    public final static short info_status_normal = 0;
    public final static short info_status_hint = 1;
    public final static short info_status_warning = 2;
    public final static short info_status_error = 3;

    private final ForegroundColorSpan span_light_blue;
    private final ForegroundColorSpan span_red;
    private final ForegroundColorSpan span_light_red;
    private final ForegroundColorSpan span_yellow;
    private final ForegroundColorSpan span_white;
    private final LinearLayout web_info_LinearLayout;
    private final AppCompatActivity activity;
    private final ScrollView scrollview;

    public ZLogView(AppCompatActivity a, LinearLayout ll, ScrollView sv) {
        activity = a;
        span_light_blue = new ForegroundColorSpan(ContextCompat.getColor(activity, R.color.light_blue));
        span_red = new ForegroundColorSpan(ContextCompat.getColor(activity, R.color.red));
        span_light_red = new ForegroundColorSpan(ContextCompat.getColor(activity, R.color.light_red));
        span_yellow = new ForegroundColorSpan(ContextCompat.getColor(activity, R.color.yellow));
        span_white = new ForegroundColorSpan(ContextCompat.getColor(activity, R.color.white));
        web_info_LinearLayout = ll;
        scrollview = sv;
        scrollview.getViewTreeObserver().addOnGlobalLayoutListener(() -> scrollview.post(new Runnable() {
            @Override
            public void run() {
                scrollview.fullScroll(View.FOCUS_DOWN);
            }
        }));
    }

    public void info_add(int R_color, String text) {
        final ForegroundColorSpan temp_color_span = new ForegroundColorSpan(R_color);
        SpannableStringBuilder builder = new SpannableStringBuilder("> ");
        builder.setSpan(span_light_blue, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        int start = builder.length();
        builder.append(text);
        builder.setSpan(temp_color_span, start, start + text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        final TextView t = new TextView(activity);
        t.setTextIsSelectable(true);
        t.setVisibility(View.INVISIBLE);
        t.setLayoutParams(new LinearLayout.LayoutParams
                (ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        activity.runOnUiThread(() ->{
            t.setText(builder);
            web_info_LinearLayout.addView(t);
            load_view(t,300,1);
        });
    }

    public void info_add(short mode, String text) {
        // 改变textview的部分颜色
        // 参考自https://blog.csdn.net/qq_21036939/article/details/50239543
        // 其中的span颜色在initial_span_colors中初始化
        SpannableStringBuilder builder = new SpannableStringBuilder("> ");
        builder.setSpan(span_light_blue, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        switch (mode) {
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

        int start = builder.length();
        builder.append(text);
        builder.setSpan(span_white, start, start + text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        final TextView t = new TextView(activity);
        t.setTextIsSelectable(true);
        t.setVisibility(View.INVISIBLE);
        t.setLayoutParams(new LinearLayout.LayoutParams
                (ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        activity.runOnUiThread(() -> {
            t.setText(builder);
            web_info_LinearLayout.addView(t);
            load_view(t,300,1);
        });
    }

    public void clear() {
        activity.runOnUiThread(web_info_LinearLayout::removeAllViews);
    }
}
