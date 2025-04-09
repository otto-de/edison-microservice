#!/usr/bin/env bash

set -e
SCRIPT_DIR=$(dirname $0)

USER_JRELEASER_PROPERTIES=~/.jreleaser/config.toml

check_configured() {
    grep -q $1 ${USER_JRELEASER_PROPERTIES} || ( echo "$1 not configured in ${USER_JRELEASER_PROPERTIES}. $2" && exit 1 )
}

check_configuration() {
    if [ ! -f ${USER_JRELEASER_PROPERTIES} ]; then
        echo "${USER_JRELEASER_PROPERTIES} does not exist. Edison Microservice uses JReleaser to release packages. Pleaser create the file and add the following variables:"
        echo "JRELEASER_GPG_PASSPHRASE=\"your_gpg_passphrase\""
        echo "JRELEASER_GPG_PUBLIC_KEY=\"your_gpg_public_key\""
        echo "JRELEASER_GPG_SECRET_KEY=\"your_gpg_secret_key\""
        echo "JRELEASER_MAVENCENTRAL_USERNAME=\"your_maven_central_username\""
        echo "JRELEASER_MAVENCENTRAL_PASSWORD=\"your_maven_central_password\""
        echo "JRELEASER_GITHUB_TOKEN=\"your_github_token\""
        exit 1
    fi

    check_configured "JRELEASER_GPG_PASSPHRASE" "This is the GPG passphrase for your GPG key"
    check_configured "JRELEASER_GPG_PUBLIC_KEY" "This is the GPG public key to sign the release. Use 'gpg --export ***KEYID*** | base64' to export the public key"
    check_configured "JRELEASER_GPG_SECRET_KEY" "This is the GPG secret key to sign the release. Use 'gpg --export-secret-keys ***KEYID*** | base64' to export the private key"
    check_configured "JRELEASER_MAVENCENTRAL_USERNAME" "This is the Maven Central username to release packages"
    check_configured "JRELEASER_MAVENCENTRAL_PASSWORD" "This is the Maven Central password to release packages"
    check_configured "JRELEASER_GITHUB_TOKEN" "This it the Github Token to release packages and release information to GitHub"
    # for GPG version >= 2.1: gpg --export-secret-keys >~/.gnupg/secring.gpg
    # gpg --send-keys --keyserver keys.openpgp.org yourKeyId
}

check_configuration

set +e
grep 'def edison_version = ".*-SNAPSHOT"' "$SCRIPT_DIR/build.gradle"
SNAPSHOT=$?
set -e

if [[ $SNAPSHOT == 1 ]]; then
  echo "INFO: This is not a SNAPSHOT, I'll release to Maven Central during upload."
else
  echo "INFO: This is a SNAPSHOT release. Packages will be released to GitHub packages only."
fi

"${SCRIPT_DIR}"/gradlew clean jreleaserConfig check
"${SCRIPT_DIR}"/gradlew publish
"${SCRIPT_DIR}"/gradlew jreleaserFullRelease
#"${SCRIPT_DIR}"/gradlew -Dorg.gradle.internal.http.socketTimeout=200000 -Dorg.gradle.internal.http.connectionTimeout=200000 build publish

#if [[ $SNAPSHOT == 1 ]]; then
#  echo "Closing and releasing into Sonatype OSS repository"
#  "${SCRIPT_DIR}"/gradlew findSonatypeStagingRepository closeAndReleaseSonatypeStagingRepository
#else
#  echo "This is a snapshot release, closing in sonatype is not necessary"
#fi
