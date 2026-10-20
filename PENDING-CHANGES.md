## Pending — Firestore plot and data-feed saves (2026-10-20)

- Save plot continues to sync the plot record through the existing Firebase profile save flow.
- Selecting a data-feed folder immediately writes `dataFeed.enabled=true` and the Drive-relative `dataFeed.relativePath` to that plot document. Android's SAF folder URI and permission stay device-local.
- The general plot-save batch omits its potentially stale local `dataFeed` map, preserving the path written by folder selection.
- Added an in-app success/failure notice for the Firestore path write.
- Source changes are on GitHub main. Static source review only; no APK build or Android/Firebase device verification yet. Keep the current version identity until the next planned build batch.

## v0.6.02 Build 02 — delivered

- Baseline: GitHub main at v0.6.02 Build 01 / versionCode 158, commit `9fd4dd0727b4bf4460929a319ff8ac8b67a49008`.
- Fixes: First-plot sign-in returns to Home before displaying the choice; My Account is a centered panel capped at 480px and scrolls within short screens.
- GitHub Actions run #119 passed ARM64 build, package/version/signature checks, and artifact upload. APK SHA-256: `7ae9425fd88d3b55b1be5275b4128581fac36784cd525d24071ad325621a86c4`.
- APK uses the same configured stable certificate as Build 01. Android visual verification pending; Pl@ntNet key absent.


## Login — local admin test access

- Added an Admin test login button on the login screen. The Android bridge permits the local demo-admin shortcut only in debug builds.
- Successful access opens the existing local demo-admin session; it does not sign into Firebase or grant cloud administrator permissions. The button stays hidden in release builds.
- Account regression now checks the button and debug-only access gate. APK generation remains pending under the 2–3 enhancement batch rule.

## Manage Farm — Plot tab

- Added a Plot tab before Crop Planning.
- The tab moves the existing Settings → Plot Management panel into Manage Farm, so plot selection, location/GPS/map, data-feed settings, and existing save/delete behavior are shared rather than duplicated. Returning to another main menu restores the panel to Settings and preserves its accordion state.
- Added regression checks for tab order and moving/restoring the shared panel. APK generation remains pending under the 2–3 enhancement batch rule.

## Build 28 — restart login fixes from current main

- Baseline: GitHub main v0.6.01 Build 26 / versionCode 153. Build 27 / versionCode 154 came from a stale branch based on Build 25 and is superseded.
- Keep Build 26's behavior: Firebase Authentication success is returned independently of Firestore profile reads/writes, and profile failures do not clear the authenticated session.
- Reapply the three requested login improvements: duplicate-email recovery, My Account dates/payment placeholder, and the first-plot modal with Plot Management/Home routes. Retain the direct Google sign-in path and current workflow's stable signing-key restore.
- Add the actual APK signing SHA-1 to the build manifest so it can be compared with the Android OAuth fingerprint in google-services.json.
- Regression and GitHub ARM64 build pending. Google OAuth signer match and on-device sign-in remain unverified.

## Build 26 — Firebase authentication and profile error handling

- Baseline: GitHub `main`, v0.6.01 Build 25 / versionCode 152, commit `446d122f9d4d12c0c0630040c314ab5c849077b5`.
- Firebase email/password and Google sign-in now report authentication success independently of Firestore profile reads or writes. Cloud-profile failures appear as warnings and do not clear an authenticated session.
- The Firebase Android OAuth fingerprint in `app/google-services.json` was corrected after Build 24's signer mismatch. The Build 26 workflow manifest must be checked against the actual APK signer.
- Build identity: v0.6.01 Build 26 / versionCode 153. GitHub Actions run #86 passed tests, ARM64 compilation, manifest/version/signature/alignment checks, and artifact upload after increasing the heap. Its runner-generated signer differs from Build 30 and from the Firebase OAuth SHA-1, so the artifact is not a usable Google-sign-in build. The workflow is being updated to use the already-configured stable signing-key secrets before generating the replacement Build 26 APK. Device verification pending.

## Build 25 — Manage Farm screen availability

- Baseline: GitHub `main` at v0.6.01 Build 24 / versionCode 151, commit `b998170244f2baabf7bdd0568a2fae7d52f3b130`.
- Fixed a JavaScript temporal-dead-zone error in `renderManageFarm()` that stopped the screen from opening.
- Added a runtime render regression check covering Crop Planning, Irrigation & Fertiliser, and Maintenance tabs.
- Version identity: v0.6.01 Build 25 / versionCode 152. Build and device verification pending.

## Build 24 prepared from GitHub Build 23

- Baseline: GitHub `main` at v0.6.01 Build 23 / versionCode 150.
- Manage Farm now filters planting, irrigation, and maintenance records by plot, greenhouse, and section; all-section records remain visible when a single section is selected.
- Maintenance shows overdue / due today / upcoming / completed cues; Mark complete saves a completion date and syncs. Irrigation records sort by their first scheduled date.
- Added `tools/tests/manage-farm-regression.js` and made it a build gate. Static regressions and inline JavaScript syntax pass.
- Build 24 batch: Firebase Google OAuth config now targets project `growplot` / package `com.growplot.farm` and contains Android plus web OAuth client entries. The project owner reports Email/Password and Google providers are enabled. Google Services Gradle plugin updated to 4.5.0 and Firebase BoM to 35.0.0. Build identity: v0.6.01 Build 24 / versionCode 151. Build 24 APK and source ZIP generated successfully in GitHub Actions run #74. Email/password and Firestore connectivity still need device verification. The Build 24 APK's Android OAuth fingerprint initially mismatched its signer. GitHub source now contains the corrected Build 24 SHA-1 (commit 57f3100071dbf72b0d64ec44ac357051af4ef186); the next APK signer must still be checked.

