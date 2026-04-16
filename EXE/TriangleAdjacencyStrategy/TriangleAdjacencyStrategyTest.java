import org.junit.Test;
import static org.junit.Assert.*;

import domini.model.adjacency.TriangleAdjacencyStrategy;
import domini.model.cell.Position;
import java.util.List;

public class TriangleAdjacencyStrategyTest {

    @Test
    public void testGetNeighborsEvenSum() {
        TriangleAdjacencyStrategy strategy = new TriangleAdjacencyStrategy();
        // Posició (1, 1). Suma = 2 (parella). Apunta cap amunt, per tant té un veí just a sota.
        List<Position> neighbors = strategy.getNeighbors(new Position(1, 1), 3, 3);
        
        // Ha de tenir veí esquerra (1,0), dreta (1,2) i a baix (2,1)
        assertEquals(3, neighbors.size());
        assertTrue(neighbors.contains(new Position(1, 0)));
        assertTrue(neighbors.contains(new Position(1, 2)));
        assertTrue(neighbors.contains(new Position(2, 1))); // A baix
    }

    @Test
    public void testGetNeighborsOddSum() {
        TriangleAdjacencyStrategy strategy = new TriangleAdjacencyStrategy();
        // Posició (1, 2). Suma = 3 (senar). Apunta cap a baix, per tant té un veí just a dalt.
        List<Position> neighbors = strategy.getNeighbors(new Position(1, 2), 3, 4);
        
        // Ha de tenir veí esquerra (1,1), dreta (1,3) i a dalt (0,2)
        assertEquals(3, neighbors.size());
        assertTrue(neighbors.contains(new Position(1, 1)));
        assertTrue(neighbors.contains(new Position(1, 3)));
        assertTrue(neighbors.contains(new Position(0, 2))); // A dalt
    }

    @Test
    public void testGetNeighborsBoundary() {
        TriangleAdjacencyStrategy strategy = new TriangleAdjacencyStrategy();
        // Posició (0, 0). Suma = 0 (parella). En teoria veí a baix.
        // A l'esquerra no hi ha res (c=0), a dalt tampoc (tot i que per parell no busca a dalt).
        List<Position> neighbors = strategy.getNeighbors(new Position(0, 0), 3, 3);
        
        assertEquals(2, neighbors.size());
        assertTrue(neighbors.contains(new Position(0, 1))); // Dreta
        assertTrue(neighbors.contains(new Position(1, 0))); // A baix
    }
}