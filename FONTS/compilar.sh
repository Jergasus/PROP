#!/bin/bash

# Compila i executa el projecte Hidato
# S'ha d'executar SEMPRE des del directori FONTS

mkdir -p ../EXE/bin [cite: 116, 117]

# Generem un fitxer temporal amb totes les rutes dels .java
find src -name "*.java" > hidato_sources.txt 
find ../EXE -name "*.java" >> hidato_sources.txt

# Detectem el sistema operatiu per al separador del Classpath
# Linux i Mac usen ":", Windows (Cygwin/Git Bash) usa ";"
if [[ "$OSTYPE" == "msys" || "$OSTYPE" == "cygwin" ]]; then
    SEP=";"
else
    SEP=":"
fi

# Compila usant la llista de fitxers i les llibreries JUnit 4 [cite: 41, 114]
javac -cp "lib/*" -d ../EXE/bin @hidato_sources.txt

# Comprovem si hi ha hagut errors de compilació [cite: 39, 40]
if [ $? -ne 0 ]; then
    echo "[ERROR] La compilacio ha fallat."
    rm hidato_sources.txt
    exit 1
fi

# Neteja de l'arxiu de text intermedi
rm hidato_sources.txt

echo "--------------------------------------------------"
echo "Executant Driver..."
echo "--------------------------------------------------"

# Executem el Driver que utilitza el CtrlDomini [cite: 76, 116]
# Ara fem servir la variable SEP per ser compatibles amb qualsevol sistema
java -cp "../EXE/bin${SEP}lib/*" SolverValidatorDriver