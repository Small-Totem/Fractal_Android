package com.zjh.fractal;

public class Definition {
    public final static int generate_mode_max = 21;// 除去-1,-2,一共21个，0->20
    public final static int fractal_id_num = 11;// 除去-1,一共11个
    public final static int iteration_max = 65536;// 为了防止渲染时间太长才设的限制
    public final static double scale_max = 4E12;// 由于fractal.c那边的限制，超过这个数后很快就不能正常渲染了
    public final static int generate_process_wait_time=50;
    public final static int image_change_time=200;

    public static double center_x = 0;
    public static double center_y = 0;
    public static double scale_times = 0.5;
    public static double pixel_times = 0.5;
    public static double generate_now_quality = 0.3;

    public static int fractal_id = 0;// 图形id 0是曼德博集合
    public static int generate_mode = 0;// 渲染模式
    public static int iteration_times = 128;// 迭代次数
    public static int use_thread=1;//使用的线程数(测试)  不得大于10 可等于
    public static int auto_iteration_max = 2000;

    public static boolean flag_if_generate_now = true;//立即渲染
    public static boolean flag_should_reload = true;// 在设置界面中调用，是否重新渲染，默认true
    public static boolean color_reversal = true;// 渲染颜色反转
    public static boolean display_color_reversal = false;// 显示反转
    public static boolean flag_should_load_from_storage = false;// 在改日夜模式后为true，是否加载图片,默认false
    public static boolean auto_iteration = false;//自适应迭代
    public static boolean flag_use_data;/*
     * 为了实现每次更新信息都暂存数据（用于恢复上次关闭时的参数），使用
     * 两份SharedPreferences保存数据（称为A和B），当进入程序后读取到的flag_use_data为true时，
     * SharedPreferences中的值将会被设置为false，每次信息更新时写入A(对应true)，要读取上次信息时从B读取。
     * 当flag_use_data_A为false时则相反。
     */
    public static boolean flag_should_change_data = true;// 只有当程序启动时，这个值为true，随后一直为false
    // （确保flag_use_data的改变在程序启动后只发生一次）
    // 顺便，用来标识第一次进入的第一次渲染不要使用动画
    public static boolean flag_is_generating=false;//用于标识当前正在渲染，防止渲染过程中又调用渲染
    public static boolean flag_use_transition_animation=true;
    public static boolean flag_monitor_generate_info=true;


    public static String generate_info="";//用于储存generate_info
    public static String file_path = "null";
}
