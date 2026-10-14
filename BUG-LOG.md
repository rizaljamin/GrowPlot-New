## BUG-064 — Hide live-feed frames when another screen opens

**Reported:** 2026-10-11 (repeated report)
**Status:** Implemented in v0.6.01 Build 3 (versionCode 130); device verification pending.

**Cause:** The previous visibility update depended on feed geometry. Opening a menu hides the dashboard, and a feed can measure zero width or height; the native positioning handler then returned before hiding the VLC frame. The delayed fade also left the frame visible briefly over the new screen.

**Fix:** The page now reports active dashboard state separately from feed position. Android immediately cancels animations and hides all three VLC frames whenever another screen is active. Frames reappear only after Home is active and valid card coordinates arrive.

**Verification / limits:** Build checks passed. Confirm CCTV01, CCTV02, and CCTV03 disappear on menu/modal open and return to their matching cards after returning Home on the Android device.

## BUG-063 — Reduce GrowPlot download size by device architecture

**Reported:** 2026-10-11
**Status:** Implemented in v0.6.01 Build 2 (versionCode 129); APK/AAB package checks passed. Device testing pending.

**Change:** Keep a universal development APK for broad compatibility, add an arm64-only APK for 64-bit ARM devices, and generate a debug Android App Bundle containing both supported ABIs. The build script checks APK signatures, package/version identity, alignment, ABI contents, and AAB archive contents.

**Verification limit:** Arm64 APK is 39.3% smaller than the universal APK. The AAB is debug-signed and intended for build/size evaluation; publishing to Google Play requires the proper upload key. Test the arm64 APK on the Lenovo TB370FU.

## BUG-049 — CCTV01 WebView feed stutters or stops

**Reported:** 2026-10-10 18:59 MYT  
**Status:** Compiled in v0.5.08 Build 19 (versionCode 100); Android playback verification pending.

**Change:** Removed the native WebView stream proxy. CCTV01 now loads `http://192.168.1.241:8080/videofeed` directly in the existing Home image card, with a reconnect attempt after a dropped connection. The periodic 15-second frame capture was removed so this path is streaming only. Existing manual camera/file selection and snapshot saving remain available.

**Limit:** Smooth playback still depends on local Wi-Fi, the IP Webcam server, and Android WebView support for its MJPEG stream. Verify on the Android device.

## BUG-048 — CCTV01 needs timed snapshot uploads to Google Drive

**Reported:** 2026-10-10 12:49 UTC  
**Status:** Deferred; the 15-second automatic capture introduced in Build 17 was removed in Build 19 at the user's direction.

**History:** Earlier builds requested a JPEG every 15 seconds and wrote it into the configured Drive image-feed folder. The feature is no longer active; manual capture remains available.

## BUG-047 — Home CCTV01 should display the user's IP Webcam stream

**Reported:** 2026-10-10 12:39 UTC  
**Status:** Compiled in v0.5.08 Build 19 (versionCode 100); same-network Android verification pending.

**Fix:** Embedded `http://192.168.1.241:8080/videofeed` over the CCTV01 Home image with an Image_Feed fallback and a reconnect attempt. The app's network security configuration permits cleartext traffic only to the supplied local IP.

**Limit:** Both devices must be reachable on the same local network, and the IP Webcam address can change if the router reassigns it.

## BUG-046 — User data is not consistently shared across devices through Google Drive

**Reported:** 2026-10-10 17:36 MYT  
**Status:** Open; not included in Build 16.

**Observed scope:** Crop plantings and the irrigation/maintenance planners have Drive sync code, currently under the shared `default-user` folder because Account sign-in is only a preview. Plot profiles/settings and diagnosis history remain device-local; diagnosis history does not sync between devices. The Android Storage Access Framework grant is also device-local.

**Required behavior:** Persist each user's GrowPlot records in that user's Google Drive data folder and load/merge them on every device where that same user signs in. Include plot/profile settings, crop plantings, irrigation schedules, maintenance tasks, and diagnosis history. Keep records isolated between users; merge updates by stable record ID and update time, and sync deletions. A first-time Drive authorization may be needed on each device unless the app later uses account-based Drive OAuth; normal launches on an authorized device should reuse the saved grant.

**Verification required:** Sign in as the same user on two Android devices and confirm records created or edited on either device appear on the other after sync. Confirm a second user cannot see or overwrite the first user's records, and confirm offline changes/deletions merge without restoring stale records.

## BUG-045 — Maintenance needs scheduled tasks and equipment lists

**Reported:** 2026-10-10 16:09 MYT  
**Status:** Compiled in v0.5.08 Build 16 (versionCode 97); JavaScript and Gradle checks passed; Android UI/Drive verification pending.

**Fix:** Replaced the Maintenance placeholder with editable task plans for polybag installation, piping/irrigation setup, weeding, pruning, fertigation-line flushing, bed preparation, transplanting, pest/disease scouting, and other work. Tasks include plot/greenhouse/section scope, due date, recurrence, status, equipment required, and notes. Common equipment is suggested for each task and remains editable. Records sync through the saved GrowPlot Drive grant to `db/userdata/<user>/farm-management.json` and retain their scope fields.

**Limit:** Android display and Drive synchronization need on-device verification.

## BUG-044 — Irrigation and fertiliser planning is only a placeholder

**Reported:** 2026-10-10 16:09 MYT  
**Status:** Compiled in v0.5.08 Build 16 (versionCode 97); JavaScript and Gradle checks passed; Android UI/Drive verification pending.

**Fix:** Replaced the placeholder with editable schedules by plot, greenhouse, and section. Records include irrigation method, frequency, start date/time, duration, fertiliser type (including A+B fertigation mix), optional user-entered mix rate, status, and notes. Rates are entered by the user from their crop plan or product guidance. Records persist locally and sync to `db/userdata/<user>/farm-management.json` through the saved Drive grant.

**Limit:** Android display and Drive synchronization need on-device verification.

## BUG-043 — Crop photo selection should come first and support all greenhouse sections

**Reported:** 2026-10-10 16:03 MYT  
**Status:** Compiled in v0.5.08 Build 16 (versionCode 97); JavaScript and data-scope checks passed; Android UI/Drive verification pending.

**Fix:** Moved the crop catalogue with crop photos to the top of Crop Planning, before planting details, and show the selected crop photo and harvest window in the form. Added “All sections in this greenhouse” alongside Sections 01–06. An all-section planting is saved once in the app and written into each of the greenhouse’s six Drive section files while retaining its shared record ID; single-section assignment remains available.

**Limit:** Google Drive read/write behavior and the crop-photo layout need verification on the Android device.

## BUG-042 — Diagnosis scrolling reaches the Home screen; secondary modules need a Home action

**Reported:** 2026-10-10 15:50 MYT  
**Status:** Compiled in v0.5.08 Build 15 (versionCode 96); JavaScript syntax and UI structure checks passed; device verification pending.

**Fix:** Background page scrolling is locked at its current position while any modal is open, and overscroll is contained within the dialog. Added a Home button to the bottom of the Health panel, Diagnosis, and shared secondary-module dialog used by Harvest, Market Place, Manage Farm, My Account, and Settings. Home closes active overlays and restores the dashboard. Renamed the A.I Yield menu label and module heading to “Harvest.”

**Limit:** Real Android touch scrolling and layout still need tablet/phone verification.

## BUG-041 — Diagnosis history should be inline and show the latest result by default

**Reported:** 2026-10-10 15:45 MYT  
**Status:** Compiled in v0.5.08 Build 15 (versionCode 96); JavaScript syntax and UI structure checks passed; device verification pending.

**Fix:** Moved diagnosis history into the diagnosis screen between “2. Review saved photos and video” and “3. Diagnosis description.” The newest saved result now loads into the description when the section opens. Selecting an older history entry replaces the description inline; the history action on the A.I. Health card now opens the same screen and scrolls to the history list instead of opening a separate history window.

**Limit:** History still uses the app’s existing local history store. Android appearance and result rendering need device verification.

