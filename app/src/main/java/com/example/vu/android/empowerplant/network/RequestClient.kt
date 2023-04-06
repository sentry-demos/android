package com.example.vu.android.empowerplant.network

import android.content.Context
import android.content.pm.PackageManager
import io.sentry.HttpStatusCodeRange
import io.sentry.Sentry
import io.sentry.android.okhttp.SentryOkHttpInterceptor
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

object RequestClient {

    private val client by lazy {
        OkHttpClient.Builder()
            .addInterceptor(
                SentryOkHttpInterceptor(
                    captureFailedRequests = true,
                    failedRequestStatusCodes = listOf(HttpStatusCodeRange(400, 599))
                )
            )
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    fun get(): OkHttpClient = client

    fun makeExampleRequest(context: Context) {
        val domain = getEmpowerPlantBaseUrl(context)
        val getToolsURL = "$domain/details"
        val request: Request = Request.Builder()
            .url(getToolsURL)
            .build()

        get().newCall(request).enqueue(object : Callback {

            override fun onResponse(call: Call, response: Response) {
//                if (response.isSuccessful) {
//                    val responseStr = response.body!!.string()
//                }
            }

            override fun onFailure(call: Call, e: IOException) {
                // Failure error will be automatically captured by the Sentry SDK
            }
        })
    }

    fun getEmpowerPlantBaseUrl(context: Context): String? {
        var domain: String? = null
        try {
            val appInfo = context.packageManager.getApplicationInfo(
                context.packageName,
                PackageManager.GET_META_DATA
            )
            if (appInfo.metaData != null) {
                domain = appInfo.metaData.getString("empowerplant.domain", null)
            }
        } catch (e: Exception) {
            Sentry.captureException(e)
        }
        return domain
    }

}
