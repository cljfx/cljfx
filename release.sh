#!/usr/bin/env bash
set -euo pipefail
VERSION=$1
clj -A:build -m version "$VERSION"
git commit -am "Release $1"
git tag "$1"
git push
git push origin "$1"
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