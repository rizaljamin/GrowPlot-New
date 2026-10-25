# GrowPlot — v0.7.02 Build 10 (Play AAB preparation)

## Google Play internal testing AAB

The Play testing build targets API 36 and is created with `tools/build-play-aab.sh` or the manual GitHub Actions workflow **Build GrowPlot Play AAB**. Configure a Play Console upload keystore and alias in the local `GROWPLOT_PLAY_UPLOAD_*` environment variables or GitHub Actions secrets. If no separate Play upload key is supplied, the workflow uses the existing stable GrowPlot signing key; confirm that its certificate is the upload certificate registered in Play Console before uploading. Set `GROWPLOT_EXPECTED_PLAY_UPLOAD_CERT_SHA256` to the certificate shown in Play Console so the script can reject a signer mismatch. The workflow produces a signed AAB artifact; it does not upload or publish to Play Console.

After the AAB is uploaded to the Internal testing track, add tester accounts and share the opt-in link from Play Console. Keep the Google Play app-signing certificate separate from the upload certificate; add the Play app-signing SHA-1 to Firebase if Google sign-in needs to work for Play-installed builds.

GrowPlot is an Android farm-management application with CCTV monitoring, Google Drive image feeds, A.I Plant Health, diagnosis workflows and plot management.

**Current source target:** v0.7.02 Build 10 (`versionCode=275`, Android package `com.growplot.farm`), prepared to target API 36 and build a Play upload AAB. Build 10 is preparation only; no signed AAB has been generated yet. The latest validated ARM64 APK remains Build 09. It shows a compact Plot/CCTV Summary with plant identity first and the latest OpenCV, Vertex, and Pl@ntNet results for each camera. The local development-only Power User login shortcut is removed. Firebase power-user access uses trusted `powerUser` or `admin` custom claims, with no hard-coded email allowlist. Build 09 passed its GitHub Actions build and package checks in run #572 (source `6448e4a23fcac35e0532f243507e02cb50e8ef61`). APK artifact: [download from run #572](https://github.com/rizaljamin/GrowPlot-New/actions/runs/40159381248/artifacts/12586390088). Android device verification remains pending. Build 08 Drive authorization and Maintenance changes are described below.

## Live CCTV snapshot capture

For each visible, playing live camera, GrowPlot attempts a snapshot when its configured interval has elapsed since that camera's last successful Drive save. Overdue snapshots are attempted shortly after the feed becomes available. Hidden feeds retry visibility checks every 30 seconds; an unconnected folder is rechecked every 60 seconds. The app must remain foregrounded with Home and the live camera visible.

## Plot maintenance

Watering, fertilising, weeding, pruning, and other farm work are recorded under Manage Farm → Maintenance. Each item is tied to a plot and has a scheduled date and time. Watering can be scheduled once, twice, or three times per day. Existing irrigation schedules migrate into Maintenance and retain their saved details.

## Confirmed species on home

The identity confirmed in diagnosis is saved per plot and camera. GrowPlot restores that confirmed name to the home CCTV card after app restart or plot switching.

## Google Drive image feed

Settings → Connect to folder opens Android's system folder picker. Choose Google Drive, select the existing plot folder (or the Projects/growplot/Image_Feed scope), and grant read/write access. GrowPlot saves the exact selected folder URI and uses it for scans and uploads; choosing a folder clears any stale Drive API folder ID. When a root scope is selected, GrowPlot creates or reuses `Projects/growplot/Image_Feed/<signed-in email>/<plot>`. The picker uses the Google Drive account selected through Android's Drive provider. Android folder permission, scan, and upload behavior require tablet verification.

## Vertex image assessment

Firebase deploys the authenticated `vertexImageDiagnosis` callable in `us-central1`; it sends inline photos to Gemini on Vertex AI and stores no photo. Run Diagnosis presents the local OpenCV result, a separate Vertex image assessment, and Pl@ntNet/Kindwise provider results. Pl@ntNet uses the encrypted key saved in Settings for species and disease analysis; Kindwise plant.id and crop.health use their separate saved keys. Provider responses and diagnosis display still need tablet verification.

## Verified app baseline

The following behavior was documented as device-verified for v0.7.00 Build 02:

- All six CCTV streaming slots display on the user's Android device.
- The Google Drive image scan reports per-camera image counts.
- A.I Plant Health displays the expected diagnosis images.
- The Build 14 source adds on-demand photo-based Pl@ntNet species candidates when Run Diagnosis is tapped. The pre-run species shown for a CCTV section is only a preset visual estimate. HSV colour cues remain preliminary; YOLO, validated leaf counts and image-based maturity estimates are not included. Plant age needs a planting date.
- A dedicated per-CCTV count summary in A.I Health remains an enhancement; the existing Java image counting works. Do not claim the summary UI is complete without Android verification.

## OpenCV plant boundary (Build 09)

OpenCV uses HSV colour thresholds and grouping rules to estimate a foliage outline, then shows the HSV-converted photo, estimated boundary, YGB mask, and pixel metrics. The boundary is approximate: soil or background may be included and leaves may be missed. The output is not validated plant detection, a health score, or disease diagnosis. Run Diagnosis displays this local screening alongside separate Vertex and Pl@ntNet/Kindwise results.

Build 29 source includes Firebase diagnosis history and Firebase Storage photo copies. The ARM64 package and Firebase rules deployment are pending; provider responses, camera behavior, Google Drive behavior, and Firebase save/load still require Android device verification.

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
