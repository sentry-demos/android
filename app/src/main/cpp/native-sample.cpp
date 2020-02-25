#include <jni.h>
#include <android/log.h>
#include <sentry.h>
#include <stdexcept>
#include <string>

#define TAG "sentry-nativesample"

extern "C" {

JNIEXPORT void JNICALL Java_com_example_vu_android_NativeSample_crash(JNIEnv *env, jclass cls) {
    __android_log_print(ANDROID_LOG_WARN, TAG, "About to crash.");
    char *ptr = 0;
    *ptr += 1;
}



JNIEXPORT void JNICALL Java_com_example_vu_android_NativeSample_message(JNIEnv *env, jclass cls) {
    __android_log_print(ANDROID_LOG_WARN, TAG, "Sending a message.");
    sentry_value_t event = sentry_value_new_message_event(
      /*   level */ SENTRY_LEVEL_INFO,
      /*  logger */ "custom",
      /* message */ "Sentry Message - It works!"
    );
    sentry_capture_event(event);
}


}