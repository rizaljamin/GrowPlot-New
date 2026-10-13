#!/usr/bin/env bash
# GrowPlot Android toolchain preflight. Never prints passwords or API keys.
set -Eeuo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
fail() { printf 'FAIL: %s\n' "$*" >&2; exit 1; }
command -v java >/dev/null || fail "Java is missing"
command -v javac >/dev/null || fail "JDK compiler is missing"
command -v node >/dev/null || fail "Node.js is missing"
command -v python3 >/dev/null || fail "Python is missing"
command -v unzip >/dev/null || fail "unzip is missing"
java -version 2>&1 | head -n 1
javac -version
JAVA_MAJOR="$(java -version 2>&1 | head -n 1 | grep -oE '[0-9]+\.[0-9]+' | head -n 1 | cut -d. -f1)"
[[ "$JAVA_MAJOR" == 17 ]] || fail "Java 17 required; found ${JAVA_MAJOR:-unknown}"
SDK="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-$ROOT/.android-sdk}}"
[[ -f "$SDK/platforms/android-35/android.jar" ]] || fail "Android API 35 missing from $SDK"
for binary in aapt apksigner zipalign; do
  [[ -x "$SDK/build-tools/35.0.0/$binary" ]] || fail "Android Build Tools 35.0.0 missing $binary"
done
[[ -f "$ROOT/gradlew" ]] || fail "Gradle wrapper missing"
printf 'PASS: JDK 17, Android SDK 35, Build Tools 35.0.0, Gradle wrapper\n'
