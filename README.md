# GrowPlot v0.6.01 Build 5

GrowPlot is an Android A.I driven app for farm camera monitoring, plant health workflows, plot management, and diagnosis media.Fully coded by A.I Agents with human directive guided by the requirement specification documents.

GrowPlot retains the selected Google Drive folder grant and storage paths between normal launches. Android may require a new one-time grant after reinstall, cleared app data, or permission revocation. The GitHub main branch currently records v0.6.01 Build 5 (Android versionCode 132). A separately supplied Build 9 APK/source archive records versionCode 136, but is not yet synchronized into this branch; do not treat this README as evidence that Build 9 has been published on GitHub.

Batch the next 2–3 discrete enhancements before generating an APK, unless the user explicitly asks for an earlier build. Keep source changes tracked between builds; then use `tools/build-apk.sh` to validate and package the batch. It uses the included Gradle wrapper and bootstraps the Android SDK once for this project environment. Use `tools/release-growplot.sh` for the stricter installable-candidate gate that requires the existing Build 30 signing key and the private Pl@ntNet key. Details are in `AGENTS.md` and `RELEASE-PROCESS.md`
The current GitHub source baseline is v0.6.01 Build 5 (versionCode 132). It retains the dashboard-only VLC frames and corrects shared snapshot intervals, selected-plot capture routing, CCTV02 source labelling, and immediate photo-history refresh. Android menu switching and VIGI playback need same-device verification. See `VERSION-LOG.txt` and `BUG-LOG.md` for details.

## APK distribution and device compatibility

From 12 October 2026 onward, distribute the ARM64-only APK (`arm64-v8a`) for GrowPlot. A universal APK is no longer required for future releases. The build and strict-release workflows generate one ARM64 APK; universal APK and debug AAB generation have been removed from the routine build.

**Compatibility disclaimer:** The ARM64 APK requires Android 7.0 (API 24) or later and a device whose Android operating system supports the `arm64-v8a` ABI. It cannot be installed on ARM32-only devices, devices running a 32-bit Android operating system even with a 64-bit processor, or x86/x86_64 devices. Confirm ARM64 Android support before installation.

The VLC streaming libraries remain bundled in GrowPlot so CCTV playback stays inside the app; users do not need to install the separate VLC app. For v0.6.01 Build 3, the measured ARM64 APK is 61,445,519 bytes (61.45 MB), compared with 101,182,574 bytes (101.18 MB) for the historical universal APK. Future sizes may change.

Settings sections open collapsed and expand on tap. VIGI login is encrypted on the device and reused after restart; blank password edits retain the saved password. Missing login prompts for input. A confirmed RTSP authentication rejection prompts for correction; a network failure retains the saved login.
