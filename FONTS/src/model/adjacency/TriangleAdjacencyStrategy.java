package model.adjacency;

import model.cell.Position;
import java.util.ArrayList;
import java.util.List;

public class TriangleAdjacencyStrategy implements AdjacencyStrategy {

    @Override
    public List<Position> getNeighbors(Position pos, int rows, int cols) {
        List<Position> neighbors = new ArrayList<>();
        int r = pos.row();
        int c = pos.col();

        if (c > 0) neighbors.add(new Position(r, c - 1));
        if (c < cols - 1) neighbors.add(new Position(r, c + 1));

        // vertical neighbor depends on triangle orientation
        if ((r + c) % 2 == 0) {
            if (r < rows - 1) neighbors.add(new Position(r + 1, c));
        } else {
            if (r > 0) neighbors.add(new Position(r - 1, c));
        }

        return neighbors;
    }
}
