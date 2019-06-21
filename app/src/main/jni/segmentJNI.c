//
// Created by DKU on 2019-06-10.
//

#include "com_example_jjm_application_MainActivity.h"
#include "com_example_jjm_application_PlayActivity.h"

#include <time.h>
#include <unistd.h>
#include <fcntl.h>
#include <stdio.h>
#include <assert.h>
#include <jni.h>

static int fd;

JNIEXPORT void JNICALL Java_com_example_jjm_1application_SegmentJNI_open
        (JNIEnv * env, jobject obj){
    fd = open("/dev/fpga_segment", O_WRONLY);
    assert(fd != -1);
}

JNIEXPORT void JNICALL Java_com_example_jjm_1application_SegmentJNI_print
        (JNIEnv * env, jobject obj, jint num){
    int buf = num;
    //sprintf(buf, "%06d", num);
    printf(stdout, "num: %d\n", num);
    write(fd, &buf, 4);
}

JNIEXPORT void JNICALL Java_com_example_jjm_1application_SegmentJNI_close
        (JNIEnv * env, jobject obj){
    close(fd);
}
