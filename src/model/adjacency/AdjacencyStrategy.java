package model.adjacency;

import model.cell.Position;
import java.util.List;
import java.io.Serializable;

/**
 * Strategy pattern interface for determining cell neighbors.
 * This allows us to switch between Square (4-way), Square (8-way), 
 * Hexagonal, and Triangular connectivity without changing the Board class.
 */
public interface AdjacencyStrategy extends Serializable {
    /**
     * Calcula las posiciones adyacentes a una posición dada,
     * respetando los límites del tablero.
     */
    List<Position> getNeighbors(Position pos, int rows, int cols);
}
