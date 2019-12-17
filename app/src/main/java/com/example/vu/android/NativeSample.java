package com.example.vu.android;

public class NativeSample {
    public static native void message();
    public static native void crash();
    public static native void handledCrash();

    static {
        System.loadLibrary("native-sample");
    }
}