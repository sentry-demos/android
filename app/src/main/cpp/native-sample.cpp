#include <jni.h>
#include <android/log.h>
#include <sentry.h>
#include <stdexcept>
#include <string>

#define TAG "sentry-nativesample"

extern "C" {

JNIEXPORT void JNICALL Java_com_example_vu_android_NativeSample_crash(JNIEnv *env, jclass cls) {
    __android_log_print(ANDROID_LOG_WARN, TAG, "About to crash.");

//    sentry_value_t user = sentry_value_new_object();
//    sentry_value_set_by_key(user, "id", sentry_value_new_int32(42));
//    sentry_value_set_by_key(user, "email", sentry_value_new_string("john.doe@example.org"));
//    sentry_value_set_by_key(user, "username", sentry_value_new_string("John Doe"));
//    sentry_set_user(user);

    sentry_set_tag("transaction_id", "lkdfjs4001");


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