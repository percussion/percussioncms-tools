#!/bin/bash -bm
source DevOptions.sh
echo "Using Java version:"
java -version
java -jar lib/perc-package-builder-@project.version@.jar "$@"
