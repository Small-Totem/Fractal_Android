package com.zjh.fractal;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import static com.zjh.fractal.Definition.*;

public class MainActivity extends AppCompatActivity {
    int screenWidth;
    int screenHeight;

    /*为了监视每个线程 但是由于jni动态传参到java好像不能只传数组的单个元素，所以这里没有用数组
此乃无奈之举（暂时没想到更好的解决方案） */
    static double generate_process_thread_0 =0;
    static double generate_process_thread_1 =0;
    static double generate_process_thread_2 =0;
    static double generate_process_thread_3 =0;
    static double generate_process_thread_4 =0;
    static double generate_process_thread_5 =0;
    static double generate_process_thread_6 =0;
    static double generate_process_thread_7 =0;
    static double generate_process_thread_8 =0;
    static double generate_process_thread_9 =0;

    static {
        System.loadLibrary("fractal");
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (flag_should_change_data) {
            flag_use_data = SaveData.get_data_boolean(getApplicationContext(), "flag_use_data_A",false);
            SaveData.set_data_boolean(getApplicationContext(), "flag_use_data_A", !flag_use_data);
            //flag_should_change_data = false;
            //这里注释掉是因为该为在onStart赋值为false(详见flag_should_change_data定义处的注释)
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);// 隐藏状态栏

        file_path = Tools.getFileRoot(this) + "/fractal.png";

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(dm);
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;

        initial_things_about_preference();
        initial_view();
    }

