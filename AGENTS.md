# GrowPlot project workflow

- Treat this directory as the GrowPlot Android project root. Do not ask the user to install system Gradle: invoke the checked-in wrapper (`./gradlew`) through `tools/build-apk.sh`.
- Keep the latest build recorded in `version.properties` as the current test baseline. Do not bump the version or generate an APK after each individual enhancement. Implement and track the next 2–3 discrete enhancements in the source first; then bump `version.properties` once, update `VERSION-LOG.txt`, run `tools/build-apk.sh`, and inspect the generated manifest. Generate earlier only if the user explicitly asks for an APK sooner. Do not claim an APK exists unless the build script produced it.
- `tools/build-apk.sh` bootstraps the Android SDK once into the ignored project-local `.android-sdk` when the project environment has network access. It reuses that SDK and the Gradle wrapper/cache on later project sessions.
- `tools/release-growplot.sh` is the stricter release check. It requires the saved Build 30 signing key and Pl@ntNet key, validates the signing fingerprint and package version, and creates the installable candidate only when those checks pass.
- Keep credentials and the signing key outside Drive and source archives. `~/.config/growplot/build.env` is loaded automatically; do not print its values or add it to the project.
- Google Drive paths belong in `Projects/GrowPlot/config/storage-paths.json`. Android's persisted folder grant is device-local and is needed once; reuse it on normal launches. A reinstall, cleared app data, or revoked Android permission may require one new authorization.
- Do not describe simulated plant diagnosis as validated AI, and do not mark Drive picker/upload behavior as verified without a real Android device test.
