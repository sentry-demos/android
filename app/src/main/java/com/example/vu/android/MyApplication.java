package com.example.vu.android;

import android.app.Application;

import java.util.List;

//import io.sentry.Sentry;
import io.sentry.android.core.SentryAndroid;
import io.sentry.SentryLevel;
import io.sentry.protocol.SentryException;
import io.sentry.protocol.User;
import io.sentry.*;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // SENTRY
        SentryAndroid.init(this, options -> {

            // This callback is used before the event is sent to Sentry.
            // You can modify the event or, when returning null, also discard the event.
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
            options.setTracesSampleRate(1.0);
        });

        SentryTransaction activityTransaction = Sentry.startTransaction("MainActivity.onCreate");
        activityTransaction.finish();

        // we now enable this in AndroidManifest.xml
        // options.setEnableSessionTracking(true);
    }

}
