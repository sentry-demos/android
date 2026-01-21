# Function to display error message and exit
error_exit() {
    echo "$1" >&2
    exit 1
}

# Check if gh is installed
if ! command -v gh &> /dev/null; then
  error_exit "gh is not installed, make sure you run 'make init' (see README.md)."
fi

# Check for required arguments
if [ "$#" -lt 3 ]; then
  error_exit "Usage: $0 <TAG_NAME> <BUILD_TYPE: release|debug> <OUTPUT_FILE_NAME>"
fi

# Potentially could fish out TAG_NAME from the app/build.gradle file
# but we're currently not using it in release.yml, instead relying on calendar versioning
# TAG_NAME=$(grep 'versionName' app/build.gradle | awk -F\" {'print $2'})

TAG_NAME=$1
BUILD_TYPE=$2
OUTPUT_FILE_NAME=$3

# Validate BUILD_TYPE
if [ "$BUILD_TYPE" != "release" ] && [ "$BUILD_TYPE" != "debug" ]; then
  error_exit "BUILD_TYPE must be either 'release' or 'debug'"
fi

releaseExists=$(gh release list | awk '{print $1}' | grep -x "$TAG_NAME")

# Build the bundle
# NOTE: in the future this will defined in *.env 
BUILD_ARTIFACT_PATH="app/build/outputs/apk/$BUILD_TYPE/app-$BUILD_TYPE.apk"
# NOTE: in the future this will be in build.sh (and BUILD_TYPE defined in *.env)
echo "Building the $BUILD_TYPE bundle..."
if [ "$BUILD_TYPE" = "release" ]; then
  ./gradlew assembleRelease || error_exit "Gradle build failed."
else
  ./gradlew assembleDebug || error_exit "Gradle build failed."
fi

# Check if build artifact exists
if [ ! -f "$BUILD_ARTIFACT_PATH" ]; then
  error_exit "$BUILD_ARTIFACT_PATH does not exist."
fi

# Rename build artifact to OUTPUT_FILE_NAME
cp "$BUILD_ARTIFACT_PATH" "$OUTPUT_FILE_NAME" || error_exit "Failed to rename build artifact."

if [ "$releaseExists" != "" ]; then
  echo "Release for $TAG_NAME already exists. Uploading artifact with --clobber..."
  gh release upload $TAG_NAME "$OUTPUT_FILE_NAME" --clobber || error_exit "Failed to upload artifact to existing release."
else
  echo "Releasing to Github..."
  gh release create $TAG_NAME "$OUTPUT_FILE_NAME" -t "$TAG_NAME" --generate-notes --latest=false || error_exit "Failed to create GitHub release."
fi

echo "Release process completed successfully for tag $TAG_NAME!"


