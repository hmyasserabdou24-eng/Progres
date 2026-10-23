@rem Gradle startup script for Windows
@if "%DEBUG%" == "" @echo off
if exist "%DIRNAME%gradle\wrapper\gradle-wrapper.jar" (
    java -jar "%DIRNAME%gradle\wrapper\gradle-wrapper.jar" %*
) else (
    gradle %*
)
