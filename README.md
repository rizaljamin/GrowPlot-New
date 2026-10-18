# GrowPlot — v0.7.00 Build 02

GrowPlot is an Android farm-management application with CCTV monitoring, Google Drive image feeds, A.I Plant Health, diagnosis workflows and plot management.

**Current GitHub baseline:** v0.7.00 Build 02 (`versionCode=215`, Android package `com.growplot.farm`). The complete working Build 02 source was restored as the **gold copy** on 18 October 2026. The next planned installable is **v0.7.00 Build 03** (`versionCode=216`), not yet released.

## Verified Build 02 important features

- All six CCTV streaming slots display on the user's Android device.
- The existing Google Drive image scan reports per-camera image counts. An independent folder listing confirmed **99 JPEG images: CCTV01 29, CCTV02 70, CCTV03–06 0**, in the `growplot2026@gmail.com` folder at the time of checking.
- A.I Plant Health displays the expected diagnosis images.
- **Outstanding:** daily image analysis is not functioning as intended. The next development focus is pixel analysis for plant identification, visible leaf counting and maturity/growth-stage estimation. Plant age inferred from images is approximate unless a planting date is available.
- A dedicated per-CCTV count summary in A.I Health remains an enhancement; the existing Java image counting works. Do not claim the summary UI is complete without Android verification.

## Gold-copy and release governance

1. Treat the restored **v0.7.00 Build 02** source on GitHub `main` as the working baseline. Preserve CCTV streaming, Google Drive scanning and the two restored Java files: `MainActivity.java` and `TimelineBuckets.java`.
2. Make the smallest required enhancement; do not replace the complete app to add a display-only summary. If an enhancement breaks working functionality, restore the gold copy first.
3. Use `version.properties` as the authoritative version identity. Increment `versionCode` for each new installable; documentation changes do not require a build.
4. Run the established `.github/workflows/android-arm64-build.yml` workflow to produce the **ARM64-only** APK, matching source ZIP and build manifest. Use `tools/build-apk.sh` locally. Do not introduce duplicate build workflows.
5. Inspect build logs, APK metadata, signing and artifact before describing a build as complete. Compilation alone does not establish Android camera or Google Drive functionality.
6. Keep signing keys and API credentials outside the repository and archives. For in-place updates, preserve the stable signing certificate.


See [AGENTS.md](AGENTS.md) and [RELEASE-PROCESS.md](RELEASE-PROCESS.md) for complete build, signing and verification procedures.

## Current workflow and next steps

- **Source of truth:** [GitHub repository](https://github.com/rizaljamin/GrowPlot-New), branch `main`.
- **APK workflow:** [Build GrowPlot ARM64 APK](https://github.com/rizaljamin/GrowPlot-New/actions/workflows/android-arm64-build.yml).
- **Next Build 03 scope:** plant type/species identification, pixel-level foliage analysis, visible leaf counting, maturity/growth-stage estimation and repair of daily image analysis.
- **Development workflow improvement:** avoid manual source ZIP download/upload where possible. The restored gold baseline currently takes priority over an experimental ZIP importer.

## Versioning

Development versions use `vMAJOR.MINOR.RELEASE Build NN`; the Android `versionCode` increases monotonically. The planned next version is **v0.7.00 Build 03 / versionCode 216**, subject to completing and testing the planned enhancements.

## Device compatibility

GrowPlot APKs target `arm64-v8a` devices running Android 7.0 (API 24) or newer. Android device testing is still required for camera playback, Google Drive access, diagnosis providers and installation over a previous signed build.
