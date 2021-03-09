@echo off
echo "NOTE:  Content Explorer requires an Oracle 1.8 JRE - OpenJDK 8 does not include JavaFX"
echo "Using Java version:"
java -version
call DevOptions.cmd
java %JAVA_OPTS% -jar lib/perc-content-explorer-@project.version@.jar