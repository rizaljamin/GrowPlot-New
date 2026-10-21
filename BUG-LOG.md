## BUG-081 — Run Diagnosis species label is only a preset estimate

**Reported:** 2026-10-21
**Status:** Corrected in v0.7.00 Build 14 source (versionCode 218); ARM64 build pending. Run Diagnosis now sends photos to Pl@ntNet single-species identification and displays ranked candidates for user review. HSV screening remains preliminary. YOLO is not implemented because the repository contains no crop-specific model weights or validation set.

**Cause:** The first on-device screen used RGB threshold rules, and a separate CV button opened a placeholder view.

**Fix:** Run Diagnosis loads the shared HSV screen and sends the selected photos to Pl@ntNet species identification and disease identification. The app displays ranked species candidates separately; selecting a match saves it as a Pl@ntNet image-based candidate. The initial CCTV label is explicitly called a preset estimate and cannot be falsely confirmed. The extra CV button is removed. YOLO remains unavailable without packaged weights and labelled validation images.

**Verification:** HSV fixtures, species endpoint/candidate wiring checks, and existing diagnosis/settings/farm regressions pass. ARM64 build, live provider call, and tablet confirmation remain pending; no field-image accuracy claim is made.

## BUG-080 — Drive folder selection fails to connect and help text overwhelms Data Feed

**Reported:** 2026-10-21
**Status:** Fixed in v0.7.00 Build 13 source (versionCode 217); ARM64 workflow pending, Android Drive verification needed.

**Cause:** First-use selection only accepted the GrowPlot directory. Selecting Projects was rejected, and picker state was not saved if Android recreated GrowPlot while Drive was open. Folder-provider errors were too generic to diagnose.

**Fix:** Accept Projects or GrowPlot as the authorized parent; create GrowPlot and the per-user/per-plot hierarchy as needed. Preserve pending selection across activity recreation and show a status for cancellation or provider errors. Replace long Data Feed help paragraphs with compact info icons that open on hover or tap.

**Verification:** Focused data-feed regression, existing app regression checks, and inline JavaScript syntax checks passed. Drive folder creation/return still requires real-device verification.

## BUG-079 — Connect to folder shows a hard-coded demo path

**Reported:** 2026-10-21
**Status:** Fixed in v0.7.00 Build 12 source (versionCode 216); ARM64 workflow/device verification pending.

**Cause:** The first-use Drive picker and wrong-folder guidance named `Plot01_demo` even when a different plot was selected. The underlying connection already created or reused the selected user/plot folders.

**Fix:** Picker guidance now names the selected plot and explains that GrowPlot creates or reuses `Image_Feed/<signed-in email>/<selected plot folder>` under `Projects/GrowPlot`. Added regression assertions for dynamic plot targeting and missing-folder creation.

**Verification:** Focused connection regression and ARM64 build pending. Google Drive provider behavior still requires testing on the user's Android device.

## BUG-078 — Data Feed shows a stale interval and redundant connection instructions

**Reported:** 2026-10-21
**Status:** Fixed in v0.7.00 Build 11 source (versionCode 215); ARM64 workflow and tablet verification pending.

**Cause:** The interval input loaded the saved device value from Android, but its adjacent status stayed at the HTML default of 0.5 minutes. The folder scan status also directed users to Settings → Plot Management while they were already on that panel.

**Fix:** The Data Feed UI now initializes both the interval field and its status from the saved Android timer. Disconnected-folder and scan messages point to the visible Connect to folder button. The expected path remains calculated from the signed-in account and selected plot even without a Drive folder grant.

**Verification:** Added tools/tests/data-feed-ui-regression.js to check saved interval display, expected path display without folder connection, and the visible connection action. ARM64 workflow/device verification pending.

## BUG-077 — First-plot sign-in prompt appears over Settings

**Reported:** 2026-10-15
**Status:** Built in v0.6.02 Build 02 (versionCode 159); GitHub ARM64 workflow run #119 passed. Android visual verification pending.

**Cause:** After sign-in to an account without plots, the first-plot dialog could be shown while the previous Settings panel was still open, leaving two screens stacked.

**Fix:** The onboarding flow now returns the underlying app to Home before presenting the first-plot choice. The My Account page now uses a centered card capped at 480 px wide and within the available viewport height, with internal scrolling on short screens.

