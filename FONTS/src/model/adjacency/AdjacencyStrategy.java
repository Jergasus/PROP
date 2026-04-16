package model.adjacency;

import model.cell.Position;
import java.util.List;
import java.io.Serializable;

public interface AdjacencyStrategy extends Serializable {
    List<Position> getNeighbors(Position pos, int rows, int cols);

    default boolean areAdjacent(Position p1, Position p2, int rows, int cols) {
        return getNeighbors(p1, rows, cols).contains(p2);
    }
}
