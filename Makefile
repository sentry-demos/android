SENTRY_ORG=testorg-az
SENTRY_PROJECT=paranoid-android
VERSION=`sentry-cli releases propose-version`

all: gradle_build setup_release

gradle_build:
	./gradlew build

upload_debug_files:
	sentry-cli upload-dif -o $(SENTRY_ORG) -p $(SENTRY_PROJECT) app/build/intermediates/cmake/ --include-sources
	sentry-cli upload-dif -o $(SENTRY_ORG) -p $(SENTRY_PROJECT) app/build/intermediates/stripped_native_libs --include-sources
	sentry-cli upload-dif -o $(SENTRY_ORG) -p $(SENTRY_PROJECT) app/build/intermediates/merged_native_libs/ --include-sources

setup_release: create_release associate_commits

create_release:
	sentry-cli releases -o $(SENTRY_ORG) new -p $(SENTRY_PROJECT) $(VERSION)

associate_commits:
	sentry-cli releases -o $(SENTRY_ORG) -p $(SENTRY_PROJECT) set-commits --auto $(VERSION)
clean:
	gradlew clean build