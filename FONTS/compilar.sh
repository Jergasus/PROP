#!/bin/bash

echo "Iniciant la compilacio del projecte Hidato..."

# 1. Creem el directori on aniran els fitxers binaris (.class)
mkdir -p bin

# 2. Creem una llista temporal amb tots els fitxers .java de FONTS i EXE
find src ../EXE -name "*.java" > sources.txt

# 3. Compilem usant l'arxiu temporal i incloent les llibreries de JUnit al Classpath
if javac -d bin -cp "lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar:src:../EXE" @sources.txt; then
    echo "Compilacio finalitzada amb exit! Els binaris son a la carpeta FONTS/bin."
else
    echo "ERROR: La compilacio ha fallat. Revisa el teu codi Java."
fi

# 4. Esborrem l'arxiu temporal per mantenir el directori net
rm -f sources.txt