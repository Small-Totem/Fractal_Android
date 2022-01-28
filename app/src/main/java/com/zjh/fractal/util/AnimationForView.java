package com.zjh.fractal.util;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

public class AnimationForView {

    public static void load_view(final View v,int time,float end_alpha){
        AlphaAnimation aa = new AlphaAnimation(0f,end_alpha);
        aa.setDuration(time);//动画持续时间
        v.setVisibility(View.VISIBLE);
        v.startAnimation(aa);
    }

    public static void close_view(final View v,int time,float start_alpha,int visibility){
        AlphaAnimation aa = new AlphaAnimation(start_alpha,0f);
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
/*
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
    }*/


    public static class ObjectAnimator_scroll_form_top{
        //注意,如果要复用的话里面的东西要改一改(写这些玩意的时候没准备复用...)
        //注意,v.getHeight()在onCreate里会返回0
        ObjectAnimator inAnimator;
        ObjectAnimator outAnimator;
        public ObjectAnimator_scroll_form_top(View v,int height){
            //因为View.getHeight()在切换日夜模式后即使使用View.post也会返回0  暂时不知道怎么解决
            //这里直接把height传进来 (妥协)
            inAnimator = ObjectAnimator.ofFloat(v, "translationY",-height,0);//从v.getHeight()变到0
            outAnimator = ObjectAnimator.ofFloat(v,"translationY",0,-height);

            inAnimator.setDuration(400);
            inAnimator.setInterpolator(new DecelerateInterpolator(1.5f));
            outAnimator.setDuration(300);
            outAnimator.setInterpolator(new DecelerateInterpolator(1.5f));
        }
        public void in(){
            if(inAnimator.isRunning())
                inAnimator.cancel();
            if(outAnimator.isRunning())
                outAnimator.cancel();
            inAnimator.start();
            System.out.println(((View)inAnimator.getTarget()).getHeight());
        }
        public void out(){
            if(inAnimator.isRunning())
                inAnimator.cancel();
            if(outAnimator.isRunning())
                outAnimator.cancel();
            outAnimator.start();
            System.out.println(((View)inAnimator.getTarget()).getHeight());
        }
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
