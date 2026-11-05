@echo off
cd /d "%~dp0"
if not exist desktop\target\thanos-demo.jar (
    echo Build the game first: mvn package
    exit /b 1
)
java --enable-native-access=ALL-UNNAMED -jar desktop\target\thanos-demo.jar %*
