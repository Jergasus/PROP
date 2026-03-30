import domini.controller.CtrlDomini;
import java.util.Scanner;

public class SolverValidatorDriver {

    private static final Scanner sc = new Scanner(System.in);
    private static final CtrlDomini domain = new CtrlDomini();

    public static void main(String[] args) {
        printBanner();

        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = readInt(0, 2);

            switch (choice) {
                case 0: running = false;       break;
                case 1: handleFlow(false);     break;
                case 2: handleFlow(true);      break;
            }
        }

        System.out.println("\nGoodbye.");
        sc.close();
    }

    private static void handleFlow(boolean solve) {
        printCatalog();

        System.out.print("Select case (0 to go back): ");
        int idx = readInt(0, domain.getCatalogSize());
        if (idx == 0) return;

        domain.selectCase(idx - 1);
        System.out.println();

        if (solve) runSolve(idx - 1);
        else       runValidate(idx - 1);

        System.out.println("\nPress ENTER to continue...");
        sc.nextLine();
    }

    private static void runValidate(int idx) {
        printSectionHeader("VALIDATE  —  " + domain.getCatalogCaseName(idx));

        System.out.print(domain.getActiveBoardAsString());
        System.out.println();

        boolean partial = domain.isPartiallyValid();
        System.out.println("  Partial validity : " + (partial ? "CONSISTENT  ✓" : "CONTRADICTIONS FOUND  ✗"));

        boolean hasSolution = partial && domain.hasSolution();
        System.out.println("  Has solution     : " + (hasSolution ? "YES  ✓" : "NO  ✗"));
    }

    private static void runSolve(int idx) {
        printSectionHeader("SOLVE  —  " + domain.getCatalogCaseName(idx));

        System.out.print(domain.getActiveBoardAsString());
        System.out.println();

        if (!domain.isPartiallyValid()) {
            System.out.println("  Contradictions found — solving aborted.");
            return;
        }

        long    t0      = System.currentTimeMillis();
        boolean solved  = domain.solve();
        long    elapsed = System.currentTimeMillis() - t0;

        if (solved) {
            System.out.println("  Solution (" + elapsed + " ms):\n");
            System.out.print(domain.getActiveBoardAsString());
            System.out.println("\n  Validator: " + (domain.isValidSolution() ? "VALID  ✓" : "INVALID  ✗"));
        } else {
            System.out.println("  No solution found (" + elapsed + " ms).");
        }
    }

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

    private static void printCatalog() {
        System.out.println();
        System.out.println("  Available test cases:");
        System.out.println();
        for (int i = 0; i < domain.getCatalogSize(); i++) {
            String outcome = domain.getCatalogCaseSolvable(i) ? "SOLVABLE" : "UNSOLVABLE";
            System.out.printf("  [%d] %s  (%s)%n", i + 1, domain.getCatalogCaseName(i), outcome);
            System.out.printf("      Adjacency : %s%n", domain.getCatalogCaseAdjacency(i));
            System.out.printf("      Description: %s%n", domain.getCatalogCaseDescription(i));
            System.out.println();
        }
    }

    private static void printSectionHeader(String title) {
        System.out.println("----------------------------------------------");
        System.out.println("  " + title);
        System.out.println("----------------------------------------------");
    }

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
