# GrowPlot — v0.7.00 Build 08

GrowPlot is an Android farm-management application with CCTV monitoring, Google Drive image feeds, A.I Plant Health, diagnosis workflows and plot management.

**Current source version:** v0.7.00 Build 08 (`versionCode=222`, Android package `com.growplot.farm`). This identity is recorded in `version.properties`. A version number in source does not by itself confirm that a matching APK has been built; check the GitHub Actions run and its build manifest before distributing an APK.

## Verified app baseline

The following behavior was documented as device-verified for v0.7.00 Build 02:

- All six CCTV streaming slots display on the user's Android device.
- The Google Drive image scan reports per-camera image counts.
- A.I Plant Health displays the expected diagnosis images.
- **Outstanding:** daily image analysis is not functioning as intended. Plant identification, visible leaf counting and maturity/growth-stage estimates remain development goals. Plant age inferred from images is approximate unless a planting date is available.
- A dedicated per-CCTV count summary in A.I Health remains an enhancement; the existing Java image counting works. Do not claim the summary UI is complete without Android verification.

## Source and release workflow

1. Use the current source on GitHub `main` as the working baseline. Preserve the restored Build 02 gold copy as a rollback point, including CCTV streaming, Google Drive scanning and `MainActivity.java` / `TimelineBuckets.java`.
2. Make the smallest required enhancement. If it breaks working functionality, restore the gold copy first.
3. Use `version.properties` as the authoritative version identity. Increment `versionCode` for each new installable; documentation changes do not require a build.
4. Build 08 uses `.github/workflows/growplot-build-08.yml` on its isolated feature branch to produce the ARM64-only APK, matching source ZIP and build manifest. The existing `.github/workflows/android-arm64-build.yml` remains unchanged.
5. Inspect build logs, APK metadata, signing and artifact before describing a build as complete. Compilation alone does not establish Android camera or Google Drive functionality.
6. Keep signing keys and API credentials outside the repository and archives. For in-place updates, preserve the stable signing certificate.

See [AGENTS.md](AGENTS.md) and [RELEASE-PROCESS.md](RELEASE-PROCESS.md) for complete build, signing and verification procedures.

## Current links

- **Source of truth:** [GitHub repository](https://github.com/rizaljamin/GrowPlot-New), branch `main`.
- **APK workflow:** [Build GrowPlot ARM64 APK](https://github.com/rizaljamin/GrowPlot-New/actions/workflows/android-arm64-build.yml).

## Device compatibility

GrowPlot APKs target `arm64-v8a` devices running Android 7.0 (API 24) or newer. Android device testing is still required for camera playback, Google Drive access, diagnosis providers and installation over a previous signed build.
