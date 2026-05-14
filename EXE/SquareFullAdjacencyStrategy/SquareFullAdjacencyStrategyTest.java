import static org.junit.Assert.*;
import org.junit.Test;

import java.util.List;
import domini.model.adjacency.SquareFullAdjacencyStrategy;
import domini.model.cell.Position;

public class SquareFullAdjacencyStrategyTest {

    private final SquareFullAdjacencyStrategy strat = new SquareFullAdjacencyStrategy();

    @Test
    public void center_has8Neighbors() {
        List<Position> n = strat.getNeighbors(new Position(1, 1), 3, 3);
        assertEquals(8, n.size());
    }

    @Test
    public void corner_has3Neighbors() {
        List<Position> n = strat.getNeighbors(new Position(0, 0), 3, 3);
        assertEquals(3, n.size());
    }

    @Test
    public void edge_has5Neighbors() {
        List<Position> n = strat.getNeighbors(new Position(0, 1), 3, 3);
        assertEquals(5, n.size());
    }

    @Test
    public void diagonalNeighborIncluded() {
        List<Position> n = strat.getNeighbors(new Position(1, 1), 3, 3);
        boolean hasDiag = n.stream().anyMatch(p -> p.row() == 0 && p.col() == 0);
        assertTrue(hasDiag);
    }

    @Test
    public void areAdjacent_diagonal_true() {
        assertTrue(strat.areAdjacent(new Position(0, 0), new Position(1, 1), 3, 3));
    }

    @Test
    public void areAdjacent_orthogonal_true() {
        assertTrue(strat.areAdjacent(new Position(0, 0), new Position(0, 1), 3, 3));
    }

    @Test
    public void areAdjacent_distant_false() {
        assertFalse(strat.areAdjacent(new Position(0, 0), new Position(2, 2), 3, 3));
    }
}
