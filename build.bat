CALL mvn clean package
@ECHO off
ECHO.
IF %ERRORLEVEL% == 0 (
	ECHO Yay. Successfully finished assembling StreamSis.
	ECHO You can check the result in 'target/assembled' subdirectory now.
)
IF NOT %ERRORLEVEL% == 0 (
	ECHO StreamSis assembly has failed. See the log.
)
ECHO.
PAUSE
