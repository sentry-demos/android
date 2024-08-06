package com.example.vu.android.empowerplant

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import io.sentry.HttpStatusCodeRange
import io.sentry.okhttp.SentryOkHttpInterceptor
import okhttp3.Interceptor

public class RequestClient {

    /** OkHttp client used to cause N+1 API issues. Make each request last at least 300ms, as required by detector. */
    @JvmField val np1Client = OkHttpClient.Builder()
        .addInterceptor(
            SentryOkHttpInterceptor(
            captureFailedRequests = true,
            failedRequestStatusCodes = listOf(HttpStatusCodeRange(400, 599))
        )
        )
        .addInterceptor(Interceptor { chain ->
            Thread.sleep(350)
            return@Interceptor chain.proceed(chain.request())
        })
        .connectTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    @JvmField val client = OkHttpClient.Builder()
            .addInterceptor(SentryOkHttpInterceptor(
                    captureFailedRequests = true,
                    failedRequestStatusCodes = listOf(HttpStatusCodeRange(400, 599))
            ))
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

    fun getClient() : OkHttpClient {
        return client;
    }

    fun getNp1Client() = np1Client

}
