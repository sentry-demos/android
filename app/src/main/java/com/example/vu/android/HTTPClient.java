package com.example.vu.android;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.example.vu.android.empowerplant.EmpowerPlantActivity;
import com.example.vu.android.empowerplant.RequestClient;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import io.sentry.Sentry;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HTTPClient {

    public static void makeRequest(Context context) {
        String domain = getEmpowerPlantDomain(context);
        String getToolsURL = domain + "/details";

        Request request = new Request.Builder()
                .url(getToolsURL)
                .build();

        OkHttpClient client = new RequestClient().getClient();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseStr = response.body().string();
                }
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                //Failure error will be automatically captured by the Sentry SDK
            }
        });
    }

    private static String getEmpowerPlantDomain(Context context) {
        String domain = null;
        try {
            final ApplicationInfo appInfo =  context.getPackageManager().getApplicationInfo(context.getPackageName(),
                    PackageManager.GET_META_DATA);

            if (appInfo.metaData != null) {
                domain = (String) appInfo.metaData.get("empowerplant.domain");
            }
        } catch (Exception e) {
            Sentry.captureException(e);
        }
        return domain;
    }
}
