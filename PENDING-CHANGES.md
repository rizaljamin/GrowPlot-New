# GrowPlot enhancement batch

- Current test baseline: v0.6.01 Build 2 (versionCode 129); universal APK, arm64 APK, debug AAB, source ZIP, and build manifest produced.
- Verification: Java/JavaScript compilation, APK signature, package/version, alignment, ABI contents, and AAB archive checks passed. Device install and VIGI playback checks remain pending.
- Enhancements queued after Build 1: none recorded yet. Keep the 2–3 enhancement batch before generating the next APK unless the user asks sooner. BUG-046 remains open outside the current batch.

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
