package com.example.vu.android;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.widget.Button;

import com.example.vu.android.empowerplant.StoreItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import io.sentry.Breadcrumb;
import io.sentry.ISpan;
import io.sentry.Sentry;
import io.sentry.SentryLevel;

public class MainActivity extends MyBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // SENTRY Tag and Breadcrumb
        String activity = this.getClass().getSimpleName();
        Sentry.setTag("activity", activity);
        Sentry.setTag("customerType", "enterprise");

        Breadcrumb breadcrumb = new Breadcrumb();
        breadcrumb.setMessage("Android activity was created");
        breadcrumb.setLevel(SentryLevel.INFO);
        breadcrumb.setData("Activity Name", activity);
        Sentry.addBreadcrumb( breadcrumb );

        // Unhandled - ArithmeticException
        Button div_by_zero_button = findViewById(R.id.div_zero);
        div_by_zero_button.setOnClickListener(view -> {
            addAttachment(false);
            Breadcrumb bc = new Breadcrumb();
            bc.setMessage("Button for ArithmeticException clicked...");
            bc.setLevel(SentryLevel.ERROR);
            bc.setData("url", "https://sentry.io");
            Sentry.addBreadcrumb(bc);

            int t = 5 / 0;
        });

        // Unhandled - NegativeArraySizeException
        Button negative_index_button = findViewById(R.id.negative_index);
        negative_index_button.setOnClickListener(view -> {
            addAttachment(false);
            Sentry.addBreadcrumb("Button for NegativeArraySizeException clicked...");
            int[] a = new int[-5];
        });

        // Handled - ArrayIndexOutOfBoundsException
        Button handled_exception_button = findViewById(R.id.handled_exception);
        handled_exception_button.setOnClickListener(view -> {
            addAttachment(false);

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
                Thread.sleep(20000);
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

        findViewById(R.id.error_404).setOnClickListener(view -> {
            HTTPClient.makeRequest(getApplicationContext());
        });

        // Slow Regex Issue
        findViewById(R.id.slow_regex).setOnClickListener(view -> {
            ISpan regexTransaction = Sentry.startTransaction("slow regex performance issue", "slow regex");
            try {
                "Long string that will be used to run a slow regex".matches(".*.*.*.*.*.*#");
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            regexTransaction.finish();
        });

        // Slow Image Decoding Issue
        findViewById(R.id.slow_image_decoding).setOnClickListener(view -> {
            ISpan regexTransaction = Sentry.startTransaction("slow image decoding performance issue", "slow image");
            try {
                BitmapFactory.decodeResource(getResources(), R.drawable.plantspider_big);
                BitmapFactory.decodeResource(getResources(), R.drawable.plantspider_big);
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            regexTransaction.finish();
        });

        // Slow Json Decoding Issue
        findViewById(R.id.slow_json_decoding).setOnClickListener(view -> {
            ISpan regexTransaction = Sentry.startTransaction("slow json decoding performance issue", "slow json");
            try {
                StringBuilder json = new StringBuilder("[");
                for (int i = 0; i < 100000; i++) {
                    json.append("{\"id\":0,\"price\":0,\"quantity\":0},{\"id\":0,\"price\":0,\"quantity\":0},");
                }
                json.append("{\"id\":0,\"price\":0,\"quantity\":0},{\"id\":0,\"price\":0,\"quantity\":0}]");
                Type listType = new TypeToken<List<StoreItem>>() {}.getType();
                new Gson().fromJson(json.toString(), listType);
                new Gson().fromJson(json.toString(), listType);
                new Gson().fromJson(json.toString(), listType);
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            regexTransaction.finish();
        });

    }

    @Override
    protected void onResume () {
        super.onResume() ;
        new Thread(() -> {

            try {
                Thread.sleep(150);
                Sentry.reportFullyDisplayed();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            // Let's finish the ui load transaction
            ISpan uiLoadSpan = Sentry.getSpan();
            if (uiLoadSpan != null && !uiLoadSpan.isFinished()) {
                uiLoadSpan.finish();
            }
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_toplevel, menu);
        return true;
    }

    public static boolean dummyFunction(boolean value) {
        return true;
    } 

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch(item.getItemId()) {
//            case R.id.action_open_empowerplant:
//                Intent intent = new Intent(this, EmpowerPlantActivity.class);
//                startActivity(intent);
//                return true;
//
//        }
//        return super.onOptionsItemSelected(item);
//    } // uncommenting will restore navigation from listapp to empowerplant store items


//     private String getIPAddress(){

//         WifiManager wm = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//         return Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
//     }

}