# GrowPlot enhancement batch

- Verified GitHub source baseline: v0.6.01 Build 7 (versionCode 134), commit a803ae919bf8db1acbe6217a87d151fd794e997e.
- Current source/build: v0.6.01 Build 8 (versionCode 135), ARM64 only; based on the verified GitHub Build 7 baseline.
- Batch: connects crop.health diagnosis to the encrypted Kindwise Settings key for photo and video-frame analysis; stores and shows Kindwise results alongside Pl@ntNet; adds a build-time regression gate.
- Build 8 ARM64 debug compilation, package/version, APK signature, and alignment checks passed. Its signer differs from the saved Build 30 certificate, so it is development-only and must not replace the installed app. Pl@ntNet key, live Kindwise request, and Android device checks remain pending.
- Keep the 2–3 enhancement batch before the next APK. BUG-046 remains open outside this batch.

## Build 16 delivered

- BUG-062: Compare each camera's two newest distinct saved snapshots using a low-resolution visible-green and brightness estimate. Show the comparison, both capture times, before/after estimates, and a plain-language limitation in each camera's Image history. Explain when there are fewer than two snapshots.
- Java compilation and inline JavaScript syntax passed. APK package/version, signature, alignment, and device checks are recorded in the build manifest; Android-device verification remains pending.
- Build the next APK after 2–3 discrete enhancements are completed, or sooner only if the user explicitly asks for an APK sooner.

## Build 15 delivered

- BUG-061: Use TextureView composition for both VLC feeds so dialog and menu visibility changes can cover/hide the native video windows; scheduled frame snapshots remain supported through TextureView bitmap capture. CCTV02 explicitly streams `http://192.168.1.227:8080/videofeed`; CCTV01 remains at `http://192.168.1.241:8080/videofeed`.

APK: `release/v0.5.09-build-15-GrowPlot-development-only.apk`. Build, package/version metadata, signature, alignment, and JavaScript checks passed. APK SHA-256: `43b80b27b028565e0aeae24548bbe75d55e65042e2224fdd5afd811b03ac763c`. The debug certificate differs from Build 30; this APK may not update an existing installation.

## Build 14 delivered

- BUG-060: Add the CCTV02 VLC live feed at `192.168.1.227:8080`, independent Refresh, same menu/lifecycle behavior, and the shared checked snapshot interval.

## Changes in Build 24

- BUG-050/051: CCTV01 retains the direct IP Webcam stream and explicit HTTP image allowance, limited to the webcam IP by Android network security.
- BUG-052: Added an **IP webcam** button which opens the webcam's own page in a full-screen in-app WebView, with Close to return to GrowPlot. The viewer has no GrowPlot JavaScript bridge.
- This APK was built from the authorized Build 19 source archive with the CCTV01 display and WebView stream corrections. Build 20/21 source changes were not available in this workspace.
- This APK was built from the authorized Build 19 source archive with the CCTV01 fix. Build 20/21 source changes were not available in this workspace.

## Changes in Build 25

- BUG-053: Added a compact CCTV07 home-screen tile that plays the IP Webcam stream directly, shows connection/retry status, and retries the `/videofeed` endpoint after errors. CCTV01–CCTV06 and the Drive history/diagnosis handling remain unchanged.
- Built from the authorized Build 19 source archive with the previously recorded Build 22–24 updates. Build 20/21 source changes were not available in this workspace.
- Android stream playback remains pending on the user's Wi-Fi.

## Changes in Build 26

- BUG-054: The CCTV07 frame remains visible but blank at startup. The adjacent **IP webcam** button starts the direct `/videofeed` stream inside that frame; clicking again restarts it. No live request is made until the button is pressed. Connection state and retry handling remain in the frame.
- Existing CCTV01–CCTV06 and Drive history/diagnosis handling remain unchanged.
- Android playback remains pending on the user's Wi-Fi.

## Changes in Build 27

- BUG-055: Removed the IP webcam button and CCTV07 frame from the home screen, along with their embedded stream-start logic. The existing six camera cards and CCTV01 feed behavior remain in place.

## Pending

- BUG-046: make all user-owned GrowPlot records consistent across devices through Google Drive. Existing crop/irrigation/maintenance Drive sync is partial; plot settings and diagnosis history remain local, and Firebase account authentication is now implemented; cross-device Drive data sync remains incomplete. Implement stable per-user identity, cloud load/merge/save/delete for each data type, then validate sync and user isolation on two devices. A first Drive folder grant may be required once per device.

2026-10-12 | Pending next build after v0.6.01 Build 5
Settings: Added a diskette Save button beside Home, visible only in Settings. It validates current plot details, GPS, camera IPs/login, snapshot interval and rescan interval, then saves all those edits without leaving Settings. Blank VIGI password keeps the saved login. Reports partial save failures instead of unconditional success. Existing folder grants/config remain unchanged.
Validation: executable combined-save regression, saved-password reuse, invalid GPS/no writes, partial failure and plot rollback checks passed; JS syntax and feed-history regression passed. Android UI verification pending. Packaged in Build 6 on explicit user request; regression and APK build checks passed. Device verification pending.

2026-10-12 | Pending next build after Build 6
Added collapsed Plugins · API Keys Settings menu with default Pl@ntNet key portal and Kindwise admin links. Masked key fields save through the overall diskette Save action; blank fields retain saved keys. Keys use Android Keystore AES-GCM encrypted device preferences and are never returned to the WebView. Pl@ntNet requests use the saved device key first, then the existing build-key fallback. Kindwise key storage is available; diagnosis provider integration remains unconnected.
Validation: Java 17 compilation against Android API 35/VLC and existing Settings/feed regression checks passed. Live provider validation and Android device checks pending; Packaged in Build 7; APK checks passed.
