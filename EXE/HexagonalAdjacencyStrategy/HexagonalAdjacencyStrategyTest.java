import org.junit.Test;
import static org.junit.Assert.*;

import domini.model.adjacency.HexagonalAdjacencyStrategy;
import domini.model.cell.Position;
import java.util.List;

public class HexagonalAdjacencyStrategyTest {

    @Test
    public void testGetNeighborsEvenRow() {
        HexagonalAdjacencyStrategy strategy = new HexagonalAdjacencyStrategy();
        // Fila 2 (parella). En un tauler gran, ha de tenir 6 veïns.
        List<Position> neighbors = strategy.getNeighbors(new Position(2, 2), 5, 5);
        
        assertEquals(6, neighbors.size());
        // Segons EVEN_ROW_DIRS: {-1,-1}, {-1,0}, {0,-1}, {0,1}, {1,-1}, {1,0}
        assertTrue(neighbors.contains(new Position(1, 1)));
        assertTrue(neighbors.contains(new Position(1, 2)));
        assertTrue(neighbors.contains(new Position(2, 1)));
        assertTrue(neighbors.contains(new Position(2, 3)));
        assertTrue(neighbors.contains(new Position(3, 1)));
        assertTrue(neighbors.contains(new Position(3, 2)));
    }

    @Test
    public void testGetNeighborsOddRow() {
        HexagonalAdjacencyStrategy strategy = new HexagonalAdjacencyStrategy();
        // Fila 1 (senar).
        List<Position> neighbors = strategy.getNeighbors(new Position(1, 1), 5, 5);
        
        assertEquals(6, neighbors.size());
        // Segons ODD_ROW_DIRS: {-1,0}, {-1,1}, {0,-1}, {0,1}, {1,0}, {1,1}
        assertTrue(neighbors.contains(new Position(0, 1)));
        assertTrue(neighbors.contains(new Position(0, 2)));
        assertTrue(neighbors.contains(new Position(1, 0)));
        assertTrue(neighbors.contains(new Position(1, 2)));
        assertTrue(neighbors.contains(new Position(2, 1)));
        assertTrue(neighbors.contains(new Position(2, 2)));
    }
    
    @Test
    public void testGetNeighborsBoundary() {
        HexagonalAdjacencyStrategy strategy = new HexagonalAdjacencyStrategy();
        // Fila 0 (parella), columna 0. Vora superior esquerra.
        List<Position> neighbors = strategy.getNeighbors(new Position(0, 0), 5, 5);
        
        // Fora de límits (nr < 0 o nc < 0) s'han d'ignorar
        // De {-1,-1}, {-1,0}, {0,-1}, {0,1}, {1,-1}, {1,0}, només sobreviuen {0,1} i {1,0}
        assertEquals(2, neighbors.size());
        assertTrue(neighbors.contains(new Position(0, 1)));
        assertTrue(neighbors.contains(new Position(1, 0)));
    }
}