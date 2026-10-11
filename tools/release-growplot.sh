#!/usr/bin/env bash
set -Eeuo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
IDENTITY="$ROOT/version.properties"
OUT_DIR="${GROWPLOT_RELEASE_DIR:-$ROOT/release}"
EXPECTED_CERT="${GROWPLOT_EXPECTED_CERT_SHA256:-d3da38540a84c06815beaec70f23d2c98dc927e5216bf83f49d3640760816466}"
ENV_FILE="${GROWPLOT_ENV_FILE:-$HOME/.config/growplot/build.env}"

# Per-user secrets and SDK settings persist across project sessions. Never keep this file in Drive or source control.
if [[ -f "$ENV_FILE" ]]; then
  set -a
  # shellcheck disable=SC1090
  source "$ENV_FILE"
  set +a
fi

fail() { printf 'GrowPlot release stopped: %s\n' "$*" >&2; exit 1; }
need() { command -v "$1" >/dev/null 2>&1 || fail "required command '$1' is missing"; }

[[ -f "$IDENTITY" ]] || fail "version.properties is missing"
VERSION_NAME="$(sed -n 's/^versionName=//p' "$IDENTITY" | head -n1)"
VERSION_CODE="$(sed -n 's/^versionCode=//p' "$IDENTITY" | head -n1)"
[[ -n "$VERSION_NAME" && "$VERSION_CODE" =~ ^[1-9][0-9]*$ ]] || fail "version.properties must contain versionName and positive integer versionCode"

need java
need zip
need sha256sum
need node
need python3
JAVA_VERSION="$(java -version 2>&1 | sed -n '1s/.*version "\([0-9]*\).*/\1/p')"
[[ "$JAVA_VERSION" == "17" ]] || fail "JDK 17 is required; found Java ${JAVA_VERSION:-unknown}"
TEMP_JS="$(mktemp --suffix=.js)"
trap 'rm -f "$TEMP_JS"' EXIT

ANDROID_HOME="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-${GROWPLOT_ANDROID_SDK:-}}}"
if [[ -n "$ANDROID_HOME" ]]; then
  ANDROID_HOME="$(ANDROID_HOME="$ANDROID_HOME" "$ROOT/tools/ensure-android-sdk.sh")" || fail "Android SDK setup failed"
else
  ANDROID_HOME="$("$ROOT/tools/ensure-android-sdk.sh")" || fail "Android SDK setup failed"
fi
[[ -d "$ANDROID_HOME" ]] || fail "Android SDK setup did not produce a usable SDK"
export ANDROID_HOME ANDROID_SDK_ROOT="$ANDROID_HOME"
export GRADLE_USER_HOME="${GRADLE_USER_HOME:-$ROOT/.gradle-cache}"

if [[ -n "${GROWPLOT_PLANTNET_API_KEY:-}" ]]; then
  export ORG_GRADLE_PROJECT_growplotPlantNetApiKey="$GROWPLOT_PLANTNET_API_KEY"
fi
[[ -n "${ORG_GRADLE_PROJECT_growplotPlantNetApiKey:-}" ]] || fail "Pl@ntNet key missing. Set GROWPLOT_PLANTNET_API_KEY in your local shell before releasing; do not put it in source or the ZIP."

if [[ -n "${GROWPLOT_DEBUG_KEYSTORE:-}" ]]; then
  [[ -f "$GROWPLOT_DEBUG_KEYSTORE" ]] || fail "GROWPLOT_DEBUG_KEYSTORE does not point to a file"
  [[ -n "${GROWPLOT_DEBUG_KEYSTORE_PASSWORD:-}" ]] || fail "GROWPLOT_DEBUG_KEYSTORE_PASSWORD is missing"
else
  [[ -f "$HOME/.android/debug.keystore" ]] || fail "stable GrowPlot signing key missing. Restore the Build 30 keystore as ~/.android/debug.keystore, or set GROWPLOT_DEBUG_KEYSTORE and its password variables. Do not generate a replacement key for an update build."
fi

HTML="$ROOT/app/src/main/assets/index.html"
python3 - "$HTML" > "$TEMP_JS" <<'PY'
import re, sys
s=open(sys.argv[1], encoding='utf-8').read()
scripts=re.findall(r'<script(?:\s[^>]*)?>(.*?)</script\s*>', s, flags=re.S|re.I)
print('\n'.join(scripts))
PY
node --check "$TEMP_JS" || fail "inline JavaScript syntax check failed"

cd "$ROOT"
./gradlew --no-daemon -Pandroid.useAndroidX=true clean assembleDebug
APK="$ROOT/app/build/outputs/apk/debug/app-debug.apk"
[[ -f "$APK" ]] || fail "Gradle completed without producing the expected debug APK"

