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
with JDK 21 and the Android SDK configured. Live IAM instrumentation is opt-in and uses
private fixture files; keep passwords and reset links out of runner arguments and Git.
