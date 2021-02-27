#include <stdint.h>
#include <stdio.h>
#include <fcntl.h>
#include "svpng.inc"
#include <jni.h>
#include <stdlib.h>
#include <math.h>
#include <pthread.h>

struct param_for_thread {
    int id;int color_reversal; int generate_mode; int iteration_times;
    double scale; int thread_id; uint8_t* p;int PIXEL_X;int PIXEL_Y;
    double CENTER_X; double CENTER_Y; int use_thread;int auto_iteration_max;
};
struct param_for_auto_iteration {
    double a, b, n;
};

double get_absolute_value(double x){
    if(x>0)return x;
    else return -x;
}

struct param_for_auto_iteration mandelbrot_continue
        (double x, double y,double a, double b,
         int id,int color_reversal,int iteration_start_num,int iteration_end_num){
    struct param_for_auto_iteration param;
    param.n=0;
    switch(id){
        // x^2
        case 0:
            for (int i = iteration_start_num; i <= iteration_end_num; i++) {
                double c = a*a-b*b +x, d = 2*a*b +y;
                a =c, b = d;
                if (a * a + b * b >4) {
                    param.n=i / (double)iteration_end_num;
                    break;
                }
            }
            break;
        case 1:
            //x^3
            for (int i = iteration_start_num; i <= iteration_end_num; i++) {
                double  c = a*a*a-3*a*b*b + x,d = 3*a*a*b-b*b*b + y;
                a = c, b = d;
                if (a * a + b * b > 4) {
                    param.n=i / (double)iteration_end_num;
                    break;
                }
            }
            break;

        case 2:
            // x^4
            for (int i = iteration_start_num; i <= iteration_end_num; i++) {
                double  c = a*a*a*a-6*a*a*b*b+b*b*b*b + x,d = 4*a*a*a*b-4*b*b*b*a + y;
                a = c, b = d;
                if (a * a + b * b > 4) {
                    param.n=i / (double)iteration_end_num;
                    break;
                }
            }
            break;
        case 3:
            // x^4+x^3+x^2
            for (int i = iteration_start_num; i <= iteration_end_num; i++) {
                double  c = a*a*a*a-6*a*a*b*b+b*b*b*b+a*a*a-3*a*b*b+a*a-b*b+x,
                        d = 4*a*a*a*b-4*b*b*b*a+3*a*a*b-b*b*b+2*a*b+y;
                a = c, b = d;
                if (a * a + b * b > 4) {
                    param.n=i / (double)iteration_end_num;
                    break;
                }
            }
            break;

        case 4:
            //x^2-x^3
            for (int i = iteration_start_num; i <= iteration_end_num; i++) {
                double c = a * a - b * b-(a * a * a - 3*a*b*b)+ x, d = 2 * a * b -(3*a*a*b-b*b*b)+ y;
                a = c, b = d;
                if (a * a + b * b > 4) {
                    param.n=i / (double)iteration_end_num;
                    break;
                }
            }
            break;

        case 5:
            //The Burning Ship fractal
            for (int i = iteration_start_num; i <= iteration_end_num; i++) {
                double c =a * a - b * b  + x, d = 2*get_absolute_value(a)*get_absolute_value(b)  + y;
                a = c, b = d;
                if (a * a + b * b > 4) {
                    param.n=i / (double)iteration_end_num;
                    break;
                }
            }
            break;

        case 6:
            //无意义的混沌
            for (int i = iteration_start_num; i <= iteration_end_num; i++) {
                double c = a * a - b * b / a+ x, d = 2 * a * b + y;
                a = c, b = d;
                if (a * a + b * b > 4) {
                    param.n=i / (double)iteration_end_num;
                    break;
                }
            }
            break;
        case 7:
            // 一个混沌
            for (int i = iteration_start_num; i <= iteration_end_num; i++) {
                double  c = a * a - b * b + x, d = 2 * a * b/(a+b) + y;
                a = c, b = d;
                if (a * a + b * b > 4) {
                    param.n=i / (double)iteration_end_num;
                    break;
                }
            }
            break;
        case 8:
            //混沌
            for (int i = iteration_start_num; i <= iteration_end_num; i++) {
                double c =  a*(a+b) - b*(a-b) + x, d = 2*a * b/(a+b)*(a-b) + y;
                a = c, b = d;
                if (a * a + b * b > 4) {
                    param.n=i / (double)iteration_end_num;
                    break;
                }
            }
            break;
        case 9:
            //茱莉亚集合
            for (int i = iteration_start_num; i <= iteration_end_num; i++) {
                double c = a*a-b*b - 0.70176, d = 2*a*b -0.3842;
                a = c, b = d;
                if (a * a + b * b > 4) {
                    param.n=i / (double)iteration_end_num;
                    break;
                }
            }
            break;
        case 10:
            //牛顿分形 f(z)=z^3-1 x_n+1=x_n-f(x_n)/f'(x_n)
            for (int i = iteration_start_num; i <= iteration_end_num; i++) {
                double c =a-(2*a*b*(3*pow(a,2)*b-pow(b,3))+(pow(a,2)-pow(b,2))*((-3*a*pow(b,2))+pow(a,3)-1))/(3*pow((pow(b,2)+pow(a,2)),2)),
                        d =b-((pow(a,2)-pow(b,2))*(3*pow(a,2)*b-pow(b,3))-2*a*b*((-3*a*pow(b,2))+pow(a,3)-1))/(3*pow((pow(b,2)+pow(a,2)),2));
                a = c, b = d;
                if (a * a + b * b > 4) {
                    param.n=i / (double)iteration_end_num;
                    break;
                }
            }
            break;
        case -1://(一个圆，用来分析渲染模式)
            for (int i = iteration_start_num; i <= iteration_end_num; i++) {
                double c = a+x, d = b+y;
                a = c, b = d;
                if (a * a + b * b > 4) {
                    param.n=i / (double)iteration_end_num;
                    break;
                }
            }
            break;
        default:
            break;
    }

    if(!color_reversal&&param.n!=0) { param.n=1-param.n; }
    param.a=a;
    param.b=b;
    return param;
}

