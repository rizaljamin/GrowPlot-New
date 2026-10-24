## BUG-079 — Home card stayed stale after A.I Health showed a species
**Reported:** 2026-10-24
**Status:** Fixed in v0.7.01 Build 27; GitHub Actions run #518 passed ARM64 build and stable signing. Tablet verification pending.

**Cause:** A.I Health reloads the selected plot's saved identity/suggestion before rendering. Home only revealed its existing camera-card DOM on return, leaving the earlier “unverified” text in place.

**Fix:** Refresh all six Home species labels from the selected plot when returning from A.I Health. The saved user confirmation remains higher priority than an unconfirmed suggestion.

**Verification:** Return-to-Home regression and ARM64 package/version/alignment checks passed in run #518; stable signing and Firebase OAuth signer checks passed. Tablet UI verification pending. APK SHA-256: `a9d5a80bcf1e5f5e0f1a607e6c7e62731f4cce46fc7f0961492c2e4efbad4164`.

---

## BUG-078 — Run diagnosis species candidate not shown on Home
**Reported:** 2026-10-24
**Status:** Fix prepared in v0.7.01 Build 26; ARM64 build and tablet verification pending.

**Cause:** Run diagnosis returns species matches inside `speciesIdentification`, but the Home card reads the separate `growplotSpeciesCandidates:<plotId>:cctv<N>` localStorage entry. The callback saved only the combined disease report, leaving Home with no candidate to display.

**Fix:** Save the nested species report to the plot/CCTV candidate key and refresh that camera’s Home card. The model result is labeled as an unconfirmed suggestion; a user-confirmed identity keeps priority.

**Verification:** Added a regression for nested-result persistence and matching-card refresh. Live provider response and tablet UI verification pending.

---

## BUG-077 — Pl@ntNet key was not called by Run diagnosis; Kindwise settings looked duplicated
**Reported:** 2026-10-24
**Status:** Fixed in v0.7.01 Build 25; GitHub Actions run #516 passed ARM64 build and stable signing. Tablet verification pending.

**Cause:** The Pl@ntNet/Kindwise provider bridge existed, but Run diagnosis never invoked it. The provider method also waited on Vertex OAuth and attempted a second Vertex request, unnecessarily coupling the services. Settings listed separate Kindwise plant.id and crop.health keys as separate branded entries, each with its own link.

**Fix:** Run diagnosis now starts Pl@ntNet species and disease analysis using the encrypted Settings key and the provider-specific callback. The provider request no longer waits on Vertex auth or duplicates its Vertex output. Kindwise has one grouped settings heading and one shared account link; separate product keys remain separate.

**Verification:** Pl@ntNet key-flow and Kindwise settings regressions, ARM64 package/version/alignment checks, stable signing, and Firebase OAuth signer checks passed in run #516. API credentials are not logged. The saved key's live provider response still needs tablet validation. APK SHA-256: `1b6539640ab61fc5391f97ca5231225681b878a9f9416176f221c2087df4f4c7`.

## BUG-076 — Diagnosis species candidate is hidden from the home CCTV card
**Reported:** 2026-10-24
**Status:** Fixed in v0.7.01 Build 24; GitHub Actions run #513 passed ARM64 build and stable signing. Tablet verification pending.

**Cause:** The Home card only used a user-confirmed identity. When diagnosis had produced a candidate but the user had not confirmed it, the card showed only “Plant identity unverified,” hiding the species name shown by diagnosis.

**Fix:** Show the newest saved diagnosis candidate for the matching plot and camera while marking it clearly as not confirmed. A confirmed identity continues to take precedence and remains locked as before.

**Verification:** Candidate fallback regression and ARM64 build/signature/package checks passed in GitHub Actions run #513. Display on the user's tablet remains to be checked.

## BUG-075 — Diagnosis-confirmed species is missing from the home CCTV card
**Reported:** 2026-10-24
**Status:** Build 23 restores confirmed identities; screenshot revealed unconfirmed diagnosis candidates still were not shown. Build 24 adds a clearly labeled suggestion. Tablet verification pending.

**Cause:** Diagnosis stored the confirmed species under a plot-and-camera local-storage key, but the home dashboard used static `plantIds` defaults and never reloaded that saved identity. The immediate in-memory update also did not refresh the already-rendered home card.

**Fix:** Restore confirmed species for the active plot before home cards render, reset camera identities to defaults when switching plots, and update the matching card as soon as diagnosis confirmation is saved.

**Verification:** Focused source regression and ARM64 build/signature/package checks passed in GitHub Actions run #512. On-device persistence and display verification remain pending.

## BUG-074 — Live CCTV snapshot can be skipped while GrowPlot is open.
**Reported:** 2026-10-24
**Status:** Fixed in v0.7.01 Build 22; GitHub Actions ARM64 run #510 passed. Tablet verification pending.

**Cause:** CCTV01 reset its first timer to the full interval when VLC started, even when the prior successful capture was already overdue. CCTV01/CCTV03 also waited a full interval after a skipped or failed attempt. A hidden camera surface skipped the capture, but the timer did not adapt its next delay to that state.

**Fix:** All three live camera timers now schedule from each camera's last successful Drive save, retry promptly while due, recheck after feed/folder interruptions, and avoid tight polling when the feed is hidden. Snapshot interval remains the delay between successful captures.

**Verification limits:** Regression and ARM64 packaging passed in GitHub Actions run #510. Actual VLC frame capture and Google Drive write still require testing on the Android tablet.

## BUG-073 — Image Feed login cannot locate the shared Projects location
**Reported:** 2026-10-23  
**Status:** Build 14 did not resolve the user's directly shared Projects folder. Build 15 adds a `sharedWithMe` search fallback; GitHub ARM64 build and device retest pending.

**Cause:** The target Projects folder is shared with the app's Google account, but not necessarily through membership in its containing Shared Drive. Build 14 searched the shared-drive corpus and could still return no matching folder. Folder creation also omitted the shared-drive support flag.

**Fix:** Keep Build 14's Shared Drive and allDrives handling, then retry a root Projects lookup using `sharedWithMe` in the user corpus. Nested GrowPlot and Image_Feed lookups remain scoped to the resolved parent folder.

**Verification / limits:** A source regression check covers the allDrives search and sharedWithMe fallback. Pending GitHub ARM64 build and real-device OAuth, Image_Feed scan, and upload verification.

---

## BUG-072 — Settings Drive connection used the wrong folder-discovery flow

**Reported:** 2026-10-23  
**Status:** Fixed in v0.7.01 Build 21 (versionCode 256). GitHub Actions run #494 passed the ARM64 build and stable-signature checks; tablet verification pending.

**Finding:** Settings Connect to folder routed through the in-app Drive API browser and required GrowPlot to find a Projects root. That was failing for the user's Drive even though Android's Drive provider can let the user choose the existing folder directly.

**Fix:** Connect to folder now opens Android's system folder picker. The user chooses Google Drive and the exact folder. GrowPlot saves the persistable folder grant and selected folder ID for scanning/uploads; it clears any stale API-folder mapping so the newly selected folder takes precedence. When the user selects Projects or growplot as a scope, GrowPlot uses the lowercase path Projects/growplot/Image_Feed/<signed-in email>/<plot>.

**Verification / limits:** Regression coverage passed for button routing, picker intent, persistable folder grant, exact selected-folder mapping, and lowercase root creation. Run #494 produced the ARM64 APK signed with the stable certificate. Android Google Drive account selection, scanning, and upload still require tablet verification.