## BUG-034 — Opening a section automatically shows a pixel-screen result

**Reported:** 2026-10-10 14:42 MYT  
**Status:** Compiled in v0.5.08 Build 12 (versionCode 93); device verification pending.

**Fix:** The Home Diagnosis shortcut now preloads the section’s latest Image_Feed photo but waits for the user to tap Run diagnosis before starting CV or Pl@ntNet. On opening a section, saved pixel-only or failed-attempt results are hidden; the description says “No historical diagnosis performed” unless a successful deep diagnosis exists for that section. A successful Pl@ntNet response marks and saves its matching pixel report as a completed deep diagnosis.

**Limit:** Android device verification remains pending.

## BUG-033 — Diagnosis history and results leak across sections

**Reported:** 2026-10-10 14:13 MYT  
**Status:** Implemented in v0.5.08 Build 11 (versionCode 92); Gradle, JavaScript syntax, and focused history/callback checks passed. Android device verification pending.

**Fix:** Diagnosis history now counts only successful Pl@ntNet deep-diagnosis responses. Pixel-only legacy events are removed from the displayed history, so untouched sections show “No historical diagnosis performed.” Delayed Pl@ntNet and video results are saved to the original plot and CCTV section and only update the open diagnosis panel when that same section is still active.

**Limit:** Android device verification remains pending. Existing screening-only history entries are intentionally filtered because they were not deep diagnoses.

## BUG-032 — Home camera cards use technical “Quadrant” label

**Reported:** 2026-10-10
**Status:** Implemented in v0.5.08 Build 10 (versionCode 91); build checks passed, Android device verification pending.

**Change:** Changed the Home camera-card headings, sensor-reading label, image-feed note, and Home diagnosis accessibility/status text to use “Section.” CCTV numbers and internal camera/storage identifiers are unchanged.

**Limit:** Android device appearance and tap behavior remain unverified.

## BUG-031 — A.I. Health needs per-quadrant diagnosis history

**Reported:** 2026-10-10 13:53 MYT  
**Status:** Initial history feature shipped in v0.5.08 Build 10 (versionCode 91); its pixel-screen event behavior is corrected in source under BUG-033 and queued for the next APK.

**Change:** Added a Diagnosis history button and occurrence count to each A.I. Health card. The initial version logged local pixel screening and migrated prior saved screening results; BUG-033 removes those entries and counts only successful Pl@ntNet deep-diagnosis results.

**Limit:** History remains stored on this device in local storage. Cross-device or Google Drive history sync is not implemented. Android visual and runtime testing remains pending.

## BUG-030 — Main menu label should read “Market Place”

**Reported:** 2026-10-10 11:54 MYT  
**Status:** Implemented in v0.5.08 Build 10 (versionCode 91); build checks passed, Android device verification pending.

**Change:** Renamed the visible main-menu label and its accessibility label from “Market” to “Market Place.” The Market screen heading and content are unchanged.

## BUG-029 — Home quadrant Diagnose control is too large and overlays the image

**Reported:** 2026-10-10 11:39 MYT  
**Status:** Implemented in v0.5.08 Build 9 (versionCode 90); build checks passed, device verification pending.

**Fix:** Moved the Diagnosis action below each quadrant image and aligned it to the right. The compact green button shows the stethoscope icon and “Diagnosis” on one line. A more specific CSS selector keeps the general full-width diagnosis-button rule from stretching it across the card or pushing the label out of place.

**Limit:** Android device appearance and tap behavior remain unverified.

## BUG-028 — Pl@ntNet remaining quota is not visible in Diagnosis

**Reported:** 2026-10-10 11:26 MYT  
**Status:** Implemented in v0.5.08 Build 6 (versionCode 87); build checks passed, device/API verification pending.

**Fix:** Added a top-right quota badge in the Diagnosis description section. It uses `remainingIdentificationRequests` from the Pl@ntNet disease response, with the `RateLimit` response header as fallback; a quota-exceeded HTTP 429 response is represented as zero remaining. The label uses requests rather than photos because the provider counts one credit per disease request, which can contain up to five images.

**Limit:** The refreshed Build 6 APK contains the user-provided Pl@ntNet key, but no live quota value or diagnosis API call has been verified on a device yet.

## BUG-027 — Pl@ntNet response should appear with the photo diagnosis description

**Reported:** 2026-10-10 11:22 MYT  
**Status:** Implemented in v0.5.08 Build 6 (versionCode 87); device/API verification pending.

**Change:** After photo color analysis completes, the existing flow requests Pl@ntNet automatically. Its pending message, ranked disease results, and precautionary care notes now render inside the same Diagnosis description area, directly after the pixel analysis.

**Limit:** Build 5 was generated without the Pl@ntNet key. The refreshed Build 6 includes it, but the API response still needs device verification.

## BUG-026 — Quadrant diagnosis action and photo analysis cues need clearer presentation

**Reported:** 2026-10-10 11:19 MYT  
**Status:** Implemented in v0.5.08 Build 6 (versionCode 87); device verification pending.

**Change:** Replaced the large quadrant Diagnose control with a compact stethoscope button at the bottom-right of the image. Renamed “Run photo CV screen” to “Run diagnosis”. Photo results now display approximate Yellow, Green, and Brown pixel percentages with matching colors and bars. The note clarifies these percentages describe sampled pixels, not plant health.

**Verification:** Inline JavaScript syntax check passed. No APK generated; tablet appearance and interaction remain unverified.

## BUG-025 — Home quadrant snapshots require manual transfer into Diagnosis

**Reported:** 2026-10-10 11:10 MYT  
**Status:** Implemented in v0.5.08 Build 5 (versionCode 86); build checks passed, device verification pending.

**Fix:** Added a Diagnose button within each Home quadrant image. It opens the matching quadrant in Diagnosis, adds that quadrant’s latest cached Image_Feed snapshot as evidence, and starts the photo screen. Pl@ntNet candidate selection also considers that same snapshot with saved Diagnosis photos. If the quadrant has no current feed snapshot, the Diagnosis module opens and asks the user to add a photo.

**Limit:** Android camera, image-feed cache, photo screen, and Pl@ntNet provider behavior require on-device verification. This workspace build has no Pl@ntNet key unless the private build environment supplies one.

## BUG-024 — Home camera capture may stall or omit the image

**Reported:** 2026-10-10 10:30 MYT  
**Status:** Fixed in v0.5.08 Build 3 (versionCode 84); device verification pending.

**Fix:** Kept capture in the quadrant source dropdown. Removed the pending MediaStore flag from the camera output and explicitly shares the output URI with the camera app. If the camera returns a usable thumbnail instead of the full-size output, GrowPlot saves that image. Imported and captured photos use `CCTVXX_yyyyMMdd_HHmmss_SSS.jpg` and are saved in the selected plot’s existing image-feed folder.

**Limit:** Camera app compatibility and Google Drive writes still need verification on the user’s device.

## BUG-023 — Home source choices placed outside the quadrant cards

**Reported:** 2026-10-10 10:26 MYT  
**Status:** Corrected in v0.5.08 Build 2 (versionCode 83); device camera/Drive verification pending.

**Fix:** Removed the extra Home photo panel. Each quadrant card now offers a compact source dropdown with CCTV · Image Feed (default), Camera, and File selection. Camera and file choices apply to that card’s CCTV number and save the JPEG to the selected plot’s configured image-feed folder.

**Limit:** The plot feed must already be connected and its saved Android read/write grant valid. Verify camera capture and Drive provider writes on the device.

## BUG-022 — Saved Drive paths can be missing from the shared config after startup

**Reported:** 2026-10-10  
**Status:** Source fix compiled in v0.5.08 Build 1 (versionCode 82); Android Google Drive provider test pending.

**Cause:** Startup restored configured folder IDs from `storage-paths.json`, but if the file did not exist it returned an empty config and never created it. The app also did not bundle its four default image-feed paths for a fresh install. Grant recovery searched persisted Android grants only for the shared GrowPlot root; a retained Diagnosis grant could be missed if its local URI preference was absent.

