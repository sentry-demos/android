package com.example.vu.android;

import android.os.Bundle;

import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import io.sentry.core.Breadcrumb;
import io.sentry.core.Sentry;

public class MainActivity extends AppCompatActivity {
    TextView total;
    EditText numerator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Sentry.addBreadcrumb(new Breadcrumb(this.getClass().getSimpleName() + " was created"));

        // DIVIDE BY ZERO
        Button div_by_zero_button = (Button)findViewById(R.id.div_zero);
        div_by_zero_button.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view){
                Sentry.addBreadcrumb(new Breadcrumb("Button for Error 1 clicked..."));
                int t = 5 / 0 ;
            }
        });

        // NEGATIVE INDEX
        Button negative_index_button = (Button)findViewById(R.id.negative_index);
        negative_index_button.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view){
                Sentry.addBreadcrumb(new Breadcrumb("Button for Error 2 clicked..."));
                int[] a = new int[-5];
            }
        });

        // HANDLED EXCEPTION
        Button handled_exception_button = (Button)findViewById(R.id.handled_exception);
        handled_exception_button.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view){
                Sentry.addBreadcrumb(new Breadcrumb("Button for Error 3 (Handled Exception) clicked..."));
                try {
                    Integer.parseInt ("str");
                } catch (Exception e) {
                    Sentry.captureException(e);
                }

            }
        });

        // ANR
        final Button anr_button = (Button)findViewById(R.id.anr);
        anr_button.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view){
                Sentry.addBreadcrumb(new Breadcrumb("Button for ANR clicked..."));
                while(true) {
                    // Wait 5 seconds for ANR....
                }
            }
        });

        // NATIVE CRASH
//        findViewById(R.id.native_crash).setOnClickListener(view -> NativeSample.crash());
        findViewById(R.id.native_crash).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                Sentry.addBreadcrumb(new Breadcrumb("Button for Native Crash clicked..."));
                NativeSample.crash();
            }
        });

    }
}

// TBD 12/17/19 not needed at this point in time
//         HANDLED NATIVE CRASH
//        findViewById(R.id.ndk_handled_crash).setOnClickListener(view -> NativeSample.handledCrash());