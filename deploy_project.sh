# Function to display error message and exit
error_exit() {
    echo "$1" >&2
    exit 1
}

# Check if gh is installed
if ! command -v gh &> /dev/null; then
  error_exit "gh is not installed, make sure you run 'make init' (see README.md)."
fi

# When this script is called from release workflow, the version is passed as an argument, otherwise it will be read from the app/build.gradle file
PACKAGE_VERSION=$1
if [ -z "$1" ]; then
  PACKAGE_VERSION=$(grep 'versionName' app/build.gradle | awk -F\" {'print $2'})
fi
PACKAGE_NAME=$(grep 'applicationId' app/build.gradle | awk -F\" {'print $2'})
REPO=sentry-demos/android

# Check if current version was already released
currentVersion=$(gh release list | awk '{print $1}' | grep -x "$PACKAGE_VERSION")
if [ "$currentVersion" != "" ]; then
  error_exit "Version $PACKAGE_VERSION was already released."
fi

# Build the release bundle
echo "Building the release bundle..."
./gradlew assemble


echo "Releasing to Github..."
gh release create $PACKAGE_VERSION app/build/outputs/apk/debug/app-debug.apk app/build/outputs/apk/release/app-release.apk -t "$PACKAGE_VERSION" --generate-notes || error_exit "Failed to create GitHub release."

echo "Release created successfully with version $PACKAGE_VERSION!"


