SENTRY_ORG=testorg-az
SENTRY_PROJECT=android
VERSION=`sentry-cli releases propose-version`

all: gradle_build setup_release

gradle_build:
	./gradlew build

setup_release: create_release associate_commits

create_release:
	sentry-cli releases -o $(SENTRY_ORG) new -p $(SENTRY_PROJECT) $(VERSION)

associate_commits:
	sentry-cli releases -o $(SENTRY_ORG) -p $(SENTRY_PROJECT) set-commits --auto $(VERSION)

clean:
	./gradlew clean build