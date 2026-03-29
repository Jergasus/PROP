package model.adjacency;

import model.cell.Position;
import java.util.ArrayList;
import java.util.List;

public class SquareFullAdjacencyStrategy implements AdjacencyStrategy {

    private static final int[][] DIRECTIONS = {
        {-1, -1}, {-1, 0}, {-1, 1},
        {0, -1},           {0, 1},
        {1, -1},  {1, 0},  {1, 1}
    };

    @Override
    public boolean areAdjacent(Position p1, Position p2, int rows, int cols) {
        int dr = Math.abs(p1.row() - p2.row());
        int dc = Math.abs(p1.col() - p2.col());
        return dr <= 1 && dc <= 1 && (dr + dc > 0);
    }

    @Override
    public List<Position> getNeighbors(Position pos, int rows, int cols) {
        List<Position> neighbors = new ArrayList<>();
        for (int[] dir : DIRECTIONS) {
            int newRow = pos.row() + dir[0];
            int newCol = pos.col() + dir[1];
            if (newRow >= 0 && newRow < rows && newCol >= 0 && newCol < cols)
                neighbors.add(new Position(newRow, newCol));
        }
        return neighbors;
    }
}
