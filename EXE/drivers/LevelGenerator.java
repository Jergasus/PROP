package test.drivers;

import model.adjacency.*;
import model.algorithms.Generator;
import model.algorithms.Solver;
import model.board.Board;
import model.cell.Cell;
import model.cell.CellShape;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

/**
 * One-shot utility to generate the 15 pregenerated level files for Delivery 2.
 * Run once, then the files live in data/levels/ and are shipped with the project.
 *
 * Usage: java -cp out test.drivers.LevelGenerator
 */
public class LevelGenerator {

    // Difficulty configs: {label, count, rows, cols, shape, adjKey, difficultyFraction}
    static final Object[][] CONFIGS = {
        // 5 Easy levels — small square boards, 8-way adjacency, low removal
        {"easy", 5, 4, 4, CellShape.SQUARE, "SQUARE_FULL", 0.35},
        // 5 Medium levels — medium boards, mixed shapes
        {"medium", 5, 5, 5, CellShape.SQUARE, "SQUARE_FULL", 0.50},
        // 5 Hard levels — larger boards, 4-way adjacency (harder)
        {"hard", 5, 6, 6, CellShape.SQUARE, "SQUARE", 0.55},
    };

    public static void main(String[] args) throws Exception {
        new File("data/levels").mkdirs();

        Generator gen = new Generator();
        Solver solver = new Solver();
        int totalGenerated = 0;

        for (Object[] cfg : CONFIGS) {
            String label = (String) cfg[0];
            int count    = (int) cfg[1];
            int rows     = (int) cfg[2];
            int cols     = (int) cfg[3];
            CellShape shape = (CellShape) cfg[4];
            String adjKey   = (String) cfg[5];
            double diff     = (double) cfg[6];

            AdjacencyStrategy strategy = resolveStrategy(adjKey);
            String difficulty = label.toUpperCase();

            for (int i = 1; i <= count; i++) {
                String levelId = String.format("%s_%02d", label, i);
                String displayName = capitalize(label) + " " + i;
                String filename = "data/levels/" + levelId + ".txt";

                System.out.printf("Generating %s (%dx%d, %s, diff=%.2f)...", levelId, rows, cols, adjKey, diff);

                Board puzzle = null;
                // Retry until we get a valid, unique-solution puzzle
                for (int attempt = 0; attempt < 50 && puzzle == null; attempt++) {
                    puzzle = gen.generatePuzzle(rows, cols, shape, strategy, diff);
                    if (puzzle != null) {
                        // Verify unique solution
                        int sols = solver.countSolutions(new Board(puzzle), 2);
                        if (sols != 1) {
                            puzzle = null; // try again
                        }
                    }
                }

                if (puzzle == null) {
                    System.out.println(" FAILED (could not generate)");
                    continue;
                }

                writeLevelFile(filename, levelId, difficulty, displayName, shape, adjKey, puzzle);
                System.out.println(" OK");
                totalGenerated++;
            }
        }

        System.out.println("\nDone. Generated " + totalGenerated + " level files in data/levels/");
    }

    static void writeLevelFile(String filename, String levelId, String difficulty,
                                String displayName, CellShape shape, String adjKey,
                                Board board) throws Exception {
        // Time thresholds based on difficulty
        long t3, t2, t1;
        switch (difficulty) {
            case "EASY":   t3 = 60000;  t2 = 120000; t1 = 180000; break;
            case "MEDIUM": t3 = 120000; t2 = 240000; t1 = 360000; break;
            default:       t3 = 180000; t2 = 360000; t1 = 540000; break;
        }

        try (PrintWriter w = new PrintWriter(new FileWriter(filename))) {
            w.println("LEVEL_ID:" + levelId);
            w.println("DIFFICULTY:" + difficulty);
            w.println("DISPLAY_NAME:" + displayName);
            w.println("SHAPE:" + shape);
            w.println("ADJ:" + adjKey);
            w.println("ROWS:" + board.getRows());
            w.println("COLS:" + board.getCols());
            w.println("DATA:");

            for (int i = 0; i < board.getRows(); i++) {
                for (int j = 0; j < board.getCols(); j++) {
                    Cell cell = board.getCell(i, j);
                    if (cell == null) continue;
                    String type = "NORMAL";
                    if (cell.isVoid()) type = "VOID";
                    else if (cell.isFixed()) type = "FIXED";
                    w.printf("%d,%d,%d,%s%n", i, j, cell.getValue(), type);
                }
            }
        }
    }

    static AdjacencyStrategy resolveStrategy(String key) {
        switch (key) {
            case "SQUARE_FULL": return new SquareFullAdjacencyStrategy();
            case "HEXAGONAL":   return new HexagonalAdjacencyStrategy();
            case "TRIANGLE":    return new TriangleAdjacencyStrategy();
            default:            return new SquareAdjacencyStrategy();
        }
    }

    static String capitalize(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
