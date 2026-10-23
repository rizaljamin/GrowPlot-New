## BUG-073 — Image Feed login cannot locate the shared Projects location
**Reported:** 2026-10-23  
**Status:** Build 14 did not resolve the user's directly shared Projects folder. Build 15 adds a `sharedWithMe` search fallback; GitHub ARM64 build and device retest pending.

**Cause:** The target Projects folder is shared with the app's Google account, but not necessarily through membership in its containing Shared Drive. Build 14 searched the shared-drive corpus and could still return no matching folder. Folder creation also omitted the shared-drive support flag.

**Fix:** Keep Build 14's Shared Drive and allDrives handling, then retry a root Projects lookup using `sharedWithMe` in the user corpus. Nested GrowPlot and Image_Feed lookups remain scoped to the resolved parent folder.

**Verification / limits:** A source regression check covers the allDrives search and sharedWithMe fallback. Pending GitHub ARM64 build and real-device OAuth, Image_Feed scan, and upload verification.

---

## BUG-072 — Settings Drive connection used the wrong folder-discovery flow

**Reported:** 2026-10-23  
**Status:** Restored Android's folder picker in v0.7.01 Build 21 (versionCode 256); ARM64 build and tablet verification pending.

**Finding:** Settings Connect to folder routed through the in-app Drive API browser and required GrowPlot to find a Projects root. That was failing for the user's Drive even though Android's Drive provider can let the user choose the existing folder directly.

**Fix:** Connect to folder now opens Android's system folder picker. The user chooses Google Drive and the exact folder. GrowPlot saves the persistable folder grant and selected folder ID for scanning/uploads; it clears any stale API-folder mapping so the newly selected folder takes precedence. When the user selects Projects or growplot as a scope, GrowPlot uses the lowercase path Projects/growplot/Image_Feed/<signed-in email>/<plot>.

**Verification / limits:** Regression coverage checks the button routing, picker intent, persistable folder grant, exact selected-folder mapping, and lowercase root creation. Android Google Drive account selection, scanning, and upload still require tablet verification.
