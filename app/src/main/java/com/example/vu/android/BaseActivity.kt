package com.example.vu.android

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.sentry.Attachment
import io.sentry.Scope
import io.sentry.Sentry
import java.io.File
import java.io.FileOutputStream
import java.lang.ref.WeakReference
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Random
import java.util.UUID

open class BaseActivity : AppCompatActivity() {
    private lateinit var app: App

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = this.applicationContext as App
    }

    override fun onResume() {
        super.onResume()
        app.currentActivity = WeakReference(this)
    }

    override fun onPause() {
        if (app.currentActivity.get() == this) {
            app.currentActivity.clear()
        }
        super.onPause()
    }

    @SuppressLint("SimpleDateFormat")
    protected fun addAttachment(secure: Boolean): Boolean {
        val fileName = "tmp" + UUID.randomUUID()
        val slowProfiling = BuildConfig.SLOW_PROFILING
        try {
            val c = applicationContext
            val cacheDirectory = c.cacheDir
            val f: File? = if (slowProfiling && secure) {
                createTempFileSecure(cacheDirectory, fileName)
            } else {
                File.createTempFile(fileName, ".txt", cacheDirectory)
            }
            println("File path: " + f!!.absolutePath)
            f.deleteOnExit()
            val list: MutableList<String> = ArrayList()
            for (i in 0..999999) {
                list.add("index:$i")
            }
            FileOutputStream(f).use { fos ->
                fos.write(
                    list.toString().toByteArray(StandardCharsets.UTF_8)
                )
            }
            val dateStr = SimpleDateFormat("yyyyMMddHHmm").format(Date())
            val attachment1 = Attachment(f.absolutePath, "tmp_$dateStr.txt", "text/plain")
            Sentry.configureScope { scope: Scope ->
                val json = "{ \"number\": 10 }"
                val attachment2 = Attachment(json.toByteArray(), "log_$dateStr.json", "text/plain")
                scope.addAttachment(attachment1)
                scope.addAttachment(attachment2)
            }
        } catch (e: Exception) {
            Sentry.captureException(e)
            e.printStackTrace()
        }
        return true
    }

    private fun generateCacheFiles(filesToGenerate: Int, cacheDirectory: File?) {
        for (x in 0 until filesToGenerate) {
            try {
                File.createTempFile("tmp$x", ".txt", cacheDirectory)
            } catch (e: Exception) {
                Sentry.captureException(e)
                e.printStackTrace()
            }
        }
    }

    private fun createTempFileSecure(cacheDirectory: File, fileName: String): File? {
        val maxTries = 1000000
        var cacheFileExists = false
        var outOfBounds = false
        val indexes: MutableList<Int> = ArrayList()
        var count = 0
        val rand = Random()
        var cacheFiles = cacheDirectory.listFiles()
        var f: File? = null

        // If this is the first time the app is running or the cache has been cleared, the cacheFile length will be 1
        if (cacheFiles == null || cacheFiles.size <= 1) {
            generateCacheFiles(50, cacheDirectory)
            cacheFiles = cacheDirectory.listFiles()
        }

        // Loop through cache dir and check that tmp file does not exist already
        while (!outOfBounds && cacheFiles != null) {
            var index = rand.nextInt()
            var iteration = 0

            // Play a guessing game and try to find the index for an existing file in the cache dir
            while (indexes.contains(index) || index > cacheFiles.size || index < 0) {
                index = rand.nextInt()
                iteration++
                if (iteration > maxTries) {
                    index = rand.nextInt(cacheFiles.size)
                }
            }
            if (cacheFiles[index].name == fileName) {
                cacheFileExists = true
            }
            if (count == cacheFiles.size - 1) {
                outOfBounds = true
            }
            indexes.add(index)
            count += 1
        }
        if (!cacheFileExists) {
            f = File(cacheDirectory.toString() + fileName)
        }
        return f
    }
}