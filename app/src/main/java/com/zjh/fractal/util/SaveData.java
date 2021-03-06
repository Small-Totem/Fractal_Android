package com.zjh.fractal.util;
import android.content.Context;
import android.content.SharedPreferences;

public class SaveData {
    public static SharedPreferences share(Context context){
        return context.getSharedPreferences("info", Context.MODE_PRIVATE);
    }

    static void putDouble(final SharedPreferences.Editor edit, final String key, final double value) {
        edit.putLong(key, Double.doubleToRawLongBits(value));
    }

    static double getDouble(final SharedPreferences prefs, final String key, final double default_value) {
        if (!prefs.contains(key))
            return default_value;
        return Double.longBitsToDouble(prefs.getLong(key, 0));
    }

    public static void save_data(
            double center_x, double center_y, double scale_times, boolean color_reversal,
            int fractal_id, int generate_mode, int iteration_times,
            boolean iteration_auto, int auto_iteration_max,
            Context context,boolean flag_use_data){
        SharedPreferences.Editor e = share(context).edit();
        if(flag_use_data){
            putDouble(e,"center_x_true",center_x);
            putDouble(e,"center_y_true",center_y);
            putDouble(e,"scale_times_true",scale_times);
            e.putBoolean("color_reversal_true",color_reversal);
            e.putBoolean("iteration_auto_true",iteration_auto);
            e.putInt("fractal_id_true",fractal_id);
            e.putInt("generate_mode_true",generate_mode);
            e.putInt("iteration_times_true",iteration_times);
            e.putInt("auto_iteration_max_true",auto_iteration_max);
        }
        else {
            putDouble(e,"center_x_false",center_x);
            putDouble(e,"center_y_false",center_y);
            putDouble(e,"scale_times_false",scale_times);
            e.putBoolean("color_reversal_false",color_reversal);
            e.putBoolean("iteration_auto_false",iteration_auto);
            e.putInt("fractal_id_false",fractal_id);
            e.putInt("generate_mode_false",generate_mode);
            e.putInt("iteration_times_false",iteration_times);
            e.putInt("auto_iteration_max_false",auto_iteration_max);
        }
        e.apply();
    }

    public static double get_data_double(Context context,String key,final double default_value){
        SharedPreferences s=share(context);
        return getDouble(s,key,default_value);
    }

    public static int get_data_int(Context context,String key,final int default_value){
        SharedPreferences s=share(context);
        return s.getInt(key,default_value);
    }

    public static boolean get_data_boolean(Context context,String key,final boolean default_value){
        SharedPreferences s=share(context);
        return s.getBoolean(key,default_value);
    }
    public static void set_data_boolean(Context context,String key,boolean b){
        share(context).edit().putBoolean(key,b).apply();
    }
}
