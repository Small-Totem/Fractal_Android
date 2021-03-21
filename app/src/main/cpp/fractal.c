#include <stdint.h>
#include <stdio.h>
#include <fcntl.h>
#include "svpng.inc"
#include <jni.h>
#include <stdlib.h>
#include <math.h>
#include <pthread.h>
#include <unistd.h>
#include "android/log.h"
#include "string.h"

#define LOG_TAG "fractal.c_log"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define GENERATE_INFO_OUTPUT(text) (*env)->CallVoidMethod(env, gs_object, method_generate_info_add, charToJstring(env, text));

static int static_id = 0;
static int static_color_reversal = 0;
static int static_generate_mode = 0;
static int static_iteration_times = 0;
static int static_PIXEL_X = 0;
static int static_PIXEL_Y = 0;
static int static_use_thread = 0;
static int static_auto_iteration_max = 0;
static int static_monitor_generate_info = 0;
static double static_scale = 0;
static double static_CENTER_X = 0;
static double static_CENTER_Y = 0;

//此乃无奈之举，待改进
static double generate_process_thread_0 = 0;
static double generate_process_thread_1 = 0;
static double generate_process_thread_2 = 0;
static double generate_process_thread_3 = 0;
static double generate_process_thread_4 = 0;
static double generate_process_thread_5 = 0;
static double generate_process_thread_6 = 0;
static double generate_process_thread_7 = 0;
static double generate_process_thread_8 = 0;
static double generate_process_thread_9 = 0;

//这里是为了在子线程中调用jni(把参数从c传递到java)
//参见https://www.cnblogs.com/aiguozhe/p/5355226.html
static jobject gs_object = NULL;
static JavaVM *gs_jvm = NULL;
static jmethodID method_generate_info_add = NULL;

static int post_thread_wait_time=50000;

struct param_for_thread
{
    int thread_id;
    uint8_t *p;
};
struct param_for_auto_iteration
{
    double a, b, n;
};

jstring charToJstring(JNIEnv *env, const char *pat)
{
    //轮子
    jclass strClass = (*env)->FindClass(env, "java/lang/String");
    jmethodID ctorID = (*env)->GetMethodID(env, strClass, "<init>", "([BLjava/lang/String;)V");
    jbyteArray bytes = (*env)->NewByteArray(env, strlen(pat));
    (*env)->SetByteArrayRegion(env, bytes, 0, strlen(pat), (jbyte *)pat);
    jstring encoding = (*env)->NewStringUTF(env, "utf-8");
    return (jstring)(*env)->NewObject(env, strClass, ctorID, bytes, encoding);
}

char num_to_char(int num)
{
    //仅支持0-9
    return (char)((int)'0' + num);
}

char *get_string_from_text_num_text(char *text1, int num, char *text2)
{
    //把char* int char*拼接起来
    //好麻烦的实现
    char *temp = (char *)malloc(40 * sizeof(char));
    temp[0] = '\0';
    strcat(temp, text1);
    for (int i = 0; i < 40; i++)
    {
        if (temp[i] == '\0')
        {
            temp[i] = num_to_char(num);
            if (temp[i] == ':')
            {
                //为10的情况     很烦，不该这样写，但是将就下吧
                //屎山创造者竟是我自己？
                temp[i] = '1';
                temp[i + 1] = '0';
                temp[i + 2] = '\0';
                break;
            }
            temp[i + 1] = '\0';
            break;
        }
    }
    strcat(temp, text2);
    return temp;
}

double get_absolute_value(double x)
{
    if (x > 0)
        return x;
    else
        return -x;
}

