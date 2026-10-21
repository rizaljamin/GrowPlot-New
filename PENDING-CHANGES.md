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
- Build 24 batch: Fi