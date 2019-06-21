//
// Created by DKU on 2019-06-11.
//

#include <unistd.h>
#include <fcntl.h>
#include <stdio.h>
#include <assert.h>
#include <jni.h>
#include "com_example_jjm_application_MainActivity.h"
#include "com_example_jjm_application_PlayActivity.h"

static int fd2;

JNIEXPORT void JNICALL Java_com_example_jjm_1application_PiezoJNI_open
(JNIEnv * env, jobject obj){
fd2 = open("/dev/fpga_piezo", O_WRONLY);
assert(fd2 != -1);
}

JNIEXPORT void JNICALL Java_com_example_jjm_1application_PiezoJNI_write
(JNIEnv * env, jobject obj, jint data){
write(fd2, &data, 1);
}

JNIEXPORT void JNICALL Java_com_example_jjm_1application_PiezoJNI_close
(JNIEnv * env, jobject obj){
close(fd2);
}