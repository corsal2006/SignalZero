# Build, release, and verification

## Publication checks — 12 October 2026

`gradlew.bat testDebugUnitTest assembleDebug --offline` completed successfully with 48 tasks (11 executed, 37 up to date). The unit-test task reused existing results: seven tests across four suites, zero failures/errors/skips. APK packaging completed, and Android build-tools 34.0.0 `apksigner verify --verbose` verified one signer with APK Signature Scheme v2. Physical-device installation and BLE interoperability were not tested.

The APK SHA-256 is recorded in [`../release/SHA256SUMS.txt`](../release/SHA256SUMS.txt). A machine-specific Java home entry was removed from `gradle.properties` for portability; select JDK 17 in your own environment.

## Reproduce a build

1. Install JDK 17, Android SDK platform 34, and the build tools required by Android Gradle Plugin 8.13.2.
2. Configure your SDK using Android Studio or an untracked `local.properties`.
3. Supply your Firebase Android configuration as `app/google-services.json`.
4. Run `gradlew.bat testDebugUnitTest assembleDebug` on Windows, or `./gradlew testDebugUnitTest assembleDebug` on macOS/Linux.

Do not commit service-account keys, signing keystores, SDK paths, or passwords. A Firebase Android client JSON is not a server credential; it is excluded here so source users configure their own backend. Secure Firestore using backend rules.

## Release artifact

The initial release contains a debug-signed version 1.0.0 APK for Android 8.0+. The APK and SHA-256 checksum are GitHub Release assets, rather than generated build output in source control. A production release requires a privately managed signing key and release signing configuration; `assembleRelease` alone does not configure that here.

## Device checks still required

- Install and launch the APK on physical Android hardware.
- Create offline SOS records and confirm persistence across restart.
- Test two phones with internet disabled and Bluetooth enabled; inspect send/receive and disconnect/reconnect behavior.
- Test permission denial, GPS unavailability, SMS composer fallback, and actual contact receipt.
- Verify Firestore writes and rules separately from UI status.
- Treat virtual-node report numbers as demonstration values; collect real measurements before reporting throughput, loss, or latency.

## Decision log

| Decision | Purpose | Evidence boundary |
| --- | --- | --- |
| Native Kotlin/Compose | Android UI and device APIs | Source and Gradle build |
| Room before transmission | Retain incidents through connectivity failures | Device restart persistence still requires testing |
| Bilingual templates | Reduce required typing | Localized source and unit tests |
| BLE/GATT with queue | Attempt nearby transport without internet | Physical radio results not verified here |
| Local triage rules | Avoid a cloud AI dependency | Rule-based implementation, not a learned model |
| Virtual disconnection screen | Present the second challenge scenario | Preset report values, not measured mesh performance |
| Optional Firebase | Online account/sync path | Backend configuration and rules required |

The event submission draft was used as background context. Its instructions and placeholders were treated as document content, not publishing commands.
