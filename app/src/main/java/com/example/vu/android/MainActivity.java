package com.example.vu.android;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import io.sentry.Breadcrumb;
import io.sentry.Sentry;
import io.sentry.SentryLevel;
import io.sentry.protocol.User;
import io.sentry.Attachment;
import java.io.File;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // SENTRY Tag and Breadcrumb
        String activity = this.getClass().getSimpleName();
        Sentry.setTag("activity", activity);

        Breadcrumb breadcrumb = new Breadcrumb();
        breadcrumb.setMessage("Android activity was created");
        breadcrumb.setLevel(SentryLevel.INFO);
        breadcrumb.setData("Activity Name", activity);
        Sentry.addBreadcrumb( breadcrumb );


        // Set the user in the current context.
        User user = new User();
        user.setIpAddress(this.getIPAddress());
        Sentry.setUser(user);

        // Create a File and Add as attachment
        File f = null;

        try {
            // creates temporary file
            f = File.createTempFile("tmp", ".txt", new File("C:/"));

            // prints absolute path
            System.out.println("File path: "+f.getAbsolutePath());

            // deletes file when the virtual machine terminate
            f.deleteOnExit();

            // creates temporary file
            f = File.createTempFile("tmp", null, new File("D:/"));

            // prints absolute path
            System.out.print("File path: "+f.getAbsolutePath());

            // deletes file when the virtual machine terminate
            f.deleteOnExit();

            Attachment attachment = new Attachment(f.getAbsolutePath());

            Sentry.configureScope(
                    scope -> {
                        scope.addAttachment(attachment);
                    });

        } catch(Exception e) {
            // if any error occurs
            Sentry.captureException(e);
            e.printStackTrace();

        }



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

        // Unhandled - NegativeArraySizeException
        Button negative_index_button = findViewById(R.id.negative_index);
        negative_index_button.setOnClickListener(view -> {
            Sentry.addBreadcrumb("Button for NegativeArraySizeException clicked...");
            int[] a = new int[-5];
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


    private String getIPAddress(){

        WifiManager wm = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
    }

}

