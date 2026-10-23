# GrowPlot — v0.7.01 Build 16

GrowPlot is an Android farm-management application with CCTV monitoring, Google Drive image feeds, A.I Plant Health, diagnosis workflows and plot management.

**Current source version:** v0.7.01 Build 16 (`versionCode=251`, Android package `com.growplot.farm`). This identity is recorded in `version.properties`. A version number in source does not by itself confirm that a matching APK has been built; check the GitHub Actions run and its build manifest before distributing an APK.

## Google Drive image feed

Build 16 changes Settings → Plot Management → Data Feed → Connect to folder to open Android's Google Drive folder picker. Select the existing plot folder under `Projects/GrowPlot/Image_Feed` to store its exact folder ID, or select `Projects`, `GrowPlot`, or `Image_Feed` to create the standard plot folder. This avoids guessing the Drive subfolder from the Firebase email. ARM64 build and real-device picker, scan, and upload checks are pending.

## Vertex image assessment

Firebase deploys the authenticated `vertexImageDiagnosis` callable in `us-central1`; it sends inline photos to Gemini on Vertex AI and stores no photo. The Build 11 APK still stops after OpenCV. The pending app change triggers the callable only after OpenCV completes when the user taps Run Diagnosis, using the same selected photo and showing the returned Vertex result in its own subsection. It does not start YOLO, Pl@ntNet, or Kindwise.

## Verified app baseline

The following behavior was documented as device-verified for v0.7.00 Build 02:

- All six CCTV streaming slots display on the user's Android device.
- The Google Drive image scan reports per-camera image counts.
- A.I Plant Health displays the expected diagnosis images.
- The Build 14 source adds on-demand photo-based Pl@ntNet species candidates when Run Diagnosis is tapped. The pre-run species shown for a CCTV section is only a preset visual estimate. HSV colour cues remain preliminary; YOLO, validated leaf counts and image-based maturity estimates are not included. Plant age needs a planting date.
- A dedicated per-CCTV count summary in A.I Health remains an enhancement; the existing Java image counting works. Do not claim the summary UI is complete without Android verification.

## OpenCV plant boundary (Build 09)

OpenCV uses the Build 09 HSV thresholds and grouping rules to estimate a foliage outline, draws it in magenta, and reports colour counts and percentages within that region. Build 10's separate Hue, Saturation and Value image tiles remain removed; the panel keeps the HSV-converted photo, estimated boundary, YGB mask and pixel metrics. Build 11 restores the earlier boundary settings after the wider settings produced an incorrect outline. The boundary is a colour-based approximation: soil or background may still be included, and leaves may be missed. The output is not validated plant detection, a health score, or disease diagnosis. In the Build 11 APK, Run Diagnosis stops after OpenCV. The pending Vertex wiring keeps that OpenCV output and then requests a separate image assessment from the Firebase callable.

The Build 12 APK and Vertex call still require Android device verification.

## Source and release workflow

1. Use the current source on GitHub `main` as the working baseline. Preserve the restored Build 02 gold copy as a rollback point, including CCTV streaming, Google Drive scanning and `MainActivity.java` / `TimelineBuckets.java`.
2. Make the smallest required enhancement. If it breaks working functionality, restore the gold copy first.
3. Use `version.properties` as the authoritative version identity. Increment `versionCode` for each new installable; documentation changes do not require a build.
4. Run the established `.github/workflows/android-arm64-build.yml` workflow to produce the ARM64-only APK, matching source ZIP and build manifest. Use `tools/build-apk.sh` locally. Do not introduce duplicate build workflows.
5. Inspect build logs, APK metadata, signing and artifact before describing a build as complete. Compilation alone does not establish Android camera or Google Drive functionality.
6. Keep signing keys and API credentials outside the repository and archives. For in-place updates, preserve the stable signing certificate.

See [AGENTS.md](AGENTS.md) and [RELEASE-PROCESS.md](RELEASE-PROCESS.md) for complete build, signing and verification procedures.

## Current links

- **Source of truth:** [GitHub repository](https://github.com/rizaljamin/GrowPlot-New), branch `main`.
- **APK workflow:** [Build GrowPlot ARM64 APK](https://github.com/rizaljamin/GrowPlot-New/actions/workflows/android-arm64-build.yml).

## Device compatibility

GrowPlot APKs target `arm64-v8a` devices running Android 7.0 (API 24) or newer. Android device testing is still required for camera playback, Google Drive access, diagnosis providers and installation over a previous signed build.
