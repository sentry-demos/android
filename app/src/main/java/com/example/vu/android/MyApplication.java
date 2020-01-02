package com.example.vu.android;

import android.app.Application;
import android.os.StrictMode;

import io.sentry.android.core.SentryAndroid;
import io.sentry.core.SentryLevel;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        districtMode();
        super.onCreate();

        // SENTRY
        SentryAndroid.init(this, options -> {
            // This callback is used before the event is sent to Sentry.
            // You can modify the event or, when returning null, also discard the event.
            options.setBeforeSend((event, hint) -> {
                if (SentryLevel.DEBUG.equals(event.getLevel()))
                    return null;
                else
                    return event;
            });
        });
    }

    private void districtMode() {
        //    https://developer.android.com/reference/android/os/StrictMode
        //    StrictMode is a developer tool which detects things you might be doing by accident and
        //    brings them to your attention so you can fix them.
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                    new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());

            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build());
        }
    }
}
