package com.example.vu.android.featurelist

object NativeSample {

    @JvmStatic
    external fun crash()

    @JvmStatic
    external fun message()

    init {
        System.loadLibrary("native-sample")
    }
}