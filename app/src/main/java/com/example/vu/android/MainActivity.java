package com.example.vu.android;

import android.os.Bundle;
import io.sentry.android.AndroidSentryClientFactory;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethod;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;
import android.widget.EditText;
import android.view.View.OnClickListener;
import io.sentry.Sentry;
import io.sentry.event.UserBuilder;
import android.content.Context;
import java.io.File;
import java.io.FileReader;

public class MainActivity extends AppCompatActivity {
    TextView total;
    EditText numerator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Context ctx = this.getApplicationContext();
        // Use the Sentry DSN (client key) from the Project Settings page on Sentry
        String sentryDsn = "https://80b8a795d2a14cf796acaae4fa6cab30:762e19f86e23471586cc4dd3b1ee15fb@sentry.io/261820";
        Sentry.init(sentryDsn, new AndroidSentryClientFactory(ctx));


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

        Button div_by_zero_button = (Button)findViewById(R.id.div_zero);
        div_by_zero_button.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view){
                int t = 5 / 0 ;
            }
        });

        Button negative_index_button = (Button)findViewById(R.id.negative_index);
        negative_index_button.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view){
                int[] a = new int[-5];
            }
        });


        Button file_not_found_button = (Button)findViewById(R.id.file_not_found);
        file_not_found_button.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view){
                Integer.parseInt ("str");
            }
        });

    }
}
