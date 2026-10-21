#!/usr/bin/env bash
# Repeatable per-iteration build. Use release-growplot.sh for the strict installable-candidate gate.
set -Eeuo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ENV_FILE="${GROWPLOT_ENV_FILE:-$HOME/.config/growplot/build.env}"
if [[ -f "$ENV_FILE" ]]; then
  set -a
  # shellcheck disable=SC1090
  source "$ENV_FILE"
  set +a
fi

fail() { printf 'GrowPlot build stopped: %s\n' "$*" >&2; exit 1; }
for tool in java node python3 zip sha256sum; do command -v "$tool" >/dev/null 2>&1 || fail "required command '$tool' is missing"; done
JAVA_VERSION="$(java -version 2>&1 | sed -n 's/.*version \"\([0-9]*\).*/\1/p' | head -n1)"
[[ "$JAVA_VERSION" == "17" ]] || fail "JDK 17 is required; found Java ${JAVA_VERSION:-unknown}"

ANDROID_HOME="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-${GROWPLOT_ANDROID_SDK:-}}}"
if [[ -n "$ANDROID_HOME" ]]; then
  ANDROID_HOME="$(ANDROID_HOME="$ANDROID_HOME" "$ROOT/tools/ensure-android-sdk.sh")" || fail "Android SDK setup failed"
else
  ANDROID_HOME="$("$ROOT/tools/ensure-android-sdk.sh")" || fail "Android SDK setup failed"
fi
export ANDROID_HOME ANDROID_SDK_ROOT="$ANDROID_HOME"
export GRADLE_USER_HOME="${GRADLE_USER_HOME:-$ROOT/.gradle-cache}"
if [[ -n "${GROWPLOT_PLANTNET_API_KEY:-}" ]]; then
  export ORG_GRADLE_PROJECT_growplotPlantNetApiKey="$GROWPLOT_PLANTNET_API_KEY"
fi

IDENTITY="$ROOT/version.properties"
VERSION_NAME="$(sed -n 's/^versionName=//p' "$IDENTITY" | head -n1)"
VERSION_CODE="$(sed -n 's/^versionCode=//p' "$IDENTITY" | head -n1)"
[[ -n "$VERSION_NAME" && "$VERSION_CODE" =~ ^[1-9][0-9]*$ ]] || fail "invalid version.properties"
TEMP_JS="$(mktemp --suffix=.js)"
trap 'rm -f "$TEMP_JS"' EXIT
python3 - "$ROOT/app/src/main/assets/index.html" > "$TEMP_JS" <<'PY'
import re, sys
html = open(sys.argv[1], encoding='utf-8').read()
scripts = re.findall(r'<script(?:\s[^>]*)?>(.*?)</script\s*>', html, flags=re.S|re.I)
print('\n'.join(scripts))
PY
node --check "$TEMP_JS" || fail "inline JavaScript syntax check failed"
node "$ROOT/tools/tests/kindwise-regression.js" || fail "Kindwise integration regression check failed"
node "$ROOT/tools/tests/manage-farm-regression.js" || fail "Manage Farm regression check failed"
node "$ROOT/tools/tests/account-registration-regression.js" || fail "Account registration regression check failed"

cd "$ROOT"
APKSIGNER="$ANDROID_HOME/build-tools/35.0.0/apksigner"
AAPT="$ANDROID_HOME/build-tools/35.0.0/aapt"
ZIPALIGN="$ANDROID_HOME/build-tools/35.0.0/zipalign"
./gradlew --no-daemon --max-workers=1 -Pandroid.useAndroidX=true -PgrowplotAbi=arm64-v8a clean assembleDebug
APK="$ROOT/app/build/outputs/apk/debug/app-debug.apk"
[[ -s "$APK" ]] || fail "Gradle completed without producing the expected ARM64 debug APK"
"$APKSIGNER" verify --verbose "$APK" >/dev/null || fail "ARM64 debug APK signature verification failed"
CERT="$("$APKSIGNER" verify --print-certs "$APK" | sed -n 's/.*certificate SHA-256 digest: //p' | head -n1 | tr '[:upper:]' '[:lower:]')"
CERT_SHA1="$("$APKSIGNER" verify --print-certs "$APK" | sed -n 's/.*certificate SHA-1 digest: //p' | head -n1 | tr '[:upper:]' '[:lower:]' | tr -d ':')"
EXPECTED_CERT="${GROWPLOT_EXPECTED_CERT_SHA256:-fc5edb30ece8df673c50aa907b61357bceb807eb843ef853d89c276e5501b204}"
verify_apk() {
  local apk_path="$1" label="$2" badging
  [[ -s "$apk_path" ]] || fail "$label APK was not produced"
  "$APKSIGNER" verify --verbose "$apk_path" >/dev/null || fail "$label APK signature verification failed"
  badging="$("$AAPT" dump badging "$apk_path")"
  grep -Fq "package: name='com.growplot.farm' versionCode='$VERSION_CODE' versionName='$VERSION_NAME'" <<<"$badging" || fail "$label APK package/version metadata does not match version.properties"
  "$ZIPALIGN" -c -v 4 "$apk_path" >/dev/null || fail "$label APK alignment check failed"
}
verify_apk "$APK" arm64

