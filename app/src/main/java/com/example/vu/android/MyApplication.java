package com.example.vu.android;

import android.app.Application;

import java.util.List;

import io.sentry.android.core.SentryAndroid;
import io.sentry.SentryLevel;
import io.sentry.android.timber.SentryTimberIntegration;
import io.sentry.protocol.SentryException;
import io.sentry.protocol.User;
import timber.log.Timber;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Timber.plant(new Timber.DebugTree());

        // SENTRY
        SentryAndroid.init(this, options -> {

            options.setEnvironment("Staging");

            // default values:
            // minEventLevel = ERROR
            // minBreadcrumbLevel = INFO
            options.addIntegration(
                        new SentryTimberIntegration(SentryLevel.ERROR, SentryLevel.INFO));

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
