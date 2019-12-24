# Android Demo

This app demonstrates how to include and configure Sentry in an Android application.

See https://docs.sentry.io/clients/java/modules/android/ for more information.

## Prerequisites  

* Android Studio
* Gradle

## Setup Instructions

1. Clone this repo

2. Open project using Android Studio

3. You may need to sync the project with the Gradle files

    ```
    Tools -> Android -> Sync Project with Gradle Files
    ```

4. Put your DSN key in `app/src/main/resources/sentry.properties`
5. `./gradlew build` and run this after any code change, to produce updated debug files
6. upload debug files
```
sentry-cli upload-dif -o {YOUR ORGANISATION} -p {PROJECT} app/build/intermediates/cmake/ --include-sources
sentry-cli upload-dif -o {YOUR ORGANISATION} -p {PROJECT} app/build/intermediates/stripped_native_libs --include-sources
sentry-cli upload-dif -o {YOUR ORGANISATION} -p {PROJECT} app/build/intermediates/merged_native_libs/ --include-sources

# e.g.
sentry-cli upload-dif -o testorg-az -p android app/build/intermediates/cmake/ --include-sources
sentry-cli upload-dif -o testorg-az -p android app/build/intermediates/stripped_native_libs --include-sources
sentry-cli upload-dif -o testorg-az -p android app/build/intermediates/merged_native_libs/ --include-sources
```

## Running the Demo

The MainActivity has 5 buttons
1. **DIVIDE BY 0**: Generates an **unhandled exception**
2. **NEGATIVE INDEX**: Generates an **unhandled exception**
3. **HANDLED EXCEPTION**: Catches a runtime exception in a try/catch clause using `Sentry.captureException(e);`
4. **APPLICATION NOT RESPONDING (ANR)**: Uses an infinite loop to crash the app after 5 seconds and reports event to Sentry.
5. **NATIVE CRASH**: Generates a native crash in c++ that the Sentry NDK will send to Sentry.io for symbolication

- TODO - Clicking any button will add a **custom breadcrumb** to the event

## Android Java Exception

![Android demo flow](android-demo.gif)

## Android ANR  

![Alt Text](android-demo-anr.gif)

## Android Native Crash c++

![Native Crash](android-native-crash-175.gif)