@echo off
cd /d "%~dp0"
if not exist target\participant3-1.0-SNAPSHOT-demo.jar (
    echo Build the demo first: mvn -f participant3\pom.xml package
    pause
    exit /b 1
)
java --enable-native-access=ALL-UNNAMED -jar target\participant3-1.0-SNAPSHOT-demo.jar %*
