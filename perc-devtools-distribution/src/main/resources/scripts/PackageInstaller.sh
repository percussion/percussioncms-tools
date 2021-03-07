#!/usr/bin/env bash

echo "Using Java version:"
java -version
source DevOptions.sh
java $JAVA_OPTS -jar lib/perc-package-installer-@project.version@.jar "$@"