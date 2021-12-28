package com.example.vu.android;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import com.example.vu.android.toolstore.ToolStoreActivity;

import java.net.URL;
import java.util.concurrent.TimeUnit;

import io.sentry.Breadcrumb;
import io.sentry.ISpan;
import io.sentry.ITransaction;
import io.sentry.Sentry;
import io.sentry.SentryLevel;
import io.sentry.SpanStatus;
import io.sentry.android.okhttp.SentryOkHttpInterceptor;
import io.sentry.protocol.User;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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
        breadcrumb.setMessage("Android activity was created... ");
        breadcrumb.setLevel(SentryLevel.INFO);
        breadcrumb.setData("Activity Name", activity);
        Sentry.addBreadcrumb( breadcrumb );


        // Set the user in the current context.
        User user = new User();
        // user.setIpAddress(this.getIPAddress());
        Sentry.setUser(user);

        String urlGET = "";
        String urlGETPath = "/unhandled";
        Context context = this;
        ApplicationInfo appInfo = null;
        try {
            appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            urlGET = (String) appInfo.metaData.get("appmon.domain") + urlGETPath;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String finalUrlGET = urlGET;

        Button sampleHttpGet = findViewById(R.id.sample_get);
        sampleHttpGet.setOnClickListener(view -> {

            ITransaction transaction = Sentry.startTransaction("sampleHttpGet()", "task", true);
            
            //OkHttp request from https://gist.github.com/just-kip/1376527af60c74b07bef7bd7f136ff56

            Breadcrumb bc = new Breadcrumb();
            bc.setMessage("Button for Sample HTTP GET clicked... ");
            //bc.setLevel(SentryLevel.ERROR);
            bc.setData("url", finalUrlGET);
            Sentry.addBreadcrumb(bc);

            final TextView textView = (TextView) findViewById(R.id.textView);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new SentryOkHttpInterceptor())
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();

            final Request request = new Request.Builder()
                    .url(finalUrlGET)
                    .build();

            AsyncTask<Void, Void, String> asyncTask = new AsyncTask<Void, Void, String>() {
                @Override
                protected String doInBackground(Void... params) {
                    try {
                        Response response = client.newCall(request).execute();
                        if (!response.isSuccessful()) {
                            Sentry.captureException(new Exception("Response unsuccessful"));
                            return null;
                        }
                        return response.body().string();
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }

                @Override
                protected void onPostExecute(String s) {
                    super.onPostExecute(s);
                    if (s != null) {
                        if (s.length() > 49) textView.setText(s.substring(0, 50) + "...");
                        else textView.setText(s);
                    } else {
                        textView.setText("Request failed");
                    }
                }
            };

            try {
                asyncTask.execute();
            } catch (Exception e) {
                transaction.setThrowable(e);
                transaction.setStatus(SpanStatus.INTERNAL_ERROR);
                throw e;
            } finally {
                transaction.finish();
            }
        });

        Button sampleHttpGet2 = findViewById(R.id.sample_get2);
        sampleHttpGet2.setOnClickListener(view -> {
            Breadcrumb bc = new Breadcrumb();
            bc.setMessage("Button for Sample HTTP GET clicked... ");
            //bc.setLevel(SentryLevel.ERROR);
            bc.setData("url", finalUrlGET);
            Sentry.addBreadcrumb(bc);

            final TextView textView = (TextView) findViewById(R.id.textView);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new SentryOkHttpInterceptor())
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();

            final Request request = new Request.Builder()
                    .url(finalUrlGET)
                    .build();

            AsyncTask<Void, Void, String> asyncTask = new AsyncTask<Void, Void, String>() {
                @Override
                protected String doInBackground(Void... params) {
                    try {
                        Response response = client.newCall(request).execute();
                        if (!response.isSuccessful()) {
                            Sentry.captureException(new Exception("Response unsuccessful"));
                            return null;
                        }
                        return response.body().string();
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }

                @Override
                protected void onPostExecute(String s) {
                    super.onPostExecute(s);
                    if (s != null) {
                        if (s.length() > 49) textView.setText(s.substring(0, 50) + "...");
                        else textView.setText(s);
                    } else {
                        textView.setText("Request failed");
                    }
                }
            };

            try {
                asyncTask.execute();
            } catch (Exception e) {
                throw e;
            }
        });

        // Unhandled - ArithmeticException
        Button div_by_zero_button = findViewById(R.id.div_zero);
        div_by_zero_button.setOnClickListener(view -> {
            addAttachment();
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
            addAttachment();
            Sentry.addBreadcrumb("Button for NegativeArraySizeException clicked...");
            int[] a = new int[-5];
        });

        // Handled - ArrayIndexOutOfBoundsException
        Button handled_exception_button = findViewById(R.id.handled_exception);
        handled_exception_button.setOnClickListener(view -> {
            addAttachment();

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
    }

    @Override
    protected void onResume () {
        super.onResume() ;

        //We disabled tx auto finish for the 2nd activity
        ISpan span = Sentry.getSpan();
        if (span != null) {
            span.finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_toplevel, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_open_toolstore:
                Intent intent = new Intent(this, ToolStoreActivity.class);
                startActivity(intent);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }


//     private String getIPAddress(){

//         WifiManager wm = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//         return Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
//     }

}

