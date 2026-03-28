package test.drivers;

import controller.DomainController;
import controller.HidatoCaseController;
import model.board.Board;

import java.util.List;
import java.util.Scanner;

/**
 * Interactive demo driver for Solver and Validator.
 *
 * All domain calls go through DomainController — this class handles
 * only console I/O and menu navigation.
 *
 * Usage:
 *   ./compilar.sh
 *   — or —
 *   javac -cp "lib/*" -d out $(find src -name "*.java")
 *   java  -cp "out:lib/*" test.drivers.SolverDriver
 */
public class SolverDriver {

    private static final Scanner          sc     = new Scanner(System.in);
    private static final DomainController domain = new DomainController();

    public static void main(String[] args) {
        printBanner();

        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = readInt(0, 2);

            switch (choice) {
                case 0:
                    running = false;
                    break;
                case 1:
                    handleFlow(false); // validate
                    break;
                case 2:
                    handleFlow(true);  // solve
                    break;
            }
        }

        System.out.println("\nGoodbye.");
        sc.close();
    }

    // ------------------------------------------------------------------ //
    //  High-level flows                                                   //
    // ------------------------------------------------------------------ //

    private static void handleFlow(boolean solve) {
        List<HidatoCaseController> catalog = domain.getCatalog();
        printCatalog(catalog);

        System.out.print("Select case (0 to go back): ");
        int idx = readInt(0, catalog.size());
        if (idx == 0) return;

        System.out.println();
        HidatoCaseController tc = domain.getCase(idx - 1);

        if (solve) runSolve(tc);
        else       runValidate(tc);

        System.out.println("\nPress ENTER to continue...");
        sc.nextLine();
    }

    // ── VALIDATE ────────────────────────────────────────────────────────

    private static void runValidate(HidatoCaseController tc) {
        Board board = tc.getBoard();
        printSectionHeader("VALIDATE  —  " + tc.getName());

        System.out.println("Puzzle state:");
        System.out.print(board);
        System.out.println();

        boolean partial = domain.isPartiallyValid(board);
        System.out.println("  Partial validity : " + (partial ? "CONSISTENT  ✓" : "CONTRADICTIONS FOUND  ✗"));

        boolean full = domain.isValidSolution(board);
        System.out.println("  Full solution    : " + (full ? "VALID  ✓" : "incomplete (puzzle input, expected)"));

        System.out.println();
        printExpected(tc);
    }

    // ── SOLVE ────────────────────────────────────────────────────────────

    private static void runSolve(HidatoCaseController tc) {
        Board board = tc.getBoard();
        printSectionHeader("SOLVE  —  " + tc.getName());

        System.out.println("Puzzle:");
        System.out.print(board);
        System.out.println();

        if (!domain.isPartiallyValid(board)) {
            System.out.println("  [ERROR] Puzzle has contradictions — solving aborted.");
            System.out.println();
            printExpected(tc);
            return;
        }

        System.out.println("  Solving...");
        long    t0      = System.currentTimeMillis();
        boolean solved  = domain.solve(board);
        long    elapsed = System.currentTimeMillis() - t0;

        if (solved) {
            System.out.println("  Solution found in " + elapsed + " ms:\n");
            System.out.print(board);
            boolean valid = domain.isValidSolution(board);
            System.out.println("\n  Validator: " + (valid ? "VALID  ✓" : "INVALID  ✗  (bug!)"));
        } else {
            System.out.println("  No solution found (" + elapsed + " ms). Puzzle is unsolvable.");
        }

        System.out.println();
        printExpected(tc);
        boolean correct = solved == tc.isExpectedSolvable();
        System.out.println("  Matches expected outcome: " + (correct ? "YES  ✓" : "NO  ✗"));
    }

    // ------------------------------------------------------------------ //
    //  Display helpers                                                    //
    // ------------------------------------------------------------------ //

    private static void printBanner() {
        System.out.println("==============================================");
        System.out.println("   HIDATO  —  Solver & Validator Demo");
        System.out.println("==============================================");
        System.out.println();
    }

    private static void printMainMenu() {
        System.out.println("----------------------------------------------");
        System.out.println("  [1] Validate    [2] Solve    [0] Exit");
        System.out.println("----------------------------------------------");
        System.out.print("> ");
    }

    private static void printCatalog(List<HidatoCaseController> catalog) {
        System.out.println();
        System.out.println("  Available test cases:");
        System.out.println();
        for (int i = 0; i < catalog.size(); i++) {
            HidatoCaseController tc = catalog.get(i);
            String outcome = tc.isExpectedSolvable() ? "SOLVABLE" : "UNSOLVABLE";
            System.out.printf("  [%d] %s  (%s)%n", i + 1, tc.getName(), outcome);
            System.out.printf("      Adjacency: %s%n", tc.getAdjacencyDesc());
            System.out.printf("      %s%n", tc.getDescription());
            System.out.println();
        }
    }

    private static void printSectionHeader(String title) {
        System.out.println("----------------------------------------------");
        System.out.println("  " + title);
        System.out.println("----------------------------------------------");
    }

    private static void printExpected(HidatoCaseController tc) {
        System.out.println("  Expected: " + (tc.isExpectedSolvable() ? "SOLVABLE" : "UNSOLVABLE"));
        System.out.println("  " + tc.getDescription());
    }

    // ------------------------------------------------------------------ //
    //  Input helper                                                       //
    // ------------------------------------------------------------------ //

    private static int readInt(int min, int max) {
        while (true) {
            try {
                int v = Integer.parseInt(sc.nextLine().trim());
                if (v >= min && v <= max) return v;
            } catch (NumberFormatException ignored) {}
            System.out.printf("  Enter a number between %d and %d: ", min, max);
        }
    }
}
