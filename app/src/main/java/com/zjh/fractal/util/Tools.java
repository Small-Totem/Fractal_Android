package com.zjh.fractal.util;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Environment;
import com.zjh.fractal.MainActivity;

import java.io.File;
import java.io.FileInputStream;

import static com.zjh.fractal.view.ZLogView.info_status_error;

public class Tools {
    public static Bitmap Negative(Bitmap bm){
        if(bm==null)return null;

        int Width = bm.getWidth();
        int Height = bm.getHeight();
        int color;
        int r,g,b,a;

        Bitmap bitmap = Bitmap.createBitmap(Width, Height, Bitmap.Config.ARGB_8888);

        int[] oldPx = new int[Width * Height];
        int[] newPx = new int[Width * Height];

        bm.getPixels(oldPx, 0, Width, 0, 0, Width, Height);

        for(int i = 0; i < Width * Height; i++){
            color = oldPx[i];

            r = Color.red(color);
            g = Color.green(color);
            b = Color.blue(color);
            a = Color.alpha(color);

            //各通道值颜色值反转
            r = 255 - r;
            g = 255 - g;
            b = 255 - b;

            //检测各通道值是否超出范围
            if(r > 255){
                r = 255;
            }else if(r < 0){
                r = 0;
            }

            if(g > 255){
                g = 255;
            }else if(g < 0){
                g = 0;
            }

            if(b > 255){
                b = 255;
            }else if(b < 0){
                b = 0;
            }
            newPx[i] = Color.argb(a, r, g, b);
        }
        bitmap.setPixels(newPx, 0, Width, 0, 0, Width, Height);
        return bitmap;
    }

    public static String getFileRoot(Context context) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File external = context.getExternalFilesDir(null);
            if (external != null) {
                return external.getAbsolutePath();
            }
        }
        return context.getFilesDir().getAbsolutePath();
    }

    public static Bitmap GetLocalBitmap(String url, MainActivity m) {
        try {
            FileInputStream fis = new FileInputStream(url);
            return BitmapFactory.decodeStream(fis); /// 把流转化为Bitmap图片
        } catch (Exception e) {
            e.printStackTrace();
            m.generate_info_add(info_status_error, "文件读取错误\n"+e.toString());
            return null;
        }
    }
}