## Build 23 prepared from GitHub Build 22

- Baseline: GitHub `main` at commit `09b12d6dd608837a7aa7009052ba556127b6d2b4`, v0.6.01 Build 22 / versionCode 149.
- Manage Farm now filters planting, irrigation, and maintenance records by plot, greenhouse, and section; all-section records remain visible when a single section is selected.
- Maintenance shows overdue / due today / upcoming / completed cues; Mark complete saves a completion date and syncs. Irrigation records sort by their first scheduled date.
- Added `tools/tests/manage-farm-regression.js` and made it a build gate. Static regressions and inline JavaScript syntax pass.
- Build identity: v0.6.01 Build 23 / versionCode 150. Git executable-mode defects in `gradlew` and `tools/ensure-android-sdk.sh` are corrected. Login and registration use email/password/confirm password with only Username removed; Google Credential Manager/Firebase Auth is wired, and first-plot onboarding is shown in the login card with Firebase write status. GitHub Actions run 38563619546 passed and uploaded the ARM64 APK/source ZIP/manifest. APK SHA-256: `7aa3bf4fbc26c318337818a005e8829f1d57bc315a39a3396541f13fdd1739f2`. Firebase Google OAuth config is still missing from `google-services.json`; Android device verification remains pending.

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

- BUG-046: make all user-owned GrowPlot records consistent across devices through Google Drive. Existing crop/irrigation/maintenance Drive sync is partial; plot settings and diagnosis history remain local, and Account sign-in is still a preview. Implement stable per-user identity, cloud load/merge/save/delete for each data type, then validate sync and user isolation on two devices. A first Drive folder grant may be required once per device.

2026-10-12 | Pending next build after v0.6.01 Build 5
Settings: Added a diskette Save button beside Home, visible only in Settings. It validates current plot details, GPS, camera IPs/login, snapshot interval and rescan interval, then saves all those edits without leaving Settings. Blank VIGI password keeps the saved login. Reports partial save failures instead of unconditional success. Existing folder grants/config remain unchanged.
Validation: executable combined-save regression, saved-password reuse, invalid GPS/no writes, partial failure and plot rollback checks passed; JS syntax and feed-history regression passed. Android UI verification pending. Packaged in Build 6 on explicit user request; regression and APK build checks passed. Device verification pending.

2026-10-12 | Pending next build after Build 6
Added collapsed Plugins · API Keys Settings menu with default Pl@ntNet key portal and Kindwise admin links. Masked key fields save through the overall diskette Save action; blank fields retain saved keys. Keys use Android Keystore AES-GCM encrypted device preferences and are never returned to the WebView. Pl@ntNet requests use the saved device key first, then the existing build-key fallback. Kindwise key storage is available; diagnosis provider integration remains unconnected.
Validation: Java 17 compilation against Android API 35/VLC and existing Settings/feed regression checks passed. Live provider validation and Android device checks pending; Packaged in Build 7; APK checks passed.
