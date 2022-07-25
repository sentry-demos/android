SENTRY_ORG=testorg-az
SENTRY_PROJECT=android
RELEASE=`sentry-cli releases propose-version`

all: gradle_build setup_release associate_commits

gradle_build:
	./gradlew build

clean:
	./gradlew clean build

setup_release: create_release associate_commits

create_release:
	sentry-cli releases -o $(SENTRY_ORG) new -p $(SENTRY_PROJECT) $(RELEASE)

associate_commits:
	sentry-cli releases -o $(SENTRY_ORG) -p $(SENTRY_PROJECT) set-commits --auto $(RELEASE)
