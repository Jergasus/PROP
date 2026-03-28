#!/bin/bash
# Compile and run the Hidato Solver & Validator demo.
# Run from the project root:  ./compilar.sh
set -e

mkdir -p out
find src -name "*.java" > /tmp/hidato_sources.txt
javac -cp "lib/*" -d out @/tmp/hidato_sources.txt
java  -cp "out:lib/*" test.drivers.SolverDriver
