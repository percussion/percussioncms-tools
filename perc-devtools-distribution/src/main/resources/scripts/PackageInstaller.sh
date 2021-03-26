#!/bin/bash -bm
source DevOptions.sh
echo "Using Java version:"
java -version
java $JAVA_OPTS -jar lib/perc-package-installer-@project.version@.jar "$@"