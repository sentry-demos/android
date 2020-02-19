package com.example.vu.android;

import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

//import io.sentry.android.core.SentryAndroid;
//import io.sentry.core.Breadcrumb;
import io.sentry.core.Sentry;
//import io.sentry.core.SentryLevel;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // SENTRY Tag and Breadcrumb
        String activity = this.getClass().getSimpleName();
        Sentry.setTag("activity", activity);
        Sentry.addBreadcrumb(activity + " was created");

        // Unhandled - ArithmeticException
        Button div_by_zero_button = findViewById(R.id.div_zero);
        div_by_zero_button.setOnClickListener(view -> {
//            Breadcrumb breadcrumb = new Breadcrumb();
//            breadcrumb.setMessage("Https Call to Sentry");
//            breadcrumb.setData("url", "https://sentry.io");
//
//            Sentry.addBreadcrumb(breadcrumb);
            Sentry.addBreadcrumb("Button for Error 1 clicked...");
            int t = 5 / 0;
        });

        // Unhandled - NegativeArraySizeException
        Button negative_index_button = findViewById(R.id.negative_index);
        negative_index_button.setOnClickListener(view -> {
            Sentry.addBreadcrumb("Button for Error 2 clicked...");
            int[] a = new int[-5];
        });

        // Handled - NumberFormatException
        //Handled - NumberFormatException
        Button handled_exception_button = findViewById(R.id.handled_exception);
        handled_exception_button.setOnClickListener(view -> {
                Sentry.addBreadcrumb("Button for Error 3 (Handled Exception) clicked..");
                try {
                    //Integer.parseInt ("str");
                    String[] strArr = new String[1];
                    String s1 = strArr[2];
                } catch (Exception e) {
                    Sentry.captureException(e);
                }
        });

        // ANR - ApplicationNotResponding
        Button anr_button = findViewById(R.id.anr);
        anr_button.setOnClickListener(view -> {
            Sentry.addBreadcrumb("Button for ANR clicked...");
            while(true) {
                // Wait 2 seconds for ANR....
            }
        });

        // Native Crash - SIGSEGV
        findViewById(R.id.native_crash).setOnClickListener(view -> {
            NativeSample.crash();
        });

    }
}













// TBD 12/17/19 not needed at this point in time
//         HANDLED NATIVE CRASH
//        findViewById(R.id.ndk_handled_crash).setOnClickListener(view -> NativeSample.handledCrash());