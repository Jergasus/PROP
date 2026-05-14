import static org.junit.Assert.*;
import org.junit.Test;

import java.util.List;
import domini.model.adjacency.HexagonalAdjacencyStrategy;
import domini.model.cell.Position;

public class HexagonalAdjacencyStrategyTest {

    private final HexagonalAdjacencyStrategy strat = new HexagonalAdjacencyStrategy();

    @Test
    public void evenRow_innerCell_has6Neighbors() {
        // 4x4 grid, (2,1) is an even row inner cell
        List<Position> n = strat.getNeighbors(new Position(2, 1), 4, 4);
        assertEquals(6, n.size());
    }

    @Test
    public void oddRow_innerCell_has6Neighbors() {
        // 4x4 grid, (1,1) is odd row inner cell
        List<Position> n = strat.getNeighbors(new Position(1, 1), 4, 4);
        assertEquals(6, n.size());
    }

    @Test
    public void cornerCell_hasFewerNeighbors() {
        List<Position> n = strat.getNeighbors(new Position(0, 0), 4, 4);
        assertTrue(n.size() < 6);
    }

    @Test
    public void neighborsAreWithinBounds() {
        List<Position> n = strat.getNeighbors(new Position(1, 2), 4, 4);
        for (Position p : n) {
            assertTrue(p.row() >= 0 && p.row() < 4);
            assertTrue(p.col() >= 0 && p.col() < 4);
        }
    }

    @Test
    public void adjacency_isSymmetric() {
        Position a = new Position(1, 1);
        List<Position> neighborsOfA = strat.getNeighbors(a, 4, 4);
        for (Position b : neighborsOfA) {
            assertTrue("Symmetry broken for " + b,
                strat.areAdjacent(a, b, 4, 4));
            assertTrue("Symmetry broken for " + b,
                strat.areAdjacent(b, a, 4, 4));
        }
    }
}
