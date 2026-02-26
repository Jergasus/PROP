package model.board;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import model.adjacency.AdjacencyStrategy;
import model.cell.Cell;
import model.cell.CellShape;
import model.cell.Position;

public class Board implements Serializable {
    private final int rows;
    private final int cols;
    private final Cell[][] grid;
    private AdjacencyStrategy adjacencyStrategy;
    private final CellShape cellShape;

    public Board(int rows, int cols, CellShape cellShape, AdjacencyStrategy strategy) {
        this.rows = rows;
        this.cols = cols;
        this.cellShape = cellShape;
        this.adjacencyStrategy = strategy;
        this.grid = new Cell[rows][cols];
        initializeGrid();
    }

    // Constructor de copia
    public Board(Board other) {
        this.rows = other.rows;
        this.cols = other.cols;
        this.cellShape = other.cellShape;
        this.adjacencyStrategy = other.adjacencyStrategy; // Strategy is stateless usually, sharing reference equals sharing logic
        this.grid = new Cell[rows][cols];
        for(int i=0; i<rows; i++) {
            for(int j=0; j<cols; j++) {
                grid[i][j] = new Cell(other.grid[i][j]); // Deep copy cells
            }
        }
    }

    private void initializeGrid() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                grid[i][j] = new Cell(new Position(i, j), cellShape);
            }
        }
    }

    public void setAdjacencyStrategy(AdjacencyStrategy strategy) {
        this.adjacencyStrategy = strategy;
    }

    public Cell getCell(Position pos) {
        if (!isValidPosition(pos)) return null;
        return grid[pos.row()][pos.col()];
    }

    public Cell getCell(int r, int c) {
        return getCell(new Position(r, c));
    }

    public List<Cell> getNeighbors(Position pos) {
        List<Position> neighborPositions = adjacencyStrategy.getNeighbors(pos, rows, cols);
        List<Cell> neighbors = new ArrayList<>();
        
        for (Position p : neighborPositions) {
            Cell cell = getCell(p);
            // Solo devolvemos celdas que existen y no son huecos "Void"
            // (El algoritmo podría necesitar saber si es Void, pero para moverse, no sirve)
            if (cell != null && !cell.isVoid()) {
                neighbors.add(cell);
            }
        }
        return neighbors;
    }

    public boolean isValidPosition(Position pos) {
        return pos.row() >= 0 && pos.row() < rows &&
               pos.col() >= 0 && pos.col() < cols;
    }

    public int getRows() { return rows; }
    public int getCols() { return cols; }

    public CellShape getCellShape() {
        return cellShape;
    }

    public String getAdjacencyStrategyName() {
        return adjacencyStrategy.getClass().getSimpleName()
            .replace("AdjacencyStrategy", "")
            .replace("Square", "Cuadrado")
            .replace("Full", " Full (8 vecinos)")
            .replace("Hexagonal", "Hexagonal")
            .replace("Triangle", "Triangular");
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                sb.append(grid[i][j].toString());
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
