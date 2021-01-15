package com.example.vu.android;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import io.sentry.Breadcrumb;
import io.sentry.Sentry;
import io.sentry.SentryLevel;
import io.sentry.protocol.User;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Timber.i("timber log entry...   ");

        setContentView(R.layout.activity_main);

        // SENTRY Tag and Breadcrumb
        String activity = this.getClass().getSimpleName();
        Sentry.setTag("activity", activity);
        Sentry.setTag("customerType", "business-plan");

        Breadcrumb breadcrumb = new Breadcrumb();
        breadcrumb.setMessage("Android activity was created");
        breadcrumb.setLevel(SentryLevel.INFO);
        breadcrumb.setData("Activity Name", activity);
        Sentry.addBreadcrumb( breadcrumb );


        // Unhandled - ArithmeticException
        Button div_by_zero_button = findViewById(R.id.div_zero);
        div_by_zero_button.setOnClickListener(view -> {

            Breadcrumb bc = new Breadcrumb();
            bc.setMessage("Button for ArithmeticException clicked...");
            bc.setLevel(SentryLevel.ERROR);
            bc.setData("url", "https://sentry.io");
            Sentry.addBreadcrumb(bc);

            int t = 5 / 0;

        });

        // Handled [Timber logged Exception] - NegativeArraySizeException
        Button negative_index_button = findViewById(R.id.negative_index);
        negative_index_button.setOnClickListener(view -> {
            Sentry.addBreadcrumb("Button for NegativeArraySizeException clicked...");
            try{
                int[] a = new int[-5];
            }catch(Exception e){
                Timber.e(e, "Negative array size");
            }
        });

        // Handled - ArrayIndexOutOfBoundsException
        Button handled_exception_button = findViewById(R.id.handled_exception);
        handled_exception_button.setOnClickListener(view -> {
            Sentry.addBreadcrumb("Button for ArrayIndexOutOfBoundsException clicked..");
                try {
                    String[] strArr = new String[1];
                    String s1 = strArr[2];
                } catch (Exception e) {
                    Sentry.captureException(e);
                }
        });

        // ANR - ApplicationNotResponding
        // no OS pop-up but UI is frozen during the pause
        Button anr_button = findViewById(R.id.anr);
        anr_button.setOnClickListener(view -> {
            Sentry.addBreadcrumb("Button for ANR clicked...");
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // Native Crash - SIGSEGV
        findViewById(R.id.native_crash).setOnClickListener(view -> {

            NativeSample.crash();
        });

        // Native Message
        findViewById(R.id.native_message).setOnClickListener(view -> {
            NativeSample.message();
        });
        

    }


}