struct param_for_auto_iteration mandelbrot_continue(double x, double y, double a, double b,
                                                    int id, int color_reversal, int iteration_start_num, int iteration_end_num)
{
    struct param_for_auto_iteration param;
    param.n = 0;
    switch (id)
    {
    // x^2
    case 0:
        for (int i = iteration_start_num; i <= iteration_end_num; i++)
        {
            double c = a * a - b * b + x, d = 2 * a * b + y;
            a = c, b = d;
            if (a * a + b * b > 4)
            {
                param.n = i / (double)iteration_end_num;
                break;
            }
        }
        break;
    case 1:
        //x^3
        for (int i = iteration_start_num; i <= iteration_end_num; i++)
        {
            double c = a * a * a - 3 * a * b * b + x, d = 3 * a * a * b - b * b * b + y;
            a = c, b = d;
            if (a * a + b * b > 4)
            {
                param.n = i / (double)iteration_end_num;
                break;
            }
        }
        break;

    case 2:
        // x^4
        for (int i = iteration_start_num; i <= iteration_end_num; i++)
        {
            double c = a * a * a * a - 6 * a * a * b * b + b * b * b * b + x, d = 4 * a * a * a * b - 4 * b * b * b * a + y;
            a = c, b = d;
            if (a * a + b * b > 4)
            {
                param.n = i / (double)iteration_end_num;
                break;
            }
        }
        break;
    case 3:
        // x^4+x^3+x^2
        for (int i = iteration_start_num; i <= iteration_end_num; i++)
        {
            double c = a * a * a * a - 6 * a * a * b * b + b * b * b * b + a * a * a - 3 * a * b * b + a * a - b * b + x,
                   d = 4 * a * a * a * b - 4 * b * b * b * a + 3 * a * a * b - b * b * b + 2 * a * b + y;
            a = c, b = d;
            if (a * a + b * b > 4)
            {
                param.n = i / (double)iteration_end_num;
                break;
            }
        }
        break;

    case 4:
        //x^2-x^3
        for (int i = iteration_start_num; i <= iteration_end_num; i++)
        {
            double c = a * a - b * b - (a * a * a - 3 * a * b * b) + x, d = 2 * a * b - (3 * a * a * b - b * b * b) + y;
            a = c, b = d;
            if (a * a + b * b > 4)
            {
                param.n = i / (double)iteration_end_num;
                break;
            }
        }
        break;

    case 5:
        //The Burning Ship fractal
        for (int i = iteration_start_num; i <= iteration_end_num; i++)
        {
            double c = a * a - b * b + x, d = 2 * get_absolute_value(a) * get_absolute_value(b) + y;
            a = c, b = d;
            if (a * a + b * b > 4)
            {
                param.n = i / (double)iteration_end_num;
                break;
            }
        }
        break;

    case 6:
        //无意义的混沌
        for (int i = iteration_start_num; i <= iteration_end_num; i++)
        {
            double c = a * a - b * b / a + x, d = 2 * a * b + y;
            a = c, b = d;
            if (a * a + b * b > 4)
            {
                param.n = i / (double)iteration_end_num;
                break;
            }
        }
        break;
    case 7:
        // 一个混沌
        for (int i = iteration_start_num; i <= iteration_end_num; i++)
        {
            double c = a * a - b * b + x, d = 2 * a * b / (a + b) + y;
            a = c, b = d;
            if (a * a + b * b > 4)
            {
                param.n = i / (double)iteration_end_num;
                break;
            }
        }
        break;
    case 8:
        //混沌
        for (int i = iteration_start_num; i <= iteration_end_num; i++)
        {
            double c = a * (a + b) - b * (a - b) + x, d = 2 * a * b / (a + b) * (a - b) + y;
            a = c, b = d;
            if (a * a + b * b > 4)
            {
                param.n = i / (double)iteration_end_num;
                break;
            }
        }
        break;
    case 9:
        //茱莉亚集合
        for (int i = iteration_start_num; i <= iteration_end_num; i++)
        {
            double c = a * a - b * b - 0.70176, d = 2 * a * b - 0.3842;
            a = c, b = d;
            if (a * a + b * b > 4)
            {
                param.n = i / (double)iteration_end_num;
                break;
            }
        }
        break;
    case 10:
        //牛顿分形 f(z)=z^3-1 x_n+1=x_n-f(x_n)/f'(x_n)
        for (int i = iteration_start_num; i <= iteration_end_num; i++)
        {
            double c = a - (2 * a * b * (3 * pow(a, 2) * b - pow(b, 3)) + (pow(a, 2) - pow(b, 2)) * ((-3 * a * pow(b, 2)) + pow(a, 3) - 1)) / (3 * pow((pow(b, 2) + pow(a, 2)), 2)),
                   d = b - ((pow(a, 2) - pow(b, 2)) * (3 * pow(a, 2) * b - pow(b, 3)) - 2 * a * b * ((-3 * a * pow(b, 2)) + pow(a, 3) - 1)) / (3 * pow((pow(b, 2) + pow(a, 2)), 2));
            a = c, b = d;
            if (a * a + b * b > 4)
            {
                param.n = i / (double)iteration_end_num;
                break;
            }
        }
        break;
    case -1: //(一个圆，用来分析渲染模式)
        for (int i = iteration_start_num; i <= iteration_end_num; i++)
        {
            double c = a + x, d = b + y;
            a = c, b = d;
            if (a * a + b * b > 4)
            {
                param.n = i / (double)iteration_end_num;
                break;
            }
        }
        break;
    default:
        break;
    }

    if (!color_reversal && param.n != 0)
    {
        param.n = 1 - param.n;
    }
    param.a = a;
    param.b = b;
    return param;
}

