# GrowPlot Master Development Log

**Coverage:** 8–10 October 2026  
**Current build at end of period:** v0.5.08 Build 27 (versionCode 108)  
**Source records consolidated:** `VERSION-LOG.txt` and `BUG-LOG.md`

**Build 22 update (2026-10-10):** Compiled the authorized Build 19 source baseline with the CCTV01 `/videofeed` display correction. Google Drive snapshots remain available for diagnosis/history but cannot replace the live view. Package/version, signature, and alignment checks passed. Build 20/21 source changes were not available in this workspace. Android playback verification remains pending; signing is development-only and Pl@ntNet is disabled without its local key.

**Build 23 update (2026-10-10):** Compiled a WebView mixed-content correction for CCTV01's direct MJPEG request while retaining the single-IP cleartext allowlist. JavaScript, package/version, APK signature, and alignment checks passed. Actual playback remains unverified because the Android device is not attached to this workspace; Build 20/21 source changes were unavailable, and the APK uses a development signing key.

**Build 24 update (2026-10-10):** Added an **IP webcam** dashboard button that opens `http://192.168.1.241:8080/` in a separate full-screen WebView, bypassing mixed-content embedding for this view. Close returns to GrowPlot. The camera viewer is not given the app's JavaScript bridge. JavaScript, package/version, signature, and alignment checks passed; device/Wi-Fi verification remains pending.

**Build 25 update (2026-10-10):** Added a compact CCTV07 live tile to the home screen using `http://192.168.1.241:8080/videofeed`, with visible connection status and automatic retry. The six existing camera cards retain their current feeds and Drive history/diagnosis behavior. Android playback verification remains pending on the user's Wi-Fi.

**Build 26 update (2026-10-10):** Corrected the CCTV07 interaction: its small stream frame stays visible and empty on startup, beside the **IP webcam** button. Only tapping the button starts the live stream inside CCTV07; tapping again restarts it. Playback verification remains pending on the user's Wi-Fi.

**Build 27 update (2026-10-10):** Removed the IP webcam button and CCTV07 blank frame from the home screen, including the associated embedded start logic. The six existing camera cards remain.

This is a chronological summary of work and defects recorded during the period, organized by **date → module → description**. It consolidates the recent entries without replacing the detailed version and bug histories.

## 8 October 2026

### App foundation and dashboard

- Started the Android GrowPlot app from an offline-first mockup, establishing plot and greenhouse selection, CCTV camera cards, and IoT readings. Refined the fertigation-tank readings and bundled sample plant photos for offline display.
- Added latest-photo timestamps and the first Image_Feed/Drive browsing experiments. Several early build attempts were withdrawn or superseded while the working source and storage approach were corrected.

### Home and navigation

- Added the main modules, including A.I Health, A.I Yield, Market, farm maintenance, Settings, and My Account.
- Added a per-camera photo timeline for Day 1, Day 7, Day 14, and Day 28; moved navigation into a sticky main menu; added Home navigation and active-tab styling.
- Adjusted the dashboard for Android status-bar and display cutout insets after the first layout did not leave enough top spacing.

### A.I. Health

- Added photo review for the supplied camera images and changed the Health action to a more prominent screen.

### Defects and build notes

- The first debug APK lacked the bundled React Native JavaScript and was retired. Early UI/layout and source attempts were superseded; use the current baseline rather than these archived candidates.
- Android device verification for the new timeline, navigation, and inset behavior was not consistently recorded for this date.

## 9 October 2026

### Home, plots, and data feeds

- Added plot management and plot-specific feed settings, including a selected-plot folder, scan interval, manual rescan, and per-plot photo cache/history.
- Established the default image-feed path convention `image_feed/plotNN_slug` (including `image_feed/plot01_demo`) and displayed the selected provider/path.
- Added map preview/pin and plot location details, with saved coordinates/address/device location used to resolve the map position.
- Corrected the major per-camera image statistics issue: map CCTV IDs from filenames/folders, then report each camera’s latest image, earliest/latest capture, image count, and elapsed time. The user confirmed that the corrected `data_feed` path restored the statistics in Build 13.

### Diagnosis and media storage

- Built photo/video diagnosis media handling around the saved Android folder grant, with Drive paths organized by plot and section. Media selection/camera capture can save locally first and queue cloud upload; normal operation reuses saved folder IDs and grant.
- Added video playback and pixel-color sampling, with display sizing, a close control, persistent results, care guidance, and an AI/human-expert disclaimer. The confirmed native Android video player path was restored after an experimental inline player failed.
- Added automatic Pl@ntNet disease requests after photo/video screening, with ranked results and precautionary care guidance. The log records one successful external API test, but that did not verify the complete Android app flow.

### Defects and verification gaps

- **CCTV01 stream:** Build 19 uses the direct IP Webcam URL and removes periodic snapshots; actual streaming smoothness and reachability are still unverified on-device.

