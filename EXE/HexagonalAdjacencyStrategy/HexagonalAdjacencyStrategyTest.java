
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import domini.model.adjacency.HexagonalAdjacencyStrategy;
import domini.model.cell.Position;

import java.util.List;

public class HexagonalAdjacencyStrategyTest {

    private HexagonalAdjacencyStrategy strategy;
    private final int ROWS = 4;
    private final int COLS = 4;

    @Before
    public void setUp() {
        strategy = new HexagonalAdjacencyStrategy();
    }

    @Test
    public void getNeighbors_evenRow_centerCell_returnsSixNeighbors() {
        Position pos = new Position(2, 1);
        List<Position> neighbors = strategy.getNeighbors(pos, ROWS, COLS);

        assertEquals("Una cel·la central en fila parell ha de tenir exactament 6 veïns", 6, neighbors.size());

        // Comprovem algunes posicions específiques segons EVEN_ROW_DIRS
        assertTrue("Ha d'incloure el veí superior esquerre (1,0)", neighbors.contains(new Position(1, 0)));
        assertTrue("Ha d'incloure el veí de la dreta (2,2)", neighbors.contains(new Position(2, 2)));
    }

    @Test
    public void getNeighbors_oddRow_centerCell_returnsSixNeighbors() {
        Position pos = new Position(1, 1);
        List<Position> neighbors = strategy.getNeighbors(pos, ROWS, COLS);

        assertEquals("Una cel·la central en fila senar ha de tenir exactament 6 veïns", 6, neighbors.size());

        // Comprovem algunes posicions específiques segons ODD_ROW_DIRS
        assertTrue("Ha d'incloure el veí superior dret (0,2)", neighbors.contains(new Position(0, 2)));
        assertTrue("Ha d'incloure el veí inferior esquerre (2,1)", neighbors.contains(new Position(2, 1)));
    }

    // Cas límit: des de (0,0) en fila parell, els offsets negatius queden fora
    @Test
    public void getNeighbors_topLeftCorner_evenRow_filtersOutOfBounds() {
        Position pos = new Position(0, 0);
        List<Position> neighbors = strategy.getNeighbors(pos, ROWS, COLS);

        // Segons EVEN_ROW_DIRS, des de (0,0) només són vàlids (0,1) i (1,0)
        assertEquals("La cantonada superior esquerra (fila 0) només ha de tenir 2 veïns dins del tauler", 2, neighbors.size());
        assertTrue("Ha de tenir el veí dret", neighbors.contains(new Position(0, 1)));
        assertTrue("Ha de tenir el veí inferior dret (segons offset hexagonal)", neighbors.contains(new Position(1, 0)));
    }

    // L'offset de les files senars fa que tinguin més veïns vàlids al marge esquerre
    @Test
    public void getNeighbors_leftEdge_oddRow_filtersOutOfBounds() {
        Position pos = new Position(1, 0);
        List<Position> neighbors = strategy.getNeighbors(pos, ROWS, COLS);

        // Segons ODD_ROW_DIRS, la posició a l'esquerra (0, -1) queda fora, la resta (5) entren.
        assertEquals("Un marge esquerre en fila senar ha de retenir 5 veïns vàlids", 5, neighbors.size());
        assertFalse("No ha d'incloure coordenades amb columnes negatives", neighbors.contains(new Position(1, -1)));
    }

    @Test
    public void areAdjacent_validAndInvalidPairs() {
        Position p1 = new Position(2, 1); // Fila parell
        Position validNeighbor = new Position(1, 1); // Superior dret per a una fila parell
        Position invalidNeighbor = new Position(0, 0); // Massa lluny

        assertTrue("Les posicions (2,1) i (1,1) han de ser adjacents en malla hexagonal",
                strategy.areAdjacent(p1, validNeighbor, ROWS, COLS));

        assertFalse("Posicions distants no han de ser considerades adjacents",
                strategy.areAdjacent(p1, invalidNeighbor, ROWS, COLS));
    }
}
