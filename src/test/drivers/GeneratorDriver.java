package test.drivers;

import java.util.Scanner;
import model.adjacency.*;
import model.algorithms.Generator;
import model.algorithms.Solver;
import model.algorithms.Validator;
import model.board.Board;
import model.cell.CellShape;

/**
 * Interactive driver for Generator.
 *
 * Usage:
 *   java -cp out test.drivers.GeneratorDriver
 *
 * Lets you choose a geometry, board size, and difficulty level, then
 * generates a puzzle, verifies its uniqueness, and optionally shows the
 * solution.
 */
public class GeneratorDriver {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("==============================================");
        System.out.println("  HIDATO — Generator Interactive Driver");
        System.out.println("==============================================");

        // 1. Cell type
        System.out.println("Cell type: [1] Square-4way  [2] Square-8way  [3] Hexagonal  [4] Triangle");
        System.out.print("> ");
        int typeChoice = readInt(sc, 1, 4);
        CellShape shape;
        AdjacencyStrategy strategy;
        switch (typeChoice) {
            case 1: shape = CellShape.SQUARE;   strategy = new SquareAdjacencyStrategy();     break;
            case 2: shape = CellShape.SQUARE;   strategy = new SquareFullAdjacencyStrategy(); break;
            case 3: shape = CellShape.HEXAGON;  strategy = new HexagonalAdjacencyStrategy();  break;
            default:shape = CellShape.TRIANGLE; strategy = new TriangleAdjacencyStrategy();   break;
        }

        // 2. Board size
        System.out.print("Rows (1-10): ");    int rows = readInt(sc, 1, 10);
        System.out.print("Columns (1-10): "); int cols = readInt(sc, 1, 10);

        // 3. Additional void cells
        System.out.print("Extra void cells (0 for none): ");
        int numVoids = readInt(sc, 0, rows * cols - 2);

        // 4. Clue mode
        System.out.println("Clue mode: [1] Difficulty fraction  [2] Exact clue count");
        System.out.print("> ");
        int clueMode = readInt(sc, 1, 2);

        // 5. Generate
        Generator generator = new Generator();
        System.out.println("\nGenerating puzzle...");
        long t0 = System.currentTimeMillis();
        Board puzzle;
        if (clueMode == 2) {
            System.out.print("Number of clues (>= 2): ");
            int numClues = readInt(sc, 2, rows * cols);
            puzzle = generator.generatePuzzle(rows, cols, shape, strategy, numVoids, numClues);
        } else {
            System.out.print("Difficulty 0.0 (none removed) to 1.0 (maximum removal): ");
            double diff = readDouble(sc, 0.0, 1.0);
            if (numVoids > 0) {
                puzzle = generator.generatePuzzle(rows, cols, shape, strategy, numVoids, diff);
            } else {
                puzzle = generator.generatePuzzle(rows, cols, shape, strategy, diff);
            }
        }
        long elapsed = System.currentTimeMillis() - t0;

        if (puzzle == null) {
            System.out.println("Generation failed — try a smaller board or lower difficulty.");
            sc.close();
            return;
        }
        System.out.println("Generated in " + elapsed + " ms.\n");
        System.out.println("Puzzle:");
        System.out.print(puzzle);

        // 5. Verify uniqueness
        Solver solver = new Solver();
        System.out.println("\nVerifying uniqueness...");
        long t1     = System.currentTimeMillis();
        int numSols = solver.countSolutions(new Board(puzzle), 2);
        long e2     = System.currentTimeMillis() - t1;
        if (numSols == 1) {
            System.out.println("[OK] Unique solution (" + e2 + " ms).");
        } else if (numSols == 0) {
            System.out.println("[ERROR] No solution found — generation bug!");
        } else {
            System.out.println("[WARNING] Multiple solutions — generation did not guarantee uniqueness.");
        }

        // 6. Show solution?
        System.out.print("\nShow solution? [y/N] ");
        if (sc.nextLine().trim().equalsIgnoreCase("y")) {
            Board solution = new Board(puzzle);
            solver.solve(solution);
            Validator v = new Validator();
            System.out.println("\nSolution:");
            System.out.print(solution);
            System.out.println("[VALIDATOR] " + (v.isValidSolution(solution) ? "VALID ✓" : "INVALID ✗"));
        }

        // 7. Generate again?
        System.out.print("\nGenerate another puzzle with the same settings? [y/N] ");
        while (sc.nextLine().trim().equalsIgnoreCase("y")) {
            Board p2 = generator.generatePuzzle(rows, cols, shape, strategy, 0.4);
            if (p2 == null) { System.out.println("Generation failed."); break; }
            System.out.println("\nPuzzle:");
            System.out.print(p2);
            System.out.print("\nGenerate another? [y/N] ");
        }

        System.out.println("\nDone.");
        sc.close();
    }

    private static int readInt(Scanner sc, int min, int max) {
        while (true) {
            try {
                int v = Integer.parseInt(sc.nextLine().trim());
                if (v >= min && v <= max) return v;
                System.out.print("  Value must be " + min + ".." + max + ": ");
            } catch (NumberFormatException e) {
                System.out.print("  Invalid, try again: ");
            }
        }
    }

    private static double readDouble(Scanner sc, double min, double max) {
        while (true) {
            try {
                double v = Double.parseDouble(sc.nextLine().trim());
                if (v >= min && v <= max) return v;
                System.out.print("  Value must be " + min + ".." + max + ": ");
            } catch (NumberFormatException e) {
                System.out.print("  Invalid, try again: ");
            }
        }
    }
}
