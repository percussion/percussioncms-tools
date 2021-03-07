#!/usr/bin/env bash

echo "Using Java version:"
java -version
source DevOptions.sh
java -jar lib/perc-package-builder-@project.version@.jar $JAVA_OPTS "$@"
