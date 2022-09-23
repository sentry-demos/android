package com.example.vu.android;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

//import com.fullstory.FS;
//import com.fullstory.FSOnReadyListener;
//import com.fullstory.FSSessionData;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import io.sentry.Attachment;
import io.sentry.Sentry;

import static java.nio.charset.StandardCharsets.UTF_8;

public class MyBaseActivity extends AppCompatActivity  {

    protected MyApplication mMyApp ;
    protected String FS_sessionURL = null;

//    @Override
//    public void onReady(FSSessionData sessionData) {
//        // Use either sessionData.getCurrentSessionURL()
//        // or FS.getCurrentSessionURL() here to retrieve session URL
//        //String sessionUrlfromData = sessionData.getCurrentSessionURL();
//        FS_sessionURL = FS.getCurrentSessionURL();
//    }

//    public String getFullStorySessionURL(){
//        return this.FS_sessionURL;
//    }

    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState) ;
        mMyApp = (MyApplication) this .getApplicationContext() ;
//        FS.setReadyListener(this);
    }
    protected void onResume () {
        super.onResume() ;
        mMyApp.setCurrentActivity( this ) ;
    }
    protected void onPause () {
        clearReferences() ;
        super.onPause() ;
    }
    protected void onDestroy () {
        clearReferences() ;
        super.onDestroy() ;
    }
    private void clearReferences () {
        Activity currActivity = mMyApp.getCurrentActivity() ;
        if ( this .equals(currActivity))
            mMyApp.setCurrentActivity( null ) ;
    }

    protected Boolean addAttachment() {
        File f = null;
        boolean slowProfiling = BuildConfig.SLOW_PROFILING;
        int maxTries = 1000000;
        try {
            Context c = getApplicationContext();
            File cacheDirectory = c.getCacheDir();

            if (slowProfiling) {
                String fileName = "tmp" + UUID.randomUUID();
                boolean cacheFileExists = false;

                boolean outOfBounds = false;
                List<Integer> indexes = new ArrayList<>();
                int count = 0;
                Random rand = new Random();
                File[] cacheFiles = cacheDirectory.listFiles();

                // Loop through cache dir and check tmp file does not exist already
                while (!outOfBounds && cacheFiles != null) {
                    int index = rand.nextInt();
                    int iteration = 0;
                    while (indexes.contains(index) || index > cacheFiles.length || index < 0) {
                        index = rand.nextInt();
                        iteration++;
                        if (iteration > maxTries) {
                            index = rand.nextInt(cacheFiles.length);
                        }
                    }

                    if (cacheFiles[index].getName().equals(fileName)) {
                        cacheFileExists = true;
                    }

                    if (count == cacheFiles.length - 1) {
                        outOfBounds = true;
                    }

                    indexes.add(index);
                    count = count + 1;
                }

                if (!cacheFileExists) {
                    f = new File(cacheDirectory + fileName);
                }
            } else {
                f = File.createTempFile("tmp", ".txt", cacheDirectory);
            }

            System.out.println("File path: "+f.getAbsolutePath());
            f.deleteOnExit();
            try (FileOutputStream fos = new FileOutputStream(f)) {
                fos.write("test".getBytes(UTF_8));
            }
            String dateStr = new SimpleDateFormat("yyyyMMddHHmm").format(new Date());

            Attachment attachment1 = new Attachment(f.getAbsolutePath(), "tmp_"+dateStr+".txt", "text/plain");

            Sentry.configureScope(
                    scope -> {
                        String json = "{ \"number\": 10 }";
                        Attachment attachment2 = new Attachment(json.getBytes(), "log_"+dateStr+".json", "text/plain");
                        scope.addAttachment(attachment1);
                        scope.addAttachment(attachment2);
                    });
        } catch(Exception e) {
            Sentry.captureException(e);
            e.printStackTrace();
        }
        return true;
    }
}