**Fix:** Bundled the canonical image-feed and Diagnosis defaults in `app/src/main/assets/config/storage-paths.json`. After the one-time Android grant, startup restores/creates those image-feed directories and creates `Projects/GrowPlot/config/storage-paths.json` if it is missing. It also searches Android's retained Google Drive grants when local URI preferences are absent. Existing non-empty Drive config is not rewritten on every launch.

**Verification / limits:** Clean Gradle compile and APK package/signature/alignment/version checks passed. No Android device is connected, so normal relaunch, retained-grant recovery, remote config creation, and folder-picker behavior still need tablet verification. Android requires OS authorization after reinstall, cleared app data, or permission revocation.

## BUG-021 — Diagnosis media picker blocked by Drive permission gate; saved Drive grant rechecked each time

**Reported:** 2026-10-09 23:55 MYT  
**Status:** Fixed in v0.5.07 Build 30 (versionCode 80). Gradle build, APK metadata, signing, alignment, and JavaScript syntax checks passed; Android device flow remains pending.

**Cause:** Build 28/29 required a Drive connection before launching the camera or media picker. A missing or stale Drive grant therefore prevented the user from selecting any photo or video. The permission helper also re-read Android's persisted URI grants and re-resolved the folder path during normal media operations.

**Fix:** Media selection and camera capture are now independent of Drive connection. GrowPlot saves the selected photo/video to private device storage immediately, queues an upload, and requests the one-time Drive folder grant afterward. The grant and canonical Diagnosis, Plot, and Quadrant folder IDs are retained. Normal capture and diagnosis flows trust these saved values; they do not poll permission state or walk the directory tree again on every use. If an actual Drive operation fails, the local copy is kept and a manual “Reconnect Google Drive” control is available. Once permission is re-granted, pending media uploads are retried.

**Verification / limits:** The app compiled as versionCode 80 / `0.5.07 Build 30`, is aligned, and uses the same Android debug signing certificate as Build 27/28. Static JavaScript syntax check passed. No Android phone/tablet is connected for end-to-end media selection, camera, Drive provider, upload, or playback testing.

## BUG-020 — Diagnosis media storage reports Google Drive access error

**Reported:** 2026-10-09 23:42 MYT  
**Status:** Fixed in v0.5.07 Build 28 (versionCode 78). Gradle build, package metadata, signature, and alignment checks passed; Android Drive-provider test remains pending.

**Cause:** If GrowPlot had no persisted read/write Storage Access Framework grant for Google Drive, or the saved grant had become unusable, the diagnosis storage flow only displayed a warning and did not reopen Android's folder authorization screen. Diagnosis media therefore could not be read from or written to Drive.

**Fix:** GrowPlot reuses a valid saved grant; otherwise it opens Android's one-time folder authorization screen. The app now checks that the saved grant is still persisted and has both read/write access before allowing capture. The user selects the existing `Projects` or `GrowPlot` parent once, then GrowPlot resolves/creates `Projects/GrowPlot/Diagnosis` and keeps using that canonical destination. Folder connection failures now include the provider's reported reason and explain the supported parent folders.

**Verification / limits:** The modified Java source compiled into Build 28. APK metadata reports versionCode 78 and `0.5.07 Build 28`; the APK is aligned and signed with the same certificate as Build 27. No Android tablet is attached, so the system picker and actual Google Drive provider read/write flow still need device verification. Android requires this one-time OS permission for private Drive files; the diagnosis subfolder is resolved automatically after it is granted.

# GrowPlot bug log

## BUG-047 — Connect IP Webcam to Home CCTV 01

**Reported:** 2026-10-10 18:39 MYT  
**Status:** Implemented in source after Build 16; inline JavaScript and Gradle resource/Java compile checks passed. APK and Android network test pending.

**Change:** For Plot 1, overlays the IP Webcam MJPEG stream at `http://192.168.1.241:8080/videofeed` on the existing CCTV 01 image frame. The existing Image_Feed snapshot remains underneath and appears if the stream is unreachable. The app's network security configuration permits cleartext traffic only to the camera's displayed IP; the stream stays inside the existing Home card.

**Limit:** The GrowPlot device must be able to reach `192.168.1.241` on the same local network. If the router changes the camera phone's address, this configured URL must be updated. The stream uses HTTP on the local network and has no credentials in the URL. Android live stream display still needs device verification; Build 16 does not include this source change.

## BUG-019 — Pl@ntNet diagnosis was not visible after video pixel analysis

**Reported:** 2026-10-09 23:28 MYT  
**Status:** Corrected in v0.5.07 Build 27 (versionCode 77). Inline JavaScript syntax, offline Gradle build, APK package metadata, and signature checks passed. Android device playback and UI/network flow remain to be confirmed on tablet.

**Cause:** The video-analysis worker called a WebView status update directly from its background thread before queuing the Pl@ntNet request. Android requires WebView interaction on its UI thread; this could throw and divert execution into the video-analysis error handler before the provider request was queued. The diagnosis pane also did not scroll to the provider result when it arrived.

**Fix:** The WebView update now runs on Android's UI thread, and the automatic provider request is queued after video pixel sampling. GrowPlot displays a visible pending state, renders the full ranked Pl@ntNet output and care guidance, and scrolls the result into view. Results are stored against the original plot/quadrant even if the selected plot changes while the call is in progress. The Pl@ntNet manual button remains removed.

**Live API verification:** Sent a plant close-up image shared in this chat to Pl@ntNet's disease endpoint. Model `2026-03-20 (7.5)` returned: Fusarium sp. (FUSASP) 66.278%; Barley yellow dwarf virus (BYDV00) 8.467%; unknown parasitic pathogen (ZZXXZZ) 3.463%; Phytophthora cinnamomi (PHYTCN) 0.846%; Diuraphis noxia / barley aphid (BRAYNO) 0.462%. This confirms the API key/endpoint return a ranked diagnosis. This was an external request, not an Android device test. The endpoint may return candidates that do not fit the confirmed host species; treat the response as a model suggestion and obtain expert confirmation before treatment.


## BUG-018 — Pl@ntNet check misses Drive quadrant photos and requires a manual action

**Reported:** 2026-10-09 23:20 MYT  
**Status:** Corrected in v0.5.07 Build 26 (versionCode 76). JavaScript checks, offline Gradle build, APK metadata, and signature checks passed; tablet runtime validation remains pending.

**Cause:** Build 25 read only the app's private photo cache for photo checks and used video frames alone for video checks. It did not reliably read saved photos from the corresponding Google Drive quadrant folder, and it still exposed a manual provider button.

**Fix:** While the video is playing, GrowPlot continues its pixel sampling. After the scan completes, it reads the matching `Projects/GrowPlot/Diagnosis/Plot_<n>/Quadrant_<nn>` Drive folder, combines those photos with sampled frames from the playing video, and ranks candidates by approximate sharpness, exposure, and visible plant area. It automatically submits the clearest five to Pl@ntNet and reports which images it selected. If Drive photos are unavailable, readable video frames can still be submitted. Photo CV scans also automatically cross-reference the best photos in that same Drive folder. The manual Pl@ntNet button has been removed.

**Limitations:** The image-quality ranking is a heuristic, not plant segmentation or clinical image validation. The disease endpoint accepts images; species, symptom, and pixel data remain context only. No Android device was connected for end-to-end playback, Drive-provider, or live UI verification. The private APK contains the user's API key; the source ZIP does not.


## BUG-017 — Pl@ntNet response is not wired into the in-app diagnosis result

**Reported:** 2026-10-09 23:10 MYT  
**Status:** Implemented in v0.5.07 Build 25 (versionCode 75). Static/build validation passed; tablet runtime validation remains pending.

**Cause:** Build 24 only called an optional, undeployed proxy. The Android diagnosis screen therefore had no live Pl@ntNet result after its on-device color screen.