struct param_for_auto_iteration mandelbrot(double x, double y, int id, int color_reversal, int iteration_times)
{
    return mandelbrot_continue(x, y, x, y, id, color_reversal, 1, iteration_times);
}

void write_data(uint8_t *p, int n, int generate_mode)
{
    switch (generate_mode)
    {
    case 0:
        *p++ = 4 * n;
        *p++ = 2 * n;
        *p++ = n;
        break;
    case 1:
        *p++ = 4 * n;
        *p++ = n;
        *p++ = 2 * n;
        break;
    case 2:
        *p++ = 2 * n;
        *p++ = 4 * n;
        *p++ = n;
        break;
    case 3:
        *p++ = n;
        *p++ = 4 * n;
        *p++ = 2 * n;
        break;
    case 4:
        *p++ = n;
        *p++ = 2 * n;
        *p++ = 4 * n;
        break;
    case 5:
        *p++ = 2 * n;
        *p++ = n;
        *p++ = 4 * n;
        break;
    case 6:
        *p++ = 16 * n;
        *p++ = 4 * n;
        *p++ = n;
        break;
    case 7:
        *p++ = 2 * n;
        *p++ = 4 * n;
        *p++ = 16 * n;
        break;
    case 8:
        *p++ = 8 * n;
        *p++ = 16 * n;
        *p++ = 2 * n;
        break;
    case 9:
        *p++ = 4 * n;
        *p++ = 16 * n;
        *p++ = n;
        break;
    case 10:
        *p++ = 32 * n;
        *p++ = 2 * n;
        *p++ = 8 * n;
        break;
    case 11:
        *p++ = 8 * n;
        *p++ = 64 * n;
        *p++ = n;
        break;
    case 12:
        *p++ = 32 * n;
        *p++ = 64 * n;
        *p++ = 4 * n;
        break;
    case 13:
        *p++ = 64 * n;
        *p++ = 16 * n;
        *p++ = 32 * n;
        break;
    case 14:
        *p++ = 8 * n;
        *p++ = 16 * n;
        *p++ = 64 * n;
        break;
    case 15:
        *p++ = 16 * n;
        *p++ = 2 * n;
        *p++ = 64 * n;
        break;
    case 16:
        *p++ = 64 * n;
        *p++ = 32 * n;
        *p++ = 8 * n;
        break;
    case 17:
        *p++ = 64 * n;
        *p++ = 4 * n;
        *p++ = 8 * n;
        break;
    case 18:
        *p++ = 32 * n;
        *p++ = 64 * n;
        *p++ = 16 * n;
        break;
    case 19:
        *p++ = 32 * n;
        *p++ = 64 * n;
        *p++ = 128 * n;
        break;
    case 20:
        *p++ = 128 * n;
        *p++ = 4 * n;
        *p++ = 30 * n;
        break;
    case -1:
        if (n > 235)
        {
            *p++ = n;
            *p++ = n;
            *p++ = n;
        } //黑白  数字越大则越透明
        else if (n > 200)
        {
            *p++ = 0;
            *p++ = n;
            *p++ = 0;
        } //绿
        else if (n > 160)
        {
            *p++ = n + 20;
            *p++ = n;
            *p++ = 0;
        } //黄
        else if (n > 120)
        {
            *p++ = 0;
            *p++ = 0;
            *p++ = n + 90.0;
        } //浅蓝
        else if (n > 80)
        {
            *p++ = 0;
            *p++ = 0;
            *p++ = n + 40.0;
        } //深蓝
        else if (n > 40)
        {
            *p++ = n;
            *p++ = 0;
            *p++ = n + 50.0;
        } //紫
        else if (n > 10)
        {
            *p++ = n + 80.0;
            *p++ = 0;
            *p++ = 0;
        } // 红
        else
        {
            *p++ = n;
            *p++ = 0;
            *p++ = 0;
        } //红，但不加鲜艳度
        break;
    default:
        *p++ = n;
        *p++ = n;
        *p++ = n;
        break;
    }
}

