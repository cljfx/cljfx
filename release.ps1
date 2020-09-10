param ([Parameter(Mandatory)]$version)

function invoke {
    $exe, $argsForExe = $Args
    $ErrorActionPreference = 'Continue'
    try { & $exe $argsForExe } catch { Throw }
    if ($LASTEXITCODE) { Throw "$exe indicated failure (exit code $LASTEXITCODE; full command: $Args)." }
}

clj -A:build -m version $version
invoke git commit -am "Release $version"
invoke git tag $version
invoke git push
invoke git push origin $version
clj -A:base:depstar cljfx-"$version".jar
clj -A:jdk8:depstar cljfx-"$version"-jdk8.jar
clj -A:jdk11:depstar cljfx-"$version"-jdk11.jar
clj -A:build -m deploy (Read-Host -Prompt "Username") (Read-Host -Prompt "Token" -AsSecureString | ConvertFrom-SecureString -AsPlainText) cljfx-"$version".jar cljfx-"$version"-jdk8.jar cljfx-"$version"-jdk11.jar
rm cljfx-"$version".jar cljfx-"$version"-jdk8.jar cljfx-"$version"-jdk11.jar