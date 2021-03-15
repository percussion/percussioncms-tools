@echo off
echo Java Version:
java -version

ECHO "Prod command example: Workbench.cmd"
ECHO "Dev command example: Workbench.cmd dev <port_number>"

IF "%1"=="" (
  goto PROD
)

IF "%1"=="dev" (
	if "%2"=="" (
		ECHO please provide valid debug port number
		goto end
	) else (goto DEV)
)
goto end

:PROD
echo Starting without debug port enabled.
start workbench\eclipse.exe
goto end

:DEV
echo Starting with debug port enabled.
start workbench\eclipse.exe --launcher.stickyVmargs -vmargs -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=%2
goto end

:end