void generate_info_output(char *a)
{
    if (static_monitor_generate_info == 0)
        return;
    JNIEnv *env;
    (*gs_jvm)->AttachCurrentThread(gs_jvm, &env, NULL);
    GENERATE_INFO_OUTPUT(a);
}

void update_process_for_multithread(int thread_id, double num)
{
    //无奈之举，详见void* post_generate_process(void* arg)函数的注释
    switch (thread_id)
    {
    case 0:
        generate_process_thread_0 = num;
        break;
    case 1:
        generate_process_thread_1 = num;
        break;
    case 2:
        generate_process_thread_2 = num;
        break;
    case 3:
        generate_process_thread_3 = num;
        break;
    case 4:
        generate_process_thread_4 = num;
        break;
    case 5:
        generate_process_thread_5 = num;
        break;
    case 6:
        generate_process_thread_6 = num;
        break;
    case 7:
        generate_process_thread_7 = num;
        break;
    case 8:
        generate_process_thread_8 = num;
        break;
    case 9:
        generate_process_thread_9 = num;
        break;
    default:
        break;
    }
}

void *multithread_generate(void *arg)
{
    struct param_for_thread tmp = *(struct param_for_thread *)arg;

    int thread_num = static_use_thread;
    uint8_t *p = tmp.p;

    int one_part_of_temp_i = (static_PIXEL_Y) / thread_num;
    int temp_i_start = tmp.thread_id * one_part_of_temp_i;

    p += temp_i_start * static_PIXEL_X * 3;
    int temp_i_end = temp_i_start + one_part_of_temp_i;
    if (tmp.thread_id == thread_num - 1)
    {
        //处理不能被整除的情况(多出来的全部由最后一个线程渲染)
        temp_i_end += static_PIXEL_Y % thread_num;
    }

    int temp_i = static_PIXEL_Y;
    int temp_j = static_PIXEL_X;
    double temp_scale = 512.0 * (static_scale);

    int divider=(temp_i_end-temp_i_start)/100;//调用update_process_for_multithread()更新渲染进度的频繁度
                                              //divider越大，则更新越频繁
    if(divider<1)divider=1;

    for (int i = temp_i_start; i < temp_i_end; ++i)
    {
        for (int j = 0; j < temp_j; ++j)
        {
            struct param_for_auto_iteration param_continue = mandelbrot((static_CENTER_X)-temp_j / (2 * (temp_scale)) + j / (temp_scale),
                                                                        (static_CENTER_Y)-temp_i / (2 * temp_scale) + i / temp_scale,
                                                                        static_id, static_color_reversal, static_iteration_times);

            uint8_t n = param_continue.n * 255;

            //自适应迭代
            if (static_auto_iteration_max > 0 && n == 0 && static_iteration_times < static_auto_iteration_max)
            {
                int it_start = static_iteration_times;
                int it_end = it_start * 5;
                while (it_end < static_auto_iteration_max)
                {
                    //对黑色的地方提高迭代次数
                    if (n == 0)
                    {
                        param_continue = mandelbrot_continue((static_CENTER_X)-temp_j / (2 * (temp_scale)) + j / (temp_scale),
                                                             (static_CENTER_Y)-temp_i / (2 * temp_scale) + i / temp_scale,
                                                             param_continue.a, param_continue.b, static_id, static_color_reversal,
                                                             it_start, it_end);
                        n = param_continue.n * 255;
                        it_start = it_end;
                        it_end *= 5;
                    }
                    else
                    {
                        break;
                    }
                }
                if (n == 0)
                {
                    //对还是黑色的地方进行最后一次迭代
                    n = mandelbrot_continue((static_CENTER_X)-temp_j / (2 * (temp_scale)) + j / (temp_scale),
                                            (static_CENTER_Y)-temp_i / (2 * temp_scale) + i / temp_scale,
                                            param_continue.a, param_continue.b, static_id, static_color_reversal,
                                            it_start, static_auto_iteration_max).n *255;
                }
            }

            write_data(p, n, static_generate_mode);
            p += 3;
        }
        if(i % divider == 0&&static_monitor_generate_info)update_process_for_multithread(tmp.thread_id, (double)(i - temp_i_start) / (temp_i_end - temp_i_start));
    }
    if(static_monitor_generate_info)update_process_for_multithread(tmp.thread_id, 1);

    generate_info_output(get_string_from_text_num_text("线程", tmp.thread_id, "结束"));

    pthread_exit(0);
}

