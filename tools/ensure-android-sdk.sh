#!/usr/bin/env bash
# Idempotently prepare the Android SDK required by GrowPlot's Gradle wrapper.
set -Eeuo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SDK_ROOT="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-${GROWPLOT_ANDROID_SDK:-$ROOT/.android-sdk}}}"
SDKMANAGER="$SDK_ROOT/cmdline-tools/latest/bin/sdkmanager"
CMDLINE_ZIP="commandlinetools-linux-15859902_latest.zip"
CMDLINE_SHA256="4e4c464f145a7512b57d088ac6c278c03c9eea610886b35a5e0804e74eedf583"

fail() { printf 'GrowPlot Android SDK setup stopped: %s\n' "$*" >&2; exit 1; }

if [[ ! -x "$SDKMANAGER" ]]; then
  command -v curl >/dev/null 2>&1 || fail "curl is required to install Android command-line tools"
  command -v unzip >/dev/null 2>&1 || fail "unzip is required to install Android command-line tools"
  command -v sha256sum >/dev/null 2>&1 || fail "sha256sum is required to verify Android command-line tools"
  [[ "$(uname -s)" == "Linux" ]] || fail "install Android command-line tools for this OS, or set ANDROID_HOME to an existing SDK"
  TMP_DIR="$(mktemp -d)"
  trap 'rm -rf "$TMP_DIR"' EXIT
  mkdir -p "$SDK_ROOT/cmdline-tools"
  curl --fail --location --retry 3 --silent --show-error \
    "https://dl.google.com/android/repository/$CMDLINE_ZIP" -o "$TMP_DIR/$CMDLINE_ZIP"
  printf '%s  %s\n' "$CMDLINE_SHA256" "$TMP_DIR/$CMDLINE_ZIP" | sha256sum --check --status \
    || fail "Android command-line tools checksum did not match Google's published checksum"
  unzip -q "$TMP_DIR/$CMDLINE_ZIP" -d "$TMP_DIR/unpacked"
  rm -rf "$SDK_ROOT/cmdline-tools/latest"
  mkdir -p "$SDK_ROOT/cmdline-tools/latest"
  cp -a "$TMP_DIR/unpacked/cmdline-tools/." "$SDK_ROOT/cmdline-tools/latest/"
  SDKMANAGER="$SDK_ROOT/cmdline-tools/latest/bin/sdkmanager"
fi

[[ -x "$SDKMANAGER" ]] || fail "sdkmanager was not found at $SDKMANAGER"
export ANDROID_HOME="$SDK_ROOT"
export ANDROID_SDK_ROOT="$SDK_ROOT"

if [[ ! -f "$SDK_ROOT/licenses/android-sdk-license" ]]; then
  set +o pipefail
  yes | "$SDKMANAGER" --sdk_root="$SDK_ROOT" --licenses >/dev/null
  set -o pipefail
fi

if [[ ! -f "$SDK_ROOT/platforms/android-35/android.jar" \
      || ! -x "$SDK_ROOT/build-tools/35.0.0/aapt" \
      || ! -x "$SDK_ROOT/build-tools/35.0.0/apksigner" \
      || ! -x "$SDK_ROOT/platform-tools/adb" ]]; then
  "$SDKMANAGER" --sdk_root="$SDK_ROOT" --install \
    'platforms;android-35' 'build-tools;35.0.0' 'platform-tools' >/dev/null
fi

[[ -f "$SDK_ROOT/platforms/android-35/android.jar" ]] || fail "Android API 35 platform is missing"
[[ -x "$SDK_ROOT/build-tools/35.0.0/aapt" ]] || fail "Android build-tools 35.0.0 are missing"
[[ -x "$SDK_ROOT/build-tools/35.0.0/apksigner" ]] || fail "Android apksigner is missing"

# Persist the SDK location for direct `./gradlew` calls in later project sessions.
python3 - "$ROOT/local.properties" "$SDK_ROOT" <<'PY'
from pathlib import Path
import sys
path = Path(sys.argv[1])
sdk = sys.argv[2].replace('\\', '\\\\').replace(':', '\\:').replace(' ', '\\ ')
lines = path.read_text().splitlines() if path.exists() else []
updated = []
seen = False
for line in lines:
    if line.startswith('sdk.dir='):
        if not seen:
            updated.append(f'sdk.dir={sdk}')
            seen = True
    else:
        updated.append(line)
if not seen:
    updated.append(f'sdk.dir={sdk}')
path.write_text('\n'.join(updated).rstrip() + '\n')
PY

printf '%s\n' "$SDK_ROOT"
