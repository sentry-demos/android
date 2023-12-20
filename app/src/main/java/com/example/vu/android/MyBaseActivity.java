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
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import io.sentry.Attachment;
import io.sentry.Sentry;
import io.sentry.ISpan;

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

    /** Add a delay based on version code. */
    protected void checkRelease() {
        ISpan span = Sentry.getSpan();
        ISpan innerSpan = span.startChild("ui.load", "Check Release");

        // Even versions will wait 1 second, to make it more obvious the difference between releases
        if (BuildConfig.VERSION_CODE % 2 == 0) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        innerSpan.finish();
    }

    protected Boolean addAttachment(Boolean secure) {
        File f = null;
        String fileName = "tmp" + UUID.randomUUID();
        boolean slowProfiling = BuildConfig.SLOW_PROFILING;

        try {
            Context c = getApplicationContext();
            File cacheDirectory = c.getCacheDir();

            if (slowProfiling && secure) {
                f = createTempFileSecure(cacheDirectory, fileName);
            } else {
                f = File.createTempFile(fileName, ".txt", cacheDirectory);
            }

            System.out.println("File path: "+f.getAbsolutePath());
            f.deleteOnExit();
            List<String> list = new ArrayList<String>();

            for (int i = 0; i < 1000000; i++) {
                list.add("index:" + i);
            }
            try (FileOutputStream fos = new FileOutputStream(f)) {
                fos.write(list.toString().getBytes(UTF_8));
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

    protected void generateCacheFiles(int filesToGenerate, File cacheDirectory) {
        for (int x = 0; x < filesToGenerate; x++) {
            try {
                File.createTempFile("tmp" + x, ".txt", cacheDirectory);
            } catch (Exception e) {
                Sentry.captureException(e);
                e.printStackTrace();
            }
        }
    }

    protected File createTempFileSecure(File cacheDirectory, String fileName){
        int maxTries = 20000;
        boolean cacheFileExists = false;
        boolean outOfBounds = false;
        List<Integer> indexes = new ArrayList<>();
        int count = 0;
        Random rand = new Random();
        File[] cacheFiles = cacheDirectory.listFiles();
        File f = null;

        // If this is the first time the app is running or the cache has been cleared, the cacheFile length will be 1
        if (cacheFiles == null || cacheFiles.length <= 1) {
            generateCacheFiles(50, cacheDirectory);
            cacheFiles = cacheDirectory.listFiles();
        }

        // Loop through cache dir and check that tmp file does not exist already
        while (!outOfBounds && cacheFiles != null) {
            int index = rand.nextInt();
            int iteration = 0;

            // Play a guessing game and try to find the index for an existing file in the cache dir
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

        return f;
    }
}
