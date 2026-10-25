# GrowPlot project workflow

## Agent-switch source synchronization

- This repository is the only supported working source for current GrowPlot tasks. The workspace-level `AGENTS.md` routes agents to this checkout; uploaded APK/source ZIP files are historical references only unless the user explicitly asks for work on that snapshot.
- Before reading project source, editing, building, or releasing, run `./tools/sync-growplot.sh`. It checks that the worktree is clean, fetches `origin/main`, fast-forwards without discarding commits, and prints the synced commit and version. If it stops, do not reset, stash, or overwrite changes automatically; inspect and preserve them first.
- Record the printed baseline commit and version in progress notes. Confirm `git rev-parse HEAD` equals `git rev-parse origin/main`, and check `version.properties` against the newest `VERSION-LOG.txt` entry.
- Do not continue with an old sandbox copy if the sync check fails. Repair or recreate the checkout from `origin/main` only after preserving any local changes.

- Treat this directory as the GrowPlot Android project root. Do not ask the user to install system Gradle: invoke the checked-in wrapper (`./gradlew`) through `tools/build-apk.sh`.
- Keep the latest build recorded in `version.properties` as the current test baseline. Do not bump the version or generate an APK after each individual enhancement. Implement and track the next 2–3 discrete enhancements in the source first; then bump `version.properties` once, update `VERSION-LOG.txt`, run `tools/build-apk.sh`, and inspect the generated manifest. Generate earlier only if the user explicitly asks for an APK sooner. Do not claim an APK exists unless the build script produced it.
- `tools/build-apk.sh` bootstraps the Android SDK once into the ignored project-local `.android-sdk` when the project environment has network access. It reuses that SDK and the Gradle wrapper/cache on later project sessions.
- `tools/release-growplot.sh` is the stricter release check. It requires the current configured stable signing key and Pl@ntNet key, validates the signing fingerprint and package version, and creates the installable candidate only when those checks pass.
- For Google Play internal testing, use `tools/build-play-aab.sh` and the `Build GrowPlot Play AAB` workflow. Configure a dedicated `GROWPLOT_PLAY_UPLOAD_*` key or use the existing stable GrowPlot key; never substitute a runner debug key or claim a signed AAB is upload-ready without checking its signer against the Play Console upload certificate. Play builds target API 36.
- Keep credentials and the signing key outside Drive and source archives. `~/.config/growplot/build.env` is loaded automatically; do not print its values or add it to the project.
- Google Drive paths belong in `Projects/GrowPlot/config/storage-paths.json`. Android's persisted folder grant is device-local and is needed once; reuse it on normal launches. A reinstall, cleared app data, or revoked Android permission may require one new authorization.
- Do not describe simulated plant diagnosis as validated AI, and do not mark Drive picker/upload behavior as verified without a real Android device test.

## Golden-source governance (agreed 12 October 2026)

- GitHub `rizaljamin/GrowPlot-New`, branch `main`, is the single source of truth. At the start of every task, inspect its current commit, `version.properties`, `AGENTS.md`, and `RELEASE-PROCESS.md` before using local files.
- If local work is needed, copy or check out that verified repository revision into the working source folder and record the baseline commit. Never use an older attached ZIP or APK as the development baseline unless the user explicitly requests historical work.
- Inspect -> modify -> compile -> validate -> commit/publish the updated source and release artifacts. Follow the enhancement batching rule above; increment the build identity for each newly generated installable, not for documentation-only changes.
- Keep each APK traceable to its exact source commit, package version, signing fingerprint, and SHA-256 checksum. Store release artifacts in GitHub Releases (or another explicitly recorded artifact location linked from the repository); source archives must identify the same source revision.
- Report measured facts separately from estimates. For APK-size investigations, inspect the actual target APK and report compressed entry sizes, native-library sizes by ABI, assets, DEX, resources, and packaging overhead. Source-file sizes alone are not an APK breakdown. If the APK is unavailable, state that limitation without substituting an older APK.
- Record verification performed and remaining device checks before claiming a release is verified. Never claim governance or artifacts were saved unless the remote write succeeded.

- Generate only the ARM64 (`arm64-v8a`) APK moving forward. Do not generate a universal APK unless the user explicitly changes this policy.
## UI window and overlay standard

- Every new app window, modal, prompt, dialog, drawer, or other foreground surface must define how it takes priority over inactive content and how it returns to the prior screen when closed.
- Keep the active surface fully visible and interactive. Native Android views such as VLC/SurfaceView content cannot be layered reliably by WebView CSS; hide or pause those views while a foreground overlay is active, then restore them after it closes.
- Use a shared foreground-state mechanism rather than a one-off fix for a single prompt. Cover HTML dialogs, app modals, account/full-screen panels, and future overlay types.
- For each new window or prompt, validate both open and close transitions with a regression check. When native video or camera surfaces are present, include a tablet/device check before claiming the layout is verified.
