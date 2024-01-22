# Function to display error message and exit
error_exit() {
    echo "$1" >&2
    exit 1
}

# Check if gh is installed
if ! command -v gh &> /dev/null; then
  error_exit "gh is not installed, make sure you run 'make init' (see README.md)."
fi

PACKAGE_NAME=$(grep 'applicationId' app/build.gradle | awk -F\" {'print $2'})
PACKAGE_VERSION=$(grep 'versionName' app/build.gradle | awk -F\" {'print $2'})
REPO=sentry-demos/android

# Build the release bundle
echo "Building the release bundle..."
./gradlew assemble


echo "Releasing to Github..."
gh release create $PACKAGE_VERSION app/build/outputs/apk/debug/app-debug.apk app/build/outputs/apk/release/app-release.apk || error_exit "Failed to create GitHub release."
#gh release create $TAG $ZIP_PATH -t "$TITLE" -n "$NOTES" || error_exit "Failed to create GitHub release."

echo "Release created successfully with version $PACKAGE_VERSION!"


