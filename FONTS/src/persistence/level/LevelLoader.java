package persistence.level;

import domini.model.adjacency.*;
import domini.model.board.Board;
import domini.model.cell.Cell;
import domini.model.cell.CellShape;
import domini.model.level.Difficulty;
import domini.model.level.Level;
import domini.model.level.LevelCatalog;
import persistence.HidatoFileParser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads pregenerated level files from data/levels/.
 *
 * Primary format (spec): Q,CA,4,4 / grid rows with #,*,?,N tokens.
 * Legacy format (old): LEVEL_ID:/DIFFICULTY:/DATA: headers — kept for backward compat.
 *
 * Metadata (levelId, difficulty, displayName) is derived from filename for the new format:
 *   easy_01.txt  -> EASY,  "easy_01",  "Easy 1"
 *   medium_03.txt -> MEDIUM, "medium_03", "Medium 3"
 *   hard_05.txt  -> HARD,  "hard_05",  "Hard 5"
 */
public class LevelLoader {

    private static final String LEVELS_DIR = "data/levels";
    private final HidatoFileParser parser = new HidatoFileParser();

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

        levels.sort((a, b) -> {
            int cmp = a.getDifficulty().compareTo(b.getDifficulty());
            return cmp != 0 ? cmp : a.getLevelId().compareTo(b.getLevelId());
        });

        return new LevelCatalog(levels);
    }

    public Level loadLevel(String filePath) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        if (lines.isEmpty()) throw new IOException("Empty file: " + filePath);

        String firstLine = "";
        for (String l : lines) {
            if (!l.trim().isEmpty()) { firstLine = l.trim(); break; }
        }

        String filename = Paths.get(filePath).getFileName().toString();
        String base = filename.replace(".txt", "");

        if (isNewFormat(firstLine)) {
            Board board = parser.loadBoard(filePath);
            return new Level(base, difficultyFromFilename(base), board, displayNameFromFilename(base));
        } else {
            return loadLegacyLevel(lines, base, filePath);
        }
    }

    private boolean isNewFormat(String firstLine) {
        return firstLine.matches("[QHT],CA?,\\d+,\\d+");
    }

    // -----------------------------------------------------------------------
    // Metadata helpers for new format (derived from filename)
    // -----------------------------------------------------------------------

    private Difficulty difficultyFromFilename(String base) {
        String lower = base.toLowerCase();
        if (lower.startsWith("hard"))   return Difficulty.HARD;
        if (lower.startsWith("medium")) return Difficulty.MEDIUM;
        return Difficulty.EASY;
    }

    private String displayNameFromFilename(String base) {
        String[] parts = base.split("_");
        if (parts.length >= 2) {
            String tier = capitalize(parts[0]);
            try {
                int num = Integer.parseInt(parts[1]);
                return tier + " " + num;
            } catch (NumberFormatException ignored) {}
        }
        return base;
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
    }

    // -----------------------------------------------------------------------
    // Legacy format (LEVEL_ID:/DIFFICULTY:/DATA: headers)
    // -----------------------------------------------------------------------

    private Level loadLegacyLevel(List<String> lines, String fallbackBase, String filePath) throws IOException {
        String levelId = fallbackBase;
        Difficulty difficulty = Difficulty.EASY;
        String displayName = fallbackBase;
        CellShape shape = CellShape.SQUARE;
        String adjType = "SQUARE";
        int rows = 0, cols = 0;
        int dataStartIndex = -1;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty() || line.startsWith("#")) continue;

            if (line.startsWith("LEVEL_ID:"))        levelId     = line.substring("LEVEL_ID:".length()).trim();
            else if (line.startsWith("DIFFICULTY:"))  difficulty  = Difficulty.valueOf(line.substring("DIFFICULTY:".length()).trim());
            else if (line.startsWith("DISPLAY_NAME:"))displayName = line.substring("DISPLAY_NAME:".length()).trim();
            else if (line.startsWith("SHAPE:"))       shape       = CellShape.valueOf(line.substring("SHAPE:".length()).trim());
            else if (line.startsWith("ADJ:"))         adjType     = line.substring("ADJ:".length()).trim();
            else if (line.startsWith("ROWS:"))        rows        = Integer.parseInt(line.substring("ROWS:".length()).trim());
            else if (line.startsWith("COLS:"))        cols        = Integer.parseInt(line.substring("COLS:".length()).trim());
            else if (line.equals("DATA:"))            { dataStartIndex = i + 1; break; }
        }

        if (rows == 0 || cols == 0 || dataStartIndex < 0)
            throw new IOException("Invalid legacy level file: " + filePath);

        AdjacencyStrategy strategy = resolveStrategy(adjType);
        Board board = new Board(rows, cols, shape, strategy);

        for (int i = dataStartIndex; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) continue;
            String[] parts = line.split(",");
            if (parts.length < 4) continue;

            int r    = Integer.parseInt(parts[0].trim());
            int c    = Integer.parseInt(parts[1].trim());
            int val  = Integer.parseInt(parts[2].trim());
            String t = parts[3].trim();

            Cell cell = board.getCell(r, c);
            if (cell == null) continue;
            if ("VOID".equalsIgnoreCase(t))        cell.setVoid(true);
            else if ("FIXED".equalsIgnoreCase(t))  cell.setFixedValue(val);
            else                                    cell.setValue(val);
        }

        return new Level(levelId, difficulty, board, displayName);
    }

    private AdjacencyStrategy resolveStrategy(String adjType) {
        return switch (adjType.toUpperCase()) {
            case "SQUARE_FULL" -> new SquareFullAdjacencyStrategy();
            case "HEXAGONAL"   -> new HexagonalAdjacencyStrategy();
            case "TRIANGLE"    -> new TriangleAdjacencyStrategy();
            default            -> new SquareAdjacencyStrategy();
        };
    }
}
