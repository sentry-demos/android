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

4. Update the DSN in the `app/src/main/resources/sentry.properties` file

## Running the Demo

- The Android application has 4 buttons
    1. **DIVIDE BY 0**: Generates an **unhandled exception**
    2. **NEGATIVE INDEX**: Generates an **unhandled exception**
    3. **HANDLED EXCEPTION**: Catches a runtime exception in a try/catch clause and uses

    ```Java
        Sentry.capture(e);
    ```

    4. **APPLICATION NOT RESPONDING (ANR)**: Simulates an ANR using an infinite while loop. Application crashes after 5 seconds and reports event to Sentry.

- Clicking any button will add a **custom breadcrumb** to the event

- SDK configuration defined in `app/src/main/resources/sentry.properties` includes: dsn, ANR, tags (key values)


![Alt Text](android-demo.gif)