void *post_generate_process(void *arg)
{
    int thread_id = (int)arg;
    //这里挺复杂的
    //这里最关键的env和thiz不能直接用struct传进来，必须按照以下网址的方法
    //参见https://www.cnblogs.com/aiguozhe/p/5355226.html
    JNIEnv *env;
    jclass c;
    (*gs_jvm)->AttachCurrentThread(gs_jvm, &env, NULL);
    c = (*env)->GetObjectClass(env, gs_object);
    jfieldID fid;
    //LOGI("--------%d",thread_id);
    //由于jni似乎只能把整个数组传递过来，所以没有用数组（暂时没想到更好的办法）
    switch (thread_id)
    {
    case 0:
        fid = (*env)->GetStaticFieldID(env, c, "generate_process_thread_0", "D");
        while (generate_process_thread_0 < 1)
        {
            (*env)->SetStaticDoubleField(env, c, fid, generate_process_thread_0);
            usleep(post_thread_wait_time);//单位为ns 1,000,000ns=1s
        }
        (*env)->SetStaticDoubleField(env, c, fid, (double)1);
        break;
    case 1:
        fid = (*env)->GetStaticFieldID(env, c, "generate_process_thread_1", "D");
        while (generate_process_thread_1 < 1)
        {
            (*env)->SetStaticDoubleField(env, c, fid, generate_process_thread_1);
            usleep(post_thread_wait_time);
        }
        (*env)->SetStaticDoubleField(env, c, fid, (double)1);
        break;
    case 2:
        fid = (*env)->GetStaticFieldID(env, c, "generate_process_thread_2", "D");
        while (generate_process_thread_2 < 1)
        {
            (*env)->SetStaticDoubleField(env, c, fid, generate_process_thread_2);
            usleep(post_thread_wait_time);
        }
        (*env)->SetStaticDoubleField(env, c, fid, (double)1);
        break;
    case 3:
        fid = (*env)->GetStaticFieldID(env, c, "generate_process_thread_3", "D");
        while (generate_process_thread_3 < 1)
        {
            (*env)->SetStaticDoubleField(env, c, fid, generate_process_thread_3);
            usleep(post_thread_wait_time);
        }
        (*env)->SetStaticDoubleField(env, c, fid, (double)1);
        break;
    case 4:
        fid = (*env)->GetStaticFieldID(env, c, "generate_process_thread_4", "D");
        while (generate_process_thread_4 < 1)
        {
            (*env)->SetStaticDoubleField(env, c, fid, generate_process_thread_4);
            usleep(post_thread_wait_time);
        }
        (*env)->SetStaticDoubleField(env, c, fid, (double)1);
        break;
    case 5:
        fid = (*env)->GetStaticFieldID(env, c, "generate_process_thread_5", "D");
        while (generate_process_thread_5 < 1)
        {
            (*env)->SetStaticDoubleField(env, c, fid, generate_process_thread_5);
            usleep(post_thread_wait_time);
        }
        (*env)->SetStaticDoubleField(env, c, fid, (double)1);
        break;
    case 6:
        fid = (*env)->GetStaticFieldID(env, c, "generate_process_thread_6", "D");
        while (generate_process_thread_6 < 1)
        {
            (*env)->SetStaticDoubleField(env, c, fid, generate_process_thread_6);
            usleep(post_thread_wait_time);
        }
        (*env)->SetStaticDoubleField(env, c, fid, (double)1);
        break;
    case 7:
        fid = (*env)->GetStaticFieldID(env, c, "generate_process_thread_7", "D");
        while (generate_process_thread_7 < 1)
        {
            (*env)->SetStaticDoubleField(env, c, fid, generate_process_thread_7);
            usleep(post_thread_wait_time);
        }
        (*env)->SetStaticDoubleField(env, c, fid, (double)1);
        break;
    case 8:
        fid = (*env)->GetStaticFieldID(env, c, "generate_process_thread_8", "D");
        while (generate_process_thread_8 < 1)
        {
            (*env)->SetStaticDoubleField(env, c, fid, generate_process_thread_8);
            usleep(post_thread_wait_time);
        }
        (*env)->SetStaticDoubleField(env, c, fid, (double)1);
        break;
    case 9:
        fid = (*env)->GetStaticFieldID(env, c, "generate_process_thread_9", "D");
        while (generate_process_thread_9 < 1)
        {
            (*env)->SetStaticDoubleField(env, c, fid, generate_process_thread_9);
            usleep(post_thread_wait_time);
        }
        (*env)->SetStaticDoubleField(env, c, fid, (double)1);
        break;
    default:
        break;
    }

    /*  这里是以前的（多线程时只显示一个线程的进度）
    jfieldID fid = (*env)->GetStaticFieldID(env, c, "generate_process_thread_0", "D");
    LOGI("aaa___%lf",generate_process_thread_0);
    while (generate_process_thread_0 < 1) {
        //获取java的参数
        //jdouble j_generate_process = (*env)->GetStaticDoubleField(env, c, fid);

        (*env)->SetStaticDoubleField(env, c, fid, generate_process_thread_0);

    }*/
    //(*env)->SetStaticDoubleField(env, c, fid, (double)1);
    pthread_exit(0);
}

