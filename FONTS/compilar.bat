@echo off
echo Iniciant la compilacio

:: Creem el directori pels binaris (.class)
if not exist "bin" mkdir bin

:: Creem llista temporal amb .java de FONTS i EXE
dir /s /B src\*.java ..\EXE\*.java > sources.txt

:: Compilem amb temporal incloent JUnit
javac -d bin -cp "lib\junit-4.13.2.jar;lib\hamcrest-core-1.3.jar;src;..\EXE" @sources.txt

:: Esborrem arxiu temporal
del sources.txt

echo Compilacio finalitzada

:: Executem el driver interactiu
java -cp "bin;lib\junit-4.13.2.jar;lib\hamcrest-core-1.3.jar;..\EXE" DriverHidato.SolverValidatorDriver