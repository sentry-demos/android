# Disables the Android Framework profiling rate limiter on a connected device.
#
# On API 35+ the Sentry SDK uses the platform ProfilingManager (Perfetto) backend,
# and the framework rate limits profiling requests -- they are "not guaranteed to be
# fulfilled". That makes profiles sparse or missing when testing locally or generating
# automated demo data. See:
# https://docs.sentry.io/platforms/android/profiling/#limitations
#
# Usage:
#   ./setup_profiling_device.sh                # uses the only connected device
#   ./setup_profiling_device.sh emulator-5554  # targets a specific device
#
# Re-run after every cold boot, "Wipe Data", or newly created AVD -- this is
# per-device state, not per-project.

error_exit() {
    echo "$1" >&2
    exit 1
}

if ! command -v adb &> /dev/null; then
  error_exit "adb is not installed or not on PATH (expected in \$ANDROID_HOME/platform-tools)."
fi

# Target a specific device if one was passed, otherwise let adb pick the only one.
DEVICE=$1
if [ -n "$DEVICE" ]; then
  ADB="adb -s $DEVICE"
else
  ADB="adb"
  DEVICE_COUNT=$(adb devices | grep -cw "device$")
  if [ "$DEVICE_COUNT" -eq 0 ]; then
    error_exit "No device connected. Start an emulator or plug in a device, then retry."
  fi
  if [ "$DEVICE_COUNT" -gt 1 ]; then
    adb devices
    error_exit "More than one device connected. Pass a serial, e.g. ./setup_profiling_device.sh emulator-5554"
  fi
fi

$ADB wait-for-device || error_exit "Timed out waiting for the device."

API_LEVEL=$($ADB shell getprop ro.build.version.sdk | tr -d '\r')
echo "Device API level: $API_LEVEL"

if [ "$API_LEVEL" -lt 35 ]; then
  echo "API < 35, so the legacy profiler is used and the framework rate limiter does not apply."
  echo "Nothing to do."
  exit 0
fi

# The platform config updater can resync the namespace and silently revert the flag
# mid-session, so pin it first. Not fatal if unsupported on this image.
echo "Disabling DeviceConfig sync..."
$ADB shell device_config set_sync_disabled_for_tests persistent \
  || echo "WARNING: could not disable DeviceConfig sync; the flag below may be reverted mid-session."

echo "Disabling the profiling rate limiter..."
$ADB shell device_config put profiling_testing rate_limiter.disabled true \
  || error_exit "Failed to set the flag. Some locked-down retail devices deny this; try an emulator."

RESULT=$($ADB shell device_config get profiling_testing rate_limiter.disabled | tr -d '\r')
if [ "$RESULT" != "true" ]; then
  error_exit "Verification failed: rate_limiter.disabled is '$RESULT', expected 'true'."
fi

echo "Done. Profiling rate limiter disabled (rate_limiter.disabled=$RESULT)."
