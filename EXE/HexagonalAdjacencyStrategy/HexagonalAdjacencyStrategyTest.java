
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import domini.model.adjacency.HexagonalAdjacencyStrategy;
import domini.model.cell.Position;

import java.util.List;

/**
 * Classe de proves unitàries per a {@link HexagonalAdjacencyStrategy}.
 * <p>
 * Verifica el correcte càlcul dels veïns en una malla hexagonal, tenint en
 * compte el desplaçament (offset) de les coordenades entre les files parells i senars,
 * i assegurant que no es retornen posicions fora dels límits del tauler.
 * </p>
 * @author [Sergi Blanco Gallardo]
 * @version 1.0
 */
public class HexagonalAdjacencyStrategyTest {

    private HexagonalAdjacencyStrategy strategy;
    private final int ROWS = 4;
    private final int COLS = 4;

    /**
     * Configuració prèvia a cada test.
     */
    @Before
    public void setUp() {
        strategy = new HexagonalAdjacencyStrategy();
    }

    // ==========================================
    // PROVES DE FILES PARELLS I SENARS (CENTRE)
    // ==========================================

    /**
     * @test Verifica els veïns d'una cel·la central situada en una fila parell.
     * En un tauler 4x4, la posició (2,1) té espai per a tots els seus 6 veïns.
     */
    @Test
    public void getNeighbors_evenRow_centerCell_returnsSixNeighbors() {
        Position pos = new Position(2, 1);
        List<Position> neighbors = strategy.getNeighbors(pos, ROWS, COLS);

        assertEquals("Una cel·la central en fila parell ha de tenir exactament 6 veïns", 6, neighbors.size());

        // Comprovem algunes posicions específiques segons EVEN_ROW_DIRS
        assertTrue("Ha d'incloure el veí superior esquerre (1,0)", neighbors.contains(new Position(1, 0)));
        assertTrue("Ha d'incloure el veí de la dreta (2,2)", neighbors.contains(new Position(2, 2)));
    }

    /**
     * @test Verifica els veïns d'una cel·la central situada en una fila senar.
     * En un tauler 4x4, la posició (1,1) té espai per a tots els seus 6 veïns.
     */
    @Test
    public void getNeighbors_oddRow_centerCell_returnsSixNeighbors() {
        Position pos = new Position(1, 1);
        List<Position> neighbors = strategy.getNeighbors(pos, ROWS, COLS);

        assertEquals("Una cel·la central en fila senar ha de tenir exactament 6 veïns", 6, neighbors.size());

        // Comprovem algunes posicions específiques segons ODD_ROW_DIRS
        assertTrue("Ha d'incloure el veí superior dret (0,2)", neighbors.contains(new Position(0, 2)));
        assertTrue("Ha d'incloure el veí inferior esquerre (2,1)", neighbors.contains(new Position(2, 1)));
    }

    // ==========================================
    // PROVES DE LÍMITS I CANTONADES
    // ==========================================

    /**
     * @test Verifica que una cel·la a la cantonada superior esquerra (fila parell)
     * no retorna posicions negatives fora del tauler.
     */
    @Test
    public void getNeighbors_topLeftCorner_evenRow_filtersOutOfBounds() {
        Position pos = new Position(0, 0);
        List<Position> neighbors = strategy.getNeighbors(pos, ROWS, COLS);

        // Segons EVEN_ROW_DIRS, des de (0,0) només són vàlids (0,1) i (1,0)
        assertEquals("La cantonada superior esquerra (fila 0) només ha de tenir 2 veïns dins del tauler", 2, neighbors.size());
        assertTrue("Ha de tenir el veí dret", neighbors.contains(new Position(0, 1)));
        assertTrue("Ha de tenir el veí inferior dret (segons offset hexagonal)", neighbors.contains(new Position(1, 0)));
    }

    /**
     * @test Verifica el comportament al marge esquerre però en una fila senar.
     * L'offset fa que tingui més veïns vàlids que el marge d'una fila parell.
     */
    @Test
    public void getNeighbors_leftEdge_oddRow_filtersOutOfBounds() {
        Position pos = new Position(1, 0); // Fila 1, columna 0
        List<Position> neighbors = strategy.getNeighbors(pos, ROWS, COLS);

        // Segons ODD_ROW_DIRS, la posició a l'esquerra (0, -1) queda fora, la resta (5) entren.
        assertEquals("Un marge esquerre en fila senar ha de retenir 5 veïns vàlids", 5, neighbors.size());
        assertFalse("No ha d'incloure coordenades amb columnes negatives", neighbors.contains(new Position(1, -1)));
    }

    // ==========================================
    // PROVES D'ADJACÈNCIA DIRECTA (areAdjacent)
    // ==========================================

    /**
     * @test Verifica que el mètode heretat areAdjacent funciona correctament per a parelles vàlides i invàlides.
     */
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
