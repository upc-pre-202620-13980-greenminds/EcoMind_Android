# EcoMind Android

The Android client uses the Azure API by default in both debug and release builds:
`https://ca-ecomind-api.icybush-bd135296.westus2.azurecontainerapps.io/api/v1/`.

For local development, set `api.base.url` in the ignored `local.properties` file.
An existing override takes precedence in debug; remove it to use Azure. Rebuild the app
after changing this setting. Release builds keep the configured Azure endpoint.

Password recovery starts from Sign in and opens `ecomind://authentication/reset-password`
from the email handoff. Reset links are single-use. Sign out clears this device's session;
the existing backend contract does not revoke previously issued JWTs.

Run `bash ./gradlew :app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest`
with the Android SDK and the checked-in Gradle daemon JVM criteria configured
(`gradle/gradle-daemon-jvm.properties` currently selects Java 25). Live IAM instrumentation is opt-in and uses
private fixture files; keep passwords and reset links out of runner arguments and Git.

The SDK Flutter checks are skipped unless explicitly selected. Stage
`flutter-fixture.json` privately in this Android app's external files directory with
`email`, `newPassword` and `recoveryLink`, then select the specific test method:

```bash
adb shell am instrument -w -r -e class pe.greenminds.ecomind.iam.FlutterControlledResetUiAutomationTest#controlledResetThenSignInAndSignOut -e iamFlutterControlledUiTest true pe.greenminds.ecomind.test/androidx.test.runner.AndroidJUnitRunner
```

This drives the separately installed normal Flutter APK. Use only approved test
accounts and a controlled reset link. `iamFlutterContinueAfterReset=true` resumes
sign-in/sign-out after a confirmed reset without reopening the single-use link.
The independent `signedOutBackAndRelaunch` method requires
`iamFlutterSignedOutOnly=true` and an already signed-out Flutter app. Never run the
whole instrumentation suite with a live opt-in. Keep fixtures and recordings private
and remove staged fixtures after verification.
