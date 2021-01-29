#include <stdint.h>
#include <stdio.h>
#include <fcntl.h>
#include "svpng.inc"
#include <jni.h>
#include <stdlib.h>
#include <math.h>

double get_absolute_value(double x){
    if(x>0)return x;
    else return -x;
}

double mandelbrot(double x, double y,int id,int color_reversal,int iteration_times)
{
    double a = x, b = y;
    switch(id){
        // x^2
        case 0:
            for (int i = 0; i < iteration_times; ++i) {
                double c = a*a-b*b +x, d = 2*a*b +y;
                a =c, b = d;
                if (a * a + b * b >4) {
                    if (color_reversal) { return i / (double)iteration_times; }
                    else return 1 - i / (double)iteration_times;
                }
            }
            break;
        case 1:
            //x^3
            for (int i = 0; i < iteration_times; ++i) {
                double  c = a*a*a-3*a*b*b + x,d = 3*a*a*b-b*b*b + y;
                a = c, b = d;
                if (a * a + b * b > 4) {
                    if (color_reversal) {return i / (double)iteration_times; }
                    else return 1 - i / (double)iteration_times;
                }
            }
            break;

        case 2:
            // x^4
            for (int i = 0; i < iteration_times; ++i) {
                double  c = a*a*a*a-6*a*a*b*b+b*b*b*b + x,d = 4*a*a*a*b-4*b*b*b*a + y;
                a = c, b = d;
                if (a * a + b * b > 4) {
                    if (color_reversal) {return i / (double)iteration_times; }
                    else return 1 - i / (double)iteration_times;
                }
            }
            break;
        case 3:
            // x^4+x^3+x^2
            for (int i = 0; i < iteration_times; ++i) {
                double  c = a*a*a*a-6*a*a*b*b+b*b*b*b+a*a*a-3*a*b*b+a*a-b*b+x,
                        d = 4*a*a*a*b-4*b*b*b*a+3*a*a*b-b*b*b+2*a*b+y;
                a = c, b = d;
                if (a * a + b * b > 4) {
                    if (color_reversal) {return i / (double)iteration_times; }
                    else return 1 - i / (double)iteration_times;
                }
            }
            break;

        case 4:
            //x^2-x^3
            for (int i = 0; i < iteration_times; ++i) {
                double c = a * a - b * b-(a * a * a - 3*a*b*b)+ x, d = 2 * a * b -(3*a*a*b-b*b*b)+ y;
                a = c, b = d;
                if (a * a + b * b > 4) {
                    if (color_reversal) {return i / (double)iteration_times; }
                    else return 1 - i / (double)iteration_times;
                }
            }
            break;

        case 5:
            //The Burning Ship fractal
            for (int i = 0; i < iteration_times; ++i) {
                double c =a * a - b * b  + x, d = 2*get_absolute_value(a)*get_absolute_value(b)  + y;
                a = c, b = d;
                if (a * a + b * b > 4) {
                    if (color_reversal) {return i / (double)iteration_times; }
                    else return 1 - i / (double)iteration_times;
                }
            }
            break;

        case 6:
            //无意义的混沌
            for (int i = 0; i < iteration_times; ++i) {
                double c = a * a - b * b / a+ x, d = 2 * a * b + y;
                a = c, b = d;
                if (a * a + b * b > 4) {
                    if (color_reversal) { return i / (double)iteration_times; }
                    else return 1 - i / (double)iteration_times;
                }
            }
            break;
        case 7:
            // 一个混沌
            for (int i = 0; i < iteration_times; ++i) {
                double  c = a * a - b * b + x, d = 2 * a * b/(a+b) + y;
                a = c, b = d;
                if (a * a + b * b > 4) {
                    if (color_reversal) {return i / (double)iteration_times; }
                    else return 1 - i / (double)iteration_times;
                }
            }
            break;
        case 8:
            //混沌
            for (int i = 0; i < iteration_times; ++i) {
                double c =  a*(a+b) - b*(a-b) + x, d = 2*a * b/(a+b)*(a-b) + y;
                a = c, b = d;
                if (a * a + b * b > 4) {
                    if (color_reversal) {return i / (double)iteration_times; }
                    else return 1 - i /(double)iteration_times;
                }
            }
            break;
        case 9:
            //茱莉亚集合
            for (int i = 0; i < iteration_times; ++i) {
                double c = a*a-b*b - 0.70176, d = 2*a*b -0.3842;
                a = c, b = d;
                if (a * a + b * b > 4) {
                    if (color_reversal) {return i / (double)iteration_times; }
                    else return 1 - i /(double)iteration_times;
                }
            }
            break;
        case 10:
            //牛顿分形 f(z)=z^3-1 x_n+1=x_n-f(x_n)/f'(x_n)
            for (int i = 0; i < iteration_times; ++i) {
                double c =a-(2*a*b*(3*pow(a,2)*b-pow(b,3))+(pow(a,2)-pow(b,2))*((-3*a*pow(b,2))+pow(a,3)-1))/(3*pow((pow(b,2)+pow(a,2)),2)),
                d =b-((pow(a,2)-pow(b,2))*(3*pow(a,2)*b-pow(b,3))-2*a*b*((-3*a*pow(b,2))+pow(a,3)-1))/(3*pow((pow(b,2)+pow(a,2)),2));
                a = c, b = d;
                if (a * a + b * b > 4) {
                    if (color_reversal) {return i / (double)iteration_times; }
                    else return 1 - i /(double)iteration_times;
                }
            }
            break;
        case -1://(一个圆，用来分析渲染模式)
            for (int i = 0; i < iteration_times; ++i) {
                double c = a+x, d = b+y;
                a = c, b = d;
                if (a * a + b * b > 4) {
                    if (color_reversal) {return i / (double)iteration_times; }
                    else return 1 - i / (double)iteration_times;
                }
            }
            break;
        default:
            break;
    }
    return 0;
}