**Fix:** The native Android bridge posts up to five saved JPEGs, or up to five sampled video frames, to Pl@ntNet's disease-identification endpoint. The response is kept separate from GrowPlot's pixel screen, stored per plot/quadrant, and rendered beneath it with ranked model scores, precautionary care steps, and the AI/human-expert disclaimer. The existing video player and its scan path remain in use.

**Limitations:** Pl@ntNet's disease endpoint accepts images and does not accept species, symptom, or pixel metrics as model input; those fields are displayed as context only. Candidate scores are not confirmed probabilities, and the endpoint covers a limited species/pathology set. The personal APK contains the API key because the native client calls Pl@ntNet directly; keep it private. A server proxy is required for safer distribution. No connected Android device was available for runtime UI/network verification.


## BUG-016 — Remove duplicate diagnosis results and disclaimers

**Reported:** 2026-10-09  
**Status:** Implemented in v0.5.07 Build 23 (versionCode 73); on-device verification pending.

**Fix:** Uses one diagnosis result card for both photo and video screens. The latest scan replaces the prior report. Photo screening no longer re-screens video thumbnails, removing duplicate pixel summaries. Removes the second result panel and repeated section disclaimer; the single card retains care suggestions, limitations, and the expert-confirmation disclaimer.

**Verification:** Inline JavaScript syntax passes. Gradle compilation succeeds; APK metadata reports `com.growplot.farm`, versionCode 73, versionName `0.5.07 Build 23`. APK signature verification passes and matches Build 22. No Android device is connected here, so verify that one result card updates after each scan.

## BUG-015 — Close floating player and show retained diagnosis result

**Reported:** 2026-10-09  
**Status:** Implemented in v0.5.07 Build 22 (versionCode 72); on-device verification pending.

**Fix:** Adds a visible × close button and closes the native player after video sampling completes. The full result is retained and shown in the diagnosis results section; the result area scrolls into view. Adds cautious care suggestions: remove only fully dead leaves, check soil moisture/drainage and water retention, and verify soil pH/EC before applying crop-appropriate NPK. The capture status now stays brief. Pixel analysis is explicitly preliminary and includes a human-expert disclaimer.

**Verification:** Inline JavaScript syntax passes. Gradle compilation succeeds; APK metadata reports `com.growplot.farm`, versionCode 72, versionName `0.5.07 Build 22`. APK signature verification passes and matches Build 21. No Android device is connected here, so verify the × control, auto-close, and visible retained result on-device.

## BUG-014 — Match video player to thumbnail and show result below

**Reported:** 2026-10-09  
**Status:** Implemented in v0.5.07 Build 21 (versionCode 71); on-device verification pending.

**Fix:** Keeps native VideoView playback. Centers and caps its dimensions to the diagnosis-card thumbnail bounds while respecting portrait or landscape aspect ratio. Live pixel color status remains on the player. Completed video analysis is saved and displayed under the diagnosis screen results section with limitations and an A.I. disclaimer.

**Verification:** Inline JavaScript syntax passes. Gradle compilation succeeds; APK metadata reports `com.growplot.farm`, versionCode 71, versionName `0.5.07 Build 21`. APK signature verification passes and matches Build 20. No Android device is connected here, so verify playback, thumbnail-sized layout, and result rendering on-device.

## BUG-013 — Center and shrink the diagnosis video window

**Reported:** 2026-10-09  
**Status:** Implemented in v0.5.07 Build 20 (versionCode 70); device layout verification pending.

**Fix:** Keeps the existing native Android VideoView playback path and its pixel-color analysis. The dialog is centered, initially opens at a reduced size, then uses decoded video dimensions to fit the window to portrait or landscape clips, capped at 72% of the display width and height.

**Verification:** Inline JavaScript syntax passes. Gradle compilation succeeds; APK metadata reports `com.growplot.farm`, versionCode 70, versionName `0.5.07 Build 20`. APK signature verification passes and matches Build 19. No Android device or sample video is available here, so playback and the resized layout need on-device verification.

## BUG-012 — Fit native video player to diagnosis area and show color percentages

**Reported:** 2026-10-09  
**Status:** Implemented in v0.5.07 Build 19 (versionCode 69); Android playback and overlay timing need device verification.

**Fix:** Retains the native VideoView code path confirmed working in Build 13. The playback window is sized to a compact landscape video area rather than full screen, with the diagnosis pane visible behind it. A sampled-frame overlay shows actual colored labels and percentages for green, yellow, brown, and other pixels during playback; the final summary gives cautious follow-up and a human-expert disclaimer.

**Verification:** HTML JavaScript syntax passes. Gradle compilation succeeds; APK metadata reports `com.growplot.farm`, versionCode 69, versionName `0.5.07 Build 19`. APK signature verification passes and matches Build 18. No Android device or sample video is available here, so runtime playback and overlay updates remain unverified.

## BUG-011 — Restore confirmed video playback path

**Reported:** 2026-10-09  
**Status:** Reverted in v0.5.07 Build 18 (versionCode 68); device playback verification remains required.

**Cause:** The experimental inline HTML5 video player introduced in Build 17 did not play the user's video.

**Fix:** Restores the earlier native Android VideoView implementation from Build 13, which the user had confirmed played video. Removes the experimental inline playback and its associated analyzer bridge. Retains the simple local × hide control and updates all visible version labels to Build 18.

**Verification:** JavaScript syntax passes. Gradle compilation succeeds; APK metadata reports `com.growplot.farm`, versionCode 68, versionName `0.5.07 Build 18`. APK signature verification passes and matches Build 17. Both visible version labels show Build 18. No Android device is connected here, so playback must be confirmed on the device.

## BUG-010 — Diagnosis remove control, inline video, color labels, and stale build label

**Reported:** 2026-10-09  
**Status:** Implemented in v0.5.07 Build 17 (versionCode 67); Android WebView playback still needs device verification.

**Fix:** Replaces the native video dialog with an inline HTML5 player inside the diagnosis media card. It uses 0.5× playback and shows sampled pixel percentages with green/yellow/brown/other color chips and descriptive labels; detailed follow-up and disclaimer appear directly under the player while the diagnosis pane remains visible. The × now immediately removes the clip from this device's diagnosis list without the WebView confirmation prompt that was preventing the earlier action; the Drive original is retained. Corrects the header/footer version labels and WebView user agent to Build 17.

**Verification:** Inline JavaScript syntax passes. Gradle compilation succeeds; APK metadata reports `com.growplot.farm`, versionCode 67, versionName `0.5.07 Build 17`. APK signature verification passes and matches Build 16. The HTML header and footer both show Build 17. No Android device or test video is available here, so HTML5 video rendering and live CV timing still require on-device verification.

## BUG-009 — Video remove control and black playback surface

**Reported:** 2026-10-09  
**Status:** Implemented in v0.5.07 Build 16 (versionCode 66); device verification is still required.

**Fix:** Added a simple × over each video thumbnail. It hides the selected video from this device's diagnosis list and leaves the Google Drive original untouched, avoiding provider delete failures. Simplified the player back to the direct VideoView inside a FrameLayout, matching the previously confirmed playback structure, while retaining a compact window, 0.5× slow motion, and live pixel flags. Completed CV findings are shown in the diagnosis status after analysis.

**Verification:** HTML JavaScript syntax passes. Gradle compilation succeeds; APK metadata reports `com.growplot.farm`, versionCode 66, versionName `0.5.07 Build 16`. APK signature verification passes and matches Build 15. No Android device or test MP4 is available here; actual video rendering still needs confirmation on the device.

## BUG-008 — Slow-motion video playback with live pixel flags

**Reported:** 2026-10-09  
**Status:** Implemented in v0.5.07 Build 15 (versionCode 65); Android playback speed and in-video overlay timing require device verification.

