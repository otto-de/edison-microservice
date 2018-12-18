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

${SCRIPT_DIR}/../gradlew clean build uploadArchives closeAndReleaseRepository
