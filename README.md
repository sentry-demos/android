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
5. `./gradlew build`
6. upload debug files
```
sentry-cli upload-dif -o {YOUR ORGANISATION} -p {PROJECT} build/intermediates/cmake/
sentry-cli upload-dif -o {YOUR ORGANISATION} -p {PROJECT} build/intermediates/stripped_native_libs
sentry-cli upload-dif -o {YOUR ORGANISATION} -p {PROJECT} build/intermediates/merged_native_libs/
```

## Running the Demo

- This app has 5 buttons
    1. **DIVIDE BY 0**: Generates an **unhandled exception**
    2. **NEGATIVE INDEX**: Generates an **unhandled exception**
    3. **HANDLED EXCEPTION**: Catches a runtime exception in a try/catch clause and uses

    ```Java
        Sentry.captureException(e);
    ```

    4. **APPLICATION NOT RESPONDING (ANR)**: Simulates an ANR using an infinite while loop. Application crashes after 5 seconds and reports event to Sentry.
    5. **NATIVE CRASH**: Generates a native crash that the Sentry NDK will send to Sentry.io for symbolication

- TODO - Clicking any button will add a **custom breadcrumb** to the event

- SDK configuration defined in `app/src/main/resources/sentry.properties` includes: DSN, ANR, tags (key values)

![Android demo flow](android-demo.gif)

## Android ANR  

![Alt Text](android-demo-anr.gif)
