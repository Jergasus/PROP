import org.junit.Test;
import static org.junit.Assert.*;

import domini.model.adjacency.SquareAdjacencyStrategy;
import domini.model.cell.Position;
import java.util.List;

public class SquareAdjacencyStrategyTest {

    @Test
    public void testAreAdjacent() {
        SquareAdjacencyStrategy strategy = new SquareAdjacencyStrategy();
        
        // Cas normal: adjacents horitzontalment i verticalment
        assertTrue(strategy.areAdjacent(new Position(1, 1), new Position(1, 2), 3, 3));
        assertTrue(strategy.areAdjacent(new Position(1, 1), new Position(2, 1), 3, 3));
        
        // Casos no adjacents (diagonal o lluny)
        assertFalse(strategy.areAdjacent(new Position(1, 1), new Position(2, 2), 3, 3)); // Diagonal
        assertFalse(strategy.areAdjacent(new Position(0, 0), new Position(0, 2), 3, 3)); // Mateixa fila, no adjacents
    }

    @Test
    public void testGetNeighborsCenter() {
        SquareAdjacencyStrategy strategy = new SquareAdjacencyStrategy();
        List<Position> neighbors = strategy.getNeighbors(new Position(1, 1), 3, 3);
        
        // En una graella de 3x3, el centre (1,1) té 4 veïns
        assertEquals(4, neighbors.size());
        assertTrue(neighbors.contains(new Position(0, 1)));
        assertTrue(neighbors.contains(new Position(2, 1)));
        assertTrue(neighbors.contains(new Position(1, 0)));
        assertTrue(neighbors.contains(new Position(1, 2)));
    }

    @Test
    public void testGetNeighborsCorner() {
        // Cas extrem: Cantonada superior esquerra
        SquareAdjacencyStrategy strategy = new SquareAdjacencyStrategy();
        List<Position> neighbors = strategy.getNeighbors(new Position(0, 0), 3, 3);
        
        // Una cantonada només té 2 veïns (dins dels límits)
        assertEquals(2, neighbors.size());
        assertTrue(neighbors.contains(new Position(0, 1)));
        assertTrue(neighbors.contains(new Position(1, 0)));
    }
}