**Fix:** The media control now reads “Play & diagnose.” Android playback is set to 0.5× speed. A colored overlay on the video shows sampled green/yellow/brown percentages, rough green leaf-like region count, and flags for elevated yellow/brown or very low green pixels. Frame analysis is paced across the video timeline so the overlay changes during playback. The report still labels outputs as approximate local pixel screening and includes the human-expert disclaimer.

**Verification:** HTML inline JavaScript syntax passes. Gradle compilation succeeds; APK metadata reports `com.growplot.farm`, versionCode 65, versionName `0.5.07 Build 15`. APK signature verification passes and uses the same debug certificate as Build 14. No Android device is connected in this workspace, so speed/overlay runtime behavior is not yet confirmed.

## BUG-007 — Diagnosis media cleanup, oversized playback, and missing video screening

**Reported:** 2026-10-09  
**Status:** Implemented in v0.5.07 Build 14 (versionCode 64). Source, JavaScript syntax, Gradle compilation, APK metadata, and signature checks pass; Android Drive deletion, playback layout, and video analysis runtime tests remain pending.

**Observed:** The diagnosis pane had no way to remove duplicate media; video opened full screen; video playback did not show sampled visual analysis or expert follow-up guidance.

**Fix:** Added a confirmed remove action per photo/video. For Drive items, the app verifies that the document is a direct child of the selected Plot/Quadrant folder before deleting it, then removes the corresponding local media/index/cache entries. It keeps same-name files distinct using Drive document IDs. The player now opens as a compact panel inside the diagnosis experience. While video plays, a background task samples up to 12 downscaled frames and updates progress, then reports approximate pixel colour proportions and green leaf-like connected regions, followed by cautious next checks. Clear limitations and a human expert disclaimer appear in the video report and diagnosis analysis.

**Verification:** HTML inline JavaScript syntax passes. Gradle compilation, APK package metadata, and APK signature checks pass. No Android device/ADB is available in this environment, so actual video rendering, the on-screen layout, CV results, and Google Drive provider delete support remain unverified.

## BUG-001 — Serious — per-quadrant image statistics missing

**Reported:** 2026-10-09  
**Status:** Resolved and confirmed on the user's device in v0.5.05 Build 13 (versionCode 41). Correcting the `data_feed` path restored the quadrant image statistics. APK SHA-256: `2ba03d206193dd9362c6ca535d70778e260a10fcbb6e886617ad7844e8428701`.

**Observed:** The Home dashboard did not show the latest snapshot timestamp, latest image, image-history count, earliest/latest photo times, or elapsed span for CCTV/quadrant cards. The same failure could affect every quadrant.

**Cause:** Confirmed by the user: the configured `data_feed` path pointed to the wrong location, so the app was not reading the expected per-camera images. Source inspection also found limited filename matching and a latest-snapshot display dependency; these were hardened in Build 13, but the path correction was the confirmed trigger.

**Fix:** The user selected the correct `data_feed` path, restoring the image set. Build 13 additionally recognizes CCTV, Camera, Cam, Quadrant, Quad, and Q identifiers in filenames and folder names; publishes earliest/latest timestamps with each camera's calculated statistics; and updates the visible latest-snapshot line from that result. Files without an identifiable camera are reported as unmatched.

**Acceptance checks:**
- User confirmed that correcting the `data_feed` path restored the image statistics on device.
- Build 13 passes build, JavaScript syntax, camera-label, APK signature/alignment/package, and source ZIP integrity checks.
- Files without an identifiable camera are reported as unmatched; no quadrant is guessed.

**Prevention:** Keep the selected `data_feed` path pointed at the active folder containing the CCTV images. For future timestamp-only images, include a CCTV/Q/Quadrant ID in the filename or place each set in a camera-labeled folder.


## BUG-002 — High — diagnosis video capture does not complete reliably

**Reported:** 2026-10-09  
**Status:** Code fix compiled in v0.5.07 Build 9 (versionCode 59); external camera runtime and Drive write tests pending. The candidate APK uses a different debug certificate from Build 8 and is not an in-place update.

**Observed:** User reports that the video action in A.I Health does not execute.

**Cause:** Source inspection found that Build 8 depended on the external camera returning a media URI. When it returned no URI, GrowPlot had no recovery path. The exact device-specific trigger is unconfirmed because no Android device is attached to this build environment.

**Fix:** Build 9 launches the system video camera chooser and, when the camera returns no clip URI, immediately opens a video-only picker so the recorded clip can be selected and imported. Captured media is kept in private app storage. When a writable Drive folder is selected, GrowPlot also writes a copy to `Diagnosis/<Plot>/<Quadrant_01–06>` and reports upload success or failure.

**Acceptance checks:**
- Gradle debug APK build succeeds; package metadata reports `com.growplot.farm`, versionCode 59, versionName `0.5.07 Build 9`.
- Inline JavaScript syntax check passes.
- APK signature verifies, but differs from Build 8; do not install over Build 8.
- Device camera capture, fallback picker, thumbnail playback, and Google Drive provider write tests remain pending.


## BUG-003 — High — diagnosis files can be written under a duplicated folder path

**Reported:** 2026-10-09 21:00 MYT  
**Status:** Folder-root correction implemented in v0.5.07 Build 12 (versionCode 62); Drive path checks completed. Video decode and on-device playback remain unverified, so this is not a release.

**Observed:** Drive contained the expected video under `Projects/GrowPlot/Diagnosis/Plot_1/Quadrant_01`, plus a second copy under `.../Quadrant_01/Diagnosis/Plot_1/Quadrant_01`. The second copy was moved intact into the canonical quadrant folder; the nested branch is now empty.

**Cause:** Folder selection treated any selected folder whose name was not `Diagnosis` as a parent for a new `Diagnosis` folder. Selecting `Projects` could therefore create `Projects/Diagnosis`; selecting a plot or quadrant could create a nested duplicate. Existing persisted root IDs could also keep pointing to the wrong location.

**Fix:** Build 12 resolves the canonical path `Projects/GrowPlot/Diagnosis`, refreshes that root from the persisted read/write Drive grant before listing or saving media, and refuses unsupported folder levels instead of silently nesting another `Diagnosis`. Plot and quadrant folders remain exactly one level each below `Diagnosis`.

**Verification / limits:** The connected Drive listing confirms both 17,219,311-byte MP4 copies are now direct children of `Projects/GrowPlot/Diagnosis/Plot_1/Quadrant_01`. The now-empty nested folder branch could not be permanently deleted because the approval reviewer blocked that action. Build 12 compiles, but no Android device is attached and the media bytes could not be decoded in this environment. Do not publish until the actual video downloads completely and plays on-device.


## BUG-004 — High — diagnosis video is not visible/playable in the WebView player

**Reported:** 2026-10-09 21:14 MYT  
**Status:** Native in-app player implemented in v0.5.07 Build 13 (versionCode 63); Gradle compilation and inline-JavaScript syntax checks pass. On-device playback remains unverified.

**Observed:** The Drive MP4 opens in Chrome and reports a duration of about 9.5 seconds with no browser media error. The browser reports a 0×0 video frame size, so a decodable visual stream is not confirmed. GrowPlot WebView playback still fails.

**Cause:** The diagnosis UI depended on a custom `WebResourceResponse` byte-range bridge for HTML video. This unnecessarily routed media through WebView interception and made playback dependent on range/header handling.

**Fix:** The diagnosis card now shows the saved video thumbnail and a Play video button. It asks the Android bridge to play the validated local/cached MP4 in an Android `VideoView` with native media controls. It reports Android decoder errors directly in the diagnosis status. The external Drive player remains a fallback.

**Verification:** Candidate compilation and static checks do not substitute for the required tablet test. The build environment has no attached Android device or emulator, so the native player cannot be launched here. Confirm the cached media opens, dimensions render, playback advances, seeking works, and the dialog closes cleanly on the user's Android tablet before publishing. If Android reports a decoder error, re-encode the source to H.264/AAC MP4.

## BUG-035 — Manage Farm has no crop planting records

**Reported:** 2026-10-10  
**Status:** Compiled in v0.5.08 Build 12 (versionCode 93); device verification pending.

