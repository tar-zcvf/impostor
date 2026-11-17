#!/usr/bin/env bash
# Build the Masked release APK and AAB, then run the JVM test suite.
# Usage: ./scripts/build_release.sh
set -euo pipefail

cd "$(dirname "$0")/.."
ROOT="$(pwd)"

export JAVA_HOME="${JAVA_HOME:-$HOME/dev-tools/jdk-17}"
export ANDROID_HOME="${ANDROID_HOME:-$HOME/dev-tools/android-sdk}"
export PATH="$JAVA_HOME/bin:$PATH"

GRADLE="$HOME/dev-tools/gradle-8.13/bin/gradle"

echo "==> JAVA_HOME=$JAVA_HOME"
echo "==> ANDROID_HOME=$ANDROID_HOME"
echo "==> Using Gradle: $GRADLE"

# 1. Compile + unit/Robolectric tests (fast feedback, runs on the JVM).
echo ""
echo "==> [1/4] Unit + Robolectric tests"
"$GRADLE" -p "$ROOT" --console=plain :app:testDebugUnitTest \
    --stacktrace --warning-mode=summary

# 2. Build signed release APK.
echo ""
echo "==> [2/4] Signed release APK"
"$GRADLE" -p "$ROOT" --console=plain :app:assembleRelease \
    --stacktrace --warning-mode=summary

# 3. Build signed release App Bundle (Play Store artifact).
echo ""
echo "==> [3/4] Signed release AAB"
"$GRADLE" -p "$ROOT" --console=plain :app:bundleRelease \
    --stacktrace --warning-mode=summary

# 4. Build the instrumented (debug) test APK so it ships in the bundle.
echo ""
echo "==> [4/4] Instrumented test APK (debug)"
"$GRADLE" -p "$ROOT" --console=plain :app:assembleDebugAndroidTest \
    --stacktrace --warning-mode=summary

echo ""
echo "==> Locating build artifacts"
APK="$(find "$ROOT/app/build/outputs/apk/release" -name '*.apk' 2>/dev/null | head -n1)"
AAB="$(find "$ROOT/app/build/outputs/bundle/release" -name '*.aab' 2>/dev/null | head -n1)"
TEST_APK="$(find "$ROOT/app/build/outputs/apk/androidTest/debug" -name '*.apk' 2>/dev/null | head -n1)"

echo "Release APK : $APK"
echo "Release AAB : $AAB"
echo "Test APK    : $TEST_APK"

if [[ -z "$APK" || -z "$AAB" ]]; then
  echo "ERROR: expected artifact missing" >&2
  exit 1
fi

echo ""
echo "==> Verifying signatures"
"$JAVA_HOME/bin/apksigner" verify --print-certs "$APK" || true
echo ""
echo "DONE. Artifacts under $ROOT/app/build/outputs"
