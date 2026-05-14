package persistence;

import model.adjacency.AdjacencyStrategy;
import model.adjacency.HexagonalAdjacencyStrategy;
import model.adjacency.SquareAdjacencyStrategy;
import model.board.Board;
import model.cell.Cell;
import model.cell.CellShape;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Implementación simple basada en texto.
 * Formato:
 * SHAPE: [SQUARE|HEXAGON|TRIANGLE]
 * ROWS: [int]
 * COLS: [int]
 * DATA:
 * row,col,value,type
 * ...
 */
public class SimpleTextSerializer implements BoardSerializer {

    @Override
    public void saveBoard(Board board, String filePath) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println("SHAPE:" + board.getCellShape()); 
            writer.println("ROWS:" + board.getRows());
            writer.println("COLS:" + board.getCols());
            writer.println("DATA:");
            
            for (int i = 0; i < board.getRows(); i++) {
                for (int j = 0; j < board.getCols(); j++) {
                    Cell cell = board.getCell(i, j);
                    if (cell == null) continue;
                    
                    // Format: r,c,val,type(FIXED/VOID/NORMAL)
                    String type = "NORMAL";
                    if (cell.isVoid()) type = "VOID";
                    else if (cell.isFixed()) type = "FIXED";
                    
                    writer.printf("%d,%d,%d,%s%n", i, j, cell.getValue(), type);
                }
            }
        }
    }

    @Override
    public Board loadBoard(String filePath) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        CellShape shape = CellShape.SQUARE;
        int rows = 0;
        int cols = 0;
        int dataStartIndex = -1;
        
        // 1. Parse Header
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) continue;
            
            if (line.startsWith("SHAPE:")) {
                shape = CellShape.valueOf(line.split(":")[1].trim());
            } else if (line.startsWith("ROWS:")) {
                rows = Integer.parseInt(line.split(":")[1].trim());
            } else if (line.startsWith("COLS:")) {
                cols = Integer.parseInt(line.split(":")[1].trim());
            } else if (line.equals("DATA:")) {
                dataStartIndex = i + 1;
                break;
            }
        }

        if (rows == 0 || cols == 0) {
            throw new IOException("Invalid file format: Missing ROWS or COLS");
        }

        // 2. Create Board
        AdjacencyStrategy strategy;
        switch (shape) {
            case HEXAGON: strategy = new HexagonalAdjacencyStrategy(); break;
            case SQUARE: default: strategy = new SquareAdjacencyStrategy(); break;
        }

        Board board = new Board(rows, cols, shape, strategy);

        // 3. Parse Data
        if (dataStartIndex != -1) {
            for (int i = dataStartIndex; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split(",");
                if (parts.length < 4) continue;

                int r = Integer.parseInt(parts[0].trim());
                int c = Integer.parseInt(parts[1].trim());
                int val = Integer.parseInt(parts[2].trim());
                String type = parts[3].trim(); // FIXED, VOID, NORMAL

                Cell cell = board.getCell(r, c);
                if (cell != null) {
                    if ("VOID".equalsIgnoreCase(type)) {
                        cell.setVoid(true);
                    } else if ("FIXED".equalsIgnoreCase(type)) {
                        cell.setFixedValue(val);
                    } else {
                        cell.setValue(val);
                    }
                }
            }
        }
        
        return board;
    }
}