void generate(char *file_path, int PIXEL_Y,
              int PIXEL_X, double CENTER_X,
              double CENTER_Y,
              double SCALE_times,int id,
              int color_reversal,int generate_mode,
              int iteration_times)
{
    uint8_t data[(PIXEL_Y) * (PIXEL_X)*3];
    uint8_t *p = data;

    double temp_i = (PIXEL_Y);
    double temp_j = (PIXEL_X);
    double temp_scale = 512.0 * (SCALE_times);

    for (int i = 0; i < temp_i; ++i)
    {
        for (int j = 0; j < temp_j; ++j)
        {

            uint8_t n = mandelbrot((CENTER_X)-temp_j / (2*( temp_scale)) + j / (temp_scale),
                                   (CENTER_Y)-temp_i / (2 * temp_scale) + i / temp_scale,
                                   id,color_reversal,iteration_times) *255;
            //越接近mandelbrot集合内，则n值越高

            switch(generate_mode){
                case 0:
                    *p++ = 4*n;
                    *p++ = 2*n;
                    *p++ = n;
                    break;
                case 1:
                    *p++ = 4*n;
                    *p++ = n;
                    *p++ = 2*n;
                    break;
                case 2:
                    *p++ = 2*n;
                    *p++ = 4*n;
                    *p++ = n;
                    break;
                case 3:
                    *p++ = n;
                    *p++ = 4*n;
                    *p++ = 2*n;
                    break;
                case 4:
                    *p++ = n;
                    *p++ = 2*n;
                    *p++ = 4*n;
                    break;
                case 5:
                    *p++ = 2*n;
                    *p++ = n;
                    *p++ = 4*n;
                    break;
                case 6:
                    *p++ = 16*n;
                    *p++ = 4*n;
                    *p++ = n;
                    break;
                case 7:
                    *p++ = 2*n;
                    *p++ = 4*n;
                    *p++ = 16*n;
                    break;
                case 8:
                    *p++ = 8*n;
                    *p++ = 16*n;
                    *p++ = 2*n;
                    break;
                case 9:
                    *p++ = 4*n;
                    *p++ = 16*n;
                    *p++ = n;
                    break;
                case 10:
                    *p++ = 32*n;
                    *p++ = 2*n;
                    *p++ = 8*n;
                    break;
                case 11:
                    *p++ = 8*n;
                    *p++ = 64*n;
                    *p++ = n;
                    break;
                case 12:
                    *p++ = 32*n;
                    *p++ = 64*n;
                    *p++ = 4*n;
                    break;
                case 13:
                    *p++ = 64*n;
                    *p++ = 16*n;
                    *p++ = 32*n;
                    break;
                case 14:
                    *p++ = 8*n;
                    *p++ = 16*n;
                    *p++ = 64*n;
                    break;
                case 15:
                    *p++ = 16*n;
                    *p++ = 2*n;
                    *p++ = 64*n;
                    break;
                case 16:
                    *p++ = 64*n;
                    *p++ = 32*n;
                    *p++ = 8*n;
                    break;
                case 17:
                    *p++ = 64*n;
                    *p++ = 4*n;
                    *p++ = 8*n;
                    break;
                case 18:
                    *p++ = 32*n;
                    *p++ = 64*n;
                    *p++ = 16*n;
                    break;
                case 19:
                    *p++ = 32*n;
                    *p++ = 64*n;
                    *p++ = 128*n;
                    break;
                case 20:
                    *p++ = 128*n;
                    *p++ = 4*n;
                    *p++ = 30*n;
                    break;
                case -1 :
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
    }

    FILE *file = fopen(file_path, "wb");
    svpng(file, (PIXEL_X), (PIXEL_Y), data, 0);
    fclose(file);
}


JNIEXPORT void JNICALL
Java_com_zjh_fractal_MainActivity_GenerateFractal(JNIEnv *env, jobject thiz, jstring path,
                                                  jint screen_height, jint screen_width,
                                                  jdouble center_x, jdouble center_y,
                                                  jdouble scale_times,jint fractal_id,
                                                  jint color_reversal,jint generate_mode,
                                                  jint iteration_times) {
    char *file_path = (*env)->GetStringUTFChars(env, path, JNI_FALSE);
    generate(file_path, screen_height, screen_width, center_x, center_y,
            scale_times,fractal_id,color_reversal,generate_mode,iteration_times);
}