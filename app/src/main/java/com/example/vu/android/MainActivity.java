package com.example.vu.android;

import android.graphics.Color;
import android.os.Bundle;
import io.sentry.android.AndroidSentryClientFactory;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;
import io.sentry.Sentry;
import io.sentry.event.Breadcrumb;
import io.sentry.event.BreadcrumbBuilder;
import io.sentry.event.UserBuilder;
import android.content.Context;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    TextView total;
    EditText numerator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //  Sentry DSN + configuration defined in app/sr/main/resources/sentry.properties file
        Sentry.init(new AndroidSentryClientFactory(this));


        Button submit_email_button = (Button)findViewById(R.id.submit_email);
        submit_email_button.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view){

                InputMethodManager input = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                input.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);

                EditText email = (EditText) findViewById(R.id.email);
                TextView display = (TextView) findViewById(R.id.display);
                String email_string = email.getText().toString();
                Sentry.getContext().setUser(
                  new UserBuilder().setEmail(email_string).build()
                );
                String greeting = "Hello, " + email_string + "!";
                display.setText(greeting);


            }
        });

        final Button anr_button = (Button)findViewById(R.id.anr_button);
        anr_button.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view){
//                Toast.makeText(getBaseContext(), "ANR...", Toast.LENGTH_LONG).show();
//                anr_button.setBackgroundColor(Color.BLACK);
                Sentry.getContext().recordBreadcrumb(
                        new BreadcrumbBuilder().setLevel(Breadcrumb.Level.DEBUG).setCategory("custom").setType(Breadcrumb.Type.USER).setMessage("User clicked button: ANR").build()
                );

                while(true) {
                    //Wait for ANR....
                }
            }
        });


        Button div_by_zero_button = (Button)findViewById(R.id.div_zero);
        div_by_zero_button.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view){
                Sentry.getContext().recordBreadcrumb(
                    new BreadcrumbBuilder().setLevel(Breadcrumb.Level.DEBUG).setCategory("custom").setType(Breadcrumb.Type.USER).setMessage("User clicked button: DIVIDE BY ZERO").build()
                );
                int t = 5 / 0 ;
            }
        });

        Button negative_index_button = (Button)findViewById(R.id.negative_index);
        negative_index_button.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view){
                Sentry.getContext().recordBreadcrumb(
                    new BreadcrumbBuilder().setLevel(Breadcrumb.Level.DEBUG).setCategory("custom").setType(Breadcrumb.Type.USER).setMessage("User clicked button: NEGATIVE INDEX").build()
                );
                int[] a = new int[-5];
            }
        });


        Button file_not_found_button = (Button)findViewById(R.id.file_not_found);
        file_not_found_button.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view){
                Sentry.getContext().recordBreadcrumb(
                    new BreadcrumbBuilder().setLevel(Breadcrumb.Level.DEBUG).setCategory("custom").setType(Breadcrumb.Type.USER).setMessage("User clicked button: FILE NOT FOUND").build()
                );
                Integer.parseInt ("str");
            }
        });

    }
}
