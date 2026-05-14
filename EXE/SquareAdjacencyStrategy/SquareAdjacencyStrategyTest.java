import static org.junit.Assert.*;
import org.junit.Test;

import java.util.List;
import domini.model.adjacency.SquareAdjacencyStrategy;
import domini.model.cell.Position;

public class SquareAdjacencyStrategyTest {

    private final SquareAdjacencyStrategy strat = new SquareAdjacencyStrategy();

    @Test
    public void center_has4Neighbors() {
        List<Position> n = strat.getNeighbors(new Position(1, 1), 3, 3);
        assertEquals(4, n.size());
    }

    @Test
    public void corner_has2Neighbors() {
        List<Position> n = strat.getNeighbors(new Position(0, 0), 3, 3);
        assertEquals(2, n.size());
    }

    @Test
    public void edge_has3Neighbors() {
        List<Position> n = strat.getNeighbors(new Position(0, 1), 3, 3);
        assertEquals(3, n.size());
    }

    @Test
    public void noDiagonalNeighbors() {
        List<Position> n = strat.getNeighbors(new Position(1, 1), 3, 3);
        for (Position p : n) {
            int dr = Math.abs(p.row() - 1);
            int dc = Math.abs(p.col() - 1);
            assertTrue("No diagonal expected", dr == 0 || dc == 0);
        }
    }

    @Test
    public void areAdjacent_orthogonal_true() {
        assertTrue(strat.areAdjacent(new Position(0, 0), new Position(0, 1), 3, 3));
        assertTrue(strat.areAdjacent(new Position(1, 1), new Position(2, 1), 3, 3));
    }

    @Test
    public void areAdjacent_diagonal_false() {
        assertFalse(strat.areAdjacent(new Position(0, 0), new Position(1, 1), 3, 3));
    }

    @Test
    public void areAdjacent_sameCell_false() {
        assertFalse(strat.areAdjacent(new Position(1, 1), new Position(1, 1), 3, 3));
    }
}
