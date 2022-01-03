PACKAGE_NAME=com.example.vu.android # minor todo: we could grab this from build.gradle instead of hardcoding it
PACKAGE_VERSION=$(adb shell dumpsys package $PACKAGE_NAME | grep versionName | awk -F "=" '/1/ {print $2}')
REPO=sentry-demos/android

while true; do
  read -p "Do you wish to create Github Release $PACKAGE_VERSION for $REPO and upload the generated release and debug artifacts? Answer y/n: " yn
    case $yn in
        [Yy]* ) make install; break;;
        [Nn]* ) exit;;
        * ) echo "Please answer y or n.";;
    esac
done

echo "Releasing to Github..."
gh release create $PACKAGE_VERSION app-debug.apk app-release.apk
