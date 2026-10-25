#!/usr/bin/env bash
set -Eeuo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ENV_FILE="${GROWPLOT_ENV_FILE:-$HOME/.config/growplot/build.env}"
if [[ -f "$ENV_FILE" ]]; then
  set -a
  # shellcheck disable=SC1090
  source "$ENV_FILE"
  set +a
fi

fail() { printf 'GrowPlot Play AAB build stopped: %s\n' "$*" >&2; exit 1; }
for tool in java node python3 sha256sum zip jarsigner keytool; do
  command -v "$tool" >/dev/null 2>&1 || fail "required command '$tool' is missing"
done

JAVA_VERSION="$(java -version 2>&1 | sed -n 's/.*version "\([0-9]*\).*/\1/p' | head -n1)"
[[ "$JAVA_VERSION" == 17 ]] || fail "JDK 17 is required; found Java ${JAVA_VERSION:-unknown}"

PLAY_KEYSTORE="${GROWPLOT_PLAY_UPLOAD_KEYSTORE:-${GROWPLOT_DEBUG_KEYSTORE:-}}"
PLAY_ALIAS="${GROWPLOT_PLAY_UPLOAD_KEY_ALIAS:-${GROWPLOT_DEBUG_KEY_ALIAS:-}}"
PLAY_STORE_PASSWORD="${GROWPLOT_PLAY_UPLOAD_KEYSTORE_PASSWORD:-${GROWPLOT_DEBUG_KEYSTORE_PASSWORD:-}}"
PLAY_KEY_PASSWORD="${GROWPLOT_PLAY_UPLOAD_KEY_PASSWORD:-${GROWPLOT_DEBUG_KEY_PASSWORD:-$PLAY_STORE_PASSWORD}}"
[[ -n "$PLAY_KEYSTORE" && -f "$PLAY_KEYSTORE" ]] || \
  fail "set GROWPLOT_PLAY_UPLOAD_KEYSTORE or configure the current stable signing keystore"
[[ -n "$PLAY_ALIAS" ]] || fail "Play upload key alias is missing"
[[ -n "$PLAY_STORE_PASSWORD" ]] || fail "Play upload keystore password is missing"
[[ -n "$PLAY_KEY_PASSWORD" ]] || fail "Play upload key password is missing"
export GROWPLOT_PLAY_UPLOAD_KEYSTORE="$PLAY_KEYSTORE"
export GROWPLOT_PLAY_UPLOAD_KEY_ALIAS="$PLAY_ALIAS"
export GROWPLOT_PLAY_UPLOAD_KEYSTORE_PASSWORD="$PLAY_STORE_PASSWORD"
export GROWPLOT_PLAY_UPLOAD_KEY_PASSWORD="$PLAY_KEY_PASSWORD"
if [[ -n "${GROWPLOT_PLANTNET_API_KEY:-}" ]]; then
  export ORG_GRADLE_PROJECT_growplotPlantNetApiKey="$GROWPLOT_PLANTNET_API_KEY"
fi

IDENTITY="$ROOT/version.properties"
VERSION_NAME="$(sed -n 's/^versionName=//p' "$IDENTITY" | head -n1)"
VERSION_CODE="$(sed -n 's/^versionCode=//p' "$IDENTITY" | head -n1)"
[[ -n "$VERSION_NAME" && "$VERSION_CODE" =~ ^[1-9][0-9]*$ ]] || fail "invalid version.properties"
grep -Eq '^[[:space:]]*compileSdk[[:space:]]+36[[:space:]]*$' "$ROOT/app/build.gradle" || fail "compileSdk must be 36"
grep -Eq '^[[:space:]]*targetSdk[[:space:]]+36[[:space:]]*$' "$ROOT/app/build.gradle" || fail "targetSdk must be 36"

keytool -list -keystore "$PLAY_KEYSTORE" \
  -storepass "$PLAY_STORE_PASSWORD" -alias "$PLAY_ALIAS" >/dev/null 2>&1 \
  || fail "Play upload keystore, password, or alias is invalid"