- Drive folder authorization/recovery, actual Drive provider reads/writes, and video playback required device testing. Several code fixes were built, but the logs do not establish end-to-end verification on the user’s phone/tablet for those flows.
- The inline video experiment caused playback trouble and was reverted to the native player. Subsequent compact-player and video-analysis changes still needed device testing.
- Plot/account sign-in remained a UI preview; no authentication backend was configured, so farm data used the `default-user` directory.

## 10 October 2026

### Home

- Added an IP Webcam MJPEG preview to CCTV01 using the user's server address. A 15-second still-frame capture loop was tried, then removed in Build 19 at the user's direction; streaming is now the only automatic camera function.
- After the feed lagged, removed the native WebView stream proxy and pointed the existing CCTV01 image directly to `http://192.168.1.241:8080/videofeed`, with reconnect after drops. Android same-network playback and smoothness still need the user's device test.

- Added the Home section’s CCTV / camera / file source choices inside each camera card; removed the separate image panel.
- Fixed camera capture handling in code, including portrait EXIF rotation and `CCTVXX_<timestamp>` snapshot names. Added a per-section Diagnosis shortcut using that section’s latest feed image.
- Changed the user-facing “Quadrant” label to “Section,” renamed the main menu item to “Market Place,” and refined the Diagnosis button to be compact, green, and right-aligned below the photo.

### A.I. Health and Diagnosis

- Added per-section diagnosis history, occurrence counts, timestamps, and an empty-history prompt. Corrected history/result handling so records are associated with the original plot and section and only successful Pl@ntNet deep-diagnosis results count as completed diagnoses.
- When a section opens, its most recent saved diagnosis is shown inline by default. History appears between saved-media review and the diagnosis description; selecting an older event replaces the description inline.
- Added yellow/green/brown pixel-color percentages and visual bars, automatic Pl@ntNet result display, and a remaining-request badge. Added modal background-scroll locking and Home buttons for secondary modules. Renamed “A.I Yield” to “Harvest.”

### Manage Farm

- Added crop planning with crop photos ahead of planting details, an all-sections option for a greenhouse, and planting records for crop/variety, dates, harvest estimate, and plant/tree count.
- Replaced the Irrigation & Fertiliser placeholder with schedules scoped to plot, greenhouse, and section, including method, frequency, time, duration, fertiliser type (including A+B mix), optional user-entered rate, status, and notes.
- Replaced the Maintenance placeholder with scheduled tasks such as polybag installation, piping, weeding, pruning, line flushing, bed preparation, transplanting, and pest scouting. Tasks include recurrence, equipment suggestions that can be edited, status, and notes.

### Drive and farm-data sync

- Added crop, irrigation, and maintenance records to the farm-management data sync under `GrowPlot/db/userdata/<user>/farm-management.json`; crop records also carry plot/greenhouse/section scope. Local and remote records merge by ID and update time, and deletions are tracked.
- **New open defect (BUG-046, reported 17:36 MYT):** GrowPlot data is not yet consistently available across devices. Plot profiles/settings and diagnosis history remain device-local; account sign-in is still a preview, so existing farm-record sync currently uses `default-user`. The requested fix is stable per-user identity and Drive load/merge/save/delete for all user-owned records, verified on two devices.
- Build 16 records passing static/JavaScript/Gradle checks. Actual Drive synchronization, the crop-photo layout, and Android touch behavior still require device testing.

### Defects and verification gaps

- **CCTV01 stream:** Build 19 uses the direct IP Webcam URL and removes periodic snapshots; actual streaming smoothness and reachability are still unverified on-device.

- **Diagnosis history leakage / missing history:** addressed in Builds 11–15, including filtering pixel-only entries, isolating callbacks by section, recovering saved timestamps, and showing the latest completed result inline. The log still marks Android display and persistence verification as pending; confirm on-device with previously diagnosed and never-diagnosed sections.
- **Home camera/Diagnosis controls:** capture and button-size fixes are in the builds, but camera compatibility, portrait save behavior, Drive writes, and visual placement are not fully confirmed on-device.
- **Drive persistence/sync:** code uses the saved grant and data paths, but real provider behavior has not been verified on the device. Android may require a new one-time grant after reinstall, cleared app data, or permission revocation.
- **History sync:** diagnosis history remains local to the device; cross-device history sync is not implemented.
- **Install/update signing:** Build 16’s signing certificate differs from the saved Build 30 release certificate. Android may reject it as an in-place update. Do not uninstall the installed app to work around this, because that may risk local data and the saved Drive grant.

## End-of-period status

- **Latest build:** v0.5.08 Build 19, versionCode 100.
- **Build checks recorded:** Gradle/Java, JavaScript syntax, APK signature, alignment, and package metadata passed.
- **Still pending:** phone/tablet UI and behavior checks, live Google Drive provider read/write/sync, and confirmation of section-specific diagnosis history on the device.

