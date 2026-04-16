# Delivery 1 — Testing Guide

## Prerequisites

- Java 17+ installed
- You are on the `delivery1` branch: `git checkout delivery1`

All commands must be run from inside the `FONTS/` directory.

---

## macOS / Linux (bash)

### 1. Compile

```bash
cd FONTS
find src -name "*.java" > /tmp/hidato_sources.txt && javac -cp "../lib/*" -d out @/tmp/hidato_sources.txt
```

### 2. Run JUnit test suite (69 tests)

```bash
java -cp "out:../lib/*" org.junit.runner.JUnitCore test.HidatoTestSuite
```

### 3. Interactive driver

```bash
java -cp "out:../lib/*" Main
```

### 4. Quick one-liner (compile + run driver)

```bash
./compilar.sh
```

---

## Windows (PowerShell)

### 1. Compile

```powershell
cd FONTS
Get-ChildItem -Recurse -Filter "*.java" src | Select-Object -ExpandProperty FullName | Out-File $env:TEMP\hidato_sources.txt -Encoding utf8
javac -cp "..\lib\*" -d out "@$env:TEMP\hidato_sources.txt"
```

### 2. Run JUnit test suite (69 tests)

```powershell
java -cp "out;..\lib\*" org.junit.runner.JUnitCore test.HidatoTestSuite
```

### 3. Interactive driver

```powershell
java -cp "out;..\lib\*" Main
```

### 4. Quick one-liner (compile + run driver)

```powershell
compilar.bat
```

---

## What the tests cover

| Test class | Tests | What it validates |
|---|---|---|
| `CellTest`      | 18 | Cell state, fixed/void flags, copy constructor, toString |
| `BoardTest`     | 19 | Grid dimensions, neighbors, adjacency, copy, connectivity |
| `ValidatorTest` | 14 | Valid/invalid solutions, partial validation, edge cases |
| `SolverTest`    | 18 | Backtracking solver, unique/multiple solutions, all geometries |

Expected output: `OK (69 tests)`
