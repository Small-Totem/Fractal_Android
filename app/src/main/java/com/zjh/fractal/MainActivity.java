package com.zjh.fractal;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.zjh.fractal.util.ActivityManager;
import com.zjh.fractal.util.AnimationForView;
import com.zjh.fractal.util.SaveData;
import com.zjh.fractal.util.Tools;
import com.zjh.fractal.view.ZLogView;

import java.math.RoundingMode;
import java.text.NumberFormat;

import static com.zjh.fractal.Definition.*;
import static com.zjh.fractal.view.ZLogView.*;

public class MainActivity extends AppCompatActivity {
    public int screenWidth;
    public int screenHeight;

    public ImageView fractal;

    private TextView text_x;
    private TextView text_y;
    private TextView text_pixel;
    private TextView text_scale;
    private TextView text_reversal;
    private TextView text_generate_mode;
    private TextView text_generate_id;
    private TextView text_text_iteration;

    private AppCompatButton fractal_double_button;
    private AppCompatButton fractal_1_5x_button;
    private AppCompatButton iteration_double_button;
    private AppCompatButton iteration_half_button;
    private AppCompatButton iteration_minus_1_button;
    private AppCompatButton generate_color_reverse_button;
    private AppCompatButton display_color_reverse_button;
    private AppCompatButton auto_iteration_button;

    private ProgressBar[] generate_info_ProgressBar;
    private ZLogView log_view;

    // 监视每个线程的进度
    private double[] generate_progress_thread;

    public static ActivityManager am;

    static {
        System.loadLibrary("fractal");
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        am = new ActivityManager();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (flag_should_change_data) {
            flag_use_data = SaveData.get_data_boolean(getApplicationContext(), "flag_use_data_A", false);
            SaveData.set_data_boolean(getApplicationContext(), "flag_use_data_A", !flag_use_data);
            // flag_should_change_data = false;
            // 这里注释掉是因为该在onStart赋值为false(详见flag_should_change_data定义处的注释)
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);// 隐藏状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);// 导航栏透明

        file_path = Tools.getFileRoot(this) + "/fractal.png";

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(dm);
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;

        initial_things_about_preference();
        initial_view();
        init(file_path);

        generate_progress_thread = new double[use_thread];

        if (!flag_should_change_data) {
            log_view.info_add(info_status_hint, "布局已重载");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        flag_should_reload = false;
    }

    // 处理旋转屏幕时的事件
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        recreate();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (flag_should_change_data) {
            // 处理进入程序的第一次渲染
            // 第一次渲染不使用多线更新ui的方式，这样虽然会慢一点但是不会因为图片加载而闪烁一下
            GenerateFractal((int) (screenHeight * pixel_times), (int) (screenWidth * pixel_times), center_x, center_y,
                    scale_times * pixel_times, fractal_id, boolean_to_int(color_reversal), generate_mode,
                    iteration_times, use_thread, get_iteration_auto_max(), 0,0);
            update_info();
            Bitmap bitmap = Tools.GetLocalBitmap(file_path, this);
            fractal.setImageBitmap(bitmap);

            flag_should_change_data = false;
        } else if (flag_should_reload) {
            // 处理从SettingsActivity返回的情况
            generate(pixel_times, true);
        } else if (flag_should_load_from_storage) {
            if(paint_mode==0){
                Bitmap bitmap = Tools.GetLocalBitmap(file_path, this);
                fractal.setImageBitmap(bitmap);
            }
            update_info();
        }

        flag_should_load_from_storage = false;
        flag_should_reload = true;
    }

    @Override
    public void onBackPressed() {
        am.exit();
    }