**Verification:** JavaScript syntax and account regression checks passed. GitHub run #119 passed ARM64 compilation, package/version/signature verification, and artifact upload. APK SHA-256: `7ae9425fd88d3b55b1be5275b4128581fac36784cd525d24071ad325621a86c4`. Its signer SHA-256 is `fc5edb30ece8df673c50aa907b61357bceb807eb843ef853d89c276e5501b204` (SHA-1 `1a15eb3ce95b1eaee5e48d525476ace83bd41cdd`), matching v0.6.02 Build 01's configured stable signer. Pl@ntNet key is absent. Visual validation on the tablet remains pending.

## BUG-076 — Plot map links do not consistently open Google Maps

**Reported:** 2026-10-15
**Status:** Implemented in v0.6.02 Build 01 source; build and device verification pending.

**Fix:** Plot coordinate and address links now explicitly open the Google Maps Android app when installed. If it is unavailable, GrowPlot opens the same Google Maps URL in the available browser. Device GPS permission remains foreground-only and is requested only for location lookup; Google Maps navigation itself uses the selected plot coordinates/address and does not need background location.

**Verification / limits:** Manage Farm/Maps regression covers the explicit Maps package, web fallback, and foreground-only location declarations. Android app launch and permission behavior require tablet verification.

## BUG-075 — Manage Farm should open on Plot

**Reported:** 2026-10-15
**Status:** Implemented in v0.6.02 Build 01 source; build and device verification pending.

**Fix:** Opening Manage Farm resets the selected section to Plot, so plot management appears first every time. Crop Planning, Irrigation & Fertiliser, and Maintenance remain available from the tabs.

**Verification / limits:** Manage Farm regression checks the Plot default and existing tab behaviors. Android tap/layout verification pending.

## BUG-074 — Plot Management map thumbnail styles do not apply

**Reported:** 2026-10-15
**Status:** Fixed in v0.6.01 Build 30 source; GitHub ARM64 build and Android-device verification pending.

**Cause:** The Plot Management map preview button's `class` attribute was missing its opening quote (`class=plot-map-thumbnail"`). This malformed value prevented the `.plot-map-thumbnail` CSS rule from matching, so the preview did not receive its intended fixed height, clipping, positioning, or overlay layout.

**Fix:** Corrected the attribute to `class="plot-map-thumbnail"`. The thumbnail continues to render OpenStreetMap tiles and opens the same location in Google Maps when tapped. Sign-in code is unchanged.

**Verification / limits:** HTML parser confirms the button class is exactly `plot-map-thumbnail`; GitHub ARM64 build checks and visual verification on the tablet remain pending.

## BUG-072 — Build 27 login branch regressed Build 26 authentication handling

**Reported:** 2026-10-15  
**Status:** Fixed in Build 28 source; GitHub build and Android verification pending.

**Cause:** The Build 27 branch was based on Build 25, so it restored an older accountResponse() that waited for a Firestore profile read before reporting success. If Firestore access failed after Firebase accepted the email/password credentials, the app could report login as failed. This source-path regression is separate from Firebase rejecting an incorrect password.

**Fix:** Reapplied the login-screen changes on current Build 26 source. The native bridge reports Firebase Authentication success immediately, performs profile setup as best-effort, and reports later profile failures as warnings without clearing the authenticated session. Added regression assertions for the separation.

**Verification:** Source regression and GitHub ARM64 build pending. Exact runtime behavior requires testing the resulting APK on the tablet.

## BUG-073 — Build 27 Google sign-in artifact used a different signer

**Reported:** 2026-10-15  
**Status:** Build 28 carries the stable-key workflow; signer and OAuth fingerprint check pending.

**Cause:** Build 27's workflow did not restore the project's stable signing key. Its uploaded manifest records signing certificate SHA-256 ce42ea38117a12adf320b0601b5a5262eadee5e337e05d243df243052dcdff1a and marks the APK development-only, different from the saved Build 30 certificate. The build therefore did not establish that the Google OAuth fingerprint in Firebase matched the APK.

**Fix:** Build 28 is based on current main, which restores the saved signing key in GitHub Actions. The build manifest now records both SHA-256 and SHA-1 so the actual signer can be checked against Firebase's Android OAuth client.

**Verification:** Build 28 GitHub Actions and OAuth fingerprint comparison pending. Google sign-in still requires a real device test.

## BUG-071 — Rotating CI signer invalidates Android Google OAuth fingerprint

**Reported:** 2026-10-15  
**Status:** Stable signing-key restoration added to the Build 26 GitHub workflow; verification pending.

**Cause:** The routine GitHub workflow used a runner-generated debug key. Build 26's APK signer SHA-1 was `4b926d6ebd5efd65c9926de51dbb2a45bba6d4ad`, while the Firebase Android OAuth configuration is for SHA-1 `90abc505a3aabd5760c09dc5f8d2da0fdf9244a7`. The Build 26 artifact also cannot update an app signed with the saved Build 30 key.

