@echo off
echo Java Version:
java -version
IF "%1"=="" (
  ECHO Invalid argument: %1
  ECHO.
  ECHO Usage:  %~n0  command
  ECHO.
  ECHO Where:  command may be prod, dev
  ECHO Where:  prod is without debug port enabled suitable for production and dev is with debug port enabled suitable for developers
  ECHO "Prod command example: Workbench.cmd prod"
  ECHO "Dev command example: Workbench.cmd dev <port_number>"
  GOTO:EOF
)
IF "%1"=="dev" (
	if "%2"=="" (
		ECHO please provide valid debug port number
		goto end
	)
)
if "%1"=="prod" (goto PROD) else (goto DEV)
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