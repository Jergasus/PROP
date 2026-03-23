# Delivery 1 — Testing Guide

## Prerequisites

- Java 17+ installed
- You are on the `delivery1` branch: `git checkout delivery1`

## 1. Compile everything

```bash
find src -name "*.java" > sources.txt && javac -cp "lib/*" -d out @sources.txt
```

## 2. Run JUnit test suite (40 tests)

```bash
java -cp "out:lib/*" org.junit.runner.JUnitCore test.HidatoTestSuite
```

Expected output: `OK (40 tests)`

### What the tests cover

| Test class | Tests | What it validates |
|---|---|---|
| `ValidatorTest` | 16 | Valid/invalid solutions, partial validation, edge cases |
| `SolverTest` | 14 | Backtracking solver, unique/multiple solutions, all geometries |
| `GeneratorTest` | 10 | Puzzle generation, unique solutions, difficulty levels |

## 3. Interactive drivers

### Solver Driver

```bash
java -cp out test.drivers.SolverDriver
```

Lets you manually input a board and see the solver find a solution step by step.

### Generator Driver

```bash
java -cp out test.drivers.GeneratorDriver
```

Lets you configure shape, size, adjacency, and difficulty, then generates a puzzle and verifies it.

## 4. Quick one-liner (compile + test)

```bash
find src -name "*.java" > sources.txt && javac -cp "lib/*" -d out @sources.txt && java -cp "out:lib/*" org.junit.runner.JUnitCore test.HidatoTestSuite
```