**Observed:** Manage Farm was a placeholder and did not let the user choose crops, assign a crop to a plot section, record planting milestones, or estimate harvest timing.

**Fix:** Added a searchable starter catalogue for chilli (Kulai, MC 11, MC 12, Bangi), eggplant, cucumber, okra, long bean, bitter gourd, and sweet corn. Crop cards show open-licensed photos with attribution links, typical varieties, approximate first-harvest windows, and timing-source links. Planting records can be assigned to a plot, greenhouse, and one of six sections, and store sowing, seedling-ready, and field planting dates, variety, crop stage, area, and notes. The app estimates first harvest from the crop-specific range, supports editing and deletion, and stores records in device-local app storage.

**Verification:** Inline JavaScript passes `node --check`; Gradle build, APK metadata, signature, and alignment checks pass. Photo hosting uses Wikimedia Commons links and needs internet access. Android layout, image loading, and saved-record behavior remain to be verified on the user's test device.

## BUG-036 — Manage Farm needs navigation for upcoming farm modules

**Reported:** 2026-10-10  
**Status:** Compiled in v0.5.08 Build 12 (versionCode 93); device verification pending.

**Observed:** Manage Farm had crop planning but no place to navigate to irrigation/fertiliser or maintenance workflows.

**Fix:** Added three Manage Farm menus: Crop Planning, Irrigation & Fertiliser, and Maintenance. The latter two show simple “Coming soon” placeholders describing their intended scope.

**Verification:** Inline JavaScript syntax, Gradle build, APK metadata, signature, and alignment checks pass. Android device layout and interaction checks remain pending.


## BUG-037 — Diagnosis history disappears for previously diagnosed sections

**Reported:** 2026-10-10  
**Status:** Fix prepared for v0.5.08 Build 13 (versionCode 94); device verification pending.

**Cause:** History loading removed every stored entry without the exact `diagnosisDepth: "deep"` flag. Completed on-device photo screenings and video analyses were not recorded as history unless Pl@ntNet returned a successful deep result.

**Fix:** History loading now preserves and normalizes existing plot-and-section records, recovers that section's latest completed result from its saved analysis/provider record, and merges recovered entries without duplicating occurrences. Completed photo and video screenings are recorded immediately; a later Pl@ntNet response upgrades the same occurrence. Counts and histories remain scoped by plot ID and CCTV section.

**Verification / limits:** JavaScript syntax and focused checks for legacy-entry retention, latest-result recovery, deep-result upgrade without duplication, and plot isolation pass. Earlier occurrences already erased from device storage cannot be reconstructed if no saved analysis remains. Android device history behavior remains to be checked on the user's device.

## BUG-038 — Crop planting records do not store plant/tree count

**Reported:** 2026-10-10  
**Status:** Fix prepared for v0.5.08 Build 13 (versionCode 94); device verification pending.

**Fix:** Added an optional integer number-of-plants/trees input to crop planting and editing. The value is saved in each planting record and displayed in the assigned-crops list; existing records without a count continue to load.

**Verification / limits:** Source checks confirm the field is included in the form, saved record, edit state, and assigned-crop display. Device form layout and persistence remain pending.

## BUG-039 — Older diagnosis runs are not recovered or visible at the top of diagnosis

**Reported:** 2026-10-10  
**Status:** Compiled in v0.5.08 Build 14 (versionCode 95); device verification pending.

**Cause:** Build 10 stored a legacy diagnosis result using `capturedAt` when `completedAt` was unavailable. Build 13's recovery check did not recognize that timestamp field. Also, diagnosis history existed only behind a separate history button, so the diagnosis screen did not show the count and occurrence timestamps immediately.

**Fix:** Recovery now checks legacy `capturedAt`, provider timestamps, the plot/section's photo and video analysis keys, and saved Pl@ntNet results. The diagnosis screen displays the diagnosis count and timestamps at the top. Selecting a timestamp opens that occurrence's saved result. New history records retain the summary, colour metrics, findings, and Pl@ntNet result needed to review it later.

**Verification / limits:** Inline JavaScript syntax and focused tests for legacy `capturedAt` recovery, count/timestamp display, result links, and plot isolation pass. The history was destructively removed by earlier code from some devices; only diagnosis entries still present in history or recoverable from saved reports can be restored. A single latest report cannot recreate multiple older occurrence timestamps that no longer exist on-device. APK and device verification are pending.


## BUG-040 — Farm management records are only stored on the device

**Reported:** 2026-10-10  
**Status:** Compiled in v0.5.08 Build 14 (versionCode 95); Google Drive provider verification pending.

**Fix:** Crop planting records now sync through the existing persisted Google Drive GrowPlot folder grant. GrowPlot creates `db/userdata/<user>/<plot>/greenhouse_<n>/section_<nn>/crop-plantings.json` and a per-user index under `GrowPlot/db/userdata`. Each section file includes crop type, variety, sowing/seedling/field planting dates, number of plants/trees, area, crop stage, notes, and estimate. Startup merges Drive and device records by record ID and update time; deletes are carried as tombstones so they sync across launches. The device copy remains available if Drive sync fails. The app's account sign-in is currently a preview, so records use a stable `default-user` folder until real authentication is connected.

**Verification / limits:** JavaScript syntax checks pass. Android source compilation and a real Google Drive provider read/write test are pending. The Drive operation reuses the saved GrowPlot folder grant and does not launch the folder picker.
## BUG-050 — CCTV01 still shows a Google Drive or bundled photo instead of the stream

**Reported:** 2026-10-10 19:15 MYT  
**Status:** Compiled in v0.5.08 Build 22 (versionCode 103); Android playback verification pending.

**Cause:** The CCTV01 card still rendered a Google Drive image underneath the stream, and Drive refreshes could replace the card image. A stream error could also fall back to a bundled still photo.

**Fix:** CCTV01 now uses `http://192.168.1.241:8080/videofeed` as its only displayed feed and retries that same endpoint after an error. Drive photos remain recorded as the latest snapshot for diagnosis and history, but cannot overwrite the CCTV01 display. CCTV02–CCTV06 retain their Google Drive photo feeds.

**Verification / limits:** Inline JavaScript syntax, focused source checks, Gradle compilation, package/version metadata, APK signature, and alignment checks pass. The APK uses a development signing key that does not match Build 30, and Pl@ntNet is disabled because its key is absent. Playback, reconnect behavior, and Drive snapshot diagnosis need verification on the Android device.

## BUG-051 — CCTV01 stream loads in the browser but not in GrowPlot

**Reported:** 2026-10-10 20:22 MYT  
**Status:** Compiled in v0.5.08 Build 23 (versionCode 104); Android stream verification pending.

**Finding:** The app's WebView loaded GrowPlot from its HTTPS app-assets origin while CCTV01 used the local HTTP MJPEG endpoint. The WebView was in mixed-content compatibility mode, which may still block an image stream. The user confirms the IP Webcam works in their browser; the build workspace cannot reach their private Wi-Fi address and no Android device is attached for direct verification.

**Fix:** Explicitly allow mixed content and network images in the app WebView. Android's network security configuration continues to permit cleartext only to `192.168.1.241`. CCTV01 remains a direct `<img>` feed from `/videofeed`; no Drive photo or periodic capture replaces it.

**Verification / limits:** Inline JavaScript syntax, Gradle compilation, package/version metadata, APK signature, and alignment checks passed. Actual playback and reconnect behavior must be confirmed on the user's device. Signing is development-only and does not match the saved Build 30 certificate.

## BUG-052 — Add a direct IP Webcam viewer button

**Reported:** 2026-10-10 20:30 MYT  
**Status:** Compiled in v0.5.08 Build 24 (versionCode 105); Android viewer connectivity test pending.

**Change:** Added an **IP webcam** button to the camera dashboard. It opens the camera's own page at `http://192.168.1.241:8080/` in a full-screen in-app WebView with a Close control. The camera page is loaded directly rather than as an HTTP image inside GrowPlot's HTTPS page. The viewer does not receive GrowPlot's JavaScript bridge.

