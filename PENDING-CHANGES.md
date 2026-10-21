## Completed — Build 26 interval photo restoration (2026-10-21)

- Separate per-plot timelines persist for daily, weekly, monthly, and yearly intervals and restore on app launch, plot selection, and interval selection.
- When a scan omits history but the selected plot has a cached Image_Feed photo whose timestamp is in the current period, the timeline displays that local cached image. Older periods still require matching saved history.
- Timeline persistence and current-cache fallback plus feed-history regressions are build gates. GitHub Actions ARM64 run #398 succeeded from source commit `b368b55962b1da48fd39978044d1ce999ed22e8a`; stable signing verified. APK SHA-256: `d77ac5afca3112508bf4bd604fb1db0dcab0a8562d5b4c7f1bd16631e3e48beb`. Tablet verification pending.

## Completed — Build 25 interval timeline persistence (2026-10-21)

- Added per-plot, per-interval timeline storage and immediate restoration across app reloads and interval changes. This intermediate build did not yet add the current cached-photo fallback.
- GitHub Actions ARM64 run #397 succeeded from source commit `dc27abaf85de7c0379576c550cb4b7d20a2922ed`; stable signing verified. APK SHA-256: `717833763f3e190620e9a03890e2dbb79a4d32d805c94c29c2417f6f7e37bad7`.

## Completed — Build 24 diagnosis fallback and interval timeline retention (2026-10-21)

- Run Diagnosis and foliage CV now try the latest cached CCTV Image_Feed snapshot when a saved diagnosis photo cannot be read.
- Status-only scan callbacks preserve the last successful four-period interval timeline; complete scans still refresh it. Added a feed-history regression check for this case.
- v0.7.00 Build 24 / versionCode 228 ARM64 build succeeded in GitHub Actions run #395 from source commit `96d998797d99495becfbc06db5c9ea8a4842fa06`. APK SHA-256: `e9a628f5658f8e9438240fe9590a09ca93b8adcf1588e92e4d390aad65564d91`; stable signing verified.
- YOLO and Pl@ntNet remain unavailable in this APK because their service URL/token and API key are not configured. Android tablet verification remains pending.

## Completed — Build 23 diagnosis HSV/CV stages and plant confirmation (2026-10-21)

- Run Diagnosis now runs shared HSV pixel analysis, waits for foliage/basic CV and YOLO request status, and displays an hourglass plus photo → HSV → foliage CV → YOLO → provider → final stages.
- Final state waits for Pl@ntNet and Kindwise callbacks, or ends after a 150-second timeout with the local results retained. User-confirmed plant identity stays fixed for its plot/CCTV and shows the confirmation timestamp.
- GitHub ARM64 build #393 succeeded from source commit `fdd03705447b4b366a44e273a8b0ce0274a4c47e`; stable signature verified. APK is 68,321,469 bytes; `index.html` is 245,948 bytes. APK SHA-256: `c03ad2ec562cbf41eaab4b021cdfb666699ffb341ea2b987d1a64c3feb47ad53`.
- YOLO remains unavailable: the manifest confirms the diagnosis proxy URL/token are not configured. The Pl@ntNet key is also absent, so provider-backed plant ID/disease checks are disabled in this APK. Android tablet verification remains pending.

## Pending — Build 22 remove stale packaged HTML (2026-10-21)

- APK inspection found unreferenced assets/index_old.html (4,945,337 bytes) still bundled beside the active 238,785-byte assets/index.html.
- Removed the stale asset; no source references existed. Version: v0.7.00 Build 22 / versionCode 226.
- Build 21 GitHub ARM64 workflow succeeded and its stable certificate matched. Build 22 GitHub ARM64 workflow succeeded. The APK is 68,319,081 bytes, the stale page is absent, and stable signing matches. Android tablet verification remains pending.

## Pending — Build 21 foliage CV/HSV and sample-photo size fix (2026-10-21)

