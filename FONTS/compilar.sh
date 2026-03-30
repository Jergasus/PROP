#!/bin/bash

echo "Iniciant la compilacio"

# Creem directori per binaris (.class)
mkdir -p bin

# Creem llista temporal amb .java de FONTS i EXE
find src ../EXE -name "*.java" > sources.txt

# Compilem amb arxiu temporal incloent JUnit
if javac -d bin -cp "lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar:src:../EXE" @sources.txt; then
    echo "Compilacio finalitzada"
else
    echo "ERROR: La compilacio ha fallat."
fi

# Esborrem arxiu temporal 
rm -f sources.txt