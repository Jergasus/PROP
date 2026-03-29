@echo off
if not exist out mkdir out

dir /s /b src\*.java > %TEMP%\hidato_sources.txt
javac -cp "..\lib\*" -d out @%TEMP%\hidato_sources.txt
if errorlevel 1 (
    echo Compilation failed.
    exit /b 1
)

java -cp "out;..\lib\*" Main
