# GrowPlot v0.6.01 Build 3

GrowPlot is an Android A.I driven app for farm camera monitoring, plant health workflows, plot management, and diagnosis media.Fully coded by A.I Agents with human directive guided by the requirement specification documents.

GrowPlot retains the selected Google Drive folder grant and storage paths between normal launches. Android may require a new one-time grant after reinstall, cleared app data, or permission revocation. Build 11 corrects diagnosis history so only successful Pl@ntNet results count, and prevents a delayed result from one section replacing another section’s open diagnosis.

Batch the next 2–3 discrete enhancements before generating an APK, unless the user explicitly asks for an earlier build. Keep source changes tracked between builds; then use `tools/build-apk.sh` to validate and package the batch. It uses the included Gradle wrapper and bootstraps the Android SDK once for this project environment. Use `tools/release-growplot.sh` for the stricter installable-candidate gate that requires the existing Build 30 signing key and the private Pl@ntNet key. Details are in `AGENTS.md` and `RELEASE-PROCESS.md`
The active baseline is v0.6.01 Build 3 (versionCode 130). It hides all three native VLC feed frames immediately whenever the dashboard is not active, including when hidden feed cards have zero layout size. Android menu switching and VIGI playback need same-device verification. See `VERSION-LOG.txt` and `BUG-LOG.md` for details.
