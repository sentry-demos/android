package com.example.vu.android.empowerplant

import io.sentry.android.okhttp.SentryOkHttpInterceptor
import io.sentry.android.okhttp.SentryOkHttpEventListener
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import io.sentry.HttpStatusCodeRange

public class RequestClient {

    @JvmField val client = OkHttpClient.Builder()
            .addInterceptor(SentryOkHttpInterceptor(
                    captureFailedRequests = true,
                    failedRequestStatusCodes = listOf(HttpStatusCodeRange(400, 599))
            ))
            .eventListener(SentryOkHttpEventListener())
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

    fun getClient() : OkHttpClient {
        return client;
    }

}
