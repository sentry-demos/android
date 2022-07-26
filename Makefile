SENTRY_ORG=testorg-az
SENTRY_PROJECT=android-ben-demo
RELEASE=`sentry-cli releases propose-version`

all: gradle_build create_release associate_commits

gradle_build:
	./gradlew build

clean:
	./gradlew clean build

create_release:
	sentry-cli releases -o $(SENTRY_ORG) new -p $(SENTRY_PROJECT) $(RELEASE)

associate_commits:
	sentry-cli releases -o $(SENTRY_ORG) -p $(SENTRY_PROJECT) set-commits --auto $(RELEASE)
