import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import domini.model.adjacency.HexagonalAdjacencyStrategy;
import domini.model.cell.Position;

import java.util.List;

public class HexagonalAdjacencyStrategyTest {

    private HexagonalAdjacencyStrategy strategy;
    private final int ROWS = 5;
    private final int COLS = 5;

    @Before
    public void setUp() {
        strategy = new HexagonalAdjacencyStrategy();
    }

    @Test
    public void testGetNeighborsEvenRowCenter() {
        // Fila 2 (parella). En un tauler 5x5, la posició (2, 2) té espai per a tots 6 veïns.
        List<Position> neighbors = strategy.getNeighbors(new Position(2, 2), ROWS, COLS);
        
        assertEquals("Una cel·la central en fila parell ha de tenir exactament 6 veïns", 6, neighbors.size());
        
        // Segons EVEN_ROW_DIRS: {-1,-1}, {-1,0}, {0,-1}, {0,1}, {1,-1}, {1,0}
        assertTrue("Ha d'incloure el veí superior esquerre (1, 1)", neighbors.contains(new Position(1, 1)));
        assertTrue("Ha d'incloure el veí superior dret (1, 2)", neighbors.contains(new Position(1, 2)));
        assertTrue("Ha d'incloure el veí esquerre (2, 1)", neighbors.contains(new Position(2, 1)));
        assertTrue("Ha d'incloure el veí dret (2, 3)", neighbors.contains(new Position(2, 3)));
        assertTrue("Ha d'incloure el veí inferior esquerre (3, 1)", neighbors.contains(new Position(3, 1)));
        assertTrue("Ha d'incloure el veí inferior dret (3, 2)", neighbors.contains(new Position(3, 2)));
    }

    @Test
    public void testGetNeighborsOddRowCenter() {
        // Fila 1 (senar).
        List<Position> neighbors = strategy.getNeighbors(new Position(1, 1), ROWS, COLS);
        
        assertEquals("Una cel·la central en fila senar ha de tenir exactament 6 veïns", 6, neighbors.size());
        
        // Segons ODD_ROW_DIRS: {-1,0}, {-1,1}, {0,-1}, {0,1}, {1,0}, {1,1}
        assertTrue("Ha d'incloure el veí superior esquerre (0, 1)", neighbors.contains(new Position(0, 1)));
        assertTrue("Ha d'incloure el veí superior dret (0, 2)", neighbors.contains(new Position(0, 2)));
        assertTrue("Ha d'incloure el veí esquerre (1, 0)", neighbors.contains(new Position(1, 0)));
        assertTrue("Ha d'incloure el veí dret (1, 2)", neighbors.contains(new Position(1, 2)));
        assertTrue("Ha d'incloure el veí inferior esquerre (2, 1)", neighbors.contains(new Position(2, 1)));
        assertTrue("Ha d'incloure el veí inferior dret (2, 2)", neighbors.contains(new Position(2, 2)));
    }
    
    @Test
    public void testGetNeighborsTopLeftCornerBoundary() {
        // Fila 0 (parella), columna 0. Vora superior esquerra.
        List<Position> neighbors = strategy.getNeighbors(new Position(0, 0), ROWS, COLS);
        
        // Fora de límits (nr < 0 o nc < 0) s'han d'ignorar
        assertEquals("La cantonada superior esquerra (fila parell) només ha de tenir 2 veïns dins del tauler", 2, neighbors.size());
        assertTrue("Ha de tenir el veí dret", neighbors.contains(new Position(0, 1)));
        assertTrue("Ha de tenir el veí inferior dret", neighbors.contains(new Position(1, 0)));
    }

    @Test
    public void testGetNeighborsLeftEdgeOddRowBoundary() {
        // Fila 1 (senar), columna 0. Marge esquerre.
        List<Position> neighbors = strategy.getNeighbors(new Position(1, 0), ROWS, COLS);

        // Segons ODD_ROW_DIRS, la posició a l'esquerra (1, -1) queda fora, la resta (5) entren.
        assertEquals("Un marge esquerre en fila senar ha de retenir 5 veïns vàlids", 5, neighbors.size());
        assertFalse("No ha d'incloure coordenades amb columnes negatives", neighbors.contains(new Position(1, -1)));
    }

    @Test
    public void testAreAdjacentValidAndInvalidPairs() {
        Position p1 = new Position(2, 1); // Fila parell
        Position validNeighbor = new Position(1, 1); // Superior dret per a una fila parell
        Position invalidNeighbor = new Position(0, 0); // Massa lluny

        assertTrue("Les posicions (2,1) i (1,1) han de ser adjacents en malla hexagonal",
                strategy.areAdjacent(p1, validNeighbor, ROWS, COLS));

        assertFalse("Posicions distants no han de ser considerades adjacents",
                strategy.areAdjacent(p1, invalidNeighbor, ROWS, COLS));
    }
}