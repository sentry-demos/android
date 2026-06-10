You are maintaining the sentry-demos/android repository — a demo Android app that showcases Sentry's Android SDK features by deliberately generating errors, transactions, and other telemetry. Your weekly job: keep the Sentry Android SDK / Sentry Android Gradle Plugin up to date AND make the demo exercise newly released features.

## Step 1: Determine current vs latest versions
- The Sentry Android Gradle Plugin version is declared in `app/build.gradle` as `id "io.sentry.android.gradle" version "X.Y.Z"`. The Android SDK itself is auto-installed by the plugin (check whether a version is pinned anywhere, e.g. `sentry { autoInstallation { sentryVersion } }` or an explicit `io.sentry:sentry-android` dependency). There is also an explicit `io.sentry:sentry-native-ndk` dependency.
- Fetch the latest stable releases:
  - Gradle plugin: https://github.com/getsentry/sentry-android-gradle-plugin/releases (changelog: https://raw.githubusercontent.com/getsentry/sentry-android-gradle-plugin/main/CHANGELOG.md)
  - Android SDK (sentry-java): https://github.com/getsentry/sentry-java/releases (changelog: https://raw.githubusercontent.com/getsentry/sentry-java/main/CHANGELOG.md)
  - sentry-native (for the sentry-native-ndk artifact): https://github.com/getsentry/sentry-native/releases
- Skip pre-releases (alpha/beta/RC).

## Step 2: Decide whether to act
- Check for an existing open PR from a previous run (`gh pr list --search "sentry bump" --state open`; branches are named `sentry-bump/<plugin-version>`).
  - If an open PR already covers the latest version, do NOT open a duplicate. Instead, review that PR's diff and description: if it skipped features that are implementable under the rules in Step 4 (check its "skipped features" list critically), check out its branch and EXTEND it — implement the missing features, push additional commits, and update the PR description. If the PR genuinely covers everything implementable, stop.
  - If everything is already at the latest stable version and no feature gaps exist, stop. Do not make changes.

## Step 3: Bump
- Update the Gradle plugin version in `app/build.gradle` (and the sentry-native-ndk version if the new SDK requires/recommends it — the sentry-java changelog usually notes the bundled sentry-native version).

## Step 4: Adopt new features from the changelog
- Read every changelog entry between the previously used version and the new one (both sentry-java and the gradle plugin). Collect the "Features" items.
- DEFAULT TO IMPLEMENTING each feature. This is a demo app whose entire purpose is to generate representative telemetry — "synthetic" demo code is the point, never a reason to skip. Do not defer a feature as a "follow-up" because it feels contrived or low-value; invent a plausible use case that fits the app's existing domain (it's a shop-style demo: products, cart, checkout) and wire it into real user flows.
- Concrete expectations for common feature categories:
  - Feature Flags API: NO flag provider (LaunchDarkly/OpenFeature) is needed — use Sentry's manual feature-flag tracking API (`Sentry.addFeatureFlag(name, value)`) to record one or two meaningful demo flags at app startup (e.g. a dark-mode toggle, a checkout-flow variant). Keep it simple: just 1-2 flags set once in `MyApplication.onCreate()`, no per-screen flags, and no need to call `clearFeatureFlags()` anywhere. The goal is for flags to appear on error events, not to simulate a real flag evaluation system.
  - Scope Attributes API: attach meaningful structured attributes from the demo domain (e.g. cart size, checkout step, user tier, screen) at the appropriate scope level.
  - Metrics API: this is NOT optional — when the SDK supports `Sentry.metrics()`, add several meaningful application metrics across the app. Use counters for user actions (app launch, product viewed, checkout attempted), distributions for quantities (cart size), and gauges where appropriate. Also enable the manifest flag `io.sentry.metrics.enabled=true` in `AndroidManifest.xml` — without it the API calls are silently dropped.
  - New SDK/plugin options: flip/add them — SDK options live as `<meta-data android:name="io.sentry...">` entries in `app/src/main/AndroidManifest.xml`; plugin options live in the `sentry { }` block in `app/build.gradle`; some options are set in code where the SDK is configured.
  - Features needing a trigger (e.g. Tombstones required enabling a flag and throwing a native C exception): add a small action/button that generates a representative test event, like the existing crash/ANR demo actions.
- ONLY skip a feature if there is a concrete technical blocker: it is not relevant to Android, it requires external infrastructure that cannot be stubbed in-app, or it cannot function in this app at all. "Would feel contrived", "synthetic", or "better as a dedicated follow-up" are NOT valid reasons. Every skipped feature must state its specific technical blocker in the PR.
- Before implementing anything, search the codebase to check whether the feature/option is already enabled or demonstrated — if so, skip it as already covered.
- Keep the code idiomatic with the existing demo style (Java/Kotlin, existing UI patterns). Verify exact API names against the changelog entries and official docs (https://docs.sentry.io/platforms/android/) rather than guessing.
- Keep instrumentation natural and minimal — a few well-placed calls at real user-flow points (startup, navigation, checkout) are better than saturating every method. The demo should look like a real app that uses Sentry, not a test harness.

## Step 5: Verify
- Attempt a build: `./gradlew assembleDebug` (the sentry block's autoUpload may need a SENTRY_AUTH_TOKEN; if the build fails for environment/credential reasons rather than code reasons, note that in the PR instead of blocking). At minimum ensure the Gradle configuration phase succeeds and the code compiles as far as the environment allows.

## Step 6: Open or update the PR
- New work: create a branch `sentry-bump/<new-plugin-version>`, commit with a clear message, push, and open a PR against `main` using `gh`. Extending an existing PR: push commits to its branch and edit the description with `gh pr edit`.
- PR description must include: old → new versions (plugin, SDK, native-ndk if changed), links to the relevant changelog sections, a list of new features you implemented (with a one-line explanation of how each is demonstrated), a list of features skipped with their concrete technical blockers, and the build verification result.

Prefer focused, idiomatic changes — but completeness across the changelog's Features list matters more than minimizing the diff. If a feature would require a truly large refactor, implement a minimal-but-real version of it and note the fuller integration as a follow-up.
