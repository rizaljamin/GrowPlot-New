# GrowPlot automated ARM64 builds and signing-key continuity

Workflow: `.github/workflows/growplot-android.yml` on branch `ci/android-apk-build-signing`.

## Development build
Push to the CI or feature branches, or open a PR. GitHub Actions provisions Temurin JDK 17, Android SDK API 35 and Build Tools 35, runs the existing regression test, then calls `tools/build-apk.sh`. The APK is a **development-only** artifact if the original signing key is unavailable. Never install it over the existing app if its signing certificate differs.

## Signed candidate (manual approval)
1. Recover the **existing** signing keystore from a secure offline backup. Never create a replacement keystore for updating existing installations.
2. In GitHub repository Settings > Secrets and variables > Actions, create these **repository secrets**:
   - `GROWPLOT_KEYSTORE_BASE64`: base64 of the original keystore bytes (not a new key). Example on a trusted computer: `base64 -w 0 original.keystore` (macOS: `base64 < original.keystore | tr -d '\\n'`).
   - `GROWPLOT_KEYSTORE_PASSWORD`: original keystore password.
   - `GROWPLOT_KEY_PASSWORD`: original key password (if different).
   - `GROWPLOT_KEY_ALIAS`: original signing alias (default `androiddebugkey` if omitted).
   - `GROWPLOT_PLANTNET_API_KEY`: key needed by the current strict release script.
3. Go to Actions > GrowPlot ARM64 APK > Run workflow, choose the CI branch and enable `signed_candidate`.
4. The workflow checks the recovered keystore certificate SHA-256 against the recorded stable fingerprint `d3da38540a84c06815beaec70f23d2c98dc927e5216bf83f49d3640760816466` before building. The strict release script independently checks the built APK signer and version.
5. Download the artifact from the workflow run, verify its manifest and SHA-256, and test installing/updating on the target device before calling it a release.

## Security boundaries
- Never commit the keystore, passphrases, tokens, or base64 keystore to GitHub source, Issues, PRs, Drive, or chat.
- GitHub Secrets protect values from ordinary source access but repository administrators and trusted workflows can use them. Restrict write/admin access and require PR reviews before merging workflows. For stronger protection, move release secrets into a GitHub Environment with required reviewers and limit release jobs to protected branches.
- GitHub's default debug key on a fresh runner is **not** the stable GrowPlot key. Development artifacts are not update-compatible unless verified otherwise.
- Base64 is encoding, **not encryption**. Store the original keystore in an encrypted offline backup, with a second secure backup and access-recovery plan.
- The existing Android Gradle configuration injects the Pl@ntNet key into BuildConfig, which is extractable from the APK. Migrate API calls to a server-side proxy before considering that API key secret from app users.
- Build provenance records the exact Git commit and APK SHA-256; signing continuity requires the original certificate, not simply a matching filename or password.
- The CI workflow does not automatically publish to Google Drive, Play Store, or GitHub Releases.
