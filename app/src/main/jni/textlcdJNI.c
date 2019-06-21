//
// Created by DKU on 2019-06-11.
//

#include <unistd.h>
#include <fcntl.h>
#include <stdio.h>
#include <assert.h>
#include <jni.h>
#include <string.h>
#include <termios.h>
#include <stdlib.h>

#include "com_example_jjm_application_MainActivity.h"
#include "com_example_jjm_application_PlayActivity.h"

#include "textlcd.h"
#define TEXTLCD_DD_ADDRESS  _IOW(0xbc,7,int)


static int fd;
static struct strcommand_variable strcommand;

JNIEXPORT void JNICALL Java_com_example_jjm_1application_TextlcdJNI_on
        (JNIEnv * env, jobject obj){
    if (fd == 0)
        fd = open("/dev/fpga_textlcd", O_WRONLY);
    assert(fd != 0);

    ioctl(fd, TEXTLCD_ON);
}

JNIEXPORT void JNICALL Java_com_example_jjm_1application_TextlcdJNI_off
        (JNIEnv * env, jobject obj){
    if (fd )
    {
        ioctl(fd, TEXTLCD_OFF);
        close(fd);
    }

    fd = 0;
}

JNIEXPORT void JNICALL Java_com_example_jjm_1application_TextlcdJNI_initialize
        (JNIEnv * env, jobject obj){
    if (fd == 0)
        fd = open("/dev/fpga_textlcd", O_WRONLY);
    assert(fd != -1);

    strcommand.rows = 0;
    strcommand.nfonts = 0;
    strcommand.display_enable = 1;
    strcommand.cursor_enable = 0;

    strcommand.nblink = 0;
    strcommand.set_screen = 0;
    strcommand.set_rightshit = 1;
    strcommand.increase = 1;
    strcommand.nshift = 0;
    strcommand.pos = 10;
    strcommand.command = 1;
    strcommand.strlenght = 16;

    ioctl(fd, TEXTLCD_INIT);
}

JNIEXPORT void JNICALL Java_com_example_jjm_1application_TextlcdJNI_clear
        (JNIEnv * env, jobject obj){
    //if (fd )
    ioctl(fd, TEXTLCD_CLEAR);
}

JNIEXPORT void JNICALL Java_com_example_jjm_1application_TextlcdJNI_print1Line
        (JNIEnv * env, jobject obj, jstring msg){
    const char *str;

    if (fd )
    {
        str = (*env)->GetStringUTFChars(env, msg, 0);
        ioctl(fd, TEXTLCD_CLEAR);
        strcommand.pos = 0;
        ioctl(fd,TEXTLCD_DD_ADDRESS,&strcommand,32);
        //ioctl(fd, TEXTLCD_LINE1);
        write(fd, str, strlen(str));
        (*env)->ReleaseStringUTFChars(env, msg, str);
    }
}

JNIEXPORT void JNICALL Java_com_example_jjm_1application_TextlcdJNI_print2Line
        (JNIEnv * env, jobject obj, jstring msg){
    const char *str;

    if (fd )
    {
        str = (*env)->GetStringUTFChars(env, msg, 0);
        ioctl(fd, TEXTLCD_CLEAR);
        strcommand.pos = 40;
        ioctl(fd,TEXTLCD_DD_ADDRESS,&strcommand,32);
        //ioctl(fd, TEXTLCD_LINE2);
        write(fd, str, strlen(str));
        (*env)->ReleaseStringUTFChars(env, msg, str);
    }
}
