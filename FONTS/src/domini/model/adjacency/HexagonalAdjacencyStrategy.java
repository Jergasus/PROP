package domini.model.adjacency;

import domini.model.cell.Position;
import java.util.ArrayList;
import java.util.List;

public class HexagonalAdjacencyStrategy implements AdjacencyStrategy {

    private static final int[][] EVEN_ROW_DIRS = {
        {-1, -1}, {-1, 0},
        {0, -1},  {0, 1},
        {1, -1},  {1, 0}
    };

    private static final int[][] ODD_ROW_DIRS = {
        {-1, 0}, {-1, 1},
        {0, -1}, {0, 1},
        {1, 0},  {1, 1}
    };

    @Override
    public List<Position> getNeighbors(Position pos, int rows, int cols) {
        List<Position> neighbors = new ArrayList<>();
        int r = pos.row();
        int c = pos.col();
        int[][] directions = (r % 2 == 0) ? EVEN_ROW_DIRS : ODD_ROW_DIRS;

        for (int[] dir : directions) {
            int nr = r + dir[0];
            int nc = c + dir[1];
            if (nr >= 0 && nr < rows && nc >= 0 && nc < cols)
                neighbors.add(new Position(nr, nc));
        }
        return neighbors;
    }
}
