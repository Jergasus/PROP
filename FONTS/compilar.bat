@echo off
REM Compila i executa el projecte Hidato
REM S'ha d'executar SEMPRE des del directori FONTS

if not exist ..\EXE\bin mkdir ..\EXE\bin

dir /s /b src\*.java > %TEMP%\hidato_sources.txt
dir /s /b ..\EXE\*.java >> %TEMP%\hidato_sources.txt

javac -cp "lib\*" -d ..\EXE\bin @%TEMP%\hidato_sources.txt

if errorlevel 1 (
    echo [ERROR] La compilacio ha fallat.
    exit /b 1
)

echo --------------------------------------------------
echo Executant Driver...
echo --------------------------------------------------

java -cp "..\EXE\bin;lib\*" SolverValidatorDriver