void generate(char *file_path)
{
    uint8_t *data = (uint8_t *)malloc((static_PIXEL_Y) * (static_PIXEL_X)*3);

    //多线程
    if (static_use_thread > 1 && static_PIXEL_Y >= 10)
    {
        pthread_t my_thread[10];

        struct param_for_thread param1[10];
        for (int i = 0; i < static_use_thread; i++)
        {
            param1[i].thread_id = i;
            param1[i].p = data;
            pthread_create(&my_thread[i], NULL, multithread_generate, &param1[i]);
        }

        for (int i = 0; i < static_use_thread; i++)
        {
            pthread_join(my_thread[i], NULL);
        }
        generate_info_output("渲染完成,正在写入");

        FILE *file = fopen(file_path, "wb");
        svpng(file, (static_PIXEL_X), (static_PIXEL_Y), data, 0);
        fclose(file);
        free(data);
        generate_info_output("完成");
        return;
    }

    //普通
    else
    {
        uint8_t *p = data;
        double temp_i = (static_PIXEL_Y);
        double temp_j = (static_PIXEL_X);
        double temp_scale = 512.0 * (static_scale);

        for (int i = 0; i < temp_i; ++i)
        {
            for (int j = 0; j < temp_j; ++j)
            {
                struct param_for_auto_iteration param_continue = mandelbrot((static_CENTER_X)-temp_j / (2 * (temp_scale)) + j / (temp_scale),
                                                                            (static_CENTER_Y)-temp_i / (2 * temp_scale) + i / temp_scale,
                                                                            static_id, static_color_reversal, static_iteration_times);

                uint8_t n = param_continue.n * 255;
                //越接近mandelbrot集合内，则n值越高   但在集合内,n=0   n=255时 为白

                if (static_auto_iteration_max > 0 && n == 0 && static_iteration_times < static_auto_iteration_max)
                {
                    int it_start = static_iteration_times;
                    int it_end = it_start * 5;
                    while (it_end < static_auto_iteration_max)
                    {
                        //对黑色的地方提高迭代次数
                        if (n == 0)
                        {
                            param_continue = mandelbrot_continue((static_CENTER_X)-temp_j / (2 * (temp_scale)) + j / (temp_scale),
                                                                 (static_CENTER_Y)-temp_i / (2 * temp_scale) + i / temp_scale,
                                                                 param_continue.a, param_continue.b, static_id, static_color_reversal,
                                                                 it_start, it_end);
                            n = param_continue.n * 255;
                            it_start = it_end;
                            it_end *= 5;
                        }
                        else
                        {
                            break;
                        }
                    }
                    if (n == 0)
                    {
                        //对还是黑色的地方进行最后一次迭代
                        n = mandelbrot_continue((static_CENTER_X)-temp_j / (2 * (temp_scale)) + j / (temp_scale),
                                                (static_CENTER_Y)-temp_i / (2 * temp_scale) + i / temp_scale,
                                                param_continue.a, param_continue.b, static_id, static_color_reversal,
                                                it_start, static_auto_iteration_max).n *255;
                    }
                }

                write_data(p, n, static_generate_mode);
                p += 3;
            }
            if (i % 10 == 0&&static_monitor_generate_info)
            {
                generate_process_thread_0 = (double)i / (double)temp_i;
            }
        }
        if(static_monitor_generate_info)generate_process_thread_0 = 1;

        generate_info_output("渲染完成,正在写入");

        FILE *file = fopen(file_path, "wb");
        svpng(file, (static_PIXEL_X), (static_PIXEL_Y), data, 0);
        fclose(file);
        free(data);
        generate_info_output("完成");
        return;
    }
}

