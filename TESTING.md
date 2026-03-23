# Delivery 1 — Testing Guide

## Prerequisites

- Java 17+ installed
- You are on the `delivery1` branch: `git checkout delivery1`

---

## macOS / Linux (bash)

### 1. Compile

```bash
find src -name "*.java" > sources.txt && javac -cp "lib/*" -d out @sources.txt
```

### 2. Run JUnit test suite (40 tests)

```bash
java -cp "out:lib/*" org.junit.runner.JUnitCore test.HidatoTestSuite
```

### 3. Interactive drivers

```bash
java -cp out test.drivers.SolverDriver
java -cp out test.drivers.GeneratorDriver
```

### 4. Quick one-liner (compile + test)

```bash
find src -name "*.java" > sources.txt && javac -cp "lib/*" -d out @sources.txt && java -cp "out:lib/*" org.junit.runner.JUnitCore test.HidatoTestSuite
```

---

## Windows (PowerShell)

### 1. Compile

```powershell
Get-ChildItem -Recurse -Filter "*.java" src | Select-Object -ExpandProperty FullName | Out-File sources.txt -Encoding utf8
javac -cp "lib/*" -d out "@sources.txt"
```

### 2. Run JUnit test suite (40 tests)

```powershell
java -cp "out;lib/*" org.junit.runner.JUnitCore test.HidatoTestSuite
```

### 3. Interactive drivers

```powershell
java -cp "out;lib/*" test.drivers.SolverDriver
java -cp "out;lib/*" test.drivers.GeneratorDriver
```

### 4. Quick one-liner (compile + test)

```powershell
Get-ChildItem -Recurse -Filter "*.java" src | Select-Object -ExpandProperty FullName | Out-File sources.txt -Encoding utf8; javac -cp "lib/*" -d out "@sources.txt"; java -cp "out;lib/*" org.junit.runner.JUnitCore test.HidatoTestSuite
```

---

## What the tests cover

| Test class | Tests | What it validates |
|---|---|---|
| `ValidatorTest` | 16 | Valid/invalid solutions, partial validation, edge cases |
| `SolverTest` | 14 | Backtracking solver, unique/multiple solutions, all geometries |
| `GeneratorTest` | 10 | Puzzle generation, unique solutions, difficulty levels |

Expected output: `OK (40 tests)`
