# Android Demo

This app demonstrates how to use Sentry in an Android application for capturing 4 types of exceptions:

- Unhandled Exceptions (2)
- Handled Exceptions
- Application Not Responding
- Native Crashes from c++ native code

This app has all configuration (e.g. gradle) set to include Sentry SDK and ANR and NDK (crash) events.

Sentry NDK libraries are used in addition to the Sentry SDK, for capturing errors and crashes in c++.

For use in **Production** see the [Official Sentry Android Documentation](https://docs.sentry.io/platforms/android/)
Additional documentation:
[ANR Configuration](https://docs.sentry.io/platforms/android/#configuration-options)
[NDK Configuration](https://docs.sentry.io/platforms/android/#integrating-the-ndk)

## Versions

* Android Studio 3.5.3
* Gradle 5.6.4
* AVD `Nexus 5x API 29 x86`

```
// testing last performed 01/01/19
Android Studio 3.5.3
Build #AI-191.8026.42.35.6010548, built on November 15, 2019
JRE: 1.8.0_202-release-1483-b49-5587405 x86_64
JVM: OpenJDK 64-Bit Server VM by JetBrains s.r.o
macOS 10.14.4
```


## Setup

1. Clone this repo

2. Open project using Android Studio

3. You may need to sync the project with the Gradle files

    ```
    Tools -> Android -> Sync Project with Gradle Files
    ```

4. Put your Sentry DSN key in `AndroidManifest.xml`
5. `./gradlew build` and run this after any code change, to produce updated debug files
6. upload debug files
```
sentry-cli upload-dif -o {YOUR ORGANISATION} -p {PROJECT} app/build/intermediates/cmake/ --include-sources
sentry-cli upload-dif -o {YOUR ORGANISATION} -p {PROJECT} app/build/intermediates/stripped_native_libs --include-sources
sentry-cli upload-dif -o {YOUR ORGANISATION} -p {PROJECT} app/build/intermediates/merged_native_libs/ --include-sources

sentry-cli upload-dif -o testorg-az -p android app/build/intermediates/cmake/ --include-sources
sentry-cli upload-dif -o testorg-az -p android app/build/intermediates/stripped_native_libs --include-sources
sentry-cli upload-dif -o testorg-az -p android app/build/intermediates/merged_native_libs/ --include-sources
```

You can see they were uploaded in your Sentry Project Settings:
`https://sentry.io/settings/${ORG}/projects/${PROJECT}/debug-symbols/`
`https://sentry.io/settings/testorg-az/projects/android/debug-symbols/`

## Run
1. Run it in Android Studio on an Android Virtual Device.

## What's Happening

The MainActivity has 5 buttons that generate the following exception types:

1. DIVIDE BY 0: **Unhandled Exception** of type Arithmetic Eception
2. NEGATIVE INDEX: **Unhandled Exception** of type NegativeArraySizeException
3. HANDLED EXCEPTION: **Handled Exception** of type NumberFormatException
4. APPLICATION NOT RESPONDING (ANR): **ApplicationNotResponding** Uses an infinite loop to crash the app after 5 seconds and reports event to Sentry.
5. NATIVE CRASH: **Native Crash** of type SIGSEGV from native c++. The Sentry NDK sends this to Sentry.io for symbolication

## GIF Android Java Exception

![Android demo flow](android-demo.gif)

## GIF Android ANR

![Alt Text](android-demo-anr.gif)

## GIF Android Native Crash c++

![Native Crash](android-native-crash-175.gif)