    public void initial_things_about_preference(){
        //读取使用的线程数
        String thread_str=getSharedPreferences("fractal_settings_Preferences", MODE_PRIVATE)
                .getString("thread_Preference",getResources().getStringArray(R.array.thread)[0]);
        final String[] str= getResources().getStringArray(R.array.thread);
        for(int i=0;i<str.length;i++){
            if(str[i].equals(thread_str)){
                use_thread=i*2;
                if(use_thread<2){
                    //防止第一次启动程序时thread_Preference的值为空
                    getSharedPreferences("fractal_settings_Preferences", MODE_PRIVATE).edit()
                            .putString("thread_Preference", str[0]).apply();
                }
                break;
            }
        }
        //设置SharedPreferences中的日夜模式为夜间(即启动时默认为夜间模式)
        getSharedPreferences("fractal_settings_Preferences", MODE_PRIVATE).edit()
                .putBoolean("night_mode_Preference",AppCompatDelegate.getDefaultNightMode()==AppCompatDelegate.MODE_NIGHT_YES)
                .apply();

        //读取是否使用图像渐变动画
        flag_use_transition_animation=getSharedPreferences("fractal_settings_Preferences", MODE_PRIVATE)
                .getBoolean("transition_Preference",true);
        //读取是否监视渲染信息
        flag_monitor_generate_info=getSharedPreferences("fractal_settings_Preferences", MODE_PRIVATE)
                .getBoolean("generate_info_Preference",true);
    }
    public void initial_view(){
        SwitchCompat switch1 = findViewById(R.id.fractal_switch1);
        switch1.setOnCheckedChangeListener((buttonView, isChecked) -> flag_if_generate_now = isChecked);
        /*
         * 上面是lambda表达式，这是替换之前的 switch1.setOnCheckedChangeListener(new
         * CompoundButton.OnCheckedChangeListener() {
         *
         * @Override public void onCheckedChanged(CompoundButton buttonView, boolean
         * isChecked) { flag_if_generate_now=isChecked; } });
         */
        SwitchCompat switch2 = findViewById(R.id.fractal_switch2);
        switch2.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ConstraintLayout C2 = findViewById(R.id.fractal_ConstraintLayout2);
            ConstraintLayout C3 = findViewById(R.id.fractal_ConstraintLayout3);
            if (isChecked) {
                C2.setAlpha(0.2f);
                C3.setAlpha(0.2f);
                /*
                这view会闪一下，很烦，不搞了
                if(C3.getVisibility()==View.VISIBLE)
                {AnimationForView.alpha_animation(C3,400,1f,0.15f,0.15f);}
                else {C3.setAlpha(0.15f);}
                AnimationForView.alpha_animation(C2,400,1f,0.15f,0.15f);
                */
            } else {

                C2.setAlpha(0.7f);
                C3.setAlpha(0.7f);
                /*
                if(C3.getVisibility()==View.VISIBLE)
                {AnimationForView.alpha_animation(C3,400,0.15f/0.7f,1f,0.7f);}
                else {C3.setAlpha(0.7f);}
                AnimationForView.alpha_animation(C2,400,0.15f,1f,0.7f);
                */
            }
        });
        SwitchCompat switch3 = findViewById(R.id.fractal_switch3);
        switch3.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ConstraintLayout C3 = findViewById(R.id.fractal_ConstraintLayout3);
            if (isChecked) {
                AnimationForView.load_view(C3,200,1f);
            } else {
                AnimationForView.close_view(C3,200,1f,View.INVISIBLE);
            }
        });
        SwitchCompat switch4 = findViewById(R.id.fractal_switch4);
        switch4.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ConstraintLayout v = findViewById(R.id.axis);
            if (isChecked) {
                AnimationForView.load_view(v,200,1f);
            } else {
                AnimationForView.close_view(v,200,1f,View.GONE);
            }
        });
        SwitchCompat switch5 = findViewById(R.id.fractal_switch5);
        switch5.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ConstraintLayout v = findViewById(R.id.generate_info_ConstraintLayout);
            if (isChecked) {
                AnimationForView.load_view(v,200,1f);
            } else {
                AnimationForView.close_view(v,200,1f,View.GONE);
            }
        });

        //轮子:当generate_info内容太多时自动滑到底部
        ScrollView sv=findViewById(R.id.generate_info_ScrollView);
        sv.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                sv.post(new Runnable() {
                    @Override
                    public void run() {
                        sv.fullScroll(View.FOCUS_DOWN);
                    }
                });
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        flag_should_reload = false;
    }

    // 处理旋转屏幕时的事件
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        recreate();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(flag_should_change_data){
            //处理进入程序的第一次渲染
            generate(pixel_times,false);
            flag_should_change_data=false;
        }
        else if (flag_should_reload) {
            //处理从SettingsActivity返回的情况
            generate(pixel_times,true);
        }
        else if (flag_should_load_from_storage) {
            ImageView i = findViewById(R.id.fractal);
            Bitmap bitmap = Tools.GetLocalBitmap(file_path);
            i.setImageBitmap(bitmap);
            update_info();
        }

        flag_should_load_from_storage = false;
        flag_should_reload = true;
    }

    public void generate(double quality,boolean use_transition_animation) {
        if(flag_is_generating){
            generate_info_add("警告:由于渲染未完成，取消了一次渲染请求");
            return;
        }
        flag_is_generating=true;
        pixel_times = quality;
        generate_info="";

        TextView t=findViewById(R.id.generate_info_TextView);
        t.setText(generate_info);

        if(!flag_monitor_generate_info) generate_info_add("提示:渲染信息监视已关闭");

        if(iteration_times*Math.pow(pixel_times,2)>=4000 ||
                (auto_iteration_max>=8000 &&iteration_times*Math.pow(pixel_times,2)>=2000) ){
            generate_info_add("警告:预计渲染耗时较长");
        }

        update_info();
        if(flag_monitor_generate_info) init_process_threads();

        new Thread(new Runnable() {
            @Override
            public void run() {
                show_generating_icon();
                long time=System.currentTimeMillis();

                generate_info_add("图像分辨率:"+ (int) (screenHeight * pixel_times)+"x"+(int) (screenWidth * pixel_times));

                GenerateFractal(file_path, (int) (screenHeight * pixel_times), (int) (screenWidth * pixel_times), center_x,
                        center_y, scale_times * pixel_times, fractal_id, boolean_to_int(color_reversal), generate_mode,
                        iteration_times, use_thread, get_iteration_auto_max(),boolean_to_int(flag_monitor_generate_info));

                time=System.currentTimeMillis()-time;
                double time_d=time/1000.0;
                generate_info_add("共花费"+time_d+"秒");

                generate_process_thread_0 = 1;
                generate_process_thread_1 = 1;
                generate_process_thread_2 = 1;
                generate_process_thread_3 = 1;
                generate_process_thread_4 = 1;
                generate_process_thread_5 = 1;
                generate_process_thread_6 = 1;
                generate_process_thread_7 = 1;
                generate_process_thread_8 = 1;
                generate_process_thread_9 = 1;

                runOnUiThread(new Runnable() {
                    //在thread里面更新view就要这样写
                    @Override
                    public void run() {
                        ImageView i = findViewById(R.id.fractal);
                        Bitmap bitmap = Tools.GetLocalBitmap(file_path);
                        if(use_transition_animation){
                            AnimationForView.transition_animation(i,getApplicationContext(),bitmap,image_change_time);
                        }
                        else {i.setImageBitmap(bitmap);}
                        flag_is_generating=false;
                    }
                });
            }
        }).start();
    }

    public void show_generating_icon(){
        new Thread(new Runnable() {
            @Override
            public void run() {

                ProgressBar p=findViewById(R.id.generating);
                while(flag_is_generating) {

                    if(p.getVisibility()==View.INVISIBLE) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                p.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                AnimationForView.close_view(p,200,1f,View.INVISIBLE);
            }
        }).start();
    }

    public void init_process_threads(){
        //对每个线程渲染情况的监听
        //待改进
        new Thread(new Runnable() {
            @Override
            public void run() {
                generate_process_thread_0 =0;
                ProgressBar p=findViewById(R.id.process_0);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        p.setVisibility(View.VISIBLE);
                    }
                });
                while(generate_process_thread_0 <1) {
                    try {
                        p.setProgress((int)(generate_process_thread_0 *100));
                        Thread.sleep(generate_process_wait_time);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                AnimationForView.close_view(p,150,1,View.INVISIBLE);
            }
        }).start();
        if(use_thread<=1)return;
        new Thread(new Runnable() {
            @Override
            public void run() {
                generate_process_thread_1 =0;
                ProgressBar p=findViewById(R.id.process_1);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        p.setVisibility(View.VISIBLE);
                    }
                });
                while(generate_process_thread_1 <1) {
                    try {
                        p.setProgress((int)(generate_process_thread_1 *100));
                        Thread.sleep(generate_process_wait_time);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                AnimationForView.close_view(p,150,1,View.INVISIBLE);
            }
        }).start();
        if(use_thread<=2)return;
        new Thread(new Runnable() {
            @Override
            public void run() {
                generate_process_thread_2 =0;
                ProgressBar p=findViewById(R.id.process_2);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        p.setVisibility(View.VISIBLE);
                    }
                });
                while(generate_process_thread_2 <1) {
                    try {
                        p.setProgress((int)(generate_process_thread_2 *100));
                        Thread.sleep(generate_process_wait_time);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                AnimationForView.close_view(p,150,1,View.INVISIBLE);
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                generate_process_thread_3 =0;
                ProgressBar p=findViewById(R.id.process_3);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        p.setVisibility(View.VISIBLE);
                    }
                });
                while(generate_process_thread_3 <1) {
                    try {
                        p.setProgress((int)(generate_process_thread_3 *100));
                        Thread.sleep(generate_process_wait_time);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                AnimationForView.close_view(p,150,1,View.INVISIBLE);
            }
        }).start();
        if(use_thread<=4)return;
        new Thread(new Runnable() {
            @Override
            public void run() {
                generate_process_thread_4 =0;
                ProgressBar p=findViewById(R.id.process_4);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        p.setVisibility(View.VISIBLE);
                    }
                });
                while(generate_process_thread_4 <1) {
                    try {
                        p.setProgress((int)(generate_process_thread_4 *100));
                        Thread.sleep(generate_process_wait_time);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                AnimationForView.close_view(p,150,1,View.INVISIBLE);
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                generate_process_thread_5 =0;
                ProgressBar p=findViewById(R.id.process_5);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        p.setVisibility(View.VISIBLE);
                    }
                });
                while(generate_process_thread_5 <1) {
                    try {
                        p.setProgress((int)(generate_process_thread_5 *100));
                        Thread.sleep(generate_process_wait_time);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                AnimationForView.close_view(p,150,1,View.INVISIBLE);
            }
        }).start();
        if(use_thread<=6)return;
        new Thread(new Runnable() {
            @Override
            public void run() {
                generate_process_thread_6 =0;
                ProgressBar p=findViewById(R.id.process_6);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        p.setVisibility(View.VISIBLE);
                    }
                });
                while(generate_process_thread_6 <1) {
                    try {
                        p.setProgress((int)(generate_process_thread_6 *100));
                        Thread.sleep(generate_process_wait_time);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                AnimationForView.close_view(p,150,1,View.INVISIBLE);
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                generate_process_thread_7 =0;
                ProgressBar p=findViewById(R.id.process_7);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        p.setVisibility(View.VISIBLE);
                    }
                });
                while(generate_process_thread_7 <1) {
                    try {
                        p.setProgress((int)(generate_process_thread_7 *100));
                        Thread.sleep(generate_process_wait_time);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                AnimationForView.close_view(p,150,1,View.INVISIBLE);
            }
        }).start();
        if(use_thread<=8)return;
        new Thread(new Runnable() {
            @Override
            public void run() {
                generate_process_thread_8 =0;
                ProgressBar p=findViewById(R.id.process_8);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        p.setVisibility(View.VISIBLE);
                    }
                });
                while(generate_process_thread_8 <1) {
                    try {
                        p.setProgress((int)(generate_process_thread_8 *100));
                        Thread.sleep(generate_process_wait_time);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                AnimationForView.close_view(p,150,1,View.INVISIBLE);
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                generate_process_thread_9 =0;
                ProgressBar p=findViewById(R.id.process_9);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        p.setVisibility(View.VISIBLE);
                    }
                });
                while(generate_process_thread_9 <1) {
                    try {
                        p.setProgress((int)(generate_process_thread_9 *100));
                        Thread.sleep(generate_process_wait_time);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                AnimationForView.close_view(p,150,1,View.INVISIBLE);
            }
        }).start();
    }

    public native void GenerateFractal(String path, int screen_height, int screen_width, double center_x,
            double center_y, double scale_times, int fractal_id, int color_reversal, int generate_mode,
            int iteration_times,int use_thread,int auto_iteration_max,int monitor_generate_info);

    /*这个方法也由jni调用*/
    public void generate_info_add(String text){
        TextView t=findViewById(R.id.generate_info_TextView);
        generate_info+="> "+text+"\n";
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        t.setText(generate_info);
                    }
                });
            }
        }).start();
    }

    private int boolean_to_int(boolean b) {
        if (b)
            return 1;
        else
            return 0;
    }

    private int get_iteration_auto_max() {
        if (auto_iteration)
            return auto_iteration_max;
        else
            return 0;
    }

    public void update_info() {
        display_color_reversal=false;
        if(scale_times>=scale_max)scale_times=scale_max;

        TextView text_x = findViewById(R.id.fractal_text_x);
        TextView text_y = findViewById(R.id.fractal_text_y);
        TextView text_pixel = findViewById(R.id.fractal_text_pixel);
        TextView text_scale = findViewById(R.id.fractal_text_scale);
        TextView text_reversal = findViewById(R.id.fractal_text_reversal);
        TextView text_generate_mode = findViewById(R.id.fractal_text_generate_mode);
        TextView text_generate_id = findViewById(R.id.fractal_text_generate_id);
        TextView text_text_iteration = findViewById(R.id.fractal_text_iteration);

        String str_x = "X=" + center_x;
        String str_y = "Y=" + center_y;
        String str_pixel = "渲染倍率=" + pixel_times;
        String str_scale = "缩放=" + scale_times;
        String str_reversal = "反转=" + color_reversal+"/"+display_color_reversal;
        String str_generate_mode = "渲染模式=" + generate_mode;
        String str_generate_id = "ID=" + fractal_id;
        String  str_iteration;
        if(auto_iteration&&iteration_times<auto_iteration_max) str_iteration="迭代="+ iteration_times+"->"+auto_iteration_max;
        else str_iteration= "迭代="+ iteration_times;

        text_x.setText(str_x);
        text_y.setText(str_y);
        text_pixel.setText(str_pixel);
        text_scale.setText(str_scale);
        text_reversal.setText(str_reversal);
        text_generate_mode.setText(str_generate_mode);
        text_generate_id.setText(str_generate_id);
        text_text_iteration.setText(str_iteration);

        Button fractal_double_button = findViewById(R.id.fractal_btn3);
        Button iteration_double_button = findViewById(R.id.fractal_more_btn5);
        Button iteration_half_button = findViewById(R.id.fractal_more_btn6);
        Button generate_color_reverse_button = findViewById(R.id.fractal_more_btn4);
        Button display_color_reverse_button = findViewById(R.id.fractal_more_btn8);
        Button auto_iteration_button = findViewById(R.id.fractal_more_btn0);

        fractal_double_button.setEnabled(scale_times < scale_max); // 小于4E12时才准放大
        iteration_double_button.setEnabled(iteration_times < iteration_max);
        iteration_half_button.setEnabled(iteration_times>1);

        if(color_reversal) generate_color_reverse_button.setTextColor(getResources().getColor(R.color.light_blue));
        else generate_color_reverse_button.setTextColor(getResources().getColor(R.color.text_color));
        if(auto_iteration) auto_iteration_button.setTextColor(getResources().getColor(R.color.light_blue));
        else auto_iteration_button.setTextColor(getResources().getColor(R.color.text_color));
        display_color_reverse_button.setTextColor(getResources().getColor(R.color.text_color));


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

        SaveData.save_data(center_x, center_y, scale_times, color_reversal, fractal_id,
                generate_mode, iteration_times,auto_iteration,auto_iteration_max, getApplicationContext(), flag_use_data);
    }

    //////////////////////////////////
    /// onclick函数
    public void generate_fractal(View v) {
        generate(1,flag_use_transition_animation);
    }

    public void generate_fractal_low_quality(View v) {
        generate(0.5,flag_use_transition_animation);
    }

    public void settings(View v) {
        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
    }

    public void fractal_double(View v) {
        scale_times *= 2f;
        if (flag_if_generate_now)
            generate(generate_now_quality,flag_use_transition_animation);
    }

    public void fractal_half(View v) {
        scale_times /= 2f;
        if (flag_if_generate_now)
            generate(generate_now_quality,flag_use_transition_animation);
    }

    public void fractal_up(View v) {
        center_y -= 1 / (5f * scale_times);
        if (flag_if_generate_now)
            generate(generate_now_quality,flag_use_transition_animation);
    }

    public void fractal_down(View v) {
        center_y += 1 / (5f * scale_times);
        if (flag_if_generate_now)
            generate(generate_now_quality,flag_use_transition_animation);
    }

    public void fractal_left(View v) {
        center_x -= 1 / (5f * scale_times);
        if (flag_if_generate_now)
            generate(generate_now_quality,flag_use_transition_animation);
    }

    public void fractal_right(View v) {
        center_x += 1 / (5f * scale_times);
        if (flag_if_generate_now)
            generate(generate_now_quality,flag_use_transition_animation);
    }

    public void fractal_left_up(View v) {
        center_y -= 1 / (5f * scale_times);
        center_x -= 1 / (5f * scale_times);
        if (flag_if_generate_now)
            generate(generate_now_quality,flag_use_transition_animation);
    }

    public void fractal_left_down(View v) {
        center_x -= 1 / (5f * scale_times);
        center_y += 1 / (5f * scale_times);
        if (flag_if_generate_now)
            generate(generate_now_quality,flag_use_transition_animation);
    }

    public void fractal_right_up(View v) {
        center_x += 1 / (5f * scale_times);
        center_y -= 1 / (5f * scale_times);
        if (flag_if_generate_now)
            generate(generate_now_quality,flag_use_transition_animation);
    }

    public void fractal_right_down(View v) {
        center_x += 1 / (5f * scale_times);
        center_y += 1 / (5f * scale_times);
        if (flag_if_generate_now)
            generate(generate_now_quality,flag_use_transition_animation);
    }

    // 横向、纵向缩放两倍
    public void double_horizontal(View v) {
    }

    public void double_vertical(View v) {
    }

    public void reverse(View v) {
        color_reversal = !color_reversal;
        if (flag_if_generate_now)
            generate(generate_now_quality,flag_use_transition_animation);
    }

    public void change_generate_mode(View v) {
        generate_mode++;
        if (generate_mode == generate_mode_max)
            generate_mode = 0;
        if (flag_if_generate_now)
            generate(generate_now_quality,flag_use_transition_animation);
    }

    public void iteration_times_double(View v) {
        iteration_times *= 2;
        if (flag_if_generate_now)
            generate(generate_now_quality,flag_use_transition_animation);
    }

    public void iteration_times_half(View v) {
        if (iteration_times == 1)
            return;
        iteration_times /= 2;
        if (flag_if_generate_now)
            generate(generate_now_quality,flag_use_transition_animation);
    }
    public void iteration_times_plus_one(View v){
        iteration_times++;
        if (flag_if_generate_now)
            generate(generate_now_quality,flag_use_transition_animation);
    }
    public void display_color_reverse(View v) {
    //显示反转 而非渲染反转
        if(display_color_reversal) ((Button)v).setTextColor(getResources().getColor(R.color.text_color));
        else ((Button)v).setTextColor(getResources().getColor(R.color.light_blue));

        ImageView i = findViewById(R.id.fractal);
        Bitmap bitmap = Tools.GetLocalBitmap(file_path);

        if (!display_color_reversal) bitmap= Tools.Negative(bitmap);
        if(flag_use_transition_animation) {
            AnimationForView.transition_animation(i, this, bitmap, image_change_time);
        }
        else {
            i.setImageBitmap(bitmap);
        }

        display_color_reversal=!display_color_reversal;
        
        TextView text_reversal = findViewById(R.id.fractal_text_reversal);
        String str_reversal = "反转=" + color_reversal+"/"+display_color_reversal;
        text_reversal.setText(str_reversal);
    }
    public void iteration_auto(View v) {
        auto_iteration =!auto_iteration;

        if (flag_if_generate_now)
            generate(generate_now_quality,flag_use_transition_animation);
    }
}