SDK_TOOLS="$ANDROID_HOME/build-tools"
APKSIGNER="${APKSIGNER:-}"
if [[ -z "$APKSIGNER" ]]; then
  APKSIGNER="$(find "$SDK_TOOLS" -type f -name apksigner -print 2>/dev/null | sort -V | tail -n1)"
fi
[[ -x "$APKSIGNER" ]] || fail "apksigner not found under $SDK_TOOLS/build-tools"
"$APKSIGNER" verify --verbose "$APK" >/dev/null || fail "APK signature verification failed"
CERT="$("$APKSIGNER" verify --print-certs "$APK" | sed -n 's/.*certificate SHA-256 digest: //p' | head -n1 | tr '[:upper:]' '[:lower:]')"
EXPECTED_CERT="$(printf '%s' "$EXPECTED_CERT" | tr '[:upper:]' '[:lower:]')"
[[ -n "$CERT" ]] || fail "could not read APK signing certificate"
[[ "$CERT" == "$EXPECTED_CERT" ]] || fail "APK is signed with $CERT, expected the existing GrowPlot certificate $EXPECTED_CERT. No release files were published."
ZIPALIGN="$ANDROID_HOME/build-tools/35.0.0/zipalign"
[[ -x "$ZIPALIGN" ]] || fail "zipalign not found under $ANDROID_HOME/build-tools/35.0.0"
"$ZIPALIGN" -c -v 4 "$APK" >/dev/null || fail "APK alignment check failed"

BADGING_TOOL="$(find "$SDK_TOOLS" -type f -name aapt -print 2>/dev/null | sort -V | tail -n1)"
if [[ -n "$BADGING_TOOL" ]]; then
  BADGING="$("$BADGING_TOOL" dump badging "$APK")"
  grep -Fq "package: name='com.growplot.farm' versionCode='$VERSION_CODE' versionName='$VERSION_NAME'" <<<"$BADGING" || fail "APK package/version metadata does not match version.properties"
else
  fail "aapt not found under $SDK_TOOLS/build-tools; install Android build tools to verify package metadata"
fi

mkdir -p "$OUT_DIR"
SAFE_VERSION="$(printf '%s' "$VERSION_NAME" | tr '[:upper:] ' '[:lower:]-' | tr -cd 'a-z0-9.-')"
APK_OUT="$OUT_DIR/v${SAFE_VERSION}-GrowPlot-test-candidate.apk"
ZIP_OUT="$OUT_DIR/v${SAFE_VERSION}-GrowPlot-source.zip"
cp "$APK" "$APK_OUT"

rm -f "$ZIP_OUT"
zip -qr "$ZIP_OUT" . \
  -x '.git/*' './.git/*' '.gradle/*' './.gradle/*' '*/.gradle/*' \
  '.gradle-cache/*' './.gradle-cache/*' '*/.gradle-cache/*' \
  '.android-sdk/*' './.android-sdk/*' '*/.android-sdk/*' \
  'local.properties' './local.properties' 'gradle.properties' './gradle.properties' \
  '*.jks' '*.keystore' '*.key' '*.apk' '*.aab' 'release/*' '*/build/*' 'build/*' \
  './release/*' './build/*' './*/build/*' 'node_modules/*' 'backend/node_modules/*'

APK_HASH="$(sha256sum "$APK_OUT" | awk '{print $1}')"
ZIP_HASH="$(sha256sum "$ZIP_OUT" | awk '{print $1}')"
MANIFEST="$OUT_DIR/v${SAFE_VERSION}-release-manifest.txt"
cat > "$MANIFEST" <<EOF
GrowPlot release candidate
Version: $VERSION_NAME
versionCode: $VERSION_CODE
Application ID: com.growplot.farm
APK: $(basename "$APK_OUT")
APK SHA-256: $APK_HASH
Source ZIP: $(basename "$ZIP_OUT")
Source ZIP SHA-256: $ZIP_HASH
Signing certificate SHA-256: $CERT
Checks: inline JavaScript syntax, Gradle clean assembleDebug, APK signature and package/version metadata
Device verification: pending; this script cannot substitute for installing and testing on the target device.
EOF

printf 'Release candidate verified.\n%s\n%s\n%s\n' "$APK_OUT" "$ZIP_OUT" "$MANIFEST"
printf 'APK SHA-256: %s\nSource ZIP SHA-256: %s\nSigning certificate SHA-256: %s\n' "$APK_HASH" "$ZIP_HASH" "$CERT"
