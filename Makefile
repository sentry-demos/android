# Must have `sentry-cli` installed globally
# Following env variables have to be set or passed in:
#  SENTRY_AUTH_TOKEN
#  or have it set as environment variable

SENTRY_ORG=testorg-az
SENTRY_PROJECT=angular
VERSION=`sentry-cli releases propose-version`

setup_release: create_release associate_commits

create_release:
	sentry-cli releases -o $(SENTRY_ORG) new -p $(SENTRY_PROJECT) $(VERSION)

associate_commits:
	sentry-cli releases -o $(SENTRY_ORG) -p $(SENTRY_PROJECT) set-commits --auto $(VERSION)

