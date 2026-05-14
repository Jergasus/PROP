package model.algorithms;

import model.adjacency.AdjacencyStrategy;
import model.adjacency.HexagonalAdjacencyStrategy;
import model.adjacency.SquareAdjacencyStrategy;
import model.adjacency.TriangleAdjacencyStrategy;
import model.board.Board;
import model.cell.Cell;
import model.cell.CellShape;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Generator {

    private static final int MAX_ATTEMPTS = 20;
    private final Random random = new Random();

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /**
     * Generates a Hidato puzzle with a unique solution.
     *
     * @param rows       number of rows
     * @param cols       number of columns
     * @param shape      cell geometry
     * @param strategy   adjacency strategy to use for generation AND play
     * @param difficulty fraction of removable cells to actually remove [0.0, 1.0]
     * @return a valid puzzle board, or null if generation failed
     */
    public Board generatePuzzle(int rows, int cols, CellShape shape,
                                AdjacencyStrategy strategy, double difficulty) {
        Board board = generateFullBoard(rows, cols, shape, strategy);
        if (board == null) return null;

        int maxVal = board.getCellCount(); // number of playable cells

        // Mark all non-void cells as fixed, collect candidates to remove
        // (never remove 1 or the maximum value — keeps the puzzle anchored)
        List<Cell> candidates = new ArrayList<>();
        for (int i = 0; i < board.getRows(); i++) {
            for (int j = 0; j < board.getCols(); j++) {
                Cell c = board.getCell(i, j);
                if (!c.isVoid()) {
                    c.setFixedValue(c.getValue());
                    if (c.getValue() != 1 && c.getValue() != maxVal) {
                        candidates.add(c);
                    }
                }
            }
        }

        Collections.shuffle(candidates, random);

        Solver solver = new Solver();
        int targetToRemove = (int) (candidates.size() * difficulty);
        int removedCount = 0;

        for (Cell c : candidates) {
            if (removedCount >= targetToRemove) break;

            int originalValue = c.getValue();
            c.setAsEmpty();

            // Keep removal only if solution remains unique
            if (solver.countSolutions(board, 2) == 1) {
                removedCount++;
            } else {
                c.setFixedValue(originalValue);
            }
        }

        return board;
    }

    /**
     * Convenience overload that picks a default strategy based on cell shape.
     * Prefer the overload that accepts an explicit strategy when possible.
     */
    public Board generatePuzzle(int rows, int cols, CellShape shape, double difficulty) {
        return generatePuzzle(rows, cols, shape, defaultStrategy(shape), difficulty);
    }

    /**
     * Generates a completely filled, valid Hidato board using the given strategy.
     *
     * @return a fully solved board, or null if all attempts failed
     */
    public Board generateFullBoard(int rows, int cols, CellShape shape, AdjacencyStrategy strategy) {
        Solver solver = new Solver();

        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            Board board = new Board(rows, cols, shape, strategy);

            // Prune dead-end cells (essential for triangle grids)
            board.pruneDeadEnds();

            int playable = board.getCellCount();
            if (playable < 2) continue;

            // Pick a random non-void starting cell
            List<Cell> nonVoid = new ArrayList<>();
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    if (!board.getCell(i, j).isVoid()) nonVoid.add(board.getCell(i, j));
                }
            }
            Cell start = nonVoid.get(random.nextInt(nonVoid.size()));
            start.setValue(1);

            if (solver.solve(board)) {
                return board;
            }
        }
        return null;
    }

    /**
     * Convenience overload that picks a default strategy based on cell shape.
     */
    public Board generateFullBoard(int rows, int cols, CellShape shape) {
        return generateFullBoard(rows, cols, shape, defaultStrategy(shape));
    }

    /**
     * Generates a puzzle with explicit void cells and clue count.
     *
     * @param numVoids  number of void (hole) cells to place randomly
     * @param numClues  number of fixed clue cells in the final puzzle (>= 2)
     * @return a valid puzzle board, or null if generation failed
     */
    public Board generatePuzzle(int rows, int cols, CellShape shape,
                                AdjacencyStrategy strategy,
                                int numVoids, int numClues) {
        if (numClues < 2) return null;

        Board board = generateFullBoard(rows, cols, shape, strategy, numVoids);
        if (board == null) return null;

        int maxVal = board.getCellCount();
        if (numClues > maxVal) return null;

        // Mark all non-void cells as fixed, collect removal candidates
        List<Cell> candidates = new ArrayList<>();
        for (int i = 0; i < board.getRows(); i++) {
            for (int j = 0; j < board.getCols(); j++) {
                Cell c = board.getCell(i, j);
                if (!c.isVoid()) {
                    c.setFixedValue(c.getValue());
                    if (c.getValue() != 1 && c.getValue() != maxVal) {
                        candidates.add(c);
                    }
                }
            }
        }

        Collections.shuffle(candidates, random);

        // We always keep 1 and maxVal as clues, so removable target is:
        // current fixed count (maxVal) minus desired numClues
        Solver solver = new Solver();
        int targetToRemove = maxVal - numClues;
        int removedCount = 0;

        for (Cell c : candidates) {
            if (removedCount >= targetToRemove) break;

            int originalValue = c.getValue();
            c.setAsEmpty();

            if (solver.countSolutions(board, 2) == 1) {
                removedCount++;
            } else {
                c.setFixedValue(originalValue);
            }
        }

        return board;
    }

    /**
     * Generates a puzzle with void cells and difficulty fraction.
     */
    public Board generatePuzzle(int rows, int cols, CellShape shape,
                                AdjacencyStrategy strategy,
                                int numVoids, double difficulty) {
        Board board = generateFullBoard(rows, cols, shape, strategy, numVoids);
        if (board == null) return null;

        int maxVal = board.getCellCount();

        List<Cell> candidates = new ArrayList<>();
        for (int i = 0; i < board.getRows(); i++) {
            for (int j = 0; j < board.getCols(); j++) {
                Cell c = board.getCell(i, j);
                if (!c.isVoid()) {
                    c.setFixedValue(c.getValue());
                    if (c.getValue() != 1 && c.getValue() != maxVal) {
                        candidates.add(c);
                    }
                }
            }
        }

        Collections.shuffle(candidates, random);

        Solver solver = new Solver();
        int targetToRemove = (int) (candidates.size() * difficulty);
        int removedCount = 0;

        for (Cell c : candidates) {
            if (removedCount >= targetToRemove) break;

            int originalValue = c.getValue();
            c.setAsEmpty();

            if (solver.countSolutions(board, 2) == 1) {
                removedCount++;
            } else {
                c.setFixedValue(originalValue);
            }
        }

        return board;
    }

    /**
     * Generates a fully solved board with random void cells.
     * Also prunes dead-end cells (degree < 2) to ensure solvability.
     */
    public Board generateFullBoard(int rows, int cols, CellShape shape,
                                   AdjacencyStrategy strategy, int numVoids) {
        Solver solver = new Solver();

        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            Board board = new Board(rows, cols, shape, strategy);

            // Prune dead-end cells first (essential for triangle grids)
            board.pruneDeadEnds();

            // Then place additional random voids
            if (numVoids > 0 && !board.placeRandomVoids(numVoids, random)) {
                continue; // couldn't place all voids, retry
            }

            int playable = board.getCellCount();
            if (playable < 2) continue;

            // Pick a random non-void starting cell
            List<Cell> nonVoid = new ArrayList<>();
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    if (!board.getCell(i, j).isVoid()) nonVoid.add(board.getCell(i, j));
                }
            }
            Cell start = nonVoid.get(random.nextInt(nonVoid.size()));
            start.setValue(1);

            if (solver.solve(board)) {
                return board;
            }
        }
        return null;
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /** Returns the canonical adjacency strategy for a given cell shape. */
    private AdjacencyStrategy defaultStrategy(CellShape shape) {
        return switch (shape) {
            case HEXAGON  -> new HexagonalAdjacencyStrategy();
            case TRIANGLE -> new TriangleAdjacencyStrategy();
            default       -> new SquareAdjacencyStrategy();
        };
    }
}
