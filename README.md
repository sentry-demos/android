# Android Demo

This app demonstrates how to use Sentry in an Android application:

**Error Events**
- Unhandled Exceptions
- Handled Exceptions
- HTTP Client Errors
- Application Not Responding
- Native Crashes from C++ native code
- Message Capture

**Performance Issues**
- DB on Main Thread
- File IO on Main Thread
- Image Decoding on Main Thread
- Regex on Main Thread
- JSON Decoding on Main Thread

**Tracing**
- Time to Initial/Full Display
- Automatic Instrumentation for UI Activity (UI Load, App Start Warm/Cold, Slow and Frozen Frames, OkHttp Library, File I/O Instrumentation, SQLite and Room query)
- Manual Instrumentation
- Profiling
- CPU Usage, Memory

**Other**
- Session Replay
- Logs
- Screenshots
- View Hierary
- User Feedback

## Versions

| dependency    | version
| ------------- |:-------------:|
| sentry-java | 8.17.0 |
| sentry-android-gradle-plugin | 5.8.0 |
| Android Studio Narwhal | 2025.1.1 Patch 1 |
| Android Gradle Plugin | 8.6.0 (requires a minimum Gradle version of 8.7) |
| sentry-cli | 2.44.0 |
| macOS | Sequoia 15.5 |
| OpenJDK supported, supported `java -version`:
```
openjdk version "21.0.2" 2024-01-16
OpenJDK Runtime Environment Homebrew (build 21.0.2)
OpenJDK 64-Bit Server VM Homebrew (build 21.0.2, mixed mode, sharing)
```

## Setup

1. `git clone git@github.com:sentry-demos/android.git`

2. Open project using Android Studio. It will take a few minutes for Gradle to configure itself and download dependencies. <img width="406" alt="Screenshot 2023-02-06 at 4 33 15 PM" src="https://user-images.githubusercontent.com/490201/217118488-8b3e0264-d421-4c08-b534-10af9432b7bd.png">

3. Set your Build Variant to 'release' instead of debug. Or else debug symbols won't get uploaded.
 Build Variants tab (left side of Android Studio) > Select 'release' under Active Build Variant Column > if that's missing then go to Build > Edit Build Types.

4. Sync the project with the Gradle files

    ```
    Tools -> Android -> Sync Project with Gradle Files

    In some Android Studio version this will be available under:

    File -> Sync Project with Gradle Files
    ```

5. Put your Sentry DSN key in `AndroidManifest.xml` and your 'project' name in the Makefile

6. For Performance Demo: Configure your *backend endpoint* in the `empowerplant.domain` attribute in `AndroidManifest.xml` (`application-monitoring-python` by default)

7. Put your AUTH Token and project name in sentry.properties

8. `make all`

9. Android Studio install Android NDK in Preferences > System & Behavior > System Settings > Android SDK > SDK Tools > and install the following:
![AndroidTools](screenshots/android-tools.png)

![gif](screenshots/debug-information-files-settings.png)

10. Maintain a separate branch which has your auth token.

11. Optional - Add se:<yourname> tag to buildConfigField in build.gradle.


## Run

1. `make all` if you haven't yet, or have made significant changes to your code. Otherwise run the app.
2. Run 'app' in Android Studio on an Android Virtual Device.
3. Open the app, add items to cart, check out; open List App and click error-related buttons
![demo](screenshots/demo.gif)

## How To Make a New Release

:warning: Only follow these steps when on the `master` branch, with no untracked git changes. This is necessary because we rely on these releases for our automated test data ("TDA") and don't want unintended local modifications (i.e. to DSNs or project names) to accidentally make it into our automated data. :warning:

### Part 1: Generate Release artifacts

1. Checkout a new git branch.
2. Run `./generate_release_artifacts.sh`, then run `./gradlew assembleRelease` to generate debug-build and release-build `.apk` files; copy them to the `/release` folder with `cp app/build/outputs/apk/release/app-release.apk ./release`
3. Commit the changes to `build.gradle`, `app-release.apk`, and `app-debug.apk`.
4. Push up the changes in a pull request.
5. Get an approval and merge the changes.

### Part 2: Create the GitHub release

(After completing the steps in Part 1, and once your release branch is merged in.)
1. Run the Release GitHub Action https://github.com/sentry-demos/android/actions/workflows/release.yml, choose a version name and version code as prompted, then `Run workflow`.
2. You'll see that a new release was created in https://github.com/sentry-demos/android/releases.
3. Restart your demo automation tools so they'll still hitting the latest APK release.

### Other Notes on releases

The version code is unique. This is already part of build system in Android. The app won't compile without it.

Optional - Setting the release in AndroidManifest.xml will override what's set in src/build.gradle. Possible uses cases would be:
1. Indicating Paid vs Free versions of your apps
2. Match versions for your Android and iOS apps together. force a release name.
3. The pattern 'package@name+version' is new from Sentry, so you could override that in AndroidManifest.xml
4. Good for testing if you're iterating quickly, but not publishing your app.
```
<meta-data android:name="io.sentry.release" android:value="io.sentry.sample@1.0.0+1" />
```

## How To Upgrade SDK
1. Change to the latest Sentry Android Gradle Plugin as shown in https://docs.sentry.io/platforms/android/configuration/gradle/#setup:
```
plugins {
    id "com.android.application"
    id "io.sentry.android.gradle" version "5.8.0"
}
```
2. Match the sdk_version from https://github.com/getsentry/sentry-android-gradle-plugin/blob/main/plugin-build/gradle.properties in src/build.gradle like:
```
    implementation 'io.sentry:sentry-android:8.17.0'
``` 
Note: This step may not be necessary with newer versions of Sentry Android Gradle Plugin

3. Make a new Release
4. Click 'Sync Now' for sync'ing your gradle files in AndroidStudio
5. `make all` will do a new `./gradlew build`

## Sessions
- If you put app to background, then put to foreground within 30 seconds, it does not create new Session.
- If you put app to background, then wait more than 30 seconds, then put to foreground, it will create new session.
    - 30 seconds is mentioned here, but we set our default in AndroidManifest.xml to 3 seconds (3000 millisecond) for demo purposes.
- Swiping up "close", there's no way to know what happened to the session. It's not a error/crash. It's a normal exited session.
    - Opening the app again right away should great a fresh new session.
- If device has a stable connection, events sent right away.
    - SentryServer has a pipeline that's queuing events, depends on state of Sentry.
    - C++ crashes go through Symbolicator which has its own queuing and symbolication takes slightly longer.
        - Need to restart the app.
- Session (ending) is sent when App goes to Background OR there's a crash.
- Session data is sent when Session Starts and when Session Ends.
- So if you make a Handled Error, the Session data is not sent just yet. updates the session only locally in the device.
- Difficult to compare crashes with a report in Discover (with `handled:no`) vs the Release because when a crash happens, you have to wait for the device to come back online again.
- If you are ever filtering, sampling, or rate .imiting events/crashes out, then it's possible that the Sessions data isn't getting filtered/sampled and so your Crash Free Rate will appear higher than it actually is.