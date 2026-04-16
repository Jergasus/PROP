package model.board;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
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

    /**
     * Returns true if p1 and p2 are adjacent according to the current strategy.
     * Delegates to AdjacencyStrategy.areAdjacent() which may be O(1) for simple
     * geometries (Square, SquareFull) or O(k) for Hex/Triangle via the default.
     */
    public boolean areAdjacent(Position p1, Position p2) {
        return adjacencyStrategy.areAdjacent(p1, p2, rows, cols);
    }

    public int getRows() { return rows; }
    public int getCols() { return cols; }

    /** Returns the number of non-void (playable) cells on the board. */
    public int getCellCount() {
        int count = 0;
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                if (grid[i][j] != null && !grid[i][j].isVoid()) count++;
        return count;
    }

    public CellShape getCellShape() {
        return cellShape;
    }

    public AdjacencyStrategy getAdjacencyStrategy() {
        return adjacencyStrategy;
    }

    public String getAdjacencyStrategyName() {
        return adjacencyStrategy.getClass().getSimpleName()
            .replace("AdjacencyStrategy", "")
            .replace("Square", "Cuadrado")
            .replace("Full", " Full (8 vecinos)")
            .replace("Hexagonal", "Hexagonal")
            .replace("Triangle", "Triangular");
    }

    /**
     * Iteratively marks degree-1 cells as void until no dead-ends remain.
     * A dead-end is a non-void cell with fewer than 2 non-void neighbors.
     * This is essential for triangle/hex grids where corners may have
     * only 1 neighbor, making Hamiltonian paths impossible.
     *
     * @return the number of cells that were voided
     */
    public int pruneDeadEnds() {
        int totalPruned = 0;
        boolean changed = true;
        while (changed) {
            changed = false;
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    Cell cell = grid[i][j];
                    if (cell.isVoid()) continue;
                    int neighborCount = getNeighbors(cell.getPosition()).size();
                    if (neighborCount < 2) {
                        cell.setVoid(true);
                        totalPruned++;
                        changed = true;
                    }
                }
            }
        }
        return totalPruned;
    }

    /**
     * Randomly places numVoids void cells while keeping all remaining
     * non-void cells connected.
     *
     * @return true if all voids were placed successfully
     */
    public boolean placeRandomVoids(int numVoids, Random random) {
        if (numVoids <= 0) return true;
        if (numVoids >= getCellCount() - 1) return false;

        List<Position> candidates = new ArrayList<>();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (!grid[i][j].isVoid()) {
                    candidates.add(new Position(i, j));
                }
            }
        }
        Collections.shuffle(candidates, random);

        int placed = 0;
        for (Position pos : candidates) {
            if (placed >= numVoids) break;
            Cell cell = grid[pos.row()][pos.col()];
            cell.setVoid(true);
            if (isConnected()) {
                placed++;
            } else {
                cell.setVoid(false); // restore — would disconnect the board
            }
        }
        return placed == numVoids;
    }

    /**
     * Checks whether all non-void cells form a single connected component
     * using BFS through the adjacency strategy.
     */
    public boolean isConnected() {
        // Find first non-void cell
        Position start = null;
        int totalNonVoid = 0;
        for (int i = 0; i < rows && start == null; i++) {
            for (int j = 0; j < cols && start == null; j++) {
                if (!grid[i][j].isVoid()) {
                    start = new Position(i, j);
                }
            }
        }
        if (start == null) return true; // all void is trivially connected

        // BFS
        boolean[][] visited = new boolean[rows][cols];
        Queue<Position> queue = new LinkedList<>();
        queue.add(start);
        visited[start.row()][start.col()] = true;
        int reachable = 1;

        while (!queue.isEmpty()) {
            Position current = queue.poll();
            for (Cell neighbor : getNeighbors(current)) {
                Position np = neighbor.getPosition();
                if (!visited[np.row()][np.col()]) {
                    visited[np.row()][np.col()] = true;
                    reachable++;
                    queue.add(np);
                }
            }
        }

        return reachable == getCellCount();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rows; i++) {
            // Hex boards: indent odd rows to show offset visually
            if (cellShape == CellShape.HEXAGON && i % 2 != 0) {
                sb.append("  ");
            }
            for (int j = 0; j < cols; j++) {
                sb.append(grid[i][j].toString());
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
