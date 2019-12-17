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

JNIEXPORT void JNICALL Java_com_example_vu_android_NativeSample_handledCrash(JNIEnv *env, jclass cls) {
    __android_log_print(ANDROID_LOG_WARN, TAG, "About to crash.");
    try {
        std::string("abc").substr(10);
    } catch (const std::exception& e) {
        sentry_value_t exc_item = sentry_value_new_object();
        sentry_value_set_by_key(exc_item, "type", sentry_value_new_string("Exception"));
        sentry_value_set_by_key(exc_item, "value", sentry_value_new_string(e.what()));
        sentry_value_t values = sentry_value_new_list();
        sentry_value_set_by_index(values, 0, exc_item);
        sentry_value_t event = sentry_value_new_event();
        sentry_value_t exc = sentry_value_new_object();
        sentry_value_set_by_key(exc, "values", values);
        sentry_value_set_by_key(event, "exception", exc);
        sentry_capture_event(event);
    }
}

JNIEXPORT void JNICALL Java_com_example_vu_android_NativeSample_message(JNIEnv *env, jclass cls) {
    __android_log_print(ANDROID_LOG_WARN, TAG, "About to crash.");
    sentry_value_t event = sentry_value_new_message_event(
      /*   level */ SENTRY_LEVEL_INFO,
      /*  logger */ "custom",
      /* message */ "It works!"
    );
    sentry_capture_event(event);
}

}