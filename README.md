# GrowPlot v0.6.01 Build 28

GrowPlot is an Android A.I driven app for farm camera monitoring, plant health workflows, plot management, and diagnosis media.Fully coded by A.I Agents with human directive guided by the requirement specification documents.

GrowPlot retains the selected Google Drive folder grant and storage paths between normal launches. Android may require a new one-time grant after reinstall, cleared app data, or permission revocation. Build 8 connects the saved Kindwise crop.health key to automatic photo and video-frame diagnosis alongside Pl@ntNet; provider results remain separate and are saved with the section history.

Batch the next 2–3 discrete enhancements before generating an APK, unless the user explicitly asks for an earlier build. Keep source changes tracked between builds; then use `tools/build-apk.sh` to validate and package the batch. It uses the included Gradle wrapper and bootstraps the Android SDK once for this project environment. Use `tools/release-growplot.sh` for the stricter installable-candidate gate that requires the existing Build 30 signing key and the private Pl@ntNet key. Details are in `AGENTS.md` and `RELEASE-PROCESS.md`
Current source prepares v0.6.01 Build 28 (Android versionCode 155, package `com.growplot.farm`) from the current Build 26 main branch. It preserves Firebase authentication success independently of Firestore, restores the login recovery/account details/first-plot modal changes, and retains the stable signing-key workflow. GitHub ARM64 build and Android device verification are pending.

The previously verified Build 9 ARM64 development APK has SHA-256 `dce0cc453307290d422e933f2f1142d964c8abfcea2283fc0877ed874776d469`; its matching source ZIP SHA-256 is `4a6d531b4f79874ad257f8f837ef28671f262d99f482a7a815873ba184f4cf71`. Its signing certificate differs from the saved Build 30 certificate, and the Pl@ntNet build key is absent, so it is development-only and is not a safe in-place update. Keep the existing installed app and its data; use only an APK signed by the saved Build 30 key for an in-place update. Android device and live provider verification remain pending. Google Play upload/publication has not been performed or verified.

## APK distribution and device compatibility

From 12 October 2026 onward, distribute the ARM64-only APK (`arm64-v8a`) for GrowPlot. A universal APK is no longer required for future releases. The build and strict-release workflows generate one ARM64 APK; universal APK and debug AAB generation have been removed from the routine build.

**Compatibility disclaimer:** The ARM64 APK requires Android 7.0 (API 24) or later and a device whose Android operating system supports the `arm64-v8a` ABI. It cannot be installed on ARM32-only devices, devices running a 32-bit Android operating system even with a 64-bit processor, or x86/x86_64 devices. Confirm ARM64 Android support before installation.

The VLC streaming libraries remain bundled in GrowPlot so CCTV playback stays inside the app; users do not need to install the separate VLC app. For v0.6.01 Build 3, the measured ARM64 APK is 61,445,519 bytes (61.45 MB), compared with 101,182,574 bytes (101.18 MB) for the historical universal APK. Future sizes may change.

Settings sections open collapsed and expand on tap. VIGI login is encrypted on the device and reused after restart; blank password edits retain the saved password. Missing login prompts for input. A confirmed RTSP authentication rejection prompts for correction; a network failure retains the saved login.


## Standard APK build workflow (14 October 2026)

When the project owner instructs **build APK** or **generate APK**, follow this process without requesting separate GitHub build instructions:

1. Implement and commit all approved source changes in a development branch. A version-number-only commit is not a completed implementation.
2. Validate source syntax and regression tests, and review differences against the protected Build 9 baseline.
3. Merge the completed, reviewed branch into `main` only when the actual source is present and checks pass. Preserve `archive/main-before-build9-recovery-20261013` for rollback.
4. The `.github/workflows/android-arm64-build.yml` GitHub Actions workflow runs on relevant source pushes to `main` and produces an ARM64 APK, source ZIP, and build manifest. It bootstraps Android SDK using `tools/ensure-android-sdk.sh`, the same path used locally. Only this workflow builds APKs, preventing duplicate CI runs.
5. Inspect GitHub Actions success, APK package/version, signing certificate and ABI before sharing the artifact. Device installation and CCTV streaming remain user-device acceptance checks.
6. **Do not sync to Google Drive as part of the APK build.** The existing separate Drive workflow is not a prerequisite for APK generation.

Do not describe an APK as built merely because source was zipped, a version file changed, or a workflow was configured. If a step is blocked, report the exact blocked operation and last successful commit.

**Build 10 status:** The repository version identity is Build 10 / versionCode 137, but app code still matches Build 9. Do not describe the Build 10 APK as produced until the repaired GitHub Actions run succeeds and its artifact is inspected.
