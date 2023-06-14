#!/usr/bin/env bash

set -e
SCRIPT_DIR=$(dirname $0)

USER_GRADLE_PROPERTIES=~/.gradle/gradle.properties

check_configured() {
    grep -q $1 ${USER_GRADLE_PROPERTIES} || echo "$1 not configured in ${USER_GRADLE_PROPERTIES}. $2"
}

check_configuration() {
    if [ ! -f ${USER_GRADLE_PROPERTIES} ]; then
        echo "${USER_GRADLE_PROPERTIES} does not exist"
        exit 1
    fi

    check_configured "sonatypeUsername" "This is the username you use to authenticate with sonatype nexus (e.g. otto-de)"
    check_configured "sonatypePassword" "This is the password you use to authenticate with sonatype nexus (ask Guido or one of the developers)"
    check_configured "signing.secretKeyRingFile" "This is the gpg secret key file, e.g. ~/.gnupg/secring.gpg. If this doesn't exist, generate a key: gpg --gen-key"
    check_configured "signing.keyId" "This is the id of your key (e.g. 72FE5380). Use gpg --list-keys to find yours"
    check_configured "signing.password" "This is the password you defined for your gpg key"
    # for GPG version >= 2.1: gpg --export-secret-keys >~/.gnupg/secring.gpg
    # gpg --send-keys --keyserver keyserver.ubuntu.com yourKeyId
}

check_configuration

set +e
grep 'def edison_version = ".*-SNAPSHOT"' "$SCRIPT_DIR/build.gradle"
SNAPSHOT=$?
set -e

if [[ $SNAPSHOT == 1 ]]; then
  echo "INFO: This is not a SNAPSHOT, I'll release after upload."
else
  echo "INFO: This is a SNAPSHOT release."
fi

"${SCRIPT_DIR}"/gradlew clean
"${SCRIPT_DIR}"/gradlew check
"${SCRIPT_DIR}"/gradlew -Dorg.gradle.internal.http.socketTimeout=200000 -Dorg.gradle.internal.http.connectionTimeout=200000 build publish

if [[ $SNAPSHOT == 1 ]]; then
  echo "Closing and releasing into Sonatype OSS repository"
  "${SCRIPT_DIR}"/gradlew findSonatypeStagingRepository closeAndReleaseSonatypeStagingRepository
else
  echo "This is a snapshot release, closing in sonatype is not necessary"
fi
