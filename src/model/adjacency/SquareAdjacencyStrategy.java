package model.adjacency;

import model.cell.Position;
import java.util.ArrayList;
import java.util.List;

/**
 * Adyacencia ortogonal (lados) para celdas cuadradas.
 * Vecinos: Arriba, Abajo, Izquierda, Derecha.
 */
public class SquareAdjacencyStrategy implements AdjacencyStrategy {

    private static final int[][] DIRECTIONS = {
        {-1, 0}, // Arriba
        {1, 0},  // Abajo
        {0, -1}, // Izquierda
        {0, 1}   // Derecha
    };

    /**
     * O(1): two positions are orthogonally adjacent iff their Manhattan
     * distance is exactly 1.
     */
    @Override
    public boolean areAdjacent(Position p1, Position p2, int rows, int cols) {
        return Math.abs(p1.row() - p2.row()) + Math.abs(p1.col() - p2.col()) == 1;
    }

    @Override
    public List<Position> getNeighbors(Position pos, int rows, int cols) {
        List<Position> neighbors = new ArrayList<>();
        
        for (int[] dir : DIRECTIONS) {
            int newRow = pos.row() + dir[0];
            int newCol = pos.col() + dir[1];
            
            if (newRow >= 0 && newRow < rows && newCol >= 0 && newCol < cols) {
                neighbors.add(new Position(newRow, newCol));
            }
        }
        return neighbors;
    }
}
