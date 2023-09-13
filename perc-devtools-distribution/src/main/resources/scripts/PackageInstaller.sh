#!/bin/bash -bm
source ./DevOptions.sh
echo "Using Java version:"
java -version
echo Using JAVA_OPTS=$JAVA_OPTS
java $JAVA_OPTS -jar lib/perc-package-installer-@project.version@.jar "$@"
