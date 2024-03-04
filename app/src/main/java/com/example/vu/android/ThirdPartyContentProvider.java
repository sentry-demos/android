package com.example.vu.android;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;
import java.util.UUID;

public class ThirdPartyContentProvider extends ContentProvider {

    /** @noinspection FieldCanBeLocal*/
    private static String installationIdentifier;

    @Override
    public boolean onCreate() {
        final int runs = BuildConfig.VERSION_CODE >= 44 ? 100000 : 100;
        try {
            final File installationFile = new File(getContext().getFilesDir(), "INSTALLATION-ID");
            final StringBuilder identifier = new StringBuilder();
            for (int i = 0; i < runs; i++) {
                identifier.append(UUID.randomUUID().toString());
            }
            installationIdentifier = identifier.toString();
            // write installationID to file
            try (final FileWriter fw = new FileWriter(installationFile)) {
                fw.write(installationIdentifier);
            }
        } catch (Exception e) {
            // ignored
        }
        return true;
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
