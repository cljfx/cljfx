#!/usr/bin/env bash
clj -Spom
clj -A:depstar
printf "Clojars Username: "
read username
stty -echo
printf "Clojars Password: "
read password
printf "\n"
stty echo
CLOJARS_USERNAME=${username} CLOJARS_PASSWORD=${password} clj -A:deploy
rm cljfx.jar