**Fix:** Restored the repository's existing stable-keystore secret step so CI signs with the Build 30 key already configured for the project. This should keep the APK signer and update signature consistent across runs. The resulting SHA-1 still must be checked against Firebase OAuth configuration.

**Verification:** Build 26 run #86 passed ARM64 compile/package checks but used the rotating runner key. Stable-key workflow retry and Google sign-in device verification pending.

## BUG-070 — GitHub ARM64 build runs out of heap during D8 merge

**Reported:** 2026-10-15  
**Status:** Resolved for GitHub Actions run #86; ARM64 artifact uploaded. Stable-signer retry pending.

**Cause:** The first Build 26 GitHub Actions attempt passed the account, farm, and Kindwise regression checks and compiled Java, then D8 failed in `:app:mergeExtDexDebug` with `OutOfMemoryError: Java heap space`. APK verification and artifact upload did not run.

**Fix:** Increased the workflow's `JAVA_TOOL_OPTIONS` heap from 4 GB to 5 GB and reduced the maximum metaspace to 768 MB. The workflow already limits Gradle to one worker.

**Verification:** GitHub Actions retry and ARM64 APK package/version/signature/alignment checks pending.

## BUG-069 — Authentication reported as failed when Firestore profile access failed

**Reported:** 2026-10-15  
**Status:** Fixed in v0.6.01 Build 26 source; GitHub ARM64 build and Android device verification pending.

**Cause:** Email/password and Google Firebase Authentication were followed by Firestore profile reads/writes before the app reported sign-in success. A Firestore permission, network, or service failure therefore surfaced as a failed login even when Firebase Authentication had accepted the credentials. Profile-load errors also cleared the authenticated session in the WebView.

**Fix:** Return the authenticated Firebase identity immediately after successful sign-in. Treat first-profile creation as best-effort, report cloud-profile failures as warnings, and keep an authenticated session active if a later profile read fails. The current Firebase Android OAuth fingerprint remains in the build config and must match the generated APK signer.

**Verification:** Account regression checks now cover auth success independent of Firestore, separate profile warnings, and retaining the session after profile-load failure. GitHub Actions ARM64 build, package/version/signature/alignment, and artifact verification pending. Google credential selection and Firebase authentication still require device testing.

## BUG-068 — Manage Farm screen blank on open

**Reported:** 2026-10-14
**Status:** Fixed in v0.6.01 Build 25 (versionCode 152); Android device verification pending.

**Cause:** `renderManageFarm()` built the greenhouse filter using `existing.greenhouse` before initializing `existing`. JavaScript's temporal dead zone raised a `ReferenceError`, preventing Crop Planning and the other Manage Farm tabs from rendering.

**Fix:** Initialize the selected/editing planting record before generating plot and greenhouse filters.

**Verification:** Added a runtime regression smoke test that opens Manage Farm and checks that Crop Planning, Irrigation & Fertiliser, and Maintenance render. JavaScript syntax and all three app regression scripts pass. Device layout and save/sync behavior still need device verification.

## BUG-065 — Firebase Google sign-in configuration missing

**Reported:** 2026-10-14
**Status:** Built in v0.6.01 Build 24 (versionCode 151); Google sign-in device verification remains pending because the configured Android OAuth SHA-1 differs from the APK signer.

**Cause:** Build 23's google-services.json had no OAuth clients, while the Google Credential Manager flow requires the web OAuth client ID generated by the Google Services plugin.

**Fix:** The project owner reports Email/Password and Google are enabled in Firebase Authentication and supplied refreshed configuration. The config targets Firebase project growplot and Android package com.growplot.farm, with Android and web OAuth client entries. The Google Services plugin and Firebase BoM were updated.

**Verification / limits:** GitHub Actions run #74 passed ARM64 compilation, regression checks, APK manifest/signature/alignment checks, and artifact upload. The supplied config's Android OAuth certificate hash is a789e505c817a122ea906ea6eaa3d48b3a30ab18; Build 24 APK SHA-1 is 90abc505a3aabd5760c09dc5f8d2da0fdf9244a7. These do not match, so add Build 24's SHA-1 in Firebase and download a new config before Google sign-in can be verified. Email/password registration/login and Firestore profile writes still need a live device test.

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

**Fix:** Added a top-right quota badge in the Diagnosis description section. It uses `remainingIdentificationRequests` from the Pl@ntNet disease response, with the `RateLimit` response header as fallback; a quota-exceeded HTTP 429 response is represented as zer