#!/usr/bin/env sh
set -eu
cd "$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
if [ ! -f desktop/target/thanos-demo.jar ]; then
    echo 'Build the game first: mvn package' >&2
    exit 1
fi
if [ "$(uname -s)" = Darwin ]; then
    exec java -XstartOnFirstThread --enable-native-access=ALL-UNNAMED -jar desktop/target/thanos-demo.jar "$@"
fi
exec java --enable-native-access=ALL-UNNAMED -jar desktop/target/thanos-demo.jar "$@"
