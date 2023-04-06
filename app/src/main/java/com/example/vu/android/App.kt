package com.example.vu.android

import android.app.Activity
import android.app.Application
import android.content.pm.PackageManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import io.sentry.Hint
import io.sentry.Sentry
import io.sentry.SentryEvent
import io.sentry.SentryLevel
import io.sentry.SentryOptions.BeforeSendCallback
import io.sentry.UserFeedback
import io.sentry.android.core.SentryAndroid
import io.sentry.android.core.SentryAndroidOptions
import io.sentry.protocol.SentryId
import io.sentry.protocol.User
import java.lang.ref.WeakReference

class App : Application() {
    var currentActivity: WeakReference<Activity> = WeakReference(null)

    private val applicationName: String
        private get() {
            return applicationInfo.loadLabel(packageManager).toString()
        }

    override fun onCreate() {
        super.onCreate()

        SentryAndroid.init(this) { options: SentryAndroidOptions ->
            try {
                val pInfo = this.packageManager.getPackageInfo(this.packageName, 0)
                options.setTag("versionName", pInfo.versionName)
                options.setTag("applicationName", applicationName)
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
            options.isAttachThreads = true
            options.beforeSend = BeforeSendCallback { event: SentryEvent, hint: Hint? ->

                // Remove PII
                val exceptions = event.exceptions
                if (exceptions != null && exceptions.size > 0) {
                    val exception = exceptions[0]
                    if ("NegativeArraySizeException" == exception.type) {
                        event.user?.ipAddress = null
                    }
                }

                // Custom fingerprints
                event.fingerprints = if (BuildConfig.SE === "tda") {
                    listOf("{{ default }}", BuildConfig.SE, BuildConfig.VERSION_NAME)
                } else {
                    listOf("{{ default }}", BuildConfig.SE)
                }

                // Show user feedback dialog
                val currentException = event.exceptions!![0]
                if (currentException != null && currentException.type!!.endsWith("ItemDeliveryProcessException")) {
                    launchUserFeedback(event.eventId)
                }

                // Drop debug events
                return@BeforeSendCallback if (SentryLevel.DEBUG == event.level) null else event
            }
        }
        Sentry.setTag("se", BuildConfig.SE)

        // Set User info on Sentry event using a random email
        val user = User().apply {
            email = getRandomEmail()
        }
        Sentry.setUser(user)
    }

    private fun getRandomEmail(): String {
        val alphaNumericString = "abcdefghijklmnopqrstuvxyz0123456789"
        val n = 4
        val sb = StringBuilder(n)
        for (i in 0 until n) {
            val index = (alphaNumericString.length * Math.random()).toInt()
            sb.append(alphaNumericString[index])
        }
        return "$sb@gmail.com"
    }

    private fun launchUserFeedback(sentryId: SentryId?) {
        currentActivity.get()?.let { activity ->

            val editTextName1 = EditText(this@App)
            editTextName1.hint = "OMG! What happened??"
            val layoutName = LinearLayout(this)
            layoutName.orientation = LinearLayout.VERTICAL
            layoutName.setPadding(60, 20, 60, 20)
            layoutName.addView(editTextName1)

            val alertDialogBuilder = AlertDialog.Builder(activity)
            alertDialogBuilder.setView(layoutName)
            alertDialogBuilder.setTitle("Ooops, Checkout Failed!")
            alertDialogBuilder.setPositiveButton(
                "Submit"
            ) { _, _ ->
                Toast.makeText(activity, "Thank you!", Toast.LENGTH_LONG).show()
                val txt = editTextName1.text.toString() // variable to collect user input
                val userFeedback = UserFeedback(sentryId).apply {
                    comments = txt
                    email = "john.doe@example.com"
                    name = "John Doe"
                }
                Sentry.captureUserFeedback(userFeedback)
            }
            alertDialogBuilder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

            activity.runOnUiThread {
                alertDialogBuilder.show()
            }
        }
    }
}