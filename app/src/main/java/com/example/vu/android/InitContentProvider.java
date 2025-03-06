package com.example.vu.android;

import static io.sentry.Sentry.APP_START_PROFILING_CONFIG_FILE_NAME;

import android.annotation.SuppressLint;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import io.sentry.SentryAppStartProfilingOptions;
import io.sentry.SentryOptions;

/*
 * This class is used to initialize the options of the App Start Profiling.
 * It creates the options and writes them to a file to make the SDK think it was already initialized, so that ui tests will run app start profiling.
 * It has no effect on the SDK initialization or behavior.
 */
public class InitContentProvider extends ContentProvider {

    @Override
    public boolean onCreate() {

        final Context context = getContext();
        if (context == null) {
            return true;
        }

        final @NotNull SentryOptions options = createOptions(context);
        final @NotNull SentryAppStartProfilingOptions appStartProfilingOptions = createAppStartOptions(options);
        // Manually create all folders required by the SDK to work
        final @NotNull File cacheDir = new File(context.getCacheDir(), "sentry");
        final @NotNull File configFile = new File(cacheDir, APP_START_PROFILING_CONFIG_FILE_NAME);
        cacheDir.mkdirs();
        new File(options.getProfilingTracesDirPath()).mkdirs();

        // Write option to config file to make the SDK think it was already initialized.
        try (final OutputStream outputStream = new FileOutputStream(configFile);
             final Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))) {
            options.getSerializer().serialize(appStartProfilingOptions, writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    @SuppressLint("VisibleForTests")
    private SentryOptions createOptions(final @NotNull Context context) {
        SentryOptions options = new SentryOptions();
        options.setDsn("https://6de4ac3993f24074820d83751794ca39@sandbox-mirror.sentry.gg/1");
        options.setCacheDirPath(new File(context.getCacheDir(), "sentry").getAbsolutePath());
        return options;
    }

    @SuppressLint("VisibleForTests")
    private SentryAppStartProfilingOptions createAppStartOptions(SentryOptions options) {
        SentryAppStartProfilingOptions appStartProfilingOptions = new SentryAppStartProfilingOptions();
        appStartProfilingOptions.setProfileSampled(true);
        appStartProfilingOptions.setTraceSampled(true);
        appStartProfilingOptions.setProfileSampleRate(1.0);
        appStartProfilingOptions.setTraceSampleRate(1.0);
        appStartProfilingOptions.setProfilingTracesDirPath(options.getProfilingTracesDirPath());
        appStartProfilingOptions.setProfilingEnabled(true);
        appStartProfilingOptions.setProfilingTracesHz(options.getProfilingTracesHz());
        return appStartProfilingOptions;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}