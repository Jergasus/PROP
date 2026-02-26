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
