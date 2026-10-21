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

**Fix:** Build 28 is based on current main, which restores the saved signing key in GitHub Actions. The build manifest now records both SHA-256 and