mkdir -p "$ROOT/release"
SAFE_VERSION="$(printf '%s' "$VERSION_NAME" | tr '[:upper:] ' '[:lower:]-' | tr -cd 'a-z0-9.-')"
if [[ "$CERT" == "${EXPECTED_CERT,,}" && -n "${GROWPLOT_PLANTNET_API_KEY:-}" ]]; then
  APK_SUFFIX="test-candidate"
  SIGNING_STATE="Matches the current GrowPlot stable certificate."
elif [[ "$CERT" == "${EXPECTED_CERT,,}" ]]; then
  APK_SUFFIX="stable"
  SIGNING_STATE="Matches the current GrowPlot stable certificate."
elif [[ "${GROWPLOT_STABLE_SIGNING_KEY_AVAILABLE:-false}" == "true" ]]; then
  APK_SUFFIX="signed-new-key"
  SIGNING_STATE="Signed with configured stable keystore; certificate differs from current GrowPlot stable certificate."
else
  APK_SUFFIX="development-only"
  SIGNING_STATE="Does not match the current GrowPlot stable certificate; Android may reject it as an update. Do not uninstall the installed app to install this file."
fi
ARM64_OUT="$ROOT/release/v${SAFE_VERSION}-GrowPlot-arm64-${APK_SUFFIX}.apk"
ARM64_ENTRIES="$(unzip -Z1 "$APK")"
if ! grep -qx 'lib/arm64-v8a/libvlc.so' <<<"$ARM64_ENTRIES" || grep -q '^lib/armeabi-v7a/' <<<"$ARM64_ENTRIES"; then
  fail "ARM64 APK ABI contents are incorrect"
fi
cp "$APK" "$ARM64_OUT"

if [[ -n "${GROWPLOT_PLANTNET_API_KEY:-}" ]]; then
  PLANTNET_STATE="Pl@ntNet key injected from the local environment."
else
  PLANTNET_STATE="Pl@ntNet key is absent; Pl@ntNet diagnosis is disabled in this APK."
fi
ZIP_OUT="$ROOT/release/v${SAFE_VERSION}-GrowPlot-source.zip"
rm -f "$ZIP_OUT"
zip -qr "$ZIP_OUT" . \
  -x '.git/*' './.git/*' '.gradle/*' './.gradle/*' '*/.gradle/*' \
  '.gradle-cache/*' './.gradle-cache/*' '*/.gradle-cache/*' \
  '.android-sdk/*' './.android-sdk/*' '*/.android-sdk/*' \
  'local.properties' './local.properties' 'gradle.properties' './gradle.properties' \
  '*.jks' '*.keystore' '*.key' '*.apk' '*.aab' 'release/*' '*/build/*' 'build/*' \
  './release/*' './build/*' './*/build/*' 'node_modules/*' 'backend/node_modules/*' 'project_sources/*' './project_sources/*'
MANIFEST="$ROOT/release/v${SAFE_VERSION}-build-manifest.txt"
cat > "$MANIFEST" <<EOF
GrowPlot development build
Version: $VERSION_NAME
versionCode: $VERSION_CODE
Application ID: com.growplot.farm
Arm64 APK: $(basename "$ARM64_OUT")
Arm64 APK SHA-256: $(sha256sum "$ARM64_OUT" | awk '{print $1}')
Source ZIP: $(basename "$ZIP_OUT")
Source ZIP SHA-256: $(sha256sum "$ZIP_OUT" | awk '{print $1}')
Signing certificate SHA-256: $CERT
Signing certificate SHA-1: $CERT_SHA1
Signing status: $SIGNING_STATE
Pl@ntNet status: $PLANTNET_STATE
Device verification: pending; install and test on the user's Android tablet.
EOF
printf 'Build outputs:\n%s\n%s\n' "$ARM64_OUT" "$ZIP_OUT"
printf 'Build manifest:\n%s\n' "$MANIFEST"
