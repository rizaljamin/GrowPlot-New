Warning: truncated output (original token count: 23279)
Total output lines: 913

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

**Fix:** Replaced the Maintenance placeholder with editable task plans for polybag installation, piping/irrigation setup, weeding, pruning, fertigation-line flushing, bed preparat…13279 tokens truncated…eck only runs while the CCTV01 live-feed surface is visible.

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
**Status:** Fixed in GitHub commit `e0135ce89e84456c24ea9823f495adeb168971dc`; Build 10 CI and artifact upload passed. Android device verification pending.

**Finding:** The Build 10 GitHub Actions run failed in `Prepare Android SDK`; Gradle compilation and APK packaging were skipped. The repository also had two workflows that could both start an APK build for a `main` source change. The manifest checks in the newer workflow were hardcoded to Build 10 / versionCode 137, so the workflow would need another edit for every future version.

**Fix:** Removed the separate Android SDK setup steps and made the single build workflow use the repository's tested `tools/build-apk.sh` → `tools/ensure-android-sdk.sh` bootstrap. Removed the duplicate workflow and made artifact identity checks read `version.properties` dynamically. The workflow now includes workflow-file changes in its push trigger and uploads APK, source ZIP, and manifest together.

**Verification / limits:** Local shell syntax, workflow YAML parsing, `git diff --check`, and Kindwise, Settings-save, and feed-history regression checks passed. The repaired GitHub Actions run passed SDK bootstrap, Gradle build, manifest/package/version checks, and artifact upload. The ARM64 APK SHA-256 is `dc14b903a4bee044060dc541f828c75b41c20db68d509b70dab5a918c4fa8465`; the source ZIP SHA-256 is `c5528b7df0c7201bf1b5eb4c8d32e453e847e63bc9448ef86e817e6ff56127e0`. The generated artifact is development-only because its signer does not match the saved Build 30 certificate and the Pl@ntNet build key is absent. Install/update compatibility and app behavior still require Android device verification.


## BUG-068 — Run Diagnosis has no YOLO inference

**Reported:** 2026-10-21
**Status:** Integrated in v0.7.00 Build 15 source; ARM64 build, Cloud Run deployment, and tablet verification pending.

**Finding:** The previous diagnosis step used HSV colour thresholds and connected regions. No YOLO model or inference endpoint was packaged or called.

**Fix:** Run Diagnosis now submits up to five saved photos to a YOLO-World inference endpoint through the authenticated diagnosis proxy, and annotates returned plant, leaf, flower, and fruit boxes. Added a separate FastAPI model service with server-side token validation. The model is downloaded and loaded by the service at startup; it is not embedded in the APK. Species identification and disease checks remain separate.

**Limits and verification:** This generic open-vocabulary detector does not identify GrowPlot crop species or diagnose disease. A crop-specific detector still needs representative labeled local images and training/validation. JavaScript syntax, Node/Python syntax, YOLO integration regression, HSV, Kindwise, and Data Feed checks pass. Cloud Run model/proxy deployment, actual inference response, and Android tablet behavior remain unverified. The APK needs a configured diagnosis proxy URL/token; the proxy needs the deployed YOLO service URL/token.

## BUG-065 — Manage Farm records are difficult to filter and prioritise

**Reported:** 2026-10-14
**Status:** Included in v0.6.01 Build 23 (versionCode 150); GitHub ARM64 build passed; Android device verification pending.

**Finding:** Crop planting history filtered by plot only, while irrigation and maintenance lists filtered by plot and greenhouse. None of the lists could be narrowed by section. Maintenance dates had no due-state cue or quick completion action, and irrigation schedules were not ordered by their start date.

**Fix:** Added consistent Plot, Greenhouse, and Section filters across Manage Farm tabs. Records scoped to all sections remain visible in an individual section view. Maintenance now shows overdue, due-today, upcoming, or completed status; Mark complete stores a local completion date and syncs the farm record. Irrigation records sort by start date.

**Verification / limits:** Manage Farm regression covers scope filtering, all-section inclusion, filter markup, date cues, completion timestamp and sync calls, and irrigation ordering. Manage Farm, Kindwise, Settings-save, and feed-history regression checks and inline JavaScript syntax pass. APK build, Google Drive provider behavior, and Android touch/layout verification remain pending.

## BUG-066 — ARM64 build scripts are not executable from a clean checkout

**Reported:** 2026-10-14
**Status:** Fixed in v0.6.01 Build 23; GitHub ARM64 build passed.

**Finding:** A clean Git checkout records `tools/ensure-android-sdk.sh` and `gradlew` as non-executable, although `tools/build-apk.sh` invokes both directly. The build stopped at SDK bootstrap, then at the Gradle wrapper after the SDK was initialized.

**Fix:** Corrected both executable file modes in Git. The local retry passed JavaScript, Kindwise, and Manage Farm checks and reached the Gradle wrapper; Gradle distribution download then failed because this workspace cannot reach `services.gradle.org`. GitHub Actions will perform the complete ARM64 build and artifact checks.


## BUG-067 — Account registration and first-plot onboarding

**Reported:** 2026-10-14
**Status:** Included in v0.6.01 Build 23 (versionCode 150); GitHub ARM64 build passed. Google OAuth configuration and Android device verification remain pending.

**Fix:** Removed only the account Username field; login and registration use email, password and confirm password. Password confirmation is checked before Firebase registration. Google Credential Manager sign-in is exchanged with Firebase Auth and creates a Firestore user profile on first use. A new or empty account now sees the first-plot prompt inside the login card, then waits for the Firebase save response before entering the dashboard. Local plot saves for authenticated users also write the profile to Firestore and show success or failure.

**Verification / limits:** Account registration regression, JavaScript syntax, Manage Farm, Kindwise and feed-history checks pass. The GitHub `google-services.json` currently has no OAuth clients, so Google sign-in cannot complete until the Google provider is enabled in Firebase Authentication and a new config with the web OAuth client is committed. Target-device sign-in and layout verification remain pending.

- 2026-10-17 | v0.6.04 Build 04 | Demo Plot streaming: added VLC error retry for CCTV01/02 IP Webcam and transient RTSP retry for VIGI CCTV03. Device network, stream playback and snapshot capture remain unverified.

- 2026-10-18 | v0.7.00 Build 02 | CCTV02 VIGI snapshot cadence: fixed overlapping saves and scheduler drift; checks derive remaining delay from last successful saved frame. Default 15 minutes; stored user preference overrides it. Verify JPEG timestamps and Google Drive history on PP5 tablet; compilation/signature and live test pending.


## BUG-069 — CCTV plant identity incorrectly shown as confirmed Citrus

**Reported:** 2026-10-21
**Status:** Fixed in v0.7.00 Build 19 (versionCode 223); ARM64 build and target-device verification pending. Applies to CCTV01–CCTV06.

**Fix:** Removed unverified default identities from the diagnosis identity card for every CCTV. Saved photos can be compared with Pl@ntNet and Kindwise plant.id, and the user must select a candidate before it becomes the section identity. Older Pl@ntNet-only selections are no longer treated as verified.

**Verification / limits:** JavaScript syntax and regression checks cover provider candidate rendering, plot/camera scoping, and explicit selection. Each provider needs its own configured key. The screenshot itself remains insufficient for exact species confirmation; no automatic species is asserted. ARM64 build and target-device validation remain pending.

**UI follow-up:** Build 19 puts the ginger/galangal/turmeric photo-review shortlist in the CCTV02 Plant identity card, labeled as manual visual review rather than an AI result. It also adds manual species entry to all six CCTV identity cards, with confirmation saved per plot and camera. Users can use that path when provider keys are missing.