- Fix the standalone CV analysis action so it runs on the latest selected CCTV photo instead of opening a placeholder.
- Bound on-device image processing to a 480-pixel longest side and reuse HSV class labels for connected foliage-colour regions to avoid the WebView stall.
- Show HSV colour shares and basic computer-vision measurements: grayscale brightness, contrast, saturation, edge density, and annotated candidate regions. Keep the result clearly labeled as preliminary image screening, not a health score, leaf count, or disease diagnosis.
- Remove duplicate base64 photos from `index.html`; fallback views use the existing bundled JPEGs instead. File size fell from 4,968,925 bytes to 238,785 bytes (95.2% smaller, 233.2 KiB).
- HSV/connected-region fixtures, inline and asset JavaScript syntax, and YOLO, Kindwise, data-feed, settings, farm, and account regressions passed. Feed-history regression fails on the unmodified Build 20 baseline (`emptyTimeline` is undefined in its harness).
- Source baseline: v0.7.00 Build 20 / versionCode 224, commit `0a7c7a6d8aff85e509113add325c504f3160b56a`. Build 21 / versionCode 225 is prepared locally; GitHub ARM64 build and Android tablet verification remain pending.

## Pending — HSV screening and photo-based species ID (2026-10-21)

- Run Diagnosis runs local HSV colour screening, then calls Pl@ntNet single-species identification and disease identification with the selected photos. Ranked species matches are displayed separately for user review.
- The species shown before the API response is a preset estimate based on CCTV section, not photo recognition. The user selects one of Pl@ntNet’s ranked candidates; the app records its source and score. HSV outputs are preliminary pixel cues, not a health score or disease diagnosis.
- No YOLO model is present in the repository. A crop-specific model and representative labelled field images are required before YOLO inference can be added and validated.
- No YOLO model is present; crop-specific weights and labelled field data are still required.
- Target: v0.7.00 Build 14 / versionCode 218, ARM64 only.
- HSV/species wiring and app regressions pass; ARM64 build, live API and Android tablet checks pending.

## Pending — Google Drive picker and compact Data Feed help (2026-10-21)

- First-use setup accepts the Projects or GrowPlot folder in My Drive; GrowPlot creates missing folders through the selected parent permission.
- Picker selection state survives activity recreation, and cancellation/provider errors are shown back in GrowPlot.
- Data Feed explanations are collapsed behind tap/hover info icons.
- Target: v0.7.00 Build 13 / versionCode 217, ARM64 only.
- Focused and existing static tests passed; GitHub Actions ARM64 build and Android Drive picker/folder verification pending.

## Pending — Data feed plot targeting and cloud path display (2026-10-20)

- Home plot synchronization now refreshes the Plot Management selection even when the visible dropdown value was already current.
- The Data Feed label always shows the canonical expected path `/Projects/GrowPlot/Image_Feed/<signed-in-email>/<selected-plot>`, calculated for the currently selected plot and signed-in account even when that Drive folder does not exist. The saved Firestore `relativepath` remains for sync, while connection status is shown separately; each Android device still needs its own local folder permission.
- Verified with a Node syntax check and mocked display test: an existing legacy folder path does not replace the expected path, and changing the plot or signed-in account refreshes the label. APK build and Android/Firebase/Drive verification remain pending.
- Static JS syntax checks passed; APK build and device verification pending.

## Pending — Firestore relativepath field correction (2026-10-20)

- Folder selection writes the selected Drive-relative path to the plot document's top-level `relativepath` string, matching the existing Firestore schema, and mirrors it to `dataFeed.relativePath` for compatibility.
- The generic Save Plot batch excludes stale local copies of both fields so it cannot erase the path written on selection.
- Static source review only; APK build and device-side Firestore confirmation pending.

## Pending — Firestore plot and data-feed saves (2026-10-20)
## Pending — Home data-feed shortcut removal (2026-10-20)

- Removed the Home data-feed status strip and Feed settings shortcut. Data-feed controls remain under Settings → Plot Management / Manage Farm → Plot Management.
- Demo Plot stays read-only. Selecting a user-owned plot clears the Demo-only notice and leaves the folder picker enabled for that plot.
- Static source review only; no APK build or Android device verification yet. Keep Build 07 identity until the next planned batch.


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


2026-10-20 | Pending next APK
Google Drive image-feed Connect now creates or reuses `Image_Feed/<signed-in-email>/<selected-plot>` under the authorized GrowPlot folder. Reuses the saved Android folder grant when available; otherwise, the user authorizes the GrowPlot folder once, then the selected user's plot path is created. If folder creation fails, the expected path and the failure status are reported separately.
Verification: source change committed; Android build and Google Drive provider/device verification pending.
