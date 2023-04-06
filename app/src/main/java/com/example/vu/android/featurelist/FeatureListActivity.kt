package com.example.vu.android.featurelist

import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.Button
import com.example.vu.android.BaseActivity
import com.example.vu.android.R
import com.example.vu.android.empowerplant.network.RequestClient
import io.sentry.Breadcrumb
import io.sentry.Sentry
import io.sentry.SentryLevel

class FeatureListActivity : BaseActivity() {

    @Suppress("DIVISION_BY_ZERO", "UNUSED_VARIABLE")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Tagging
        val activityName = this.javaClass.simpleName
        Sentry.setTag("activity", activityName)
        Sentry.setTag("customerType", "enterprise")

        // Breadcrumb
        val breadcrumb = Breadcrumb().apply {
            message = "Android activity was created"
            level = SentryLevel.INFO
            setData("Activity Name", activityName)
        }
        Sentry.addBreadcrumb(breadcrumb)

        // Unhandled - ArithmeticException
        findViewById<Button>(R.id.div_zero).setOnClickListener {
            addAttachment(false)
            val bc = Breadcrumb()
            bc.message = "Button for ArithmeticException clicked..."
            bc.level = SentryLevel.ERROR
            bc.setData("url", "https://sentry.io")
            Sentry.addBreadcrumb(bc)

            val t = 5 / 0
        }

        // Unhandled - NegativeArraySizeException
        findViewById<Button>(R.id.negative_index).setOnClickListener {
            addAttachment(false)
            Sentry.addBreadcrumb("Button for NegativeArraySizeException clicked...")
            val a = IntArray(-5)
        }

        // Handled - ArrayIndexOutOfBoundsException
        findViewById<Button>(R.id.handled_exception).setOnClickListener {
            addAttachment(false)
            Sentry.addBreadcrumb("Button for ArrayIndexOutOfBoundsException clicked..")
            try {
                val strArr = arrayOfNulls<String>(1)
                val s1 = strArr[2]
            } catch (e: Exception) {
                Sentry.captureException(e)
            }
        }

        // ANR - ApplicationNotResponding
        // no OS pop-up but UI is frozen during the pause
        findViewById<Button>(R.id.anr).setOnClickListener {
            Sentry.addBreadcrumb("Button for ANR clicked...")
            try {
                Thread.sleep(20000)
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }

        // Native Crash - SIGSEGV
        findViewById<View>(R.id.native_crash).setOnClickListener {
            NativeSample.crash()
        }

        // Native Message
        findViewById<View>(R.id.native_message).setOnClickListener {
            NativeSample.message()
        }

        // HTTP 404
        findViewById<View>(R.id.error_404).setOnClickListener {
            RequestClient.makeExampleRequest(
                applicationContext
            )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_toplevel, menu)
        return true
    }
}