//
// Created by DKU on 2019-06-11.
//

#include <unistd.h>
#include <fcntl.h>
#include <assert.h>
#include <jni.h>
#include "com_example_jjm_application_MainActivity.h"
#include "com_example_jjm_application_PlayActivity.h"

JNIEXPORT void JNICALL Java_com_example_jjm_1application_LedJNI_on
        (JNIEnv * env, jobject obj, jint data){
    int fd;

    fd = open("/dev/fpga_led", O_WRONLY);
    assert(fd != 0);

    write(fd, &data, 1);
    close(fd);
}
