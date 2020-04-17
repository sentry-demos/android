package com.example.vu.android;

import android.app.Application;

import java.util.List;

import io.sentry.android.core.SentryAndroid;
import io.sentry.core.SentryLevel;
import io.sentry.core.protocol.SentryException;
import io.sentry.core.protocol.User;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // SENTRY
        SentryAndroid.init(this, options -> {


            // This callback is used before the event is sent to Sentry.
            // You can modify the event or, when returning null, also discard the event.
            options.setEnableSessionTracking(true);
            options.setBeforeSend((event, hint) -> {

                //Remove PII
                List<SentryException> exceptions = event.getExceptions();
                if(exceptions.size() > 0){
                    SentryException exception = exceptions.get(0);
                    if(exception.getType().contains("NegativeArraySizeException")){
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
