#!/usr/bin/env bash

echo "NOTE:  Content Explorer Requires and Oracle 1.8 JRE - OpenJDK 8 does not include JavaFX"
echo "Using Java version:"
java -version
source DevOptions.sh
java $JAVA_OPTS -jar lib/perc-content-explorer-@project.version@.jar "$@"