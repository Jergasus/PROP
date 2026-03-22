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

    /**
     * Comprueba si dos posiciones son adyacentes según esta estrategia.
     *
     * La implementación por defecto delega en getNeighbors() (O(k)).
     * Las subclases con geometría simple pueden sobreescribirla con
     * aritmética directa para evitar la alocación de la lista (O(1)).
     */
    default boolean areAdjacent(Position p1, Position p2, int rows, int cols) {
        return getNeighbors(p1, rows, cols).contains(p2);
    }
}
