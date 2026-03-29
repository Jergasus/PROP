#!/bin/bash
# Compile and run the Hidato Solver & Validator demo.
# Run from the project root:  ./compilar.sh
set -e

mkdir -p out
find FONTS/src -name "*.java" > hidato_sources.txt
find EXE -name "*.java" >> hidato_sources.txt
javac -cp "lib/*" -d out @hidato_sources.txt
java  -cp "out;lib/*" test.drivers.SolverValidatorDriver
