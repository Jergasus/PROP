#!/bin/bash
set -e

mkdir -p out
find src -name "*.java" > /tmp/hidato_sources.txt
javac -cp "../lib/*" -d out @/tmp/hidato_sources.txt
java  -cp "out:../lib/*" Main
