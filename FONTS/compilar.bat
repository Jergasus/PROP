@echo off
echo Iniciant la compilacio

:: Creem el directori pels binaris (.class)
if not exist "bin" mkdir bin

:: Creem llista temporal amb tots els .java de FONTS/src i EXE
dir /s /B src\*.java ..\EXE\*.java > sources.txt

:: Compilem amb les llibreries JUnit
javac -d bin -cp "lib\junit-4.13.2.jar;lib\hamcrest-core-1.3.jar" @sources.txt

if %errorlevel% neq 0 (
    echo ERROR: La compilacio ha fallat.
    del sources.txt
    exit /b 1
)

:: Esborrem arxiu temporal
del sources.txt

echo Compilacio finalitzada correctament
echo.
echo Per executar l'aplicacio:
echo   java -cp "bin;lib\*" Main
echo.
echo Per executar els tests:
echo   java -cp "bin;lib\junit-4.13.2.jar;lib\hamcrest-core-1.3.jar" org.junit.runner.JUnitCore test.HidatoTestSuite
