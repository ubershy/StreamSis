CALL mvn clean package assembly:single
@ECHO off
ECHO.
IF %ERRORLEVEL% == 0 (
	ECHO Yay. Successfully finished assembling StreamSis.
	ECHO You can check 'target' subdirectory now.
)
IF NOT %ERRORLEVEL% == 0 (
	ECHO StreamSis assembly fail. See log.
)
ECHO.
PAUSE
