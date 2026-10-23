# GrowPlot — v0.7.01 Build 19

GrowPlot is an Android farm-management application with CCTV monitoring, Google Drive image feeds, A.I Plant Health, diagnosis workflows and plot management.

**Current source version:** v0.7.01 Build 19 (`versionCode=254`, Android package `com.growplot.farm`). This identity is recorded in `version.properties`. GitHub Actions ARM64 run #487 passed package, version, and stable-signature checks and produced the APK. APK SHA-256: `32c3e20a3b86885a3ade5468e0bba4f8e90349f3f43719053dd1203cc93090e2`. Drive account connection and folder scanning still need tablet verification.

## Google Drive image feed

Build 17 lists available Drive paths beneath `Projects/GrowPlot/Image_Feed` in an in-app selector. Build 19 requests Drive authorization for the signed-in GrowPlot user's Google account, verifies the account returned by Google, and preselects that user's `<email>/<plot>` folder. Other accessible folders remain selectable. Build 18 retries child-folder and file listings in the user's Drive corpus when `allDrives` cannot see descendants of a directly shared folder, and chooses the `Projects` tree that actually contains `GrowPlot/Image_Feed` when duplicate names exist. GrowPlot stores the selected folder's exact ID and path. GitHub Actions ARM64 run #486 passed and produced a stable-signed APK. Google authorization, listing, scan, and upload still need Android tablet verification.

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