**Verification / limits:** Inline JavaScript, Gradle compilation, package/version metadata, APK signature, and alignment checks pass. The camera page and live image still need testing on the user's device and Wi-Fi. APK signing remains development-only and does not match the saved Build 30 certificate.

## BUG-053 — Add CCTV07 live stream tile to the home screen

**Reported:** 2026-10-10 20:45 MYT  
**Status:** Initial version in Build 25 was corrected in v0.5.08 Build 26 (versionCode 107); Android playback verification pending.

**Change:** Added a compact, right-aligned CCTV07 card below the six existing camera cards. It loads `http://192.168.1.241:8080/videofeed`, reports when the stream loads or reconnects, and retries after an image error. The existing CCTV01–CCTV06 and Drive snapshot history/diagnosis paths remain unchanged.

**Build 26 correction:** CCTV07 remains visible as an empty frame until the adjacent **IP webcam** button is pressed. Build 25 started loading automatically; Build 26 starts the request only on button tap.

**Verification / limits:** Inline JavaScript syntax, Gradle compilation, package/version metadata, APK signature, and alignment checks passed. This workspace cannot reach the user's private Wi-Fi address and no Android device is attached; live playback and reconnection must be tested on the user's device. APK signing is development-only and may not update the installed app.

## BUG-054 — Start CCTV07 only when its adjacent button is pressed

**Reported:** 2026-10-10 20:53 MYT  
**Status:** Implemented in v0.5.08 Build 26 (versionCode 107); Android playback verification pending.

**Change:** CCTV07 remains visible as a blank compact stream frame beside the **IP webcam** button. The button starts the `/videofeed` request inside that frame; pressing it again restarts the stream. The image request is not made during home-screen initialization. Loading/error status and automatic retry are shown in the frame.

**Verification / limits:** Inline JavaScript syntax, Gradle compilation, package/version metadata, APK signature, and alignment checks passed. This workspace cannot reach the user's private Wi-Fi address and has no Android device attached, so actual playback and reconnect behavior remain unverified. APK signing is development-only and may not update the installed app.

## BUG-055 — Remove IP webcam button and CCTV07 frame

**Reported:** 2026-10-10 20:57 MYT  
**Status:** Implemented in v0.5.08 Build 27 (versionCode 108); JavaScript, build, package, signature, and alignment checks passed.

**Change:** Removed the IP webcam button and CCTV07 frame from the home screen, including CCTV07-specific startup and retry logic. The existing six camera cards and CCTV01 stream behavior remain unchanged.

## BUG-056 — Keep the CCTV01 VLC overlay attached to its card and behind other menus

**Reported:** 2026-10-11
**Status:** Compiled in v0.5.09 Build 12 (versionCode 121); the follow-up modal regression is fixed in v0.5.09 Build 13 (versionCode 122), with device verification pending.

**Cause:** The native VLC surface was positioned from WebView measurements, but its updates could lag during scrolling, and the overlay remained visible above other main-menu pages.

**Fix:** Coalesce position updates to one per animation frame and send a new position only when it changes. The app now tracks whether the Home dashboard is active; when another main menu opens, the CCTV01 VLC surface fades out and becomes invisible. Returning Home fades it back into the CCTV01 card. The stream itself remains available for the existing snapshot behavior.

**Verification / limits:** Inline JavaScript syntax and Android Java compilation pass. Scroll alignment and fade behavior need confirmation on the user's Android device. The Build 12 modal visibility condition did not account for menu dialogs; see BUG-059 for the correction.

## BUG-057 — Skip CCTV01 snapshots when IP Webcam is unavailable

**Reported:** 2026-10-11
**Status:** Compiled in v0.5.09 Build 12 (versionCode 121); camera availability and skipped-capture behavior need Wi-Fi device verification.

**Fix:** Before each scheduled CCTV01 snapshot, a background HTTP check requests the same `/videofeed` endpoint used by VLC. It requires an HTTP 200 response and the beginning of a JPEG frame within bounded connection/read timeouts. If the camera is unreachable or no frame arrives, the app skips PixelCopy and Drive upload, and reports that the snapshot was skipped. The check only runs while the CCTV01 live-feed surface is visible.

**Verification / limits:** Android Java compilation passed. The app's connection check and skipped-capture behavior must be tested on the user's Wi-Fi; this workspace cannot reach the webcam's private IP address.

## BUG-058 — Add Radar Venture copyright statement

**Reported:** 2026-10-11
**Status:** Compiled in v0.5.09 Build 12 (versionCode 121); footer placement needs device verification.

**Change:** Added “Copyright controlled by Radar Venture 2026” as a centered, low-emphasis footer at the bottom of the app content. The footer uses the existing content width and safe-area spacing so it remains readable on phones and tablets.

**Verification / limits:** Inline JavaScript syntax passes. Visual placement should be checked on the Android phone and tablet after the next build.



## BUG-059 — Hide the CCTV01 VLC window when switching to another menu

**Reported:** 2026-10-11
**Status:** Compiled in v0.5.09 Build 13 (versionCode 122); Android menu switching and modal visibility verification pending.

**Cause:** The visibility check considered the Home dashboard active underneath a menu dialog, so the native VLC surface remained visible above the dialog and its dimmed backdrop.

**Fix:** VLC now hides unless the Home dashboard is active and no modal dialog or account screen is open. A throttled DOM observer watches for modal, hidden-state, and dynamically created dialog changes so the native surface responds when the user opens or closes a menu.

**Verification / limits:** Inline JavaScript syntax, Gradle compilation, package/version metadata, APK signature, and alignment checks passed. Confirm that CCTV01 disappears when opening Harvest, Market, Manage Farm, My Account, Settings, diagnosis, and map dialogs, then returns when Home is active.


## BUG-060 — Add the second IP Webcam stream to CCTV02

**Reported:** 2026-10-11
**Status:** Compiled in v0.5.09 Build 14 (versionCode 123); device/network verification pending.

**Change:** CCTV02 now displays a separate VLC stream from `http://192.168.1.227:8080/videofeed`; CCTV01 remains connected to `http://192.168.1.241:8080/videofeed`. The second player has an independent Refresh button, aligns with the CCTV02 card, hides with menu dialogs, and reconnects when GrowPlot resumes. The shared live-feed snapshot interval applies to both feeds. Before saving, CCTV02 checks its own stream for HTTP 200 and a JPEG frame, then saves as `CCTV02_yyyyMMdd_HHmmss_SSS.jpg` to the configured Plot 1 image-feed folder. Android cleartext is enabled for the two camera IP addresses only.

**Verification / limits:** Inline JavaScript, Gradle compilation, package/version metadata, APK signature, and alignment checks passed. Confirm both IP endpoints are reachable from the user's Wi-Fi and that CCTV01/CCTV02 display and reconnect independently on the Android device. Drive snapshots from CCTV02 require the saved image-feed folder grant.


## BUG-061 — CCTV02 uses the exact webcam stream URL and both feed windows stay behind menus

**Reported:** 2026-10-11
**Status:** Compiled in v0.5.09 Build 15 (versionCode 124); source and window behavior need device verification.

**Finding:** CCTV02 is configured to use `http://192.168.1.227:8080/videofeed`, matching CCTV01's `/videofeed` endpoint at its separate IP. Build 14 already routes its VLC player to that URL. The remaining foreground-window behavior can occur because LibVLC was attached using SurfaceView composition, which may render above app dialogs.

**Fix:** Both VLC players now attach with TextureView composition, allowing menu and modal visibility to follow normal app view layering. Scheduled snapshots use the TextureView bitmap path when no SurfaceView is present. CCTV02 continues to use only `192.168.1.227:8080/videofeed`; CCTV01 continues to use `192.168.1.241:8080/videofeed`.

