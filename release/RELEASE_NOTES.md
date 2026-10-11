# SignalZero 1.0.0 — Android demonstration build

Native Android SOS prototype with local incident persistence, bilingual templates, BLE/GATT nearby communication, trusted-contact workflows, optional Firebase sync, and a Grevix virtual-node resilience demonstration.

## Download

Install `SignalZero-1.0.0-debug.apk` on Android 8.0 or newer. This is a debug-signed demonstration APK. `SHA256SUMS.txt` contains its SHA-256 checksum.

## Included repository material

- Full Android source and Gradle wrapper.
- Ten screenshots and the supplied MP4 demo.
- Installation instructions, architecture explanation, file structure, and verification notes.

## Verification and limits

Gradle `testDebugUnitTest assembleDebug --offline` completed successfully on 12 October 2026. Unit-test results were up to date; the APK packaging tasks completed. APK signature verification is recorded in the repository build notes. No physical-device install or multi-phone BLE test was performed during publication.

The resilience report includes preset demonstration metrics, not measured mesh performance. Known routing and cloud-status limitations are documented in `docs/HOW_IT_WORKS.md`. This prototype has no demonstrated official responder dispatch integration.
