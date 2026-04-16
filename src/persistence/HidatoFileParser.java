package persistence;

import model.adjacency.*;
import model.board.Board;
import model.cell.Cell;
import model.cell.CellShape;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Reads and writes boards in the official Hidato file format:
 *
 *   Q,CA,3,4
 *   #,1,?,#
 *   ?,?,?,?
 *   7,?,9,#
 *
 * Where:
 *   Line 1: cellType(Q/H/T), adjacency(C/CA), rows, cols
 *   Data:   # = void, * = inaccessible (also void), ? = empty, N = fixed clue
 */
public class HidatoFileParser implements BoardSerializer {

    @Override
    public Board loadBoard(String filePath) throws IOException {
        List<String> lines = Files.readAllLines(Path.of(filePath));
        if (lines.isEmpty()) throw new IOException("Empty file");

        // Parse header
        String[] header = lines.get(0).split(",");
        if (header.length < 4) throw new IOException("Invalid header: " + lines.get(0));

        CellShape shape = parseShape(header[0].trim());
        AdjacencyStrategy strategy = parseStrategy(header[0].trim(), header[1].trim());
        int rows = Integer.parseInt(header[2].trim());
        int cols = Integer.parseInt(header[3].trim());

        if (lines.size() < rows + 1) {
            throw new IOException("Expected " + rows + " data rows, got " + (lines.size() - 1));
        }

        Board board = new Board(rows, cols, shape, strategy);

        for (int i = 0; i < rows; i++) {
            String[] tokens = lines.get(i + 1).split(",");
            for (int j = 0; j < Math.min(tokens.length, cols); j++) {
                String token = tokens[j].trim();
                Cell cell = board.getCell(i, j);

                if (token.equals("#") || token.equals("*")) {
                    cell.setVoid(true);
                } else if (token.equals("?")) {
                    // empty — default state, nothing to do
                } else {
                    int value = Integer.parseInt(token);
                    cell.setFixedValue(value);
                }
            }
        }

        return board;
    }

    @Override
    public void saveBoard(Board board, String filePath) throws IOException {
        StringBuilder sb = new StringBuilder();

        // Header
        String shapeCode = encodeShape(board.getCellShape());
        String adjCode = encodeAdjacency(board.getAdjacencyStrategy());
        sb.append(shapeCode).append(",").append(adjCode).append(",")
          .append(board.getRows()).append(",").append(board.getCols()).append("\n");

        // Data rows
        for (int i = 0; i < board.getRows(); i++) {
            for (int j = 0; j < board.getCols(); j++) {
                if (j > 0) sb.append(",");
                Cell cell = board.getCell(i, j);
                if (cell.isVoid()) {
                    sb.append("#");
                } else if (cell.getValue() == 0) {
                    sb.append("?");
                } else {
                    sb.append(cell.getValue());
                }
            }
            sb.append("\n");
        }

        Files.writeString(Path.of(filePath), sb.toString());
    }

    // -----------------------------------------------------------------------
    // Mapping helpers
    // -----------------------------------------------------------------------

    private CellShape parseShape(String code) throws IOException {
        return switch (code.toUpperCase()) {
            case "Q" -> CellShape.SQUARE;
            case "H" -> CellShape.HEXAGON;
            case "T" -> CellShape.TRIANGLE;
            default -> throw new IOException("Unknown cell type: " + code);
        };
    }

    private AdjacencyStrategy parseStrategy(String shapeCode, String adjCode) throws IOException {
        return switch (shapeCode.toUpperCase()) {
            case "Q" -> adjCode.equalsIgnoreCase("CA")
                    ? new SquareFullAdjacencyStrategy()
                    : new SquareAdjacencyStrategy();
            case "H" -> new HexagonalAdjacencyStrategy();
            case "T" -> new TriangleAdjacencyStrategy();
            default -> throw new IOException("Unknown shape for strategy: " + shapeCode);
        };
    }

    private String encodeShape(CellShape shape) {
        return switch (shape) {
            case SQUARE   -> "Q";
            case HEXAGON  -> "H";
            case TRIANGLE -> "T";
        };
    }

    private String encodeAdjacency(AdjacencyStrategy strategy) {
        if (strategy instanceof SquareFullAdjacencyStrategy) return "CA";
        return "C";
    }
}
