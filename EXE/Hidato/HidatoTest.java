import org.junit.Test;
import static org.junit.Assert.*;

import domini.model.Hidato.Hidato;
import domini.model.board.Board;
import domini.model.cell.CellShape;
import domini.model.adjacency.SquareAdjacencyStrategy;

public class HidatoTest {

    @Test
    public void testConstructorAndGetters() {
        Board b = new Board(2, 2, CellShape.SQUARE, new SquareAdjacencyStrategy());
        Hidato hidato = new Hidato("Test1", "Quadrat", true, "Test desc", b);
        
        assertEquals("Test1", hidato.getName());
        assertEquals("Quadrat", hidato.getAdjacencyDesc());
        assertTrue(hidato.isExpectedSolvable());
        assertEquals("Test desc", hidato.getDescription());
    }

    @Test
    public void testGetBoardReturnsCopy() {
        Board originalBoard = new Board(2, 2, CellShape.SQUARE, new SquareAdjacencyStrategy());
        originalBoard.getCell(0, 0).setFixedValue(1);
        
        Hidato hidato = new Hidato("Test2", "Quadrat", true, "Test desc", originalBoard);
        
        // Obtenim el tauler a través del getter
        Board returnedBoard = hidato.getBoard();
        
        // Modifiquem el tauler retornat
        returnedBoard.getCell(0, 0).setFixedValue(99);
        
        // Comprovem que l'Hidato original NO s'ha modificat
        // Això requereix que demanem un altre cop el tauler per veure el seu estat intern
        assertEquals(1, hidato.getBoard().getCell(0, 0).getValue());
    }
}