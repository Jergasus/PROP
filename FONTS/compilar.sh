#!/bin/bash

echo "Iniciant la compilacio"

# Creem directori per binaris (.class)
mkdir -p bin

# Creem llista temporal amb tots els .java de FONTS/src i EXE
find src ../EXE -name "*.java" > sources.txt

# Compilem amb les llibreries JUnit
if javac -d bin -cp "lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar" @sources.txt; then
    echo "Compilacio finalitzada correctament"
else
    echo "ERROR: La compilacio ha fallat."
    rm -f sources.txt
    exit 1
fi

# Esborrem arxiu temporal
rm -f sources.txt

echo ""
echo "Per executar l'aplicacio:"
echo "  java -cp \"bin:lib/*\" Main"
echo ""
echo "Per executar els tests (suite completa):"
echo "  java -cp \"bin:lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar\" org.junit.runner.JUnitCore HidatoTestSuite"
echo ""
echo "Per executar un test individual (ex. BoardTest):"
echo "  java -cp \"bin:lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar\" org.junit.runner.JUnitCore BoardTest"
