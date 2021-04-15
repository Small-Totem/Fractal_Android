package com.zjh.fractal.util;
import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import java.util.LinkedList;

//小轮子，用于finish所有activity
//https://www.jianshu.com/p/269873a16937
public class ActivityManager extends Application {
    // 此处采用 LinkedList作为容器，增删速度快
    public static LinkedList<Activity> activityLinkedList;

    @Override
    public void onCreate() {
        super.onCreate();

        activityLinkedList = new LinkedList<>();

        registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                activityLinkedList.add(activity);
                // 在Activity启动时（onCreate()） 写入Activity实例到容器内
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                activityLinkedList.remove(activity);
                // 在Activity结束时（Destroyed（）） 删除 Activity实例
            }

            @Override
            public void onActivityStarted(Activity activity) {
            }

            @Override
            public void onActivityResumed(Activity activity) {
            }

            @Override
            public void onActivityPaused(Activity activity) {
            }

            @Override
            public void onActivityStopped(Activity activity) {
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }
        });
    }

    public void exitApp() {
        // 逐个退出Activity
        for (Activity activity : activityLinkedList) {
            activity.finish();
        }
        // 等待0.5s,结束进程
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                    System.exit(0);
                } catch (InterruptedException ignored) {
                }
            }
        }).start();
    }
}