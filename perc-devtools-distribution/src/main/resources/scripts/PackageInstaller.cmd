@echo off
echo Java Version:
java -version
call DevOptions.cmd
java %JAVA_OPTS% -jar lib/perc-package-installer-@project.version@.jar