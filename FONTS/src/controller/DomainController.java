package controller;

import model.adjacency.*;
import model.algorithms.Solver;
import model.algorithms.Validator;
import model.board.Board;
import model.cell.CellShape;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DomainController {

    private final Solver    solver    = new Solver();
    private final Validator validator = new Validator();
    private final List<HidatoCaseController> catalog;

    private Board activeBoard;

    public DomainController() {
        catalog = buildCatalog();
    }

    public int    getCatalogSize()                  { return catalog.size(); }
    public String getCatalogCaseName(int i)         { return catalog.get(i).getName(); }
    public String getCatalogCaseAdjacency(int i)    { return catalog.get(i).getAdjacencyDesc(); }
    public boolean getCatalogCaseSolvable(int i)    { return catalog.get(i).isExpectedSolvable(); }
    public String getCatalogCaseDescription(int i)  { return catalog.get(i).getDescription(); }

    public void selectCase(int index) {
        activeBoard = catalog.get(index).getBoard();
    }

    public String getActiveBoardAsString() {
        if (activeBoard == null) return "(no board selected)";
        return activeBoard.toString();
    }

    public boolean isPartiallyValid() {
        if (activeBoard == null) return false;
        return validator.isPartiallyValid(activeBoard);
    }

    public boolean isValidSolution() {
        if (activeBoard == null) return false;
        return validator.isValidSolution(activeBoard);
    }

    public boolean hasSolution() {
        if (activeBoard == null) return false;
        Board copy = new Board(activeBoard);
        return solver.solve(copy);
    }

    public boolean solve() {
        if (activeBoard == null) return false;
        return solver.solve(activeBoard);
    }

    private static final String HIDATOS_DIR = "../EXE/DriverHidato/hidatos";

    private List<HidatoCaseController> buildCatalog() {
        List<HidatoCaseController> list = new ArrayList<>();
        File dir = new File(HIDATOS_DIR);
        File[] files = dir.listFiles((d, name) -> name.endsWith(".txt"));

        if (files == null || files.length == 0) {
            System.err.println("[DomainController] No hidato files found in " + HIDATOS_DIR);
            return list;
        }

        Arrays.sort(files);
        for (File f : files) {
            try {
                list.add(loadFromFile(f));
            } catch (Exception e) {
                System.err.println("[DomainController] Could not load " + f.getName() + ": " + e.getMessage());
            }
        }
        return list;
    }

    private HidatoCaseController loadFromFile(File file) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null)
                if (!line.trim().isEmpty()) lines.add(line.trim());
        }
        if (lines.size() < 6)
            throw new IOException("File too short (expected at least 6 lines)");

        String  name     = lines.get(0);
        String  adjDesc  = lines.get(1);
        boolean solvable = lines.get(2).equalsIgnoreCase("SOLVABLE");
        String  desc     = lines.get(3);
        String[] boardLines = lines.subList(4, lines.size()).toArray(new String[0]);

        return parse(name, adjDesc, solvable, desc, boardLines);
    }

    private HidatoCaseController parse(String name, String adjDesc, boolean solvable,
                                       String description, String[] lines) {
        String[] header = lines[0].split(",");
        String typeCode = header[0].trim();
        String adjCode  = header[1].trim();
        int rows        = Integer.parseInt(header[2].trim());
        int cols        = Integer.parseInt(header[3].trim());

        CellShape shape;
        AdjacencyStrategy strategy;
        switch (typeCode) {
            case "H":
                shape    = CellShape.HEXAGON;
                strategy = new HexagonalAdjacencyStrategy();
                break;
            case "T":
                shape    = CellShape.TRIANGLE;
                strategy = new TriangleAdjacencyStrategy();
                break;
            default:
                shape    = CellShape.SQUARE;
                strategy = adjCode.equals("CA")
                    ? new SquareFullAdjacencyStrategy()
                    : new SquareAdjacencyStrategy();
        }

        Board board = new Board(rows, cols, shape, strategy);

        for (int r = 0; r < rows; r++) {
            String[] tokens = lines[r + 1].split(",");
            for (int c = 0; c < cols; c++) {
                String token = tokens[c].trim();
                switch (token) {
                    case "#": case "*": board.getCell(r, c).setVoid(true); break;
                    case "?": break;
                    default:  board.getCell(r, c).setFixedValue(Integer.parseInt(token));
                }
            }
        }

        return new HidatoCaseController(name, adjDesc, solvable, description, board);
    }
}