ANDROID_HOME="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-${GROWPLOT_ANDROID_SDK:-}}}"
if [[ -n "$ANDROID_HOME" ]]; then
  ANDROID_HOME="$(ANDROID_HOME="$ANDROID_HOME" "$ROOT/tools/ensure-android-sdk.sh")" || fail "Android SDK setup failed"
else
  ANDROID_HOME="$("$ROOT/tools/ensure-android-sdk.sh")" || fail "Android SDK setup failed"
fi
export ANDROID_HOME ANDROID_SDK_ROOT="$ANDROID_HOME"
export GRADLE_USER_HOME="${GRADLE_USER_HOME:-$ROOT/.gradle-cache}"

TEMP_JS="$(mktemp --suffix=.js)"
trap 'rm -f "$TEMP_JS"' EXIT
python3 - "$ROOT/app/src/main/assets/index.html" > "$TEMP_JS" <<'PY'
import re, sys
html = open(sys.argv[1], encoding='utf-8').read()
scripts = re.findall(r'<script(?:\s[^>]*)?>(.*?)</script\s*>', html, flags=re.S|re.I)
print('\n'.join(scripts))
PY
node --check "$TEMP_JS" || fail "inline JavaScript syntax check failed"

cd "$ROOT"
./gradlew --no-daemon --max-workers=1 -Pandroid.useAndroidX=true -PgrowplotAbi=arm64-v8a clean bundleRelease
AAB="$ROOT/app/build/outputs/bundle/release/app-release.aab"
[[ -s "$AAB" ]] || fail "Gradle completed without producing the release AAB"
jarsigner -verify -verbose "$AAB" >/dev/null || fail "AAB signature verification failed"

mkdir -p "$ROOT/release"
SAFE_VERSION="$(printf '%s' "$VERSION_NAME" | tr '[:upper:] ' '[:lower:]-' | tr -cd 'a-z0-9.-')"
AAB_OUT="$ROOT/release/v${SAFE_VERSION}-GrowPlot-play-arm64.aab"
cp "$AAB" "$AAB_OUT"
AAB_HASH="$(sha256sum "$AAB_OUT" | awk '{print $1}')"
CERT_INFO="$(keytool -printcert -jarfile "$AAB_OUT" 2>/dev/null)" || fail "cannot read AAB signer certificate"
CERT_SHA256="$(sed -n 's/^[[:space:]]*SHA256:[[:space:]]*//p' <<<"$CERT_INFO" | head -n1 | tr -d ':[:space:]' | tr '[:upper:]' '[:lower:]')"
[[ -n "$CERT_SHA256" ]] || fail "could not read AAB signer SHA-256"
if [[ -n "${GROWPLOT_EXPECTED_PLAY_UPLOAD_CERT_SHA256:-}" ]]; then
  EXPECTED="$(printf '%s' "$GROWPLOT_EXPECTED_PLAY_UPLOAD_CERT_SHA256" | tr -d ':[:space:]' | tr '[:upper:]' '[:lower:]')"
  [[ "$CERT_SHA256" == "$EXPECTED" ]] || fail "AAB signer does not match the registered Play upload certificate"
fi

MANIFEST="$ROOT/release/v${SAFE_VERSION}-play-aab-manifest.txt"
cat > "$MANIFEST" <<EOF
GrowPlot Play internal-testing candidate
Version: $VERSION_NAME
versionCode: $VERSION_CODE
Application ID: com.growplot.farm
compileSdk: 36
targetSdk: 36
ABI: arm64-v8a
AAB: $(basename "$AAB_OUT")
AAB SHA-256: $AAB_HASH
Play upload certificate SHA-256: $CERT_SHA256
Checks: inline JavaScript syntax, Gradle bundleRelease, AAB signature, upload key alias
Play Console upload: not performed by this script
EOF

printf 'Signed Play AAB candidate created:\n%s\n%s\n' "$AAB_OUT" "$MANIFEST"
printf 'AAB SHA-256: %s\nPlay upload certificate SHA-256: %s\n' "$AAB_HASH" "$CERT_SHA256"