**Verification / limits:** Inline JavaScript, Gradle compilation, package/version metadata, APK signature, and alignment checks passed. Confirm the IP Webcam stream from `192.168.1.227:8080/videofeed` and confirm both feeds disappear behind menu dialogs on the Android device.


## BUG-062 — Explain changes between a camera's latest and previous snapshots

**Reported:** 2026-10-11
**Status:** Compiled in v0.5.09 Build 16 (versionCode 125); Android/Drive behavior not yet tested on device.

**Change:** Each CCTV card's Image history now compares its two newest distinct saved photos. A small sampled-pixel analysis estimates visible green coverage and average brightness, then reports whether visible green increased, decreased, or stayed similar. The explanation includes both frame times and before/after estimates, and notes that camera framing, shadows, and lighting can affect the result. If fewer than two photos exist, history explains that it is waiting for another snapshot.

**Verification / limits:** Java compilation and inline JavaScript syntax passed. This is a coarse image-color comparison, not proof of plant growth and not a disease or crop-health diagnosis. The comparison is per camera, using snapshots scanned from that plot's configured image-feed source. Android-device verification is pending.

2026-10-12 | v0.6.01 Build 4 | versionCode 131
Snapshot/history correction batch:
- All three live feeds use the same saved snapshot interval; interval changes restart CCTV03 as well as CCTV01/02. Require visible, active playback before capture, and capture the selected plot at request time instead of hardcoding Plot 1.
- Successful live saves request an immediate history scan. Comparison reads stable local archived frames and an unavailable metadata entry no longer aborts all camera histories.
- CCTV02 source is labelled IP Webcam / Live, with the same snapshot timestamp and saved-photo diagnosis handling as CCTV01/03. History refresh does not replace live feed backing images with still photos.
- Routine builds produce one ARM64 APK and source ZIP; universal APK and debug AAB generation removed.
Validation: JavaScript syntax, shell syntax, and executable feed/history UI regression checks passed. MainActivity and TimelineBuckets compiled with the Java 17 compiler against Android API 35 and actual LibVLC 3.6.5 classes. Gradle packaging did not complete: direct dependency access was blocked by the environment policy, and offline mode lacks the VLC/AndroidX dependency cache. No Build 4 APK was generated. Android live capture, Drive scanning and comparison verification remain pending. Build tools are pinned to the bootstrapped 35.0.0 version.

2026-10-12 | v0.6.01 Build 5 | versionCode 132
- Live Feed Configuration, camera IP/VLC login, Plot Management and nested settings start collapsed each time Settings opens; tap a heading to expand.
- Keep encrypted VIGI credentials across reconnects/restarts and preserve saved login when Settings fields are blank. Saved username is shown; password stays masked with a saved-password hint.
- Check RTSP authentication after a VLC error. Prompt again on confirmed authentication rejection or missing fields; network/service failures keep saved credentials. Native correction dialog preloads saved username.
Validation: JVM RTSP accepted/rejected/service/unreachable and Digest-challenge tests, Settings details/nesting check, feed-history regression, JS syntax, complete Gradle ARM64 assemble, APK version/signature/alignment checks passed. Android device verification pending.
Build 4 follow-up: APK generation subsequently succeeded; uploaded to Drive as file 1Nmw0gAll1_XIWCfvZebVDjbkViDuAJlo. User confirmed CCTV02 live feed, snapshot intervals and ARM64 packaging corrected. Photo-history comparison was not separately confirmed by the user.
Signing: Build 5 uses the same development certificate as Build 4. Pl@ntNet key remains absent.

2026-10-12 | Pending next build after v0.6.01 Build 5
Settings: Added a diskette Save button beside Home, visible only in Settings. It validates current plot details, GPS, camera IPs/login, snapshot interval and rescan interval, then saves all those edits without leaving Settings. Blank VIGI password keeps the saved login. Reports partial save failures instead of unconditional success. Existing folder grants/config remain unchanged.
Validation: executable combined-save regression, saved-password reuse, invalid GPS/no writes, partial failure and plot rollback checks passed; JS syntax and feed-history regression passed. Android UI verification pending. One change queued under the existing 2–3-change build batching rule; no new APK generated.

2026-10-12 | v0.6.01 Build 6 | versionCode 133
Packages the Settings diskette Save button beside Home. Saves current plot details, camera IP/login, snapshot and rescan intervals with validation and partial-failure reporting. ARM64 only. Regression checks, Gradle ARM64 compilation, APK version/signature/alignment passed. Same signing certificate as Build 5. Android device validation pending.

2026-10-12 | Pending next build after Build 6
Added collapsed Plugins · API Keys Settings menu with default Pl@ntNet key portal and Kindwise admin links. Masked key fields save through the overall diskette Save action; blank fields retain saved keys. Keys use Android Keystore AES-GCM encrypted device preferences and are never returned to the WebView. Pl@ntNet requests use the saved device key first, then the existing build-key fallback. Kindwise key storage is available; diagnosis provider integration remains unconnected.
Validation: Java 17 compilation against Android API 35/VLC and existing Settings/feed regression checks passed. Live provider validation and Android device checks pending; no new APK generated.

2026-10-12 | v0.6.01 Build 7 | versionCode 134
Adds collapsed Plugins · API Keys Settings with Pl@ntNet and Kindwise key portal links. Masked fields save encrypted on-device via the diskette Save button; blank fields preserve saved keys. Pl@ntNet requests prefer the device key over the build fallback. Kindwise key storage is provided; Kindwise diagnosis is not yet connected. Live service validation pending.
Build 7 validation: Gradle ARM64 build, package version, signature, alignment, Settings and feed-history regression checks passed. Signature matches Build 6. User API keys must be entered in Settings; no provider credentials are bundled. Provider/device validation pending.


## BUG-063 — Connect the saved Kindwise key to crop.health diagnosis

**Reported:** 2026-10-12  
**Status:** Implemented in source and built for v0.6.01 Build 8 (versionCode 135); Android and live-provider verification pending.

**Change:** After pixel/photo or video-frame analysis, GrowPlot sends up to five clearest selected images to Kindwise crop.health and Pl@ntNet independently. Kindwise uses the encrypted API key already stored in Settings; the key remains in the native Android layer and is sent in the provider Api-Key header. Kindwise crop and health issue candidates, confidence scores, and available symptom/treatment details appear in a separate result card. Kindwise results are saved with the section's diagnosis history. A build regression check covers the key use, endpoint, image limit, photo/video fan-out, and result display.

**Verification / limits:** JavaScript syntax, feed-history, settings-save, and Kindwise integration regression checks pass. Gradle ARM64 packaging, package/version, APK signature, and alignment checks pass. A live provider request with a user-entered key and Android photo/video/history behavior remain pending. crop.health analyzes submitted images; GrowPlot's species, symptom, and pixel fields are report context and are not provider inputs. No Kindwise credentials are bundled. The debug APK certificate differs from the saved Build 30 signer, so this artifact is not an in-place update.


## BUG-064 — GitHub APK workflow stops before Gradle compilation

**Reported:** 2026-10-14
**Status:** Workflow repair committed locally; GitHub rerun and APK artifact verification pending.

**Finding:** The Build 10 GitHub Actions run failed in `Prepare Android SDK`; Gradle compilation and APK packaging were skipped. The repository also had two workflows that could both start an APK build for a `main` source change. The manifest checks in the newer workflow were hardcoded to Build 10 / versionCode 137, so the workflow would need another edit for every future version.

**Fix:** Removed the separate Android SDK setup steps and made the single build workflow use the repository's tested `tools/build-apk.sh` → `tools/ensure-android-sdk.sh` bootstrap. Removed the duplicate workflow and made artifact identity checks read `version.properties` dynamically. The workflow now includes workflow-file changes in its push trigger and uploads APK, source ZIP, and manifest together.

**Verification / limits:** GitHub Actions job metadata confirms the failure occurred before build steps. Shell/YAML validation and a successful GitHub Actions rerun are still required before calling APK generation fixed; no new APK has yet been produced.
