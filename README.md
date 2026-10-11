# GrowPlot v0.6.01 Build 2

GrowPlot is an Android app for farm camera monitoring, plant health workflows, plot management, and diagnosis media.

GrowPlot retains the selected Google Drive folder grant and storage paths between normal launches. Android may require a new one-time grant after reinstall, cleared app data, or permission revocation. Build 11 corrects diagnosis history so only successful Pl@ntNet results count, and prevents a delayed result from one section replacing another section’s open diagnosis.

Batch the next 2–3 discrete enhancements before generating an APK, unless the user explicitly asks for an earlier build. Keep source changes tracked between builds; then use `tools/build-apk.sh` to validate and package the batch. It uses the included Gradle wrapper and bootstraps the Android SDK once for this project environment. Use `tools/release-growplot.sh` for the stricter installable-candidate gate that requires the existing Build 30 signing key and the private Pl@ntNet key. Details are in `AGENTS.md` and `RELEASE-PROCESS.md`.

The active baseline is v0.6.01 Build 2 (versionCode 129). It includes a universal development APK (101.2 MB), an arm64 development APK (61.4 MB, about 39% smaller) for supported devices such as the Lenovo TB370FU, a debug AAB (51.1 MB) for bundle-size evaluation, and a matching source ZIP. The AAB is debug-signed and is not ready for Google Play upload. Build 1's CCTV03 VIGI credential prompt remains included; camera playback needs same-network testing. See `VERSION-LOG.txt` and `BUG-LOG.md` for implementation notes and verification limits.
