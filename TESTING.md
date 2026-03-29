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

### 2. Run JUnit test suite (28 tests)

```bash
java -cp "out:lib/*" org.junit.runner.JUnitCore test.HidatoTestSuite
```

### 3. Interactive driver

```bash
java -cp "out:lib/*" Main
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

### 2. Run JUnit test suite (28 tests)

```powershell
java -cp "out;lib/*" org.junit.runner.JUnitCore test.HidatoTestSuite
```

### 3. Interactive driver

```powershell
java -cp "out;lib/*" Main
```

### 4. Quick one-liner (compile + test)

```powershell
Get-ChildItem -Recurse -Filter "*.java" src | Select-Object -ExpandProperty FullName | Out-File sources.txt -Encoding utf8; javac -cp "lib/*" -d out "@sources.txt"; java -cp "out;lib/*" org.junit.runner.JUnitCore test.HidatoTestSuite
```

---

## What the tests cover

| Test class | Tests | What it validates |
|---|---|---|
| `ValidatorTest` | 14 | Valid/invalid solutions, partial validation, edge cases |
| `SolverTest` | 14 | Backtracking solver, unique/multiple solutions, all geometries |

Expected output: `OK (28 tests)`
