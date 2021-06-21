package com.zjh.fractal;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.zjh.fractal.util.ActivityManager;
import com.zjh.fractal.util.SaveData;

import static com.zjh.fractal.Definition.*;
import static com.zjh.fractal.view.ZLogView.info_status_error;

public class SettingsActivity extends AppCompatActivity {
    EditText et1;
    EditText et2;
    EditText et3;
    EditText et4;
    EditText et5;
    EditText et6;
    EditText et7;
    private Context context;

    //这个是为了日夜模式切换的过渡动画,有一点点复杂
    //由于切换的时候会短暂黑屏,暂时用一个不太完美的方案实现过渡动画:
    //  先来一个FrameLayout渐变黑屏,然后
    //  由于AppCompatDelegate.setDefaultNightMode()会recreate()
    //  在recreate()之后,用下面这个标识使用进入的过渡动画(在onStart())
    //      即渐变黑屏->黑屏recreate()->从黑屏渐变到正常状态
    static boolean should_use_enter_animation=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);// 隐藏状态栏

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.settings, new SettingsFragment()).commit();
        }

        context = getApplicationContext();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(should_use_enter_animation){
            FrameLayout f=findViewById(R.id.settings_root);
            FrameLayout f_new = new FrameLayout(this);
            f_new.setClickable(true);
            f_new.setBackgroundColor(Color.BLACK);
            f_new.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            AlphaAnimation aa = new AlphaAnimation(1f,0f);
            aa.setDuration(500);
            aa.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationEnd(Animation animation) {
                    ((FrameLayout)f_new.getParent()).removeView(f_new);
                }
                public void onAnimationRepeat(Animation animation) {}
                public void onAnimationStart(Animation animation) {}
            });
            f.addView(f_new);
            f_new.startAnimation(aa);
            should_use_enter_animation=false;
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat
            implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {
        Preference night_mode_Preference;
        Preference fractal_id_Preference;
        Preference generate_mode_Preference;
        Preference color_reverse_Preference;
        Preference read_data_Preference;
        Preference open_picture_Preference;
        Preference settings_edit_text_Preference;
        Preference samples_Preference;
        Preference about_fractal_Preference;
        Preference thread_Preference;
        Preference auto_iteration_Preference;
        Preference transition_Preference;
        Preference generate_info_Preference;
        Preference exit_Preference;
        Preference paint_mode_Preference;

        boolean flag_if_first_click_for_recovery = true;
        boolean flag_if_first_click_for_exit = true;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            getPreferenceManager().setSharedPreferencesName("fractal_settings_Preferences");
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            flag_should_reload = false;

            night_mode_Preference = findPreference("night_mode_Preference");
            fractal_id_Preference = findPreference("fractal_id_Preference");
            generate_mode_Preference = findPreference("generate_mode_Preference");
            color_reverse_Preference = findPreference("color_reverse_Preference");
            read_data_Preference = findPreference("read_data_Preference");
            open_picture_Preference = findPreference("open_picture_Preference");
            settings_edit_text_Preference = findPreference("settings_edit_text_Preference");
            samples_Preference = findPreference("samples_Preference");
            about_fractal_Preference = findPreference("about_fractal_Preference");
            thread_Preference = findPreference("thread_Preference");
            auto_iteration_Preference = findPreference("auto_iteration_Preference");
            transition_Preference = findPreference("transition_Preference");
            generate_info_Preference = findPreference("generate_info_Preference");
            exit_Preference = findPreference("exit_Preference");
            paint_mode_Preference = findPreference("paint_mode_Preference");

            night_mode_Preference.setOnPreferenceClickListener(this);// 其实不应该设置成onclick 而应该是onchange 下次一定
            color_reverse_Preference.setOnPreferenceClickListener(this);
            read_data_Preference.setOnPreferenceClickListener(this);
            open_picture_Preference.setOnPreferenceClickListener(this);
            about_fractal_Preference.setOnPreferenceClickListener(this);
            auto_iteration_Preference.setOnPreferenceClickListener(this);
            transition_Preference.setOnPreferenceClickListener(this);
            generate_info_Preference.setOnPreferenceClickListener(this);
            exit_Preference.setOnPreferenceClickListener(this);

            fractal_id_Preference.setOnPreferenceChangeListener(this);
            generate_mode_Preference.setOnPreferenceChangeListener(this);
            samples_Preference.setOnPreferenceChangeListener(this);
            thread_Preference.setOnPreferenceChangeListener(this);
            paint_mode_Preference.setOnPreferenceChangeListener(this);
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            if (preference.equals(night_mode_Preference)) {

                //fixme 不知道这里的FrameLayout会不会越来越多,待验证
                //做了个勉勉强强的动画
                should_use_enter_animation=true;
                FrameLayout f=requireActivity().findViewById(R.id.settings_root);
                FrameLayout f_new = new FrameLayout(getContext());
                f_new.setClickable(true);
                f_new.setBackgroundColor(Color.BLACK);
                f_new.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
                AlphaAnimation aa = new AlphaAnimation(0f,1f);
                aa.setDuration(200);
                aa.setAnimationListener(new Animation.AnimationListener() {
                    public void onAnimationEnd(Animation animation) {
                        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_NO) {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                        } else {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        }
                    }
                    public void onAnimationRepeat(Animation animation) {}
                    public void onAnimationStart(Animation animation) {}
                });
                requireActivity().runOnUiThread(()->{
                    f.addView(f_new);
                    f_new.startAnimation(aa);
                });

                flag_should_load_from_storage = true;
            } else if (preference.equals(color_reverse_Preference)) {
                color_reversal = !color_reversal;
                flag_should_reload = true;
            } else if (preference.equals(auto_iteration_Preference)) {
                auto_iteration = !auto_iteration;
                flag_should_reload = true;
            } else if (preference.equals(read_data_Preference)) {
                // 由于第一次进入程序时会写入值到scale_times_false中，此时scale_times_true为空值，
                // 所以用scale_times_true来判断是不是第一次进程序
                if (SaveData.get_data_double(getContext(), "scale_times_true", 0) == 0f) {
                    preference.setSummary("没有已保存的参数");
                    return false;
                }
                if (flag_if_first_click_for_recovery) {
                    preference.setSummary("再次点击以确认");
                    flag_if_first_click_for_recovery = false;
                    return false;
                }
                preference.setSummary("正在渲染");
                get_info_from_SaveData(getContext(), flag_use_data);
                flag_should_reload = true;
                requireActivity().finish();
            } else if (preference.equals(open_picture_Preference)) {
                // 轮子:打开图片
                Intent intent = new Intent(Intent.ACTION_VIEW);

                ImageView fractal=((MainActivity) ActivityManager.activityLinkedList.get(0)).fractal;
                Bitmap bitmap;
                if((fractal.getDrawable() instanceof BitmapDrawable)){
                    //第一次进去的时候是BitmapDrawable
                    bitmap=((BitmapDrawable)fractal.getDrawable()).getBitmap();
                }
                else {
                    BitmapDrawable old=(BitmapDrawable)((TransitionDrawable) fractal.getDrawable()).getDrawable(1);
                    bitmap= old.getBitmap();
                }

                try {
                    String uriString = MediaStore.Images.Media.insertImage(requireContext().getContentResolver(),
                            bitmap, Long.toString(System.currentTimeMillis()), "null");
                    Uri uri = Uri.parse(uriString);
                    intent.setDataAndType(uri, "image/*");
                    startActivity(intent);
                } catch (Exception e) {
                    ((MainActivity) ActivityManager.activityLinkedList.get(0)).generate_info_add(info_status_error,
                            e.toString());
                }
            } else if (preference.equals(about_fractal_Preference)) {
                about_fractal_Preference.setSummary(R.string.about);
                about_fractal_Preference.setSelectable(false);
            } else if (preference.equals(transition_Preference)) {
                flag_use_transition_animation = !flag_use_transition_animation;
            } else if (preference.equals(generate_info_Preference)) {
                flag_monitor_generate_info = !flag_monitor_generate_info;
            } else if (preference.equals(exit_Preference)) {
                if (flag_if_first_click_for_exit) {
                    preference.setSummary("再次点击以确认");
                    flag_if_first_click_for_exit = false;
                    return false;
                }
                // requireActivity().finish();

                MainActivity.am.exit();
            }
            return false;
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String new_str = (String) newValue;
            if (preference.equals(fractal_id_Preference)) {
                final String[] str = getResources().getStringArray(R.array.fractal_id);
                for (int i = 0; i < str.length; i++) {
                    if (str[i].equals(new_str)) {
                        fractal_id = i;
                        if (i == fractal_id_num)
                            fractal_id = -1;
                        else if (i == 10) {
                            iteration_times = 10;
                            pixel_times = 0.2;
                            generate_now_quality = 0.2;
                        }
                        break;
                    }
                }
                flag_should_reload = true;
                return true;
            } else if (preference.equals(generate_mode_Preference)) {
                final String[] str = getResources().getStringArray(R.array.generate_mode);
                for (int i = 0; i < str.length; i++) {
                    if (str[i].equals(new_str)) {
                        generate_mode = i;
                        if (i == generate_mode_max)
                            generate_mode = -1;
                        else if (i == generate_mode_max + 1)
                            generate_mode = -2;
                        break;
                    }
                }
                flag_should_reload = true;
                return true;
            } else if (preference.equals(samples_Preference)) {
                final String[] str = getResources().getStringArray(R.array.samples);
                for (int i = 0; i < str.length; i++) {
                    if (str[i].equals(new_str)) {
                        set_sample(i);
                        break;
                    }
                }
                flag_should_reload = true;
                samples_Preference.setSummary("正在渲染");
                requireActivity().finish();
            } else if (preference.equals(thread_Preference)) {
                final String[] str = getResources().getStringArray(R.array.thread);
                for (int i = 0; i < str.length; i++) {
                    if (str[i].equals(new_str)) {
                        use_thread = i * 2;
                        // i=0(不使用多线程)->use_thread=0
                        // i=1->use_thread=2->2线程
                        if (use_thread < 1)
                            use_thread = 1;
                        return true;
                    }
                }
            } else if (preference.equals(paint_mode_Preference)) {
                final String[] str = getResources().getStringArray(R.array.paint_mode);
                flag_should_reload = true;
                for (int i = 0; i < str.length; i++) {
                    if (str[i].equals(new_str)) {
                        paint_mode = i;
                        return true;
                    }
                }
            }
            return false;
        }
    }

    public static void get_info_from_SaveData(Context c, boolean b) {
        if (!b) {// 正在写入的SharedPreferences为A,则应读取的为B
                 // 详见MainActivity中对flag_use_data的注释
            center_x = SaveData.get_data_double(c, "center_x_true", 0);
            center_y = SaveData.get_data_double(c, "center_y_true", 0);
            scale_times = SaveData.get_data_double(c, "scale_times_true", 0.5);
            pixel_times = 0.1;
            color_reversal = SaveData.get_data_boolean(c, "color_reversal_true", true);
            auto_iteration = SaveData.get_data_boolean(c, "iteration_auto_true", false);
            fractal_id = SaveData.get_data_int(c, "fractal_id_true", 0);
            generate_mode = SaveData.get_data_int(c, "generate_mode_true", 0);
            iteration_times = SaveData.get_data_int(c, "iteration_times_true", 128);
            auto_iteration_max = SaveData.get_data_int(c, "auto_iteration_max_true", 2000);
        } else {
            center_x = SaveData.get_data_double(c, "center_x_false", 0);
            center_y = SaveData.get_data_double(c, "center_y_false", 0);
            scale_times = SaveData.get_data_double(c, "scale_times_false", 0.5);
            pixel_times = 0.1;
            color_reversal = SaveData.get_data_boolean(c, "color_reversal_false", true);
            auto_iteration = SaveData.get_data_boolean(c, "iteration_auto_false", false);
            fractal_id = SaveData.get_data_int(c, "fractal_id_false", 0);
            generate_mode = SaveData.get_data_int(c, "generate_mode_false", 0);
            iteration_times = SaveData.get_data_int(c, "iteration_times_false", 128);
            auto_iteration_max = SaveData.get_data_int(c, "auto_iteration_max_false", 2000);
        }
    }

    public static void set_sample(int i) {
        final int sample_id_0_num = 11;// 如果这里是8 则为0->7 共8个id=0的例子
        final int sample_id_3_num = 3;
        final int sample_id_5_num = 6;
        switch (i) {
        case 0:
            center_x = 0.0016429555369541044;
            center_y = -0.822466530016391;
            scale_times = 1024;
            pixel_times = 1;
            color_reversal = false;
            fractal_id = 0;
            generate_mode = 11;
            iteration_times = 256;
            break;
        case 1:
            center_x = -1.766995739460482;
            center_y = -0.04528191985965333;
            scale_times = 1.37438e11;
            pixel_times = 1;
            color_reversal = true;
            fractal_id = 0;
            generate_mode = 11;
            iteration_times = 256;
            break;
        case 2:
            center_x = 0.3044921875;
            center_y = 0.02265625;
            scale_times = 512;
            pixel_times = 1;
            color_reversal = true;
            fractal_id = 0;
            generate_mode = 8;
            iteration_times = 256;
            break;
        case 3:
            center_x = 0.3604841232299804;
            center_y = 0.6412729263305667;
            scale_times = 1048576;
            pixel_times = 1;
            color_reversal = true;
            fractal_id = 0;
            generate_mode = 18;
            iteration_times = 2048;
            break;
        case 4:
            center_x = 0.3604841929860413;
            center_y = 0.6412729891948403;
            scale_times = 2.147483648e9;
            pixel_times = 1;
            color_reversal = false;
            fractal_id = 0;
            generate_mode = 12;
            iteration_times = 2048;
            break;
        case 5:
            center_x = 0.25260944366455;
            center_y = -0.00021800994873;
            scale_times = 2097152;
            pixel_times = 1;
            color_reversal = true;
            fractal_id = 0;
            generate_mode = 0;
            iteration_times = 4096;
            break;
        case 6:
            center_x = 0.0010398901738078148;
            center_y = -0.8220997150833408;
            scale_times = 1.2E12;
            pixel_times = 1;
            color_reversal = true;
            fractal_id = 0;
            generate_mode = 18;
            iteration_times = 8192;
            break;
        case 7:
            center_x = -0.7506512573242186;
            center_y = -0.020174015553792283;
            scale_times = 90000;
            pixel_times = 1;
            color_reversal = true;
            fractal_id = 0;
            generate_mode = 2;
            iteration_times = 8192;
            break;
        case 8:
            center_x = 0.36796875;
            center_y = -0.14921875;
            scale_times = 8192;
            pixel_times = 1;
            color_reversal = true;
            fractal_id = 0;
            generate_mode = 1;
            iteration_times = 10000;
            break;

        case 9:
            center_x = -1.7496286153793283;
            center_y = 0;
            scale_times = 8388608;
            pixel_times = 1;
            color_reversal = true;
            fractal_id = 0;
            generate_mode = 6;
            iteration_times = 500;
            break;
        case 10:
            center_x = -1.4082885742185;
            center_y = 0.1366455078125;
            scale_times = 16384;
            pixel_times = 1;
            color_reversal = true;
            fractal_id = 0;
            generate_mode = 8;
            iteration_times = 1024;
            break;

        case sample_id_0_num:
            center_x = 0.14709487090110732;
            center_y = -0.8748064100742339;
            scale_times = 1234;
            pixel_times = 1;
            color_reversal = true;
            fractal_id = 3;
            generate_mode = 8;
            iteration_times = 200;
            break;
        case sample_id_0_num + 1:
            center_x = -0.81867175;
            center_y = -0.5589480412666;
            scale_times = 512;
            pixel_times = 1;
            color_reversal = true;
            fractal_id = 3;
            generate_mode = 8;
            iteration_times = 200;
            break;
        case sample_id_0_num + 2:
            center_x = -0.0859375;
            center_y = -1.05417;
            scale_times = 192;
            pixel_times = 1;
            color_reversal = false;
            fractal_id = 3;
            generate_mode = 1;
            iteration_times = 200;
            break;
        case sample_id_0_num + sample_id_3_num:
            center_x = -1.75;
            center_y = -0.03;
            scale_times = 24;
            pixel_times = 1;
            color_reversal = true;
            fractal_id = 5;
            generate_mode = 0;
            iteration_times = 128;
            break;
        case sample_id_0_num + sample_id_3_num + 1:
            center_x = -1.861344696144;
            center_y = -0.003114566940348595;
            scale_times = 6.87E10;
            pixel_times = 1;
            color_reversal = false;
            fractal_id = 5;
            generate_mode = 2;
            iteration_times = 128;
            break;
        case sample_id_0_num + sample_id_3_num + 2:
            center_x = -1.8101560225213849;
            center_y = -0.00364331702183375;
            scale_times = 5E12;
            pixel_times = 1;
            color_reversal = true;
            fractal_id = 5;
            generate_mode = 0;
            iteration_times = 128;
            break;
        case sample_id_0_num + sample_id_3_num + 3:
            center_x = -1.7783556904313647;
            center_y = -0.05607528583630487;
            scale_times = 8E8;
            pixel_times = 1;
            color_reversal = true;
            fractal_id = 5;
            generate_mode = 2;
            iteration_times = 256;
            break;
        case sample_id_0_num + sample_id_3_num + 4:
            center_x = -1.9433338235357482;
            center_y = -0.0020569693544075615;
            scale_times = 1E12;
            pixel_times = 1;
            color_reversal = true;
            fractal_id = 5;
            generate_mode = 0;
            iteration_times = 128;
            break;
        case sample_id_0_num + sample_id_3_num + 5:
            center_x = -1.9433338235660342;
            center_y = -0.002056970956755327;
            scale_times = 5E8;
            pixel_times = 1;
            color_reversal = true;
            fractal_id = 5;
            generate_mode = 2;
            iteration_times = 128;
            break;

        case sample_id_0_num + sample_id_3_num + sample_id_5_num:
            center_x = -1.29175;
            center_y = -0.02152;
            scale_times = 150;
            pixel_times = 1;
            color_reversal = false;
            fractal_id = 6;
            generate_mode = 4;
            iteration_times = 200;
            break;
        case sample_id_0_num + sample_id_3_num + sample_id_5_num + 1:
            center_x = -1.197229619137943;
            center_y = -0.1428861228865572;
            scale_times = 8192;
            pixel_times = 1;
            color_reversal = false;
            fractal_id = 6;
            generate_mode = 6;
            iteration_times = 512;
            break;
        default:
            break;
        }
        auto_iteration = true;
        auto_iteration_max = 2000;
    }

    //////////////////////////////////
    /// onclick函数
    public void settings_confirm(View v) {
        find_view();
        boolean flag_return = false;
        flag_should_reload = true;
        // if(!"java.lang.NumberFormatException: empty String".equals(e.toString()))
        // 显然这里的try,catch不应该这么搞，但是不知道标准的用法是怎样的，暂时将就下

        try {
            center_x = Double.parseDouble(et1.getText().toString());
            et1.setTextColor(ContextCompat.getColor(context,R.color.text_color));
        } catch (NumberFormatException e) {
            if (!"java.lang.NumberFormatException: empty String".equals(e.toString())) {
                flag_return = true;
                et1.setTextColor(ContextCompat.getColor(this,R.color.red));
            }
        }

        try {
            center_y = Double.parseDouble(et2.getText().toString());
            et2.setTextColor(ContextCompat.getColor(context,R.color.text_color));
        } catch (NumberFormatException e) {
            if (!"java.lang.NumberFormatException: empty String".equals(e.toString())) {
                flag_return = true;
                et2.setTextColor(ContextCompat.getColor(this,R.color.red));
            }
        }

        try {
            double d = Double.parseDouble(et3.getText().toString());
            if (d == 0 || d > scale_max) {
                et3.setTextColor(ContextCompat.getColor(this,R.color.red));
                flag_return = true;
            } else {
                scale_times = d;
                et3.setTextColor(ContextCompat.getColor(context,R.color.text_color));
            }
        } catch (NumberFormatException e) {
            if (!"java.lang.NumberFormatException: empty String".equals(e.toString())) {
                flag_return = true;
                et3.setTextColor(ContextCompat.getColor(this,R.color.red));
            }
        }

        try {
            double d = Double.parseDouble(et4.getText().toString());
            if (d > 3 || d < 0.1) {
                et4.setTextColor(ContextCompat.getColor(this,R.color.red));
                flag_return = true;
            } else {
                pixel_times = d;
                et4.setTextColor(ContextCompat.getColor(context,R.color.text_color));
            }
        } catch (NumberFormatException e) {
            if (!"java.lang.NumberFormatException: empty String".equals(e.toString())) {
                et4.setTextColor(ContextCompat.getColor(this,R.color.red));
                flag_return = true;
            }
        }

        try {
            double d = Double.parseDouble(et5.getText().toString());
            if (d > 1 || d < 0.1) {
                et5.setTextColor(ContextCompat.getColor(this,R.color.red));
                flag_return = true;
            } else {
                generate_now_quality = d;
                et5.setTextColor(ContextCompat.getColor(context,R.color.text_color));
            }
        } catch (NumberFormatException e) {
            if (!"java.lang.NumberFormatException: empty String".equals(e.toString())) {
                et5.setTextColor(ContextCompat.getColor(this,R.color.red));
                flag_return = true;
            }
        }

        try {
            int i = Integer.parseInt(et6.getText().toString());
            if (i <= 0) {
                et6.setTextColor(ContextCompat.getColor(this,R.color.red));
                flag_return = true;
            } else {
                iteration_times = i;
                et6.setTextColor(ContextCompat.getColor(context,R.color.text_color));
            }
        } catch (NumberFormatException e) {
            // 离谱，这里似乎是因为这里是android:inputType="number"而非android:inputType="numberDecimal"
            // 所以错误类型不一样
            if (!"java.lang.NumberFormatException: For input string: \"\"".equals(e.toString())) {
                et6.setTextColor(ContextCompat.getColor(this,R.color.red));
                flag_return = true;
            }
        }

        try {
            int i = Integer.parseInt(et7.getText().toString());
            if (i <= 0) {
                et7.setTextColor(ContextCompat.getColor(this,R.color.red));
                flag_return = true;
            } else {
                auto_iteration_max = i;
                et7.setTextColor(ContextCompat.getColor(context,R.color.text_color));
            }
        } catch (NumberFormatException e) {
            if (!"java.lang.NumberFormatException: For input string: \"\"".equals(e.toString())) {
                et7.setTextColor(ContextCompat.getColor(this,R.color.red));
                flag_return = true;
            }
        }

        if (flag_return)
            return;

        ((Button) v).setText("正在渲染");
        finish();
    }

    @SuppressLint("SetTextI18n")
    public void settings_load(View v) {
        find_view();
        et1.setText(Double.toString(center_x));
        et2.setText(Double.toString(center_y));
        et3.setText(Double.toString(scale_times));
        et4.setText(Double.toString(pixel_times));
        et5.setText(Double.toString(generate_now_quality));
        et6.setText(Integer.toString(iteration_times));
        et7.setText(Integer.toString(auto_iteration_max));
    }

    public void settings_clear(View v) {
        find_view();
        et1.setText("");
        et2.setText("");
        et3.setText("");
        et4.setText("");
        et5.setText("");
        et6.setText("");
        et7.setText("");
    }

    private void find_view() {
        // 由于这里的控件都是嵌在preferences里面的，如果在oncreate用实例化会出错
        // 似乎是因为oncreate的时候这些控件还没创建好？
        et1 = findViewById(R.id.preferences_edit1);
        et2 = findViewById(R.id.preferences_edit2);
        et3 = findViewById(R.id.preferences_edit3);
        et4 = findViewById(R.id.preferences_edit4);
        et5 = findViewById(R.id.preferences_edit5);
        et6 = findViewById(R.id.preferences_edit6);
        et7 = findViewById(R.id.preferences_edit7);
    }
}