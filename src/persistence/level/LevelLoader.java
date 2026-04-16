package persistence.level;

import model.adjacency.*;
import model.board.Board;
import model.cell.Cell;
import model.cell.CellShape;
import model.level.Difficulty;
import model.level.Level;
import model.level.LevelCatalog;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads pregenerated level files from data/levels/.
 *
 * File format:
 *   LEVEL_ID:<id>
 *   DIFFICULTY:<EASY|MEDIUM|HARD>
 *   DISPLAY_NAME:<name>
 *   SHAPE:<SQUARE|HEXAGON|TRIANGLE>
 *   ADJ:<SQUARE|SQUARE_FULL|HEXAGONAL|TRIANGLE>
 *   ROWS:<int>
 *   COLS:<int>
 *   DATA:
 *   row,col,value,type
 *   ...
 */
public class LevelLoader {

    private static final String LEVELS_DIR = "data/levels";

    /**
     * Loads all .txt files from data/levels/ and returns a LevelCatalog.
     */
    public LevelCatalog loadAllLevels() {
        List<Level> levels = new ArrayList<>();
        File dir = new File(LEVELS_DIR);
        if (!dir.exists() || !dir.isDirectory()) return new LevelCatalog(levels);

        File[] files = dir.listFiles((d, name) -> name.endsWith(".txt"));
        if (files == null) return new LevelCatalog(levels);

        for (File f : files) {
            try {
                Level level = loadLevel(f.getPath());
                if (level != null) levels.add(level);
            } catch (IOException e) {
                System.err.println("Warning: could not load level " + f.getName() + ": " + e.getMessage());
            }
        }

        // Sort by difficulty order (EASY first) then by levelId
        levels.sort((a, b) -> {
            int cmp = a.getDifficulty().compareTo(b.getDifficulty());
            return cmp != 0 ? cmp : a.getLevelId().compareTo(b.getLevelId());
        });

        return new LevelCatalog(levels);
    }

    /**
     * Loads a single level file.
     */
    public Level loadLevel(String filePath) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath));

        String levelId = null;
        Difficulty difficulty = Difficulty.EASY;
        String displayName = null;
        CellShape shape = CellShape.SQUARE;
        String adjType = "SQUARE";
        int rows = 0, cols = 0;
        int dataStartIndex = -1;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty() || line.startsWith("#")) continue;

            if (line.startsWith("LEVEL_ID:")) {
                levelId = line.substring("LEVEL_ID:".length()).trim();
            } else if (line.startsWith("DIFFICULTY:")) {
                difficulty = Difficulty.valueOf(line.substring("DIFFICULTY:".length()).trim());
            } else if (line.startsWith("DISPLAY_NAME:")) {
                displayName = line.substring("DISPLAY_NAME:".length()).trim();
            } else if (line.startsWith("SHAPE:")) {
                shape = CellShape.valueOf(line.substring("SHAPE:".length()).trim());
            } else if (line.startsWith("ADJ:")) {
                adjType = line.substring("ADJ:".length()).trim();
            } else if (line.startsWith("ROWS:")) {
                rows = Integer.parseInt(line.substring("ROWS:".length()).trim());
            } else if (line.startsWith("COLS:")) {
                cols = Integer.parseInt(line.substring("COLS:".length()).trim());
            } else if (line.equals("DATA:")) {
                dataStartIndex = i + 1;
                break;
            }
        }

        if (levelId == null || rows == 0 || cols == 0 || dataStartIndex < 0) {
            throw new IOException("Invalid level file: missing required headers in " + filePath);
        }

        if (displayName == null) displayName = levelId;

        AdjacencyStrategy strategy = resolveStrategy(adjType);
        Board board = new Board(rows, cols, shape, strategy);

        for (int i = dataStartIndex; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split(",");
            if (parts.length < 4) continue;

            int r = Integer.parseInt(parts[0].trim());
            int c = Integer.parseInt(parts[1].trim());
            int val = Integer.parseInt(parts[2].trim());
            String type = parts[3].trim();

            Cell cell = board.getCell(r, c);
            if (cell == null) continue;

            if ("VOID".equalsIgnoreCase(type)) {
                cell.setVoid(true);
            } else if ("FIXED".equalsIgnoreCase(type)) {
                cell.setFixedValue(val);
            } else {
                cell.setValue(val);
            }
        }

        return new Level(levelId, difficulty, board, displayName);
    }

    private AdjacencyStrategy resolveStrategy(String adjType) {
        switch (adjType.toUpperCase()) {
            case "SQUARE_FULL": return new SquareFullAdjacencyStrategy();
            case "HEXAGONAL":   return new HexagonalAdjacencyStrategy();
            case "TRIANGLE":    return new TriangleAdjacencyStrategy();
            default:            return new SquareAdjacencyStrategy();
        }
    }
}
