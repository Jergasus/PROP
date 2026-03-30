import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import domini.model.board.Board;
import domini.model.cell.Cell;
import domini.model.cell.CellShape;
import domini.model.cell.Position;
import domini.model.adjacency.SquareAdjacencyStrategy;
import domini.model.adjacency.SquareFullAdjacencyStrategy;
import domini.model.adjacency.HexagonalAdjacencyStrategy;
import domini.model.adjacency.TriangleAdjacencyStrategy;

import java.util.List;
import java.util.Random;

public class BoardTest {

    private Board board3x3;

    @Before
    public void setUp() {
        board3x3 = new Board(3, 3, CellShape.SQUARE, new SquareAdjacencyStrategy());
    }

    @Test
    public void testConstructorAndDimensions() {
        assertEquals("El nombre de files ha de ser 3", 3, board3x3.getRows());
        assertEquals("El nombre de columnes ha de ser 3", 3, board3x3.getCols());
        assertEquals("La geometria del tauler ha de ser SQUARE", CellShape.SQUARE, board3x3.getCellShape());
        assertEquals("En un 3x3 sense forats, el recompte ha de ser 9", 9, board3x3.getCellCount());
        
        Cell c = board3x3.getCell(0, 0);
        assertNotNull("La cel·la superior esquerra ha d'estar inicialitzada", c);
        assertEquals("La posicio interna de la cel·la ha de coincidir", new Position(0, 0), c.getPosition());
    }

    @Test
    public void testCopyConstructorIsDeepCopy() {
        board3x3.getCell(1, 1).setFixedValue(5);
        
        Board copy = new Board(board3x3);
        assertEquals("La còpia ha de preservar els valors", 5, copy.getCell(1, 1).getValue());
        
        // Verifiquem independència
        copy.getCell(1, 1).setFixedValue(9);
        assertEquals("Modificar la còpia no ha d'alterar l'original", 5, board3x3.getCell(1, 1).getValue());
    }

    @Test
    public void testIsValidPosition() {
        assertTrue("La posició 0,0 ha de ser vàlida", board3x3.isValidPosition(new Position(0, 0)));
        assertTrue("La posició 2,2 ha de ser vàlida", board3x3.isValidPosition(new Position(2, 2)));
        assertFalse("Posicions negatives no han de ser vàlides", board3x3.isValidPosition(new Position(-1, 0)));
        assertFalse("Posicions fora de límits (marge dret) no han de ser vàlides", board3x3.isValidPosition(new Position(0, 3)));
    }

    @Test
    public void testGetCellValidAndOutOfBounds() {
        assertNotNull("La cel·la (2,2) ha d'existir", board3x3.getCell(2, 2));
        assertNull("Una fila negativa ha de retornar null", board3x3.getCell(-1, 0));
        assertNull("Una columna fora de rang ha de retornar null", board3x3.getCell(0, 3));
    }

    @Test
    public void testGetCellCountWithVoids() {
        board3x3.getCell(0, 0).setVoid(true);
        board3x3.getCell(1, 1).setVoid(true);
        assertEquals("Si es marquen 2 forats, el recompte de jugables ha de ser 7", 7, board3x3.getCellCount());
    }

    @Test
    public void testGetNeighborsSquare4Way() {
        assertEquals("El centre (1,1) ha de tenir 4 veïns", 4, board3x3.getNeighbors(new Position(1, 1)).size());
        assertEquals("La cantonada (0,0) ha de tenir 2 veïns", 2, board3x3.getNeighbors(new Position(0, 0)).size());
    }

    @Test
    public void testGetNeighborsExcludesVoidCells() {
        board3x3.getCell(0, 1).setVoid(true);
        board3x3.getCell(1, 0).setVoid(true);
        List<Cell> neighbors = board3x3.getNeighbors(new Position(0, 0));
        assertEquals("Si s'anul·len els únics 2 veïns, la llista de veïns ha de ser buida", 0, neighbors.size());
    }

    @Test
    public void testAreAdjacentOrthogonalAndDiagonal() {
        assertTrue("Horizontal (0,0)-(0,1) son adjacents", board3x3.areAdjacent(new Position(0, 0), new Position(0, 1)));
        assertFalse("Diagonal (0,0)-(1,1) no son adjacents en 4way", board3x3.areAdjacent(new Position(0, 0), new Position(1, 1)));
        
        Board b8 = new Board(3, 3, CellShape.SQUARE, new SquareFullAdjacencyStrategy());
        assertTrue("En 8way, la diagonal si que es adjacent", b8.areAdjacent(new Position(0, 0), new Position(1, 1)));
    }

    @Test
    public void testIsConnectedFullAndSplitBoard() {
        assertTrue("Un tauler sencer esta connectat", board3x3.isConnected()); 
        
        // Fem un tall vertical (columna 1 buida)
        board3x3.getCell(0, 1).setVoid(true);
        board3x3.getCell(1, 1).setVoid(true);
        board3x3.getCell(2, 1).setVoid(true);
        assertFalse("En dividir el tauler, ha d'estar desconnectat", board3x3.isConnected()); 
    }

    @Test
    public void testIsConnectedSingleCell() {
        Board b1 = new Board(1, 1, CellShape.SQUARE, new SquareAdjacencyStrategy());
        assertTrue("Un tauler d'1x1 es considera connectat (cas limit)", b1.isConnected());
    }

    @Test
    public void testPruneDeadEnds() {
        // Aillem la cantonada 0,0 deixant nomes el vei 0,1 (1 vei per a 0,0)
        board3x3.getCell(1, 0).setVoid(true);
        
        int pruned = board3x3.pruneDeadEnds();
        assertTrue("Hauria d'haver podat el cul de sac", pruned > 0);
        assertTrue("La cel·la 0,0 ha de ser ara un forat", board3x3.getCell(0, 0).isVoid());
    }
    
    @Test
    public void testPlaceRandomVoidsGuaranteesConnectivity() {
        Random rand = new Random(123); // Seed fixa per repetibilitat
        boolean success = board3x3.placeRandomVoids(3, rand);
        
        assertTrue("Ha de retornar cert a l'inserir forats aleatoris", success);
        assertEquals("Hi hauria d'haver 6 cel·les jugables (9-3)", 6, board3x3.getCellCount()); 
        assertTrue("Ha de garantir connectivitat despres d'inserir els forats", board3x3.isConnected()); 
    }
}