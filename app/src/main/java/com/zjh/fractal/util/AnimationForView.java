package com.zjh.fractal.util;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;

public class AnimationForView {

    public static void load_view(final View v,int time,float alpha){
        AlphaAnimation aa = new AlphaAnimation(0f,alpha);
        aa.setDuration(time);//动画持续时间
        v.setVisibility(View.VISIBLE);
        v.startAnimation(aa);
    }

    public static void close_view(final View v,int time,float alpha,int visibility){
        AlphaAnimation aa = new AlphaAnimation(alpha,0f);
        aa.setDuration(time);
        v.startAnimation(aa);
        aa.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationEnd(Animation arg0) {
                v.setVisibility(visibility);
            }
            public void onAnimationRepeat(Animation animation) {}
            public void onAnimationStart(Animation animation) {}
        });
    }

    public static void alpha_animation(final View v,int time,float alpha1,float alpha2,float end_alpha){
        AlphaAnimation aa = new AlphaAnimation(alpha1,alpha2);
        aa.setDuration(time);
        v.startAnimation(aa);
        aa.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                v.setAlpha(end_alpha);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        //aa.setFillAfter(true);
    }

    public static void transition_animation(ImageView image_view, Context context, Bitmap bitmap, int image_change_time){
        if(bitmap==null)return;
        if(image_view.getDrawable()==null){
            AnimationForView.load_view(image_view,image_change_time,1);
            image_view.setImageBitmap(bitmap);
        }
        else{
            Drawable old;
            if(image_view.getDrawable() instanceof TransitionDrawable) {
                //这里踩了个坑 已解决
                //参见https://blog.csdn.net/shaw1994/article/details/46846075
                old=((TransitionDrawable) image_view.getDrawable()).getDrawable(1);
            }
            else {
                old= image_view.getDrawable();
            }
            TransitionDrawable td = new TransitionDrawable(new Drawable[] {
                    old,
                    new BitmapDrawable(context.getResources(), bitmap)
            });
            image_view.setImageDrawable(td);
            td.startTransition(image_change_time);
        }
    }
}
