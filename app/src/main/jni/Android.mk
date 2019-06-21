LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := segmentJNI
LOCAL_SRC_FILES := segmentJNI.c
LOCAL_LDLIBS := -llog
include $(BUILD_SHARED_LIBRARY)


include $(CLEAR_VARS)
LOCAL_MODULE := piezoJNI
LOCAL_SRC_FILES := piezoJNI.c
LOCAL_LDLIBS := -llog
include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := ledJNI
LOCAL_SRC_FILES := ledJNI.c
LOCAL_LDLIBS := -llog
include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := textlcdJNI
LOCAL_SRC_FILES := textlcdJNI.c
LOCAL_LDLIBS := -llog
include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := dotmatrixJNI
LOCAL_SRC_FILES := dotmatrixJNI.c
LOCAL_LDLIBS := -llog
include $(BUILD_SHARED_LIBRARY)