//
// Created by DKU on 2019-06-11.
//

#include <unistd.h>
#include <fcntl.h>
#include <string.h>
#include <stdio.h>
#include <assert.h>
#include <jni.h>

#include "com_example_jjm_application_MainActivity.h"
#include "com_example_jjm_application_PlayActivity.h"


int fd;
JNIEXPORT void JNICALL Java_com_example_jjm_1application_DotmatrixJNI_open(
        JNIEnv* env, jobject thiz) {
    fd = open("/dev/fpga_dotmatrix", O_RDWR | O_SYNC);

}
JNIEXPORT void JNICALL Java_com_example_jjm_1application_DotmatrixJNI_DotMatrixControl(
        JNIEnv* env, jobject thiz, jstring str)
{
    const char *pStr;
    int len;

    pStr = (*env)->GetStringUTFChars(env, str, 0);
    len = (*env)->GetStringLength(env, str);

    write(fd, pStr, len);

    //(*env)->ReleaseStringUTFChars(env, str, pStr);
}
JNIEXPORT void JNICALL Java_com_example_jjm_1application_DotmatrixJNI_close(
        JNIEnv* env, jobject thiz){
    close(fd);
}