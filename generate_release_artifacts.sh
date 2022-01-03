echo "Creating release build..."
# create the release apk and copy to current directory
./gradlew assembleRelease
cp ./app/build/outputs/apk/release/app-release.apk ./
echo "Created release build."

#create the debug apk and copy to current directory
echo "Creating debug build..."
./gradlew assembleDebug
cp ./app/build/outputs/apk/debug/app-debug.apk ./
echo "Created debug build."
echo "Done creating release artifacts."
