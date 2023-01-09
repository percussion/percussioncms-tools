#!/bin/bash -bm
source ./DevOptions.sh
echo "NOTE:  Content Explorer Requires and Oracle 1.8 JRE - OpenJDK 8 does not include JavaFX"
echo "Using Java version:"
java -version
echo Using JAVA_OPTS=$JAVA_OPTS
java $JAVA_OPTS -jar lib/DesktopContentExplorer-@project.version@.jar "$@"
