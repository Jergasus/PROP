import org.junit.Test;
import static org.junit.Assert.*;

import domini.model.board.Board;
import domini.model.cell.Cell;
import domini.model.cell.CellShape;
import domini.model.cell.Position;
import domini.model.adjacency.SquareAdjacencyStrategy;

import java.util.Random;

public class BoardTest {

    @Test
    public void testConstructor() {
        Board board = new Board(3, 4, CellShape.SQUARE, new SquareAdjacencyStrategy());
        assertEquals(3, board.getRows());
        assertEquals(4, board.getCols());
        assertEquals(CellShape.SQUARE, board.getCellShape());
        assertEquals(12, board.getCellCount());
        
        // Verifica que la primera cel·la està inicialitzada
        Cell c = board.getCell(0, 0);
        assertNotNull(c);
        assertEquals(new Position(0, 0), c.getPosition());
    }

    @Test
    public void testCopyConstructor() {
        Board original = new Board(2, 2, CellShape.SQUARE, new SquareAdjacencyStrategy());
        original.getCell(1, 1).setFixedValue(5);
        
        Board copy = new Board(original);
        assertEquals(2, copy.getRows());
        assertEquals(5, copy.getCell(1, 1).getValue());
        
        // Verifiquem independència (deep copy)
        copy.getCell(1, 1).setFixedValue(9);
        assertEquals(5, original.getCell(1, 1).getValue());
    }

    @Test
    public void testIsValidPosition() {
        Board board = new Board(3, 3, CellShape.SQUARE, new SquareAdjacencyStrategy());
        assertTrue(board.isValidPosition(new Position(0, 0)));
        assertTrue(board.isValidPosition(new Position(2, 2)));
        assertFalse(board.isValidPosition(new Position(-1, 0))); // Cas extrem: negatiu
        assertFalse(board.isValidPosition(new Position(0, 3)));  // Cas extrem: fora de límits
    }

    @Test
    public void testGetNeighbors() {
        Board board = new Board(3, 3, CellShape.SQUARE, new SquareAdjacencyStrategy());
        // Al centre (1,1) hauria de tenir 4 veïns en SquareAdjacencyStrategy
        assertEquals(4, board.getNeighbors(new Position(1, 1)).size());
        
        // A la cantonada (0,0) hauria de tenir 2 veïns
        assertEquals(2, board.getNeighbors(new Position(0, 0)).size());
    }

    @Test
    public void testIsConnected() {
        Board board = new Board(3, 3, CellShape.SQUARE, new SquareAdjacencyStrategy());
        assertTrue(board.isConnected()); // Un tauler sencer està connectat
        
        // Fem un tall que desconnecti el tauler (una línia vertical de forats)
        board.getCell(0, 1).setVoid(true);
        board.getCell(1, 1).setVoid(true);
        board.getCell(2, 1).setVoid(true);
        
        assertFalse(board.isConnected()); // Ara hauria d'estar desconnectat
    }

    @Test
    public void testPruneDeadEnds() {
        // Creem un tauler i fem un "cul de sac" aïllat
        Board board = new Board(3, 3, CellShape.SQUARE, new SquareAdjacencyStrategy());
        // Aïllem la cantonada 0,0 deixant només el veí 0,1 (1 veí per a 0,0)
        board.getCell(1, 0).setVoid(true);
        
        int pruned = board.pruneDeadEnds();
        // Hauria de podar la (0,0) perquè té només 1 veí, i després la (0,1)... etc. fins estabilitzar-se
        assertTrue("Hauria d'haver podat cel·les mortes", pruned > 0);
    }
    
    @Test
    public void testPlaceRandomVoids() {
        Board board = new Board(3, 3, CellShape.SQUARE, new SquareAdjacencyStrategy());
        Random rand = new Random(123); // Seed fixa per repetibilitat
        boolean success = board.placeRandomVoids(3, rand);
        
        assertTrue(success);
        assertEquals(6, board.getCellCount()); // 9 - 3 = 6
        assertTrue(board.isConnected()); // Sempre ha de garantir connectivitat
    }
}