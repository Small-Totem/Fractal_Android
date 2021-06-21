package com.zjh.fractal;

public class Definition {
    public final static int generate_mode_max           = 21;   // 除去-1,-2,一共21个，0->20
    public final static int fractal_id_num              = 20;   // 除去-1,一共20个
    //public final static int iteration_max = 1000000;          // 为了防止渲染时间太长才设的限制 //update:取消了此限制
    public final static double scale_max                = 4E12; // 由于fractal.c那边的限制(double精度限制)，超过这个数后很快就不能正常渲染了
    public final static int generate_progress_wait_time = 50;
    public final static int image_change_time           = 200;

    public static double center_x             = 0;
    public static double center_y             = 0;
    public static double scale_times          = 0.5;
    public static double pixel_times          = 0.5;
    public static double generate_now_quality = 0.3;

    public static int fractal_id         = 0;       // 图形id 0是曼德博集合
    public static int generate_mode      = 0;       // 渲染模式
    public static int iteration_times    = 128;     // 迭代次数
    public static int use_thread         = 1;       //使用的线程数 ( <=10 )
    public static int auto_iteration_max = 2000;
    //public static int auto_iteration_grow_times = 5;//自适应迭代的增长倍数
    public static int paint_mode         = 0;       //0=svpng,1=canvas,2=canvas/不修正

    public static boolean flag_if_generate_now          = true; //立即渲染
    public static boolean flag_should_reload            = true; // 在设置界面中调用，是否重新渲染，默认true
    public static boolean color_reversal                = true; // 渲染颜色反转
    public static boolean display_color_reversal        = false;// 显示反转
    public static boolean flag_should_load_from_storage = false;// 在改日夜模式后为true，是否加载图片,默认false
    public static boolean auto_iteration                = false;//自适应迭代

    /** 为了实现每次更新信息都暂存数据（用于恢复上次关闭时的参数），使用
     * 两份SharedPreferences保存数据（称为A和B），当进入程序后读取到的flag_use_data为true时，
     * SharedPreferences中的值将会被设置为false，每次信息更新时写入A(对应true)，要读取上次信息时从B读取。
     * 当flag_use_data_A为false时则相反。
     */
    public static boolean flag_use_data;

    /** 只有当程序启动时，这个值为true，随后一直为false
     (确保flag_use_data的改变在程序启动后只发生一次)*/
    public static boolean flag_should_change_data = true;


    public static boolean flag_use_transition_animation = true;
    public static boolean flag_monitor_generate_info = true;

    public static String file_path = "null";
}
