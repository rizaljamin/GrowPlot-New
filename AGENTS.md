# GrowPlot project workflow

- Treat this directory as the GrowPlot Android project root. Do not ask the user to install system Gradle: invoke the checked-in wrapper (`./gradlew`) through `tools/build-apk.sh`.
- Keep the latest build recorded in `version.properties` as the current test baseline. Do not bump the version or generate an APK after each individual enhancement. Implement and track the next 2–3 discrete enhancements in the source first; then bump `version.properties` once, update `VERSION-LOG.txt`, run `tools/build-apk.sh`, and inspect the generated manifest. Generate earlier only if the user explicitly asks for an APK sooner. Do not claim an APK exists unless the build script produced it.
- `tools/build-apk.sh` bootstraps the Android SDK once into the ignored project-local `.android-sdk` when the project environment has network access. It reuses that SDK and the Gradle wrapper/cache on later project sessions.
- `tools/release-growplot.sh` is the stricter release check. It requires the current configured stable signing key and Pl@ntNet key, validates the signing fingerprint and package version, and creates the installable candidate only when those checks pass.
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
