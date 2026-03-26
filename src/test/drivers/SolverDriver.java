package test.drivers;

import java.util.Scanner;
import model.adjacency.*;
import model.algorithms.Solver;
import model.algorithms.Validator;
import model.board.Board;
import model.cell.CellShape;

/**
 * Interactive driver for Solver and Validator.
 *
 * Usage:
 *   java -cp out test.drivers.SolverDriver
 *
 * The driver lets you specify a board size and type, enter clue values
 * interactively, then asks Solver to find a solution and Validator to
 * confirm it.  It also reports how long the solver took.
 */
public class SolverDriver {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("==============================================");
        System.out.println("  HIDATO — Solver & Validator Interactive Driver");
        System.out.println("==============================================");

        // 1. Choose cell type
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

        // 2. Board dimensions
        System.out.print("Rows: ");    int rows = readInt(sc, 1, 20);
        System.out.print("Columns: "); int cols = readInt(sc, 1, 20);
        Board board = new Board(rows, cols, shape, strategy);

        // Auto-prune structural dead-ends (cells with < 2 neighbors)
        int pruned = board.pruneDeadEnds();
        if (pruned > 0) {
            System.out.println("\nAuto-voided " + pruned + " dead-end cell(s) (shown as #):");
            System.out.print(board);
        }

        // 3. Mark void cells
        System.out.println("\nMark void cells (enter 'row col', blank line to stop):");
        while (true) {
            System.out.print("  void> ");
            String line = sc.nextLine().trim();
            if (line.isEmpty()) break;
            String[] parts = line.split("\\s+");
            if (parts.length < 2) { System.out.println("  Format: row col"); continue; }
            try {
                int r = Integer.parseInt(parts[0]);
                int c = Integer.parseInt(parts[1]);
                if (r < 0 || r >= rows || c < 0 || c >= cols) { System.out.println("  Out of bounds"); continue; }
                board.getCell(r, c).setVoid(true);
                System.out.println("  (" + r + "," + c + ") marked as void");
            } catch (NumberFormatException e) { System.out.println("  Invalid input"); }
        }

        // Re-prune in case manual voids created new dead ends
        int repruned = board.pruneDeadEnds();
        if (repruned > 0) {
            System.out.println("  Auto-voided " + repruned + " additional dead-end cell(s).");
        }

        // 4. Enter clues
        System.out.println("\nEnter clues (enter 'row col value', blank line to stop):");
        System.out.println("  (value 1 is mandatory — it is the start of the path)");
        while (true) {
            System.out.print("  clue> ");
            String line = sc.nextLine().trim();
            if (line.isEmpty()) break;
            String[] parts = line.split("\\s+");
            if (parts.length < 3) { System.out.println("  Format: row col value"); continue; }
            try {
                int r = Integer.parseInt(parts[0]);
                int c = Integer.parseInt(parts[1]);
                int v = Integer.parseInt(parts[2]);
                if (r < 0 || r >= rows || c < 0 || c >= cols) { System.out.println("  Out of bounds"); continue; }
                if (board.getCell(r, c).isVoid()) { System.out.println("  Cell is void"); continue; }
                board.getCell(r, c).setFixedValue(v);
                System.out.println("  Placed " + v + " at (" + r + "," + c + ")");
            } catch (NumberFormatException e) { System.out.println("  Invalid input"); }
        }

        // 5. Show board before solving
        System.out.println("\nBoard before solving:");
        System.out.print(board);

        // 6. Validate partial state
        Validator validator = new Validator();
        if (!validator.isPartiallyValid(board)) {
            System.out.println("\n[VALIDATOR] Partial board has conflicts — solving may fail.");
        } else {
            System.out.println("\n[VALIDATOR] Partial board is consistent.");
        }

        // 7. Solve
        System.out.println("\nSolving...");
        Solver solver = new Solver();
        long t0      = System.currentTimeMillis();
        boolean ok   = solver.solve(board);
        long elapsed = System.currentTimeMillis() - t0;

        if (ok) {
            System.out.println("\nSolution found in " + elapsed + " ms:\n");
            System.out.print(board);
            System.out.println("\n[VALIDATOR] Solution is " +
                (validator.isValidSolution(board) ? "VALID ✓" : "INVALID ✗ (bug!)"));
        } else {
            System.out.println("\nNo solution found (" + elapsed + " ms). The puzzle is unsolvable.");
        }

        // 8. Count solutions (optional)
        System.out.print("\nCount distinct solutions? [y/N] ");
        String ans = sc.nextLine().trim();
        if (ans.equalsIgnoreCase("y")) {
            // Rebuild the original clue board (solver mutated it)
            // Re-read from board (fixed cells kept their values)
            int limit = 5;
            System.out.println("Counting solutions (limit=" + limit + ")...");
            // Rebuild fresh board from fixed values
            Board fresh = new Board(rows, cols, shape, strategy);
            for (int i = 0; i < rows; i++)
                for (int j = 0; j < cols; j++) {
                    if (board.getCell(i, j).isVoid()) { fresh.getCell(i, j).setVoid(true); continue; }
                    if (board.getCell(i, j).isFixed()) fresh.getCell(i, j).setFixedValue(board.getCell(i,j).getValue());
                }
            long t1 = System.currentTimeMillis();
            int count = solver.countSolutions(fresh, limit);
            long e2 = System.currentTimeMillis() - t1;
            System.out.println("Solutions found: " + (count >= limit ? ">= " + limit : count) + "  (" + e2 + " ms)");
        }

        System.out.println("\nDone.");
        sc.close();
    }

    private static int readInt(Scanner sc, int min, int max) {
        while (true) {
            try {
                String line = sc.nextLine().trim();
                int v = Integer.parseInt(line);
                if (v >= min && v <= max) return v;
                System.out.print("  Enter a value between " + min + " and " + max + ": ");
            } catch (NumberFormatException e) {
                System.out.print("  Invalid input, try again: ");
            }
        }
    }
}
