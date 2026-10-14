# GrowPlot build and release

## Every development change

Read the current test baseline from `version.properties` on GitHub `rizaljamin/GrowPlot-New/main` before beginning work; do not use a hardcoded historical baseline. Accumulate 2–3 discrete app enhancements before generating the next APK, unless the user explicitly requests a sooner build. Track each source change and its verification limits; once the batch is ready, run `tools/build-apk.sh`. It uses the checked-in Gradle wrapper, checks the inline JavaScript and APK metadata/signature, and writes the APK, source ZIP, and SHA-256 manifest to `release/`. It does not depend on a system `gradle` command.

On a Linux project environment, the first run installs Google's Android command-line tools into the ignored project-local `.android-sdk`, accepts the SDK licenses non-interactively, and installs Android API 35, Build Tools 35.0.0, and platform tools. Later runs reuse that SDK and the Gradle wrapper/cache. A Java 17 runtime and working network are needed for the first bootstrap. Set `ANDROID_HOME`, `ANDROID_SDK_ROOT`, or `GROWPLOT_ANDROID_SDK` to reuse an existing SDK.

The build settings and credentials are loaded from `~/.config/growplot/build.env` when it exists. Keep this file private (`chmod 600`); never copy credentials into this project, Drive, or a source archive. `GROWPLOT_PLANTNET_API_KEY` enables Pl@ntNet in the build. Without it, the APK is marked development-only and has no Pl@ntNet diagnosis.

## Installable candidate

Use `tools/release-growplot.sh` for a strict candidate. It runs the same wrapper build and refuses to publish unless the expected stable signing certificate and Pl@ntNet key are available, and package/version metadata, signature, and alignment checks pass.

Build 30's certificate fingerprint is `d3da38540a84c06815beaec70f23d2c98dc927e5216bf83f49d3640760816466`. The current environment’s `~/.android/debug.keystore` has a different certificate (`e3e17f8cc48d8e4a8d25b4480622d3edb0da4f3ff50bc4556c58e07dec03db68`), so it is not the Build 30 key. Recover the matching private keystore from its secured backup, then set `GROWPLOT_DEBUG_KEYSTORE` and the matching password variables in the private environment file (or replace the default keystore only after verifying the fingerprint). Do not generate a replacement key for an update APK: Android rejects a different signer as an update, and uninstalling first removes app-local data and the saved Android Drive grant.

Increase `versionCode` by exactly one in `version.properties` for each APK. The Android UI and Gradle package metadata read the same version properties. When the batch is ready, update `VERSION-LOG.txt` and `BUG-LOG.md` with the included changes and their verification limits before building.

## Google Drive path behavior

Choose Google Drive → My Drive → Projects → GrowPlot once. GrowPlot saves image-feed and Diagnosis paths in `Projects/GrowPlot/config/storage-paths.json` and reuses Android's persisted folder grant on normal launches. The app can recover the selected tree from Android's retained grants if its local path preferences are missing. A new install, cleared app data, or revoked permission still requires Android's one-time authorization again; a Drive path file cannot grant OS access by itself.

## Device verification

The build manifest records source/APK hashes and whether the stable signing key and Pl@ntNet credential were used. A compiled APK does not prove Google Drive provider, camera, playback, or diagnosis behavior. Test those on the target tablet and log the result before calling a build verified.

## Source-of-truth gate

Follow the golden-source governance in `AGENTS.md`. Copy the verified GitHub revision into the working source folder when required. Publish source changes back to GitHub and link installable artifacts to their exact source commit and SHA-256 checksum. Documentation-only changes do not require a new APK or version bump.

## ARM64 build policy

The routine build produces one `arm64-v8a` APK, source ZIP and checksum manifest. It does not generate a universal APK or debug AAB. Both build scripts explicitly request ARM64. Run `node tools/tests/feed-history-regression.js` for the snapshot-history UI regression check. Run `node tools/tests/manage-farm-regression.js` for farm scope, task due status, completion, and schedule-order checks. Compilation and static checks do not replace live-camera and Google Drive testing on the target Android device.

## GitHub Actions APK build

`.github/workflows/android-arm64-build.yml` is the single APK build workflow. It invokes `tools/build-apk.sh`, which installs Android command-line tools and the pinned SDK packages through `tools/ensure-android-sdk.sh`; do not add a second SDK setup action or a duplicate APK workflow. CI reads the version and code from `version.properties` so version changes do not require hardcoded workflow edits. The workflow uploads the APK, source ZIP, and manifest as one SHA-traceable Actions artifact.
