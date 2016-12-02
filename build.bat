CALL mvn clean package appassembler:assemble
@ECHO off
ECHO.
IF %ERRORLEVEL% == 0 (
	ECHO Yay. Successfully finished assembling StreamSis.
	ECHO You can check the result in 'target/appassembler' subdirectory now.
)
IF NOT %ERRORLEVEL% == 0 (
	ECHO StreamSis assembly fail. See log.
)
ECHO.
PAUSE
