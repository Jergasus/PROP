import org.junit.Test;
import static org.junit.Assert.*;

import domini.model.adjacency.SquareFullAdjacencyStrategy;
import domini.model.cell.Position;
import java.util.List;

public class SquareFullAdjacencyStrategyTest {

    @Test
    public void testAreAdjacent() {
        SquareFullAdjacencyStrategy strategy = new SquareFullAdjacencyStrategy();
        
        // Horitzontal i vertical
        assertTrue(strategy.areAdjacent(new Position(1, 1), new Position(1, 2), 3, 3));
        assertTrue(strategy.areAdjacent(new Position(1, 1), new Position(2, 1), 3, 3));
        
        // Diagonal (aquesta és la diferència clau amb la Square normal)
        assertTrue(strategy.areAdjacent(new Position(1, 1), new Position(2, 2), 3, 3));
        assertTrue(strategy.areAdjacent(new Position(1, 1), new Position(0, 0), 3, 3));
        
        // Massa lluny
        assertFalse(strategy.areAdjacent(new Position(0, 0), new Position(0, 2), 3, 3));
        assertFalse(strategy.areAdjacent(new Position(0, 0), new Position(2, 2), 3, 3));
    }

    @Test
    public void testGetNeighborsCenter() {
        SquareFullAdjacencyStrategy strategy = new SquareFullAdjacencyStrategy();
        List<Position> neighbors = strategy.getNeighbors(new Position(1, 1), 3, 3);
        
        // En 8-way, el centre d'un 3x3 ha de tenir exactament 8 veïns
        assertEquals(8, neighbors.size());
        assertTrue(neighbors.contains(new Position(0, 0)));
        assertTrue(neighbors.contains(new Position(2, 2)));
    }

    @Test
    public void testGetNeighborsCorner() {
        SquareFullAdjacencyStrategy strategy = new SquareFullAdjacencyStrategy();
        List<Position> neighbors = strategy.getNeighbors(new Position(0, 0), 3, 3);
        
        // Una cantonada en 8-way té 3 veïns (dreta, abaix, i diagonal inferior-dreta)
        assertEquals(3, neighbors.size());
        assertTrue(neighbors.contains(new Position(0, 1)));
        assertTrue(neighbors.contains(new Position(1, 0)));
        assertTrue(neighbors.contains(new Position(1, 1)));
    }
}