import static org.junit.Assert.*;
import org.junit.Test;

import java.util.List;
import domini.model.adjacency.TriangleAdjacencyStrategy;
import domini.model.cell.Position;

public class TriangleAdjacencyStrategyTest {

    private final TriangleAdjacencyStrategy strat = new TriangleAdjacencyStrategy();

    @Test
    public void innerCell_hasNeighbors() {
        List<Position> n = strat.getNeighbors(new Position(1, 1), 4, 4);
        assertTrue(n.size() > 0);
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
    public void upTriangle_hasAtMost3Neighbors() {
        // (0,0): up-pointing triangle (0+0=even)
        List<Position> n = strat.getNeighbors(new Position(0, 0), 4, 4);
        assertTrue(n.size() <= 3);
    }

    @Test
    public void downTriangle_hasAtMost3Neighbors() {
        // (0,1): down-pointing triangle (0+1=odd)
        List<Position> n = strat.getNeighbors(new Position(0, 1), 4, 4);
        assertTrue(n.size() <= 3);
    }

    @Test
    public void adjacency_isSymmetric() {
        Position a = new Position(1, 2);
        List<Position> neighbors = strat.getNeighbors(a, 4, 4);
        for (Position b : neighbors) {
            assertTrue("Symmetry broken: " + a + " -> " + b,
                strat.areAdjacent(a, b, 4, 4));
            assertTrue("Symmetry broken: " + b + " -> " + a,
                strat.areAdjacent(b, a, 4, 4));
        }
    }

    @Test
    public void nonAdjacent_cells_false() {
        assertFalse(strat.areAdjacent(new Position(0, 0), new Position(3, 3), 4, 4));
    }
}
