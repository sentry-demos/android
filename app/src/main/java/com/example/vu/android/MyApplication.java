package com.example.vu.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import io.sentry.android.fragment.FragmentLifecycleIntegration;

import java.util.Arrays;
import java.util.List;

import io.sentry.Sentry;
import io.sentry.UserFeedback;
import io.sentry.android.core.SentryAndroid;
import io.sentry.SentryLevel;
import io.sentry.protocol.SentryException;
import io.sentry.protocol.SentryId;
import io.sentry.protocol.User;

import static android.content.ContentValues.TAG;


public class MyApplication extends Application {

    private MyBaseActivity mCurrentActivity = null;

    public MyBaseActivity getCurrentActivity () {
        return mCurrentActivity ;
    }
    public void setCurrentActivity (MyBaseActivity mCurrentActivity) {
        this.mCurrentActivity = mCurrentActivity ;
    }

    private String getApplicationName() {
        Context  context = this.getApplicationContext();
        return context.getApplicationInfo().loadLabel(context.getPackageManager()).toString();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i(TAG, BuildConfig.SE);
        String SE = BuildConfig.SE;

        SentryAndroid.init(this, options -> {

            try {
                PackageInfo pInfo = this.getPackageManager().getPackageInfo(this.getPackageName(), 0);
                String version = pInfo.versionName;
                options.setTag("versionName", pInfo.versionName);
                options.setTag("applicationName", this.getApplicationName());
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            options.addIntegration(
                    new FragmentLifecycleIntegration(
                            MyApplication.this,
                            true,
                            true
                    )
            );

            options.setAttachThreads(true);
            options.setEnableAppStartProfiling(true);
            options.setEnablePerformanceV2(true);

            // Currently under experimental options:
            options.getExperimental().getSessionReplay().setSessionSampleRate(1.0);
            options.getExperimental().getSessionReplay().setErrorSampleRate(1.0);

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

                SentryException currentException = event.getExceptions().get(0);
                if(currentException != null && currentException.getType().endsWith("BackendAPIException")){
                    this.launchUserFeedback(event.getEventId());
                }

                //event.setExtra("fullStoryURL", this.mCurrentActivity.getFullStorySessionURL());

                if (SE == "tda") {
                    event.setFingerprints(Arrays.asList("{{ default }}", SE, BuildConfig.VERSION_NAME));
                } else if (SE != null || SE.length() != 0) {
                    event.setFingerprints(Arrays.asList("{{ default }}", SE));
                }

                //Drop event
                if (SentryLevel.DEBUG.equals(event.getLevel()))
                    return null;
                else
                    return event;

            });
        });

        String[] allCustomerTypes = {"medium-plan", "large-plan", "small-plan", "enterprise"};
        String customerType = allCustomerTypes[(int) (Math.random() * 4)];
        Sentry.setTag("customerType", customerType);
        Sentry.setTag("se", SE);

        // Set User info on Sentry event using a random email
        String AlphaNumericString = "abcdefghijklmnopqrstuvxyz0123456789";
        Integer n = 4;
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            int index = (int)(AlphaNumericString.length() * Math.random());
            sb.append(AlphaNumericString.charAt(index));
        }
        String email = sb.toString() + "@gmail.com";

        User user = new User();
        user.setEmail(email);
        Sentry.setUser(user);
    }

    private void launchUserFeedback(SentryId sentryId){

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mCurrentActivity);
        final EditText editTextName1 = new EditText(MyApplication.this);
        editTextName1.setHint("OMG! What happened??");

        LinearLayout layoutName = new LinearLayout(this);
        layoutName.setOrientation(LinearLayout.VERTICAL);
        layoutName.setPadding(60, 20, 60, 20);
        layoutName.addView(editTextName1);
        alertDialogBuilder.setView(layoutName);

        alertDialogBuilder.setTitle("Ooops, Checkout Failed!");
        alertDialogBuilder.setPositiveButton("Submit", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                Toast.makeText(mCurrentActivity,"Thank you!",Toast.LENGTH_LONG).show();
                String txt = editTextName1.getText().toString(); // variable to collect user input

                UserFeedback userFeedback = new UserFeedback(sentryId);
                userFeedback.setComments( txt );
                userFeedback.setEmail("john.doe@example.com");
                userFeedback.setName("John Doe");
                Sentry.captureUserFeedback(userFeedback);

            }
        });

        alertDialogBuilder.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }


}