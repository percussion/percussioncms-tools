@echo off
echo "NOTE:  Content Explorer requires an Oracle 1.8 JRE - or an OpenJDK JRE 1.8 with an openjfx overlay"
echo "Using Java version:"
java -version
call DevOptions.cmd
echo Using JAVA_OPTS=%JAVA_OPTS%
java %JAVA_OPTS% -jar lib/perc-content-explorer-@project.version@.jar
