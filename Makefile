SENTRY_ORG=testorg-az
SENTRY_PROJECT=paranoid-android
RELEASE=`sentry-cli releases propose-version`

all: gradle_build setup_release

gradle_build:
	./gradlew build

setup_release: create_release associate_commits

create_release:
	sentry-cli releases -o $(SENTRY_ORG) new -p $(SENTRY_PROJECT) $(RELEASE)

associate_commits:
	sentry-cli releases -o $(SENTRY_ORG) -p $(SENTRY_PROJECT) set-commits --auto $(RELEASE)

clean:
	./gradlew clean build