#!/usr/bin/env bash
set -euo pipefail
if [ -z "$1" ]
then
    echo "No version supplied"
    exit 1
fi
VERSION=$1
clj -A:build -m version "$VERSION"
git commit -am "Release $VERSION"
git tag "$VERSION"
git push
git push origin "$VERSION"
clj -A:base:depstar cljfx-"$VERSION".jar
clj -A:jdk8:depstar cljfx-"$VERSION"-jdk8.jar
clj -A:jdk11:depstar cljfx-"$VERSION"-jdk11.jar
printf "Clojars Username: "
read -r username
stty -echo
printf "Clojars Password: "
read -r password
printf "\n"
stty echo
CLOJARS_USERNAME=${username} CLOJARS_PASSWORD=${password} clj -A:build -m deploy deploy cljfx-"$VERSION".jar cljfx-"$VERSION"-jdk8.jar cljfx-"$VERSION"-jdk11.jar
rm cljfx-"$VERSION".jar cljfx-"$VERSION"-jdk8.jar cljfx-"$VERSION"-jdk11.jar