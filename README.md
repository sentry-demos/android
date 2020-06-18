# Android Demo

This app demonstrates how to use Sentry in an Android application for capturing 4 types of exceptions:

- Unhandled Exceptions (2)
- Handled Exceptions
- Application Not Responding
- Native Crashes from C++ native code
- Message Capture

This app has all configuration (e.g. gradle) set to include Sentry SDK and ANR (Application Not Responding) and NDK (crash) events.

Sentry NDK libraries are used in addition to the Sentry SDK, for capturing errors and crashes in C++.

For use in **Production** see the [Official Sentry Android Documentation](https://docs.sentry.io/platforms/android/)
Additional documentation:
[ANR Configuration](https://docs.sentry.io/platforms/android/#configuration-options)
[NDK Configuration](https://docs.sentry.io/platforms/android/#integrating-the-ndk)

## Versions

| dependency    | version
| ------------- |:-------------:|
| sentry-android | 2.1.0-beta.1 |
| Android Studio | 3.6.2 |
| Gradle | 6.3 |
| AVD | Nexus 5x API 29 x86 |
| sentry-cli | 1.49.0 |
| macOS | Mojave 10.14.4 |

## Setup

1. `git clone git@github.com:sentry-demos/android.git`

2. Open project using Android Studio and set your Build Variant to 'release' instead of debug. Or else debug symbols won't get uploaded.

3. Sync the project with the Gradle files

    ```
    Tools -> Android -> Sync Project with Gradle Files

    In some Android Studio version this will be available under:

    File -> Sync Project with Gradle Files
    ```

4. Put your Sentry DSN key in `AndroidManifest.xml` and your 'project' name in the Makefile

5. Put your AUTH Token and project name in sentry.properties

6. `make all`

7. Android Studio install Android NDK in Preferences > System & Behavior > System Settings > Android SDK > SDK Tools > select NDK for download

You can see debug files were uploaded in your Project Settings
![gif](screenshots/debug-information-files-settings.png)

9. Maintain a separate branch which has your auth token.

## Run

1. `make all` if you haven't yet, or have made significant changes to your code. otherwise run the app.
2. Run 'app' in Android Studio on an Android Virtual Device.

## What's Happening

The MainActivity has 5 buttons that generate the following exception types:

1. **Unhandled Exception** of type Arithmetic Exception
2. **Unhandled Exception** of type NegativeArraySizeException + Strips PII (removes user IP address in beforeSend)
3. **Handled Exception** of type ArrayIndexOutOfBoundsException
4. **ApplicationNotResponding (ANR)** Uses an infinite loop to crash the app after 5 seconds and reports event to Sentry.
5. **Native Crash** of type SIGSEGV from native C++. The Sentry NDK sends this to Sentry.io for symbolication
6. **Native Message** send custom event/message from native C++.


## Android Native Crash: Missing Symbols for System Libraries

The Android team has added Android system symbol files to our built-in repositories (Add the new Android option in your project settings). If the native crash generated from your emulator is not fully symbolicated, this probably means our symbol server doesn't have the files relevant for your (virtual) device. 
In this case, you can fix that by updating Sentry's server. To do that:

1. Download the `Symbol Collector` app  (**io.sentry.symbol.collector-Signed.apk**) which is available in the latest [release](https://github.com/getsentry/symbol-collector/releases/)
2. Install it on to your emulator by drag-and-dropping the apk into the emulator screen.
3. Run the Symbol Collector application
4. Configure the target URL to transport the symbols to: `https://symbol-collector.services.sentry.io`
5. Click `Collect Symbols`
6. Once the transport completes, re-generate the crash.

## Release Health Testing
Use two different devices (ie. two different android emulators). Keep one device crash free and one that has crashes so you can compare the crash free user rates.
1. Select second device, different from the primary device you threw errors and crashes in
2. Click Play/run
3. Remember - do NOT click buttons and cause errors! you want to keep this one Crash Free. You could always make a new release if you want 100% crash free rates again.

1st device - errors, so you see Crash Free Rate go Down
2nd device - sessions w/out errors, so if you keep creaitng healthy sessions, the nCrash Free Rate should go back up

ANR - click button, then start clicking on other areas of the screen. The second click (not the button click) is when it starts counting the seconds
right when pop-up comes , event should be sent to Sentry. click 'close-up'.

See AndroidManifest.xml for different settings we tweak for demo's (e.g. default Session time, default ANR time

## How To Make a New Release
Standard - Incrementing these numbers in src/build.gradle. Change only versionCode, or both. can match like 14, 1.4
```
defaultConfig {
    applicationId "com.example.vu.android"
    minSdkVersion 21
    targetSdkVersion 29
    versionCode 13
    versionName "1.3"
}
```
This would make for a release of `1.3.0 (13) com.example.vu.androidh@1.3+13`.
The version code is unique. This is already part of build system in Android. The app won't compile without it.

Optional - Setting the release in AndroidManifest.xml will override what's set in src/build.gradle. Possible uses cases would be:
1. indicating Paid vs Free versions of your apps
2. match versionf for your Android and iOS apps together. force a release name.
3. the pattern 'package@name+version' is new from Sentry, so you could override that in AndroidManifest.xml
4. Good for testing if you're iterating quickly, but not publishing your app.
```
<meta-data android:name="io.sentry.release" android:value="io.sentry.sample@1.0.0+1" />
```

## How To Upgrade SDK
1. increment sdk number in src/build.gradle like `implementation 'io.sentry:sentry-android:2.1.4'`
2. Consider making a new Release
3. click 'Sync Now' for sync'ing your gradle files in AndroidStudio
4. `make all` will do a new `./gradlew build`

## ANR
Sometimes you'll see extra ANR events, because you have setting set to 3 seconds
Hard to compare Total Number of Crashes to a report in Discover on handled:no and the release, because when a crash happens, you have to wait for the device to come back online again.
There are some other technical reasons as well, which are still being sorted out.
For instance, if you're ever filtering, sampling or Rate Limiting events/crashes out, then it's possible that the Sessions data isn ot getting filtered/sampled and so your Crash Free rate will appear higher than it actually is.

## Sessions
- if you put app to background, and put to foreground in less than 30seconds, it does not create new Session
- if you put app to background, and wait more than 30seconds, then put to foreground, it will create new session
- swiping up "close"", there's no way to know what happened to the Session. it's not a error/crash. it's a normal exited session.
    - opening the app again right away, should great a fresh new session
- i write 30seconds here, but we set our default in AndroidManifest.xml to "3seconds" for demo purposes
- if device has a stable connection, events sent right away
    - SentryServer has a pipeline that's queuing events, depends on state of Sentry
    - c++ crashes go through Symbolicator which has its own queuing and symbolication takes longer
        - need to restart the app
- Session (ending) is sent when App goes to Background OR there's a crash
- Session data is sent when Session Starts and when Session Ends
- So if you make a Handled Error, the Session data is not sent just yet. updates the session only locally in the device.

## Misc Knowledge
- Release dashboard, open 1, 'All Issues' is issues across all the releases
- Release dashboard, open 1, 'New Issue' sometimes not populating...
- View Data in Discover if things aren't adding up / looking right in the Release Page
- see Notion page on 'Crashes in SDKs and Product' for status updates on this stuff
- `mechanism:signalHandler` comes from sentry-native and `mechanism:uncaughtException` comes from java/kotlin
- Now (06/02/2020) ANR reported only if the pop-up comes up. doing 5seconds like Google does
- When there's not a lot data yet, it's hard to calculate/show things.
- Unique Users isn't the user's email, it's the Device. so in Discover could try things (but not working) like user.id, device.uuid, device. We didn't want to use sensitive data for Sessions. We generate a uuid for the user - Installation ID of the app on that device
- Check Documentation, things may have changed.

## GIF Android Java Exception

![Android demo flow](android-demo.gif)

## GIF Android ANR

![Alt Text](android-demo-anr.gif)

## GIF Android Native Crash C++

![Native Crash](android-native-crash-take-1.gif)

