# Project Context — PROP Hidato (Spring 25/26)

## What this project is
University project implementing the Hidato puzzle game in Java.
Hidato: fill a grid with consecutive numbers 1..N where each consecutive pair must be in adjacent cells.
Three cell geometries: SQUARE (4-way or 8-way), HEXAGON (6-way), TRIANGLE (3-way).

## Deliveries
- **Delivery 1 — due 30 March 2026**: validation + resolution algorithms (FOCUS)
- Delivery 2 — 26 May 2026
- Delivery 3 — 1 June 2026 (interactive presentations from 2 June)

---

## Full class inventory (24 classes)

### `model/cell/` — raw data primitives
- `Position` (record) — immutable (row, col) coordinate
- `CellShape` (enum) — SQUARE | HEXAGON | TRIANGLE
- `Cell` — one grid cell: value (0=empty), isFixed (puzzle clue), isVoid (hole)

### `model/board/`
- `Board` — 2D Cell grid + AdjacencyStrategy. Key methods: getNeighbors(), areAdjacent(), getCellCount()

### `model/adjacency/` — Strategy pattern for neighbor calculation
- `AdjacencyStrategy` (interface) — getNeighbors() + default areAdjacent()
- `SquareAdjacencyStrategy` — 4-way ortho; areAdjacent() = Manhattan distance == 1 (O(1))
- `SquareFullAdjacencyStrategy` — 8-way; areAdjacent() = both diffs ≤1 (O(1))
- `HexagonalAdjacencyStrategy` — 6-way offset grid; uses default areAdjacent() (O(k))
- `TriangleAdjacencyStrategy` — 3-way alternating orientation; uses default areAdjacent() (O(k))

### `model/algorithms/` — DELIVERY 1 CORE
- `Validator` — isValidSolution(board), isPartiallyValid(board)
- `Solver` — solve(board), countSolutions(board, limit)
- `Generator` — generatePuzzle(...), generateFullBoard(...)

### `model/game/`
- `Move` (record) — (position, previousValue, newValue) for undo/redo
- `Game` — wraps Board; makeMove, undo, redo, isFinished() (uses Validator internally)

### `model/ranking/`
- `Score` — playerName, timeMillis, date (Serializable)
- `RankingManager` — loads/saves ranking.dat, addScore(), getTopScores()

### `controller/` — MVC control layer (out of scope delivery 1)
- `MenuController` — main menu loop, game config, calls Generator
- `GameController` — active play loop, move validation, save/load, scoring
- `EditorController` — custom puzzle editor, calls Validator and GameSaver

### `view/` (out of scope delivery 1)
- `ConsoleView` — all console I/O; inner record MoveInput

### `persistence/` (out of scope delivery 1)
- `BoardSerializer` (interface) — saveBoard/loadBoard
- `SimpleTextSerializer` — CSV-like text format for boards
- `GameSaver` — binary Java serialization for full Game objects

### Entry point
- `Main` — creates MenuController, calls showMainMenu()

---

## Key algorithms (Delivery 1)

### Solver (backtracking)
- Single O(N) init pass: buildClueMap() builds HashMap<value,Cell> AND finds start cell
- Fixed-clue check: O(1) HashMap lookup instead of O(N) scan
- Warnsdorff heuristic: pre-filter empty neighbours → pre-compute free-neighbour scores → insertion sort (optimal for ≤8 elements)
- areAdjacent() via strategy: O(1) for Square/SquareFull, O(k) fallback for Hex/Triangle

### Validator
- Single O(N) pass: buildLookup() counts non-void cells AND builds Cell[] array indexed by value
- Single path walk: merges "all slots filled" check and "adjacency" check
- areAdjacent() same optimization as Solver

### Generator
- generateFullBoard: up to 20 random-start attempts via Solver
- generatePuzzle: removes cells one-by-one, verifies unique solution via countSolutions(board, 2)
- Accepts explicit AdjacencyStrategy so generation uses the same rules as play

---

## Bugs fixed during session
1. Generator.maxVal was rows*cols (included void cells) → now board.getCellCount()
2. Game.isFinished() only checked cell fill, not path validity → now uses Validator
3. EditorController.modifyCell() called setValue(0) on fixed cells (silent no-op) → now setAsEmpty()
4. Generator always used SquareAdjacencyStrategy regardless of user choice → now accepts strategy param
5. Solver Warnsdorff comparator violated antisymmetry → fixed with pre-filter + pre-compute

---

## How to compile and run
```bash
cd /Users/jergasus/projects/PROP
find src -name "*.java" > sources.txt
javac -d out @sources.txt
java -cp out Main
```

## How to run JUnit 4 tests
```bash
cd /Users/jergasus/projects/PROP
find src -name "*.java" > sources.txt
javac -cp "lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar" -d out @sources.txt
java  -cp "out:lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar" org.junit.runner.JUnitCore test.HidatoTestSuite
```
JUnit jars: `lib/junit-4.13.2.jar`, `lib/hamcrest-core-1.3.jar` (copied from Gradle cache).
Test files: `src/test/ValidatorTest.java` (15 tests), `src/test/SolverTest.java` (13), `src/test/GeneratorTest.java` (12). Total: 40 tests, all passing.

## How to run interactive drivers
```bash
# Solve a board you enter manually:
java -cp out test.drivers.SolverDriver

# Generate a puzzle interactively:
java -cp out test.drivers.GeneratorDriver
```
