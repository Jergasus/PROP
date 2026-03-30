@echo off
echo Iniciant la compilacio del projecte Hidato...

:: 1. Creem el directori on aniran els fitxers binaris (.class)
if not exist "bin" mkdir bin

:: 2. Creem una llista temporal amb tots els fitxers .java de FONTS i EXE
dir /s /B src\*.java ..\EXE\*.java > sources.txt

:: 3. Compilem usant l'arxiu temporal i incloent les llibreries de JUnit al Classpath
javac -d bin -cp "lib\junit-4.13.2.jar;lib\hamcrest-core-1.3.jar;src;..\EXE" @sources.txt

:: 4. Esborrem l'arxiu temporal per mantenir el directori net
del sources.txt

echo Compilacio finalitzada amb exit! Els binaris son a la carpeta FONTS\bin.