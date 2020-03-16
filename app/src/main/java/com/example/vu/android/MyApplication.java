package com.example.vu.android;

import android.app.Application;

import io.sentry.android.core.SentryAndroid;
import io.sentry.core.SentryLevel;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // SENTRY
        SentryAndroid.init(this, options -> {


            // This callback is used before the event is sent to Sentry.
            // You can modify the event or, when returning null, also discard the event.
            options.setBeforeSend((event, hint) -> {
//                String environment = event.getEnvironment();
//                if (environment == null || environment.equals("TEST"))
                if (SentryLevel.DEBUG.equals(event.getLevel()))
                    return null;
                else
                    return event;
            });
        });
    }

}