    private void initial_things_about_preference() {
        // 读取使用的线程数
        String thread_str = getSharedPreferences("fractal_settings_Preferences", MODE_PRIVATE)
                .getString("thread_Preference", /*默认值*/ getResources().getStringArray(R.array.thread)[0]);
        final String[] str = getResources().getStringArray(R.array.thread);
        for (int i = 0; i < str.length; i++) {
            if (str[i].equals(thread_str)) {
                use_thread = i * 2;
                if (use_thread < 1){
                    use_thread = 1;
                    // 防止第一次启动程序时thread_Preference的值为空
                    getSharedPreferences("fractal_settings_Preferences", MODE_PRIVATE).edit()
                            .putString("thread_Preference", str[0]).apply();
                }
                break;
            }
        }

        String paint_mode_str = getSharedPreferences("fractal_settings_Preferences", MODE_PRIVATE)
                .getString("paint_mode_Preference", getResources().getStringArray(R.array.paint_mode)[0]);
        final String[] str1 = getResources().getStringArray(R.array.paint_mode);
        for (int i = 0; i < str1.length; i++) {
            if (str1[i].equals(paint_mode_str)) {
                paint_mode = i;
                if(i==0)
                    getSharedPreferences("fractal_settings_Preferences", MODE_PRIVATE).edit()
                            .putString("paint_mode_Preference", str1[0]).apply();
                break;
            }
        }

        // 设置SharedPreferences中的日夜模式为夜间(即启动时默认为夜间模式)
        getSharedPreferences("fractal_settings_Preferences", MODE_PRIVATE).edit().putBoolean("night_mode_Preference",
                AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES).apply();

        // 读取是否使用图像渐变动画
        flag_use_transition_animation = getSharedPreferences("fractal_settings_Preferences", MODE_PRIVATE)
                .getBoolean("transition_Preference", true);
        // 读取是否监视渲染信息
        flag_monitor_generate_info = getSharedPreferences("fractal_settings_Preferences", MODE_PRIVATE)
                .getBoolean("generate_info_Preference", true);
    }

