package com.zjh.fractal;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);//隐藏状态栏

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }


    public static class SettingsFragment extends PreferenceFragmentCompat  implements Preference.OnPreferenceClickListener,Preference.OnPreferenceChangeListener {
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

        boolean first_click=true;
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            getPreferenceManager().setSharedPreferencesName("fractal_settings_Preferences");
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            MainActivity.flag_should_reload=false;

            night_mode_Preference           =findPreference("night_mode_Preference");
            fractal_id_Preference           =findPreference("fractal_id_Preference");
            generate_mode_Preference        =findPreference("generate_mode_Preference");
            color_reverse_Preference        =findPreference("color_reverse_Preference");
            read_data_Preference            =findPreference("read_data_Preference");
            open_picture_Preference         =findPreference("open_picture_Preference");
            settings_edit_text_Preference   =findPreference("settings_edit_text_Preference");
            samples_Preference              =findPreference("samples_Preference");
            about_fractal_Preference        =findPreference("about_fractal_Preference");
            thread_Preference               =findPreference("thread_Preference");
            auto_iteration_Preference       =findPreference("auto_iteration_Preference");

            night_mode_Preference.setOnPreferenceClickListener(this);
            color_reverse_Preference.setOnPreferenceClickListener(this);
            read_data_Preference.setOnPreferenceClickListener(this);
            open_picture_Preference.setOnPreferenceClickListener(this);
            about_fractal_Preference.setOnPreferenceClickListener(this);
            auto_iteration_Preference.setOnPreferenceClickListener(this);
            fractal_id_Preference.setOnPreferenceChangeListener(this);
            generate_mode_Preference.setOnPreferenceChangeListener(this);
            samples_Preference.setOnPreferenceChangeListener(this);
            thread_Preference.setOnPreferenceChangeListener(this);
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            if(preference.equals(night_mode_Preference)){
                if(AppCompatDelegate.getDefaultNightMode()==AppCompatDelegate.MODE_NIGHT_NO) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                }
                else{
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
                MainActivity.flag_should_load_from_storage=true;
            }
            else if(preference.equals(color_reverse_Preference)){
                MainActivity.color_reversal=!MainActivity.color_reversal;
                MainActivity.flag_should_reload=true;
            }
            else if(preference.equals(auto_iteration_Preference)){
                MainActivity.auto_iteration=!MainActivity.auto_iteration;
                MainActivity.flag_should_reload=true;
            }
            else if(preference.equals(read_data_Preference)){
                //由于第一次进入程序时会写入值到scale_times_false中，此时scale_times_true为空值，
                // 所以用scale_times_true来判断是不是第一次进程序
                if(SaveData.get_data_double(getContext(),"scale_times_true")==0f){
                    preference.setSummary("没有已保存的参数");
                    return false;
                }
                if(first_click){
                    preference.setSummary("再次点击以确认");
                    first_click=false;
                    return false;
                }
                preference.setSummary("正在渲染");
                get_info_from_SaveData(getContext(),MainActivity.flag_use_data);
                MainActivity.flag_should_reload=true;
                getActivity().finish();
            }
           else if(preference.equals(open_picture_Preference)){
               //轮子:打开图片
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Bitmap bitmap =ImageProcess.GetLocalBitmap(MainActivity.file_path);;
                String uriString = MediaStore.Images.Media.insertImage(getContext().getContentResolver(),bitmap,null,null);
                Uri uri = Uri.parse(uriString);
                intent.setDataAndType(uri,"image/*");
                startActivity(intent);
            }
           else if(preference.equals(about_fractal_Preference)){
                about_fractal_Preference.setSummary(R.string.about);
                about_fractal_Preference.setSelectable(false);
            }
            return false;
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String new_str=(String) newValue;
            if(preference.equals(fractal_id_Preference)){
                final String[] str= getResources().getStringArray(R.array.fractal_id);
                for(int i=0;i<str.length;i++){
                    if(str[i].equals(new_str)){
                        MainActivity.fractal_id =i;
                        if(i==MainActivity.fractal_id_num) MainActivity.fractal_id =-1;
                        else if(i==10){
                            MainActivity.iteration_times=10;
                            MainActivity.pixel_times=0.2;
                            MainActivity.generate_now_quality=0.2;
                        }
                        break;
                    }
                }
                MainActivity.flag_should_reload=true;
                return true;
            }
            else if(preference.equals(generate_mode_Preference)){
                final String[] str= getResources().getStringArray(R.array.generate_mode);
                for(int i=0;i<str.length;i++){
                    if(str[i].equals(new_str)){
                        MainActivity.generate_mode =i;
                        if(i==MainActivity.generate_mode_max) MainActivity.generate_mode =-1;
                        else if(i==MainActivity.generate_mode_max+1) MainActivity.generate_mode =-2;
                        break;
                    }
                }
                MainActivity.flag_should_reload=true;
                return true;
            }
            else if(preference.equals(samples_Preference)) {
                final String[] str= getResources().getStringArray(R.array.samples);
                for(int i=0;i<str.length;i++){
                    if(str[i].equals(new_str)){
                        set_sample(i);
                        break;
                    }
                }
                MainActivity.flag_should_reload=true;
                samples_Preference.setSummary("正在渲染");
                getActivity().finish();
            }
            else if (preference.equals(thread_Preference)) {
                final String[] str= getResources().getStringArray(R.array.thread);
                for(int i=0;i<str.length;i++){
                    if(str[i].equals(new_str)){
                        MainActivity.use_thread=i*2;
                        //i=0(不使用多线程)->use_thread=0
                        //i=1->use_thread=2->2线程
                        return true;
                    }
                }
            }
            return false;
        }
    }

    public static void get_info_from_SaveData(Context c,boolean b){
        if(!b) {//正在写入的SharedPreferences为A,则应读取的为B
                //详见MainActivity中对flag_use_data的注释
            MainActivity.center_x = SaveData.get_data_double(c, "center_x_true");
            MainActivity.center_y = SaveData.get_data_double(c, "center_y_true");
            MainActivity.scale_times = SaveData.get_data_double(c, "scale_times_true");
            MainActivity.pixel_times = 0.5;
            //MainActivity.generate_now_quality=SaveData.get_data_double(c,"generate_now_quality");
            MainActivity.generate_now_quality = 0.3;
            MainActivity.color_reversal = SaveData.get_data_boolean(c, "color_reversal_true");
            MainActivity.auto_iteration = SaveData.get_data_boolean(c,"iteration_auto_true");
            MainActivity.fractal_id = SaveData.get_data_int(c, "fractal_id_true");
            MainActivity.generate_mode = SaveData.get_data_int(c, "generate_mode_true");
            MainActivity.iteration_times = SaveData.get_data_int(c, "iteration_times_true");
            MainActivity.auto_iteration_max =SaveData.get_data_int(c, "auto_iteration_max_true");
        }
        else{
            MainActivity.center_x = SaveData.get_data_double(c, "center_x_false");
            MainActivity.center_y = SaveData.get_data_double(c, "center_y_false");
            MainActivity.scale_times = SaveData.get_data_double(c, "scale_times_false");
            MainActivity.pixel_times = 0.5;
            //MainActivity.generate_now_quality=SaveData.get_data_double(c,"generate_now_quality");
            MainActivity.generate_now_quality = 0.3;
            MainActivity.color_reversal = SaveData.get_data_boolean(c, "color_reversal_false");
            MainActivity.auto_iteration = SaveData.get_data_boolean(c,"iteration_auto_false");
            MainActivity.fractal_id = SaveData.get_data_int(c, "fractal_id_false");
            MainActivity.generate_mode = SaveData.get_data_int(c, "generate_mode_false");
            MainActivity.iteration_times = SaveData.get_data_int(c, "iteration_times_false");
            MainActivity.auto_iteration_max =SaveData.get_data_int(c, "auto_iteration_max_false");
        }
    }

    public static void set_sample(int i){
        final int sample_id_0_num=11;//如果这里是8 则为0->7 共8个id=0的例子
        final int sample_id_3_num=3;
        final int sample_id_5_num=6;
        switch (i){
            case 0:
                MainActivity.center_x = 0.0016429555369541044;
                MainActivity.center_y = -0.822466530016391;
                MainActivity.scale_times = 1024;
                MainActivity.pixel_times = 1;
                MainActivity.color_reversal = false;
                MainActivity.fractal_id = 0;
                MainActivity.generate_mode = 11;
                MainActivity.iteration_times = 256;
                break;
            case 1:
                MainActivity.center_x = -1.766995739460482;
                MainActivity.center_y = -0.04528191985965333;
                MainActivity.scale_times = 1.37438e11;
                MainActivity.pixel_times = 1;
                MainActivity.color_reversal = true;
                MainActivity.fractal_id = 0;
                MainActivity.generate_mode = 11;
                MainActivity.iteration_times = 256;
                break;
            case 2:
                MainActivity.center_x = 0.3044921875;
                MainActivity.center_y = 0.02265625;
                MainActivity.scale_times = 512;
                MainActivity.pixel_times = 1;
                MainActivity.color_reversal = true;
                MainActivity.fractal_id = 0;
                MainActivity.generate_mode = 8;
                MainActivity.iteration_times = 256;
                break;
            case 3:
                MainActivity.center_x = 0.3604841232299804;
                MainActivity.center_y = 0.6412729263305667;
                MainActivity.scale_times = 1048576;
                MainActivity.pixel_times = 1;
                MainActivity.color_reversal = true;
                MainActivity.fractal_id = 0;
                MainActivity.generate_mode = 18;
                MainActivity.iteration_times = 2048;
                break;
            case 4:
                MainActivity.center_x = 0.3604841929860413;
                MainActivity.center_y = 0.6412729891948403;
                MainActivity.scale_times = 2.147483648e9;
                MainActivity.pixel_times = 1;
                MainActivity.color_reversal = false;
                MainActivity.fractal_id = 0;
                MainActivity.generate_mode = 12;
                MainActivity.iteration_times = 2048;
                break;
            case 5:
                MainActivity.center_x = 0.25260944366455;
                MainActivity.center_y = -0.00021800994873;
                MainActivity.scale_times = 2097152;
                MainActivity.pixel_times = 1;
                MainActivity.color_reversal = true;
                MainActivity.fractal_id = 0;
                MainActivity.generate_mode = 0;
                MainActivity.iteration_times = 4096;
                break;
            case 6:
                MainActivity.center_x = 0.0010398901738078148;
                MainActivity.center_y = -0.8220997150833408;
                MainActivity.scale_times = 1.2E12;
                MainActivity.pixel_times = 1;
                MainActivity.color_reversal = true;
                MainActivity.fractal_id = 0;
                MainActivity.generate_mode = 18;
                MainActivity.iteration_times = 8192;
                break;
            case 7:
                MainActivity.center_x = -0.7506512573242186;
                MainActivity.center_y = -0.020174015553792283;
                MainActivity.scale_times = 90000;
                MainActivity.pixel_times = 1;
                MainActivity.color_reversal = true;
                MainActivity.fractal_id = 0;
                MainActivity.generate_mode = 2;
                MainActivity.iteration_times = 8192;
                break;
            case 8:
                MainActivity.center_x = 0.36796875;
                MainActivity.center_y = -0.14921875;
                MainActivity.scale_times = 8192;
                MainActivity.pixel_times = 1;
                MainActivity.color_reversal = true;
                MainActivity.fractal_id = 0;
                MainActivity.generate_mode = 1;
                MainActivity.iteration_times = 10000;
                break;

            case 9:
                MainActivity.center_x = -1.7496286153793283;
                MainActivity.center_y = 0;
                MainActivity.scale_times = 8388608;
                MainActivity.pixel_times = 1;
                MainActivity.color_reversal = true;
                MainActivity.fractal_id = 0;
                MainActivity.generate_mode = 6;
                MainActivity.iteration_times = 500;
                break;
            case 10:
                MainActivity.center_x = -1.4082885742185;
                MainActivity.center_y = 0.1366455078125;
                MainActivity.scale_times = 16384;
                MainActivity.pixel_times = 1;
                MainActivity.color_reversal = true;
                MainActivity.fractal_id = 0;
                MainActivity.generate_mode = 8;
                MainActivity.iteration_times = 1024;
                break;

            case sample_id_0_num:
                MainActivity.center_x = 0.14709487090110732;
                MainActivity.center_y = -0.8748064100742339;
                MainActivity.scale_times = 1234;
                MainActivity.pixel_times = 1;
                MainActivity.color_reversal = true;
                MainActivity.fractal_id = 3;
                MainActivity.generate_mode = 8;
                MainActivity.iteration_times = 200;
                break;
            case sample_id_0_num+1:
                MainActivity.center_x = -0.81867175;
                MainActivity.center_y = -0.5589480412666;
                MainActivity.scale_times = 512;
                MainActivity.pixel_times = 1;
                MainActivity.color_reversal = true;
                MainActivity.fractal_id = 3;
                MainActivity.generate_mode = 8;
                MainActivity.iteration_times = 200;
                break;
            case sample_id_0_num+2:
                MainActivity.center_x = -0.0859375;
                MainActivity.center_y = -1.05417;
                MainActivity.scale_times = 192;
                MainActivity.pixel_times = 1;
                MainActivity.color_reversal = false;
                MainActivity.fractal_id = 3;
                MainActivity.generate_mode = 1;
                MainActivity.iteration_times = 200;
                break;
            case sample_id_0_num+sample_id_3_num:
                MainActivity.center_x = -1.75;
                MainActivity.center_y = -0.03;
                MainActivity.scale_times = 24;
                MainActivity.pixel_times = 1;
                MainActivity.color_reversal = true;
                MainActivity.fractal_id = 5;
                MainActivity.generate_mode = 0;
                MainActivity.iteration_times = 128;
                break;
            case sample_id_0_num+sample_id_3_num+1:
                MainActivity.center_x = -1.861344696144;
                MainActivity.center_y = -0.003114566940348595;
                MainActivity.scale_times = 6.87E10;
                MainActivity.pixel_times = 1;
                MainActivity.color_reversal = false;
                MainActivity.fractal_id = 5;
                MainActivity.generate_mode = 2;
                MainActivity.iteration_times = 128;
                break;
            case sample_id_0_num+sample_id_3_num+2:
                MainActivity.center_x = -1.8101560225213849;
                MainActivity.center_y = -0.00364331702183375;
                MainActivity.scale_times = 5E12;
                MainActivity.pixel_times = 1;
                MainActivity.color_reversal = true;
                MainActivity.fractal_id = 5;
                MainActivity.generate_mode = 0;
                MainActivity.iteration_times = 128;
                break;
            case sample_id_0_num+sample_id_3_num+3:
                MainActivity.center_x = -1.7783556904313647;
                MainActivity.center_y = -0.05607528583630487;
                MainActivity.scale_times = 8E8;
                MainActivity.pixel_times = 1;
                MainActivity.color_reversal = true;
                MainActivity.fractal_id = 5;
                MainActivity.generate_mode = 2;
                MainActivity.iteration_times = 256;
                break;
            case sample_id_0_num+sample_id_3_num+4:
                MainActivity.center_x = -1.9433338235357482;
                MainActivity.center_y = -0.0020569693544075615;
                MainActivity.scale_times = 1E12;
                MainActivity.pixel_times = 1;
                MainActivity.color_reversal = true;
                MainActivity.fractal_id = 5;
                MainActivity.generate_mode = 0;
                MainActivity.iteration_times = 128;
                break;
            case sample_id_0_num+sample_id_3_num+5:
                MainActivity.center_x = -1.9433338235660342;
                MainActivity.center_y = -0.002056970956755327;
                MainActivity.scale_times = 5E8;
                MainActivity.pixel_times = 1;
                MainActivity.color_reversal = true;
                MainActivity.fractal_id = 5;
                MainActivity.generate_mode = 2;
                MainActivity.iteration_times = 128;
                break;

            case sample_id_0_num +sample_id_3_num+ sample_id_5_num:
                MainActivity.center_x = -1.29175;
                MainActivity.center_y = -0.02152;
                MainActivity.scale_times = 150;
                MainActivity.pixel_times = 1;
                MainActivity.color_reversal = false;
                MainActivity.fractal_id = 6;
                MainActivity.generate_mode = 4;
                MainActivity.iteration_times = 200;
                break;
            case sample_id_0_num +sample_id_3_num+ sample_id_5_num+1:
                MainActivity.center_x = -1.197229619137943;
                MainActivity.center_y = -0.1428861228865572;
                MainActivity.scale_times = 8192;
                MainActivity.pixel_times = 1;
                MainActivity.color_reversal = false;
                MainActivity.fractal_id = 6;
                MainActivity.generate_mode = 6;
                MainActivity.iteration_times = 512;
                break;
            default:
                break;
        }
    }

    //////////////////////////////////
    ///onclick函数
    public void settings_confirm(View v){
        boolean flag_return=false;
        MainActivity.flag_should_reload=true;
        EditText et1=findViewById(R.id.preferences_edit1);
        EditText et2=findViewById(R.id.preferences_edit2);
        EditText et3=findViewById(R.id.preferences_edit3);
        EditText et4=findViewById(R.id.preferences_edit4);
        EditText et5=findViewById(R.id.preferences_edit5);
        EditText et6=findViewById(R.id.preferences_edit6);
        EditText et7=findViewById(R.id.preferences_edit7);

        //if(!"java.lang.NumberFormatException: empty String".equals(e.toString()))
        //显然这里的try,catch不应该这么搞，但是不知道标准的用法是怎样的，暂时将就下

        try {
            MainActivity.center_x=Double.parseDouble(et1.getText().toString());
            et1.setTextColor(getResources().getColor(R.color.text_color));
        }
        catch(NumberFormatException e){
            if(!"java.lang.NumberFormatException: empty String".equals(e.toString())) {
                flag_return=true;
                et1.setTextColor(getColor(R.color.red));
            }
        }

        try {
            MainActivity.center_y=Double.parseDouble(et2.getText().toString());
            et2.setTextColor(getResources().getColor(R.color.text_color));
        }
        catch(NumberFormatException e){
            if(!"java.lang.NumberFormatException: empty String".equals(e.toString())) {
                flag_return=true;
                et2.setTextColor(getColor(R.color.red));
            }
        }


        try {
            double d=Double.parseDouble(et3.getText().toString());
            if(d==0||d> MainActivity.scale_max){
                et3.setTextColor(getColor(R.color.red));
                flag_return=true;
            }
            else {
                MainActivity.scale_times = d;
                et3.setTextColor(getResources().getColor(R.color.text_color));
            }
        }
        catch(NumberFormatException e){
            if(!"java.lang.NumberFormatException: empty String".equals(e.toString())) {
                flag_return=true;
                et3.setTextColor(getColor(R.color.red));
            }
        }


        try {
            double d=Double.parseDouble(et4.getText().toString());
            if(d>3||d<0.1){
                et4.setTextColor(getColor(R.color.red));
                flag_return=true;
            }
            else {
                MainActivity.pixel_times = d;
                et4.setTextColor(getResources().getColor(R.color.text_color));
            }
        }
        catch(NumberFormatException e){
            if(!"java.lang.NumberFormatException: empty String".equals(e.toString())) {
                et4.setTextColor(getColor(R.color.red));
                flag_return=true;
            }
        }

        try {
            double d=Double.parseDouble(et5.getText().toString());
            if(d>1||d<0.1){
                et5.setTextColor(getColor(R.color.red));
                flag_return=true;
            }
            else {
                MainActivity.generate_now_quality = d;
                et5.setTextColor(getResources().getColor(R.color.text_color));
            }
        }
        catch(NumberFormatException e){
            if(!"java.lang.NumberFormatException: empty String".equals(e.toString())) {
                et5.setTextColor(getColor(R.color.red));
                flag_return=true;
            }
        }

        try {
            int i=Integer.parseInt(et6.getText().toString());
            if(i<=0||i>MainActivity.iteration_max){
                et6.setTextColor(getColor(R.color.red));
                flag_return=true;
            }
            else {
                MainActivity.iteration_times = i;
                et6.setTextColor(getResources().getColor(R.color.text_color));
            }
        }
        catch(NumberFormatException e){
            if(!"java.lang.NumberFormatException: For input string: \"\"".equals(e.toString())) {
                et6.setTextColor(getColor(R.color.red));
                flag_return=true;
            }
        }

        try {
            int i=Integer.parseInt(et7.getText().toString());
            if(i<=0||i>MainActivity.iteration_max){
                et7.setTextColor(getColor(R.color.red));
                flag_return=true;
            }
            else {
                MainActivity.auto_iteration_max = i;
                et7.setTextColor(getResources().getColor(R.color.text_color));
            }
        }
        catch(NumberFormatException e){
            if(!"java.lang.NumberFormatException: For input string: \"\"".equals(e.toString())) {
                et7.setTextColor(getColor(R.color.red));
                flag_return=true;
            }
        }

        if(flag_return) return;

        ((Button)v).setText("正在渲染");
        finish();
    }

    public void settings_load(View v){
        EditText et1=findViewById(R.id.preferences_edit1);
        EditText et2=findViewById(R.id.preferences_edit2);
        EditText et3=findViewById(R.id.preferences_edit3);
        EditText et4=findViewById(R.id.preferences_edit4);
        EditText et5=findViewById(R.id.preferences_edit5);
        EditText et6=findViewById(R.id.preferences_edit6);
        EditText et7=findViewById(R.id.preferences_edit7);

        et1.setText(Double.toString(MainActivity.center_x));
        et2.setText(Double.toString(MainActivity.center_y));
        et3.setText(Double.toString(MainActivity.scale_times));
        et4.setText(Double.toString(MainActivity.pixel_times));
        et5.setText(Double.toString(MainActivity.generate_now_quality));
        et6.setText(Integer.toString(MainActivity.iteration_times));
        et7.setText(Integer.toString(MainActivity.auto_iteration_max));
    }

    public void settings_clear(View v){
        EditText et1=findViewById(R.id.preferences_edit1);
        EditText et2=findViewById(R.id.preferences_edit2);
        EditText et3=findViewById(R.id.preferences_edit3);
        EditText et4=findViewById(R.id.preferences_edit4);
        EditText et5=findViewById(R.id.preferences_edit5);
        EditText et6=findViewById(R.id.preferences_edit6);
        EditText et7=findViewById(R.id.preferences_edit7);

        et1.setText("");
        et2.setText("");
        et3.setText("");
        et4.setText("");
        et5.setText("");
        et6.setText("");
        et7.setText("");
    }
}