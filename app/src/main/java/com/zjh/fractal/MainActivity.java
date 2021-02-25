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
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    final static int generate_mode_max = 21;// 除去-1,-2,一共21个，0->20
    final static int fractal_id_num = 11;// 除去-1,一共11个
    final static int iteration_max = 16384;// 为了防止渲染时间太长才设的限制
    final static double scale_max = 4E12;// 由于fractal.c那边的限制，超过这个数后很快就不能正常渲染了

    int screenWidth;
    int screenHeight;

    static double center_x = 0;
    static double center_y = 0;
    static double scale_times = 0.5;
    static double pixel_times = 0.5;
    static double generate_now_quality = 0.3;
    static int fractal_id = 0;// 图形id 0是曼德博集合
    static int generate_mode = 0;// 渲染模式
    static int iteration_times = 128;// 迭代次数
    static int use_thread=1;//使用的线程数(测试)  不得大于10 可等于
    static int auto_iteration_max = 2000;

    static boolean flag_if_generate_now = true;
    static boolean flag_should_reload = true;// 在设置界面中调用，是否重新渲染，默认true
    static boolean color_reversal = true;// 渲染颜色反转
    static boolean display_color_reversal = false;// 显示反转
    static boolean flag_should_load_from_storage = false;// 在改日夜模式后为true，是否加载图片,默认false
    static boolean auto_iteration = false;
    static boolean flag_use_data;/*
                                  * 为了实现每次更新信息都暂存数据（用于恢复上次关闭时的参数），使用
                                  * 两份SharedPreferences保存数据（称为A和B），当进入程序后读取到的flag_use_data为true时，
                                  * SharedPreferences中的值将会被设置为false，每次信息更新时写入A(对应true)，要读取上次信息时从B读取。
                                  * 当flag_use_data_A为false时则相反。
                                  */
    static boolean flag_should_change_data = true;// 只有当程序启动时，这个值为true，随后一直为false
                                                  // （确保flag_use_data的改变在程序启动后只发生一次）

    static String file_path = "null";

    static {
        System.loadLibrary("fractal");
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (flag_should_change_data) {
            flag_use_data = SaveData.get_data_boolean(getApplicationContext(), "flag_use_data_A");
            SaveData.set_data_boolean(getApplicationContext(), "flag_use_data_A", !flag_use_data);
            flag_should_change_data = false;
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);// 隐藏状态栏

        file_path = ImageProcess.getFileRoot(this) + "/fractal.png";

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(dm);
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;

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
                .putBoolean("night_mode_Preference",true).apply();

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
            ConstraintLayout C1 = findViewById(R.id.fractal_ConstraintLayout2);
            ConstraintLayout C2 = findViewById(R.id.fractal_ConstraintLayout3);
            if (isChecked) {
                C1.setAlpha(0.15f);
                C2.setAlpha(0.15f);
            } else {
                C1.setAlpha(0.7f);
                C2.setAlpha(0.75f);
            }
        });
        SwitchCompat switch3 = findViewById(R.id.fractal_switch3);
        switch3.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ConstraintLayout C = findViewById(R.id.fractal_ConstraintLayout3);
            if (isChecked) {
                C.setVisibility(View.VISIBLE);
            } else {
                C.setVisibility(View.INVISIBLE);
            }
        });
        SwitchCompat switch4 = findViewById(R.id.fractal_switch4);
        switch4.setOnCheckedChangeListener((buttonView, isChecked) -> {
            View v1 = findViewById(R.id.axis_horizontal);
            View v2 = findViewById(R.id.axis_vertical);
            if (isChecked) {
                v1.setVisibility(View.VISIBLE);
                v2.setVisibility(View.VISIBLE);
            } else {
                v1.setVisibility(View.GONE);
                v2.setVisibility(View.GONE);
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

        if (flag_should_reload)
            generate(pixel_times);
        else if (flag_should_load_from_storage) {
            ImageView i = findViewById(R.id.fractal);
            Bitmap bitmap = ImageProcess.GetLocalBitmap(file_path);
            i.setImageBitmap(bitmap);
            update_info();
        }

        flag_should_load_from_storage = false;
        flag_should_reload = true;
    }

    public void generate(double quality) {
        pixel_times = quality;
        update_info();

        GenerateFractal(file_path, (int) (screenHeight * pixel_times), (int) (screenWidth * pixel_times), center_x,
                center_y, scale_times * pixel_times, fractal_id, get_color_reversal(), generate_mode,
                iteration_times,use_thread,get_iteration_auto_max());

        ImageView i = findViewById(R.id.fractal);
        Bitmap bitmap = ImageProcess.GetLocalBitmap(file_path);
        i.setImageBitmap(bitmap);
    }

    public native void GenerateFractal(String path, int screen_height, int screen_width, double center_x,
            double center_y, double SCALE_times, int fractal_id, int color_reversal, int generate_mode,
            int iteration_times,int use_thread,int auto_iteration_max);

    private int get_color_reversal() {
        if (color_reversal)
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
        if(auto_iteration) str_iteration="迭代="+ iteration_times+"->"+auto_iteration_max;
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
        Button generate_color_reverse_button = findViewById(R.id.fractal_more_btn4);
        Button display_color_reverse_button = findViewById(R.id.fractal_more_btn8);
        Button auto_iteration_button = findViewById(R.id.fractal_more_btn0);



        fractal_double_button.setEnabled(scale_times <= scale_max); // 小于4E12时才准放大
        iteration_double_button.setEnabled(iteration_times < iteration_max);
        if(color_reversal) generate_color_reverse_button.setTextColor(getResources().getColor(R.color.light_blue));
        else generate_color_reverse_button.setTextColor(getResources().getColor(R.color.text_color));
        if(auto_iteration) auto_iteration_button.setTextColor(getResources().getColor(R.color.light_blue));
        else auto_iteration_button.setTextColor(getResources().getColor(R.color.text_color));
        display_color_reverse_button.setTextColor(getResources().getColor(R.color.text_color));

        getSharedPreferences("fractal_settings_Preferences", MODE_PRIVATE).edit()
                .putBoolean("color_reverse_Preference", color_reversal).apply();

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

        SaveData.save_data(center_x, center_y, scale_times, pixel_times, generate_now_quality,
                color_reversal, fractal_id, generate_mode, iteration_times,auto_iteration
                ,auto_iteration_max, getApplicationContext(), flag_use_data);
    }



    //////////////////////////////////
    /// onclick函数
    public void generate_fractal(View v) {
        generate(1);
    }

    public void generate_fractal_low_quality(View v) {
        generate(0.5);
    }

    public void settings(View v) {
        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
    }

    public void fractal_double(View v) {
        scale_times *= 2f;
        if (flag_if_generate_now)
            generate(generate_now_quality);
    }

    public void fractal_half(View v) {
        scale_times /= 2f;
        if (flag_if_generate_now)
            generate(generate_now_quality);
    }

    public void fractal_up(View v) {
        center_y -= 1 / (5f * scale_times);
        if (flag_if_generate_now)
            generate(generate_now_quality);
    }

    public void fractal_down(View v) {
        center_y += 1 / (5f * scale_times);
        if (flag_if_generate_now)
            generate(generate_now_quality);
    }

    public void fractal_left(View v) {
        center_x -= 1 / (5f * scale_times);
        if (flag_if_generate_now)
            generate(generate_now_quality);
    }

    public void fractal_right(View v) {
        center_x += 1 / (5f * scale_times);
        if (flag_if_generate_now)
            generate(generate_now_quality);
    }

    public void fractal_left_up(View v) {
        center_y -= 1 / (5f * scale_times);
        center_x -= 1 / (5f * scale_times);
        if (flag_if_generate_now)
            generate(generate_now_quality);
    }

    public void fractal_left_down(View v) {
        center_x -= 1 / (5f * scale_times);
        center_y += 1 / (5f * scale_times);
        if (flag_if_generate_now)
            generate(generate_now_quality);
    }

    public void fractal_right_up(View v) {
        center_x += 1 / (5f * scale_times);
        center_y -= 1 / (5f * scale_times);
        if (flag_if_generate_now)
            generate(generate_now_quality);
    }

    public void fractal_right_down(View v) {
        center_x += 1 / (5f * scale_times);
        center_y += 1 / (5f * scale_times);
        if (flag_if_generate_now)
            generate(generate_now_quality);
    }

    // 横向、纵向缩放两倍
    public void double_horizontal(View v) {
    }

    public void double_vertical(View v) {
    }

    public void reverse(View v) {
        color_reversal = !color_reversal;
        if (flag_if_generate_now)
            generate(generate_now_quality);
    }

    public void change_generate_mode(View v) {
        generate_mode++;
        if (generate_mode == generate_mode_max)
            generate_mode = 0;
        if (flag_if_generate_now)
            generate(generate_now_quality);
    }

    public void iteration_times_double(View v) {
        iteration_times *= 2;
        if (flag_if_generate_now)
            generate(generate_now_quality);
    }

    public void iteration_times_half(View v) {
        if (iteration_times == 1)
            return;
        iteration_times /= 2;
        if (flag_if_generate_now)
            generate(generate_now_quality);
    }
    public void iteration_times_plus_one(View v){
        iteration_times++;
        if (flag_if_generate_now)
            generate(generate_now_quality);
    }
    public void display_color_reverse(View v) {
    //显示反转 而非渲染反转
        if(display_color_reversal) ((Button)v).setTextColor(getResources().getColor(R.color.text_color));
        else ((Button)v).setTextColor(getResources().getColor(R.color.light_blue));

        ImageView i = findViewById(R.id.fractal);
        Bitmap bitmap = ImageProcess.GetLocalBitmap(file_path);
        if(display_color_reversal){
            i.setImageBitmap(bitmap);
        }
        else{
            i.setImageBitmap(ImageProcess.Negative(bitmap));
        }
        display_color_reversal=!display_color_reversal;
        TextView text_reversal = findViewById(R.id.fractal_text_reversal);
        String str_reversal = "反转=" + color_reversal+"/"+display_color_reversal;
        text_reversal.setText(str_reversal);
    }
    public void iteration_auto(View v) {
        auto_iteration =!auto_iteration;

        if (flag_if_generate_now)
            generate(generate_now_quality);
    }
}