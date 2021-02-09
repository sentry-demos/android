package com.example.vu.android;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import java.util.List;

import io.sentry.EventProcessor;
import io.sentry.ISpan;
import io.sentry.ITransaction;
import io.sentry.Sentry;
import io.sentry.SentryEvent;
import io.sentry.android.core.SentryAndroid;
import io.sentry.SentryLevel;
import io.sentry.protocol.SentryException;
import io.sentry.protocol.User;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        SentryAndroid.init(this, options -> {

            // This callback is used before the event is sent to Sentry.
            // You can modify the event or, when returning null, also discard the event.

            // we now enable this in AndroidManifest.xml
            // options.setEnableSessionTracking(true);
            // To set a uniform sample rate
            //options.setTracesSampleRate(1.0);
            // OR if you prefer, determine traces sample rate based on the sampling context
//            options.setTracesSampler(
//                    context -> {
//                        // return a number between 0 and 1
//                    });

            options.setAttachThreads(true);
            options.setAttachStacktrace(true);
            options.setBeforeSend((event, hint) -> {

                //Remove PII
                List<SentryException> exceptions = event.getExceptions();
                if(exceptions != null && exceptions.size() > 0){
                    SentryException exception = exceptions.get(0);
                    if("NegativeArraySizeException".equals(exception.getType())) {
                        User user = event.getUser();
                        user.setIpAddress(null);
                    }
                }

                //Drop event
                if (SentryLevel.DEBUG.equals(event.getLevel()))
                    return null;
                else
                    return event;
            });
        });

    }

}