struct param_for_auto_iteration mandelbrot
        (double x, double y,int id,int color_reversal,int iteration_times)
{
    return mandelbrot_continue(x,y,x,y,id,color_reversal,1,iteration_times);
}

void write_data(uint8_t* p ,int n,int generate_mode){
    switch (generate_mode) {
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

void* multithread_generate(void* arg) {
    struct param_for_thread tmp = *(struct param_for_thread*)arg;

    int thread_num = tmp.use_thread;
    uint8_t* p = tmp.p;


    int one_part_of_temp_i = (tmp.PIXEL_Y ) / thread_num;
    int temp_i_start = tmp.thread_id* one_part_of_temp_i;

    p += temp_i_start* tmp.PIXEL_X*3;
    int temp_i_end = temp_i_start + one_part_of_temp_i;
    if (tmp.thread_id == thread_num-1) {
        //处理不能被整除的情况(多出来的全部由最后一个线程渲染)
        temp_i_end += tmp.PIXEL_Y % thread_num;
    }

    int temp_i = tmp.PIXEL_Y;
    int temp_j = tmp.PIXEL_X;
    double temp_scale = 512.0 * (tmp.scale);

    for (int i = temp_i_start; i < temp_i_end; ++i){
        for (int j = 0; j < temp_j; ++j){
            struct param_for_auto_iteration param_continue=mandelbrot
                    ((tmp.CENTER_X)-temp_j / (2 * (temp_scale)) + j / (temp_scale),
                            (tmp.CENTER_Y)-temp_i / (2 * temp_scale) + i / temp_scale,
                            tmp.id, tmp.color_reversal, tmp.iteration_times);

            uint8_t n = param_continue.n * 255;


            //自适应迭代
            if(tmp.auto_iteration_max>0&&n==0&&tmp.iteration_times<tmp.auto_iteration_max){
                int it_start=tmp.iteration_times;
                int it_end=it_start*5;
                while(it_end<tmp.auto_iteration_max) {
                    //对黑色的地方提高迭代次数
                    if (n == 0) {
                        param_continue = mandelbrot_continue((tmp.CENTER_X) - temp_j / (2 * (temp_scale)) + j / (temp_scale),
                                                             (tmp.CENTER_Y) - temp_i / (2 * temp_scale) + i / temp_scale,
                                                             param_continue.a, param_continue.b, tmp.id, tmp.color_reversal,
                                                             it_start, it_end);
                        n= param_continue.n * 255;
                        it_start=it_end;
                        it_end*=5;
                    }
                    else{
                        break;
                    }
                }
                if (n == 0) {
                    //对还是黑色的地方进行最后一次迭代
                    n = mandelbrot_continue((tmp.CENTER_X) - temp_j / (2 * (temp_scale)) + j / (temp_scale),
                                            (tmp.CENTER_Y) - temp_i / (2 * temp_scale) + i / temp_scale,
                                            param_continue.a, param_continue.b, tmp.id, tmp.color_reversal,
                                            it_start, tmp.auto_iteration_max).n * 255;
                }
            }

            write_data(p,n,tmp.generate_mode);
            p+=3;
        }
    }
}

void generate(char *file_path, int PIXEL_Y,int PIXEL_X,
        double CENTER_X,double CENTER_Y,double SCALE_times,
        int id,int color_reversal,int generate_mode,
        int iteration_times,int use_thread,int auto_iteration_max)
{
    uint8_t *data = (uint8_t *)malloc((PIXEL_Y) * (PIXEL_X) * 3);

    //多线程
    if (use_thread>1&&PIXEL_Y>=10) {
        pthread_t my_thread[10];

        struct param_for_thread param1[10];
        //这里暂时没想到更好的办法，先将就下(这里除了thread_id以外都可以只传1次)
        for (int i = 0;i < use_thread;i++) {
            param1[i].id = id;
            param1[i].color_reversal = color_reversal;
            param1[i].generate_mode = generate_mode;
            param1[i].iteration_times = iteration_times;
            param1[i].scale = SCALE_times;
            param1[i].thread_id = i;
            param1[i].p= data;
            param1[i].PIXEL_X= PIXEL_X;
            param1[i].PIXEL_Y= PIXEL_Y;
            param1[i].CENTER_X= CENTER_X;
            param1[i].CENTER_Y= CENTER_Y;
            param1[i].use_thread= use_thread;
            param1[i].auto_iteration_max= auto_iteration_max;
            pthread_create(&my_thread[i], NULL, multithread_generate, &param1[i]);
        }

        for (int i = 0;i < use_thread;i++) {
            pthread_join(my_thread[i], NULL);
        }
        FILE *file = fopen(file_path, "wb");
        svpng(file, (PIXEL_X), (PIXEL_Y), data, 0);
        fclose(file);
        free(data);
        return;
    }

    //普通
    else {
        uint8_t *p=data;
        double temp_i = (PIXEL_Y);
        double temp_j = (PIXEL_X);
        double temp_scale = 512.0 * (SCALE_times);

        for (int i = 0; i < temp_i; ++i) {
            for (int j = 0; j < temp_j; ++j) {
                struct param_for_auto_iteration param_continue=mandelbrot
                        ((CENTER_X) - temp_j / (2 * (temp_scale)) + j / (temp_scale),
                        (CENTER_Y) - temp_i / (2 * temp_scale) + i / temp_scale,
                        id, color_reversal, iteration_times);

                uint8_t n = param_continue.n * 255;
                //越接近mandelbrot集合内，则n值越高   但在集合内,n=0   n=255时 为白

                if(auto_iteration_max>0&&n==0&&iteration_times<auto_iteration_max){
                    int it_start=iteration_times;
                    int it_end=it_start*5;
                    while(it_end<auto_iteration_max) {
                        //对黑色的地方提高迭代次数
                        if (n == 0) {
                            param_continue = mandelbrot_continue((CENTER_X) - temp_j / (2 * (temp_scale)) + j / (temp_scale),
                                                                 (CENTER_Y) - temp_i / (2 * temp_scale) + i / temp_scale,
                                                                 param_continue.a, param_continue.b, id, color_reversal,
                                                                 it_start, it_end);
                            n= param_continue.n * 255;
                            it_start=it_end;
                            it_end*=5;
                        }
                        else{
                            break;
                        }
                    }
                    if (n == 0) {
                        //对还是黑色的地方进行最后一次迭代
                        n = mandelbrot_continue((CENTER_X) - temp_j / (2 * (temp_scale)) + j / (temp_scale),
                                                (CENTER_Y) - temp_i / (2 * temp_scale) + i / temp_scale,
                                                param_continue.a, param_continue.b, id, color_reversal,
                                                it_start, auto_iteration_max).n * 255;
                    }
                }

                write_data(p,n,generate_mode);
                p+=3;
            }
        }
        FILE *file = fopen(file_path, "wb");
        svpng(file, (PIXEL_X), (PIXEL_Y), data, 0);
        fclose(file);
        free(data);
        return;
    }
}


JNIEXPORT void JNICALL
Java_com_zjh_fractal_MainActivity_GenerateFractal(JNIEnv *env, jobject thiz, jstring path,
                                                  jint screen_height, jint screen_width,
                                                  jdouble center_x, jdouble center_y,
                                                  jdouble scale_times,jint fractal_id,
                                                  jint color_reversal,jint generate_mode,
                                                  jint iteration_times,jint use_thread
                                                  ,jint auto_iteration_max) {
    char *file_path = (*env)->GetStringUTFChars(env, path, JNI_FALSE);
    generate(file_path, screen_height, screen_width, center_x, center_y,
            scale_times,fractal_id,color_reversal,generate_mode,
            iteration_times,use_thread,auto_iteration_max);
}