    private void initial_view() {
        SwitchCompat switch1 = findViewById(R.id.fractal_switch1);
        switch1.setOnCheckedChangeListener((buttonView, isChecked) -> flag_if_generate_now = isChecked);
        SwitchCompat switch2 = findViewById(R.id.fractal_switch2);
        switch2.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ConstraintLayout C2 = findViewById(R.id.fractal_ConstraintLayout2);
            ConstraintLayout C3 = findViewById(R.id.fractal_ConstraintLayout3);
            if (isChecked) {
                C2.setAlpha(0.2f);
                C3.setAlpha(0.2f);
                /*
                 * 这view会闪一下，很烦，不搞了 if(C3.getVisibility()==View.VISIBLE)
                 * {AnimationForView.alpha_animation(C3,400,1f,0.15f,0.15f);} else
                 * {C3.setAlpha(0.15f);}
                 * AnimationForView.alpha_animation(C2,400,1f,0.15f,0.15f);
                 */
            } else {
                C2.setAlpha(0.7f);
                C3.setAlpha(0.7f);
                /*
                 * if(C3.getVisibility()==View.VISIBLE)
                 * {AnimationForView.alpha_animation(C3,400,0.15f/0.7f,1f,0.7f);} else
                 * {C3.setAlpha(0.7f);} AnimationForView.alpha_animation(C2,400,0.15f,1f,0.7f);
                 */
            }
        });
        SwitchCompat switch3 = findViewById(R.id.fractal_switch3);
        switch3.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ConstraintLayout C3 = findViewById(R.id.fractal_ConstraintLayout3);
            if (isChecked) {
                AnimationForView.load_view(C3, 200, 1f);
            } else {
                AnimationForView.close_view(C3, 200, 1f, View.INVISIBLE);
            }
        });
        SwitchCompat switch4 = findViewById(R.id.fractal_switch4);
        switch4.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ConstraintLayout v = findViewById(R.id.axis);
            if (isChecked) {
                AnimationForView.load_view(v, 200, 1f);
            } else {
                AnimationForView.close_view(v, 200, 1f, View.GONE);
            }
        });
        SwitchCompat switch5 = findViewById(R.id.fractal_switch5);
        switch5.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ConstraintLayout v = findViewById(R.id.generate_info);
            if (isChecked) {
                AnimationForView.load_view(v, 200, 1f);
                log_view.scroll_down();
            } else {
                AnimationForView.close_view(v, 200, 1f, View.GONE);
            }
        });

        fractal = findViewById(R.id.fractal);

        text_x = findViewById(R.id.fractal_text_x);
        text_y = findViewById(R.id.fractal_text_y);
        text_pixel = findViewById(R.id.fractal_text_pixel);
        text_scale = findViewById(R.id.fractal_text_scale);
        text_reversal = findViewById(R.id.fractal_text_reversal);
        text_generate_mode = findViewById(R.id.fractal_text_generate_mode);
        text_generate_id = findViewById(R.id.fractal_text_generate_id);
        text_text_iteration = findViewById(R.id.fractal_text_iteration);
        fractal_double_button = findViewById(R.id.fractal_btn3);

        iteration_double_button = findViewById(R.id.fractal_more_btn_8);
        iteration_half_button = findViewById(R.id.fractal_more_btn_9);
        generate_color_reverse_button = findViewById(R.id.fractal_more_btn_7);
        display_color_reverse_button = findViewById(R.id.fractal_more_btn_6);
        auto_iteration_button = findViewById(R.id.fractal_more_btn_3);
        fractal_1_5x_button = findViewById(R.id.fractal_more_btn_0);
        iteration_minus_1_button = findViewById(R.id.fractal_more_btn_11);

        ProgressBar progress_bar = findViewById(R.id.generating);

        generate_info_ProgressBar = new ProgressBar[10];
        generate_info_ProgressBar[0] = findViewById(R.id.progress_0);
        generate_info_ProgressBar[1] = findViewById(R.id.progress_1);
        generate_info_ProgressBar[2] = findViewById(R.id.progress_2);
        generate_info_ProgressBar[3] = findViewById(R.id.progress_3);
        generate_info_ProgressBar[4] = findViewById(R.id.progress_4);
        generate_info_ProgressBar[5] = findViewById(R.id.progress_5);
        generate_info_ProgressBar[6] = findViewById(R.id.progress_6);
        generate_info_ProgressBar[7] = findViewById(R.id.progress_7);
        generate_info_ProgressBar[8] = findViewById(R.id.progress_8);
        generate_info_ProgressBar[9] = findViewById(R.id.progress_9);

        LinearLayout generate_info_LinearLayout = findViewById(R.id.generate_info_LinearLayout);
        ScrollView generate_info_ScrollView = findViewById(R.id.generate_info_ScrollView);
        log_view = new ZLogView(this, generate_info_LinearLayout, generate_info_ScrollView, progress_bar);
    }

    public void generate(double quality, boolean use_transition_animation) {
        if(log_view.start_task(true,null,true))
            return;

        pixel_times = quality;

        if (!flag_monitor_generate_info)
            log_view.info_add(info_status_hint, "渲染信息监视已关闭");

        if (iteration_times * Math.pow(pixel_times, 2) >= 4000
                || (auto_iteration_max >= 8000 && iteration_times * Math.pow(pixel_times, 2) >= 2000)) {
            log_view.info_add(info_status_warning, "预计渲染耗时较长");
        }
        if (quality>2) {
            log_view.info_add(info_status_warning, "绘制分辨率过高的图片可能会导致闪退");
        }

        update_info();

        if (flag_monitor_generate_info)
            init_progress_threads();

        final MainActivity m = this;

        int bitmap_x= (int) (screenWidth * pixel_times);
        int bitmap_y= (int) (screenHeight * pixel_times);

        log_view.info_add(ContextCompat.getColor(this, R.color.grey),
                "图像分辨率:" + bitmap_x + "x" + bitmap_y);

        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(2);
        nf.setRoundingMode(RoundingMode.UP);

        if(paint_mode==1||paint_mode==2){
            new Thread(() -> {
                long time = System.currentTimeMillis();
                byte[] data= GenerateFractal(bitmap_y, bitmap_x, center_x, center_y,
                        scale_times * pixel_times, fractal_id, boolean_to_int(color_reversal), generate_mode,
                        iteration_times, use_thread, get_iteration_auto_max(), boolean_to_int(flag_monitor_generate_info),1);
                time = System.currentTimeMillis() - time;
                double time_d = time / 1000.0;
                log_view.info_add(ContextCompat.getColor(this, R.color.grey),
                        "计算花费" + nf.format(time_d) + "秒");

                time = System.currentTimeMillis();
                Bitmap bitmap = Bitmap.createBitmap(bitmap_x, bitmap_y, Bitmap.Config.ARGB_8888);
                Canvas c = new Canvas(bitmap);
                c.drawColor(Color.WHITE);
                Paint paint = new Paint();
                log_view.info_add(info_status_normal,"正在使用canvas绘图");
                int curr=0;
                for (int y = 0; y < bitmap_y; y++) {
                    for (int x = 0; x < bitmap_x; x++) {
                        //uint8_t(native,无符号)--------jni--------->byte(java,有符号)--------&0xff--------->int(java,有符号)
                        if(paint_mode==1)
                            paint.setARGB(255,data[curr]&0xff,data[curr+1]&0xff,data[curr+2]&0xff);
                        else if(paint_mode==2)
                            paint.setARGB(255,data[curr],data[curr+1],data[curr+2]);
                        c.drawPoint(x, y, paint);
                        curr+=3;
                    }
                }
                time = System.currentTimeMillis() - time;
                time_d = time / 1000.0;
                log_view.info_add(ContextCompat.getColor(this, R.color.grey),
                        "绘制花费" + nf.format(time_d) + "秒");



                notify_free_data();
                generate_info_add((short)5,"完成");
                runOnUiThread(() -> {
                    if (use_transition_animation) {
                        AnimationForView.transition_animation(fractal, getApplicationContext(), bitmap, image_change_time);
                    } else {
                        fractal.setImageBitmap(bitmap);
                    }
                    log_view.close_task();
                });
            }).start();
        }
        else {
            new Thread(() -> {
                long time = System.currentTimeMillis();

                GenerateFractal(bitmap_y, bitmap_x, center_x, center_y,
                        scale_times * pixel_times, fractal_id, boolean_to_int(color_reversal), generate_mode,
                        iteration_times, use_thread, get_iteration_auto_max(), boolean_to_int(flag_monitor_generate_info),0);

                time = System.currentTimeMillis() - time;
                double time_d = time / 1000.0;
                float storage = 3 * bitmap_x * bitmap_y / 1048576f;// 1024*1024
                log_view.info_add(ContextCompat.getColor(this, R.color.grey),
                        "共花费" + nf.format(time_d) + "秒,占用" + nf.format(storage) + "MB");

                Bitmap bitmap = Tools.GetLocalBitmap(file_path, m);
                runOnUiThread(() -> {
                    if (use_transition_animation) {
                        AnimationForView.transition_animation(fractal, getApplicationContext(), bitmap, image_change_time);
                    } else {
                        fractal.setImageBitmap(bitmap);
                    }
                    log_view.close_task();
                });
            }).start();
        }
    }

    private void init_progress_threads() {
        if (generate_progress_thread.length != use_thread) {
            generate_progress_thread = new double[use_thread];
        }
        // 对每个线程渲染情况的监听
        for (int i = 0; i < use_thread; i++) {
            int temp_thread_id = i;
            new Thread(() -> {
                ProgressBar p = generate_info_ProgressBar[temp_thread_id];
                runOnUiThread(() -> p.setVisibility(View.VISIBLE));
                generate_progress_thread[temp_thread_id] = 0;
                while (generate_progress_thread[temp_thread_id] < 1) {
                    try {
                        p.setProgress((int) (generate_progress_thread[temp_thread_id] * 100));
                        Thread.sleep(generate_progress_wait_time);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                }
                AnimationForView.close_view(p, 150, 1, View.INVISIBLE);
            }).start();
        }
    }

    public native byte[] GenerateFractal(int screen_height, int screen_width, double center_x, double center_y,
            double scale_times, int fractal_id, int color_reversal, int generate_mode, int iteration_times,
            int use_thread, int auto_iteration_max, int monitor_generate_info, int return_byte_array);

    public native void init(String path);

    public native void notify_free_data();

    /* jni回调函数,也可由java调用 */
    public void generate_info_add(short mode, String text) {
        if (mode == 4)
            log_view.info_add(ContextCompat.getColor(this, R.color.grey), text);
        else if (mode == 5)
            log_view.info_add(Color.GREEN, text);
        else
            log_view.info_add(mode, text);
    }

    /* jni回调函数 */
    public void get_progress_from_native(double[] a, int num) {
        if (generate_progress_thread.length != num)
            generate_progress_thread = new double[num];
        generate_progress_thread = a;
    }

    public int boolean_to_int(boolean b) {
        if (b)
            return 1;
        return 0;
    }

    public int get_iteration_auto_max() {
        if (auto_iteration)
            return auto_iteration_max;
        return 0;
    }

    private void update_info() {
        display_color_reversal = false;
        if (scale_times > scale_max)
            scale_times = scale_max;
        if (scale_times <= 0)
            iteration_times = 1;
        if (iteration_times <= 0)
            iteration_times = 1;

        String str_x = "X=" + center_x;
        String str_y = "Y=" + center_y;
        String str_pixel = "渲染倍率=" + pixel_times;
        String str_scale = "缩放=" + scale_times;
        String str_reversal = "反转=" + color_reversal + "/" + display_color_reversal;
        String str_generate_mode = "渲染模式=" + generate_mode;
        String str_generate_id = "ID=" + fractal_id;
        String str_iteration = "迭代=" + iteration_times;
        if (auto_iteration && iteration_times < auto_iteration_max)
            str_iteration += "->" + auto_iteration_max;

        text_x.setText(str_x);
        text_y.setText(str_y);
        text_pixel.setText(str_pixel);
        text_scale.setText(str_scale);
        text_reversal.setText(str_reversal);
        text_generate_mode.setText(str_generate_mode);
        text_generate_id.setText(str_generate_id);
        text_text_iteration.setText(str_iteration);

        fractal_double_button.setEnabled(scale_times < scale_max); // 小于4E12时才准放大
        fractal_1_5x_button.setEnabled(scale_times < scale_max);
        iteration_double_button.setEnabled(iteration_times < Integer.MAX_VALUE);
        iteration_half_button.setEnabled(iteration_times > 1);
        iteration_minus_1_button.setEnabled(iteration_times > 1);

        if (color_reversal)
            generate_color_reverse_button.setTextColor(ContextCompat.getColor(this, R.color.light_blue));
        else
            generate_color_reverse_button.setTextColor(ContextCompat.getColor(this, R.color.text_color));
        if (auto_iteration)
            auto_iteration_button.setTextColor(ContextCompat.getColor(this, R.color.light_blue));
        else
            auto_iteration_button.setTextColor(ContextCompat.getColor(this, R.color.text_color));
        display_color_reverse_button.setTextColor(ContextCompat.getColor(this, R.color.text_color));

        getSharedPreferences("fractal_settings_Preferences", MODE_PRIVATE).edit()
                .putBoolean("color_reverse_Preference", color_reversal).apply();
        getSharedPreferences("fractal_settings_Preferences", MODE_PRIVATE).edit()
                .putBoolean("auto_iteration_Preference", auto_iteration).apply();

        final String[] str1 = getResources().getStringArray(R.array.fractal_id);
        final String[] str2 = getResources().getStringArray(R.array.generate_mode);

        if (fractal_id == -1) {
            getSharedPreferences("fractal_settings_Preferences", MODE_PRIVATE).edit()
                    .putString("fractal_id_Preference", str1[fractal_id_num]).apply();
        } else {
            getSharedPreferences("fractal_settings_Preferences", MODE_PRIVATE).edit()
                    .putString("fractal_id_Preference", str1[fractal_id]).apply();
        }

        if (generate_mode == -1) {
            getSharedPreferences("fractal_settings_Preferences", MODE_PRIVATE).edit()
                    .putString("generate_mode_Preference", str2[generate_mode_max]).apply();
        } else if (generate_mode == -2) {
            getSharedPreferences("fractal_settings_Preferences", MODE_PRIVATE).edit()
                    .putString("generate_mode_Preference", str2[generate_mode_max + 1]).apply();
        } else {
            getSharedPreferences("fractal_settings_Preferences", MODE_PRIVATE).edit()
                    .putString("generate_mode_Preference", str2[generate_mode]).apply();
        }

        SaveData.save_data(center_x, center_y, scale_times, color_reversal, fractal_id, generate_mode, iteration_times,
                auto_iteration, auto_iteration_max, getApplicationContext(), flag_use_data);
    }

    //////////////////////////////////
    /// onclick函数
    public void generate_fractal(View v) {
        generate(1, flag_use_transition_animation);
    }
    public void generate_fractal_low_quality(View v) {
        generate(0.5, flag_use_transition_animation);
    }
    public void generate_fractal_2x(View v) {
        generate(2, flag_use_transition_animation);
    }
    public void settings(View v) {
        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
    }
    public void fractal_double(View v) {
        scale_times *= 2f;
        if (flag_if_generate_now)
            generate(generate_now_quality, flag_use_transition_animation);
    }
    public void fractal_1_5x(View v) {
        scale_times *= 1.5f;
        if (flag_if_generate_now)
            generate(generate_now_quality, flag_use_transition_animation);
    }
    public void fractal_half(View v) {
        scale_times /= 2f;
        if (flag_if_generate_now)
            generate(generate_now_quality, flag_use_transition_animation);
    }
    public void fractal_up(View v) {
        center_y -= 1 / (5f * scale_times);
        if (flag_if_generate_now)
            generate(generate_now_quality, flag_use_transition_animation);
    }
    public void fractal_down(View v) {
        center_y += 1 / (5f * scale_times);
        if (flag_if_generate_now)
            generate(generate_now_quality, flag_use_transition_animation);
    }
    public void fractal_left(View v) {
        center_x -= 1 / (5f * scale_times);
        if (flag_if_generate_now)
            generate(generate_now_quality, flag_use_transition_animation);
    }
    public void fractal_right(View v) {
        center_x += 1 / (5f * scale_times);
        if (flag_if_generate_now)
            generate(generate_now_quality, flag_use_transition_animation);
    }
    public void fractal_left_up(View v) {
        center_y -= 1 / (5f * scale_times);
        center_x -= 1 / (5f * scale_times);
        if (flag_if_generate_now)
            generate(generate_now_quality, flag_use_transition_animation);
    }
    public void fractal_left_down(View v) {
        center_x -= 1 / (5f * scale_times);
        center_y += 1 / (5f * scale_times);
        if (flag_if_generate_now)
            generate(generate_now_quality, flag_use_transition_animation);
    }
    public void fractal_right_up(View v) {
        center_x += 1 / (5f * scale_times);
        center_y -= 1 / (5f * scale_times);
        if (flag_if_generate_now)
            generate(generate_now_quality, flag_use_transition_animation);
    }
    public void fractal_right_down(View v) {
        center_x += 1 / (5f * scale_times);
        center_y += 1 / (5f * scale_times);
        if (flag_if_generate_now)
            generate(generate_now_quality, flag_use_transition_animation);
    }
    // 横向、纵向缩放两倍
    public void double_horizontal(View v) {
    }
    public void double_vertical(View v) {
    }
    public void reverse(View v) {
        color_reversal = !color_reversal;
        if (flag_if_generate_now)
            generate(generate_now_quality, flag_use_transition_animation);
    }
    public void change_generate_mode(View v) {
        generate_mode++;
        if (generate_mode == generate_mode_max)
            generate_mode = 0;
        if (flag_if_generate_now)
            generate(generate_now_quality, flag_use_transition_animation);
    }
    public void iteration_times_double(View v) {
        iteration_times *= 2;
        if (flag_if_generate_now)
            generate(generate_now_quality, flag_use_transition_animation);
    }
    public void iteration_times_half(View v) {
        iteration_times /= 2;
        if (iteration_times <= 0)
            iteration_times = 1;
        if (flag_if_generate_now)
            generate(generate_now_quality, flag_use_transition_animation);
    }
    public void iteration_times_plus_one(View v) {
        iteration_times++;
        if (flag_if_generate_now)
            generate(generate_now_quality, flag_use_transition_animation);
    }
    public void iteration_times_minus_one(View v) {
        iteration_times--;
        if (iteration_times <= 0)
            iteration_times = 1;
        if (flag_if_generate_now)
            generate(generate_now_quality, flag_use_transition_animation);
    }
    public void display_color_reverse(View v) {
        // 显示反转 而非渲染反转
        if (display_color_reversal)
            ((AppCompatButton) v).setTextColor(ContextCompat.getColor(this, R.color.text_color));
        else
            ((AppCompatButton) v).setTextColor(ContextCompat.getColor(this, R.color.light_blue));

        Bitmap bitmap;
        if(fractal.getDrawable() instanceof BitmapDrawable){
            //第一次进去的时候是BitmapDrawable
            bitmap=((BitmapDrawable)fractal.getDrawable()).getBitmap();
        }
        else {
            BitmapDrawable old=(BitmapDrawable)((TransitionDrawable) fractal.getDrawable()).getDrawable(1);
            bitmap= old.getBitmap();
        }


         bitmap = Tools.Negative(bitmap);
        if (flag_use_transition_animation) {
            AnimationForView.transition_animation(fractal, this, bitmap, image_change_time);
        } else {
            fractal.setImageBitmap(bitmap);
        }

        display_color_reversal = !display_color_reversal;

        String str_reversal = "反转=" + color_reversal + "/" + display_color_reversal;
        text_reversal.setText(str_reversal);
    }
    public void iteration_auto(View v) {
        auto_iteration = !auto_iteration;

        if (flag_if_generate_now)
            generate(generate_now_quality, flag_use_transition_animation);
    }
}