JNIEXPORT void JNICALL
Java_com_zjh_fractal_MainActivity_GenerateFractal(JNIEnv *env, jobject thiz, jstring path,
                                                  jint screen_height, jint screen_width,
                                                  jdouble center_x, jdouble center_y,
                                                  jdouble scale_times, jint fractal_id,
                                                  jint color_reversal, jint generate_mode,
                                                  jint iteration_times, jint use_thread,
                                                  jint auto_iteration_max, jint monitor_generate_info)
{
    //详见线程函数post_generate_process
    (*env)->GetJavaVM(env, &gs_jvm); //保存到全局变量中JVM
    //直接赋值obj到全局变量是不行的,应该调用以下函数:
    gs_object = (*env)->NewGlobalRef(env, thiz);
    //获取方法id,用于在c中调用java里的generate_info_add函数，更新渲染输出信息
    method_generate_info_add = (*env)->GetMethodID(env, (*env)->FindClass(env, "com/zjh/fractal/MainActivity"), "generate_info_add", "(Ljava/lang/String;)V");

    static_id = fractal_id;
    static_color_reversal = color_reversal;
    static_generate_mode = generate_mode;
    static_iteration_times = iteration_times;
    static_PIXEL_X = screen_width;
    static_PIXEL_Y = screen_height;
    static_use_thread = use_thread;
    static_auto_iteration_max = auto_iteration_max;
    static_scale = scale_times;
    static_CENTER_X = center_x;
    static_CENTER_Y = center_y;
    static_monitor_generate_info = monitor_generate_info;
    if (static_use_thread < 1)
        static_use_thread = 1;

    generate_info_output("JNI调用开始");

    char *file_path = (*env)->GetStringUTFChars(env, path, JNI_FALSE);

    //输出 启用x线程渲染
    generate_info_output(get_string_from_text_num_text("启用", static_use_thread, "线程渲染"));

    pthread_t process_thread[10];

    if(monitor_generate_info) {
        generate_process_thread_0 = 0;
        generate_process_thread_1 = 0;
        generate_process_thread_2 = 0;
        generate_process_thread_3 = 0;
        generate_process_thread_4 = 0;
        generate_process_thread_5 = 0;
        generate_process_thread_6 = 0;
        generate_process_thread_7 = 0;
        generate_process_thread_8 = 0;
        generate_process_thread_9 = 0;
        for (int i = 0; i < static_use_thread; i++) {
            pthread_create(&process_thread[i], NULL, post_generate_process, i);
        }
    }

    generate(file_path);

    if(monitor_generate_info) {
        for (int i = 0; i < static_use_thread; i++) {
            pthread_join(process_thread[i], NULL);
        }
    }

    generate_info_output("JNI调用结束");
}