import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import domini.model.adjacency.SquareAdjacencyStrategy;
import domini.model.adjacency.SquareFullAdjacencyStrategy;
import domini.model.board.Board;
import domini.model.cell.Cell;
import domini.model.cell.CellShape;
import domini.model.cell.Position;

public class BoardTest {

    private Board board3x3;

    @Before
    public void setUp() {
        board3x3 = new Board(3, 3, CellShape.SQUARE, new SquareAdjacencyStrategy());
    }

    @Test
    public void dimensions_areCorrect() {
        assertEquals(3, board3x3.getRows());
        assertEquals(3, board3x3.getCols());
    }

    @Test
    public void getCell_returnsNonNull() {
        assertNotNull(board3x3.getCell(0, 0));
        assertNotNull(board3x3.getCell(2, 2));
    }

    @Test
    public void getCell_outOfBounds_returnsNull() {
        assertNull(board3x3.getCell(-1, 0));
        assertNull(board3x3.getCell(0, 3));
        assertNull(board3x3.getCell(3, 3));
    }

    @Test
    public void getCellCount_allPlayable() {
        assertEquals(9, board3x3.getCellCount());
    }

    @Test
    public void getCellCount_reducedByVoid() {
        board3x3.getCell(1, 1).setVoid(true);
        assertEquals(8, board3x3.getCellCount());
    }

    @Test
    public void getNeighbors_center_has4Neighbors() {
        assertEquals(4, board3x3.getNeighbors(new Position(1, 1)).size());
    }

    @Test
    public void getNeighbors_corner_has2Neighbors() {
        assertEquals(2, board3x3.getNeighbors(new Position(0, 0)).size());
    }

    @Test
    public void getNeighbors_excludesVoid() {
        board3x3.getCell(0, 1).setVoid(true);
        int neighbors = board3x3.getNeighbors(new Position(0, 0)).size();
        assertEquals(1, neighbors);
    }

    @Test
    public void isConnected_fullBoard() {
        assertTrue(board3x3.isConnected());
    }

    @Test
    public void isConnected_withVoidKeepsConnected() {
        board3x3.getCell(1, 1).setVoid(true);
        assertTrue(board3x3.isConnected());
    }

    @Test
    public void copyConstructor_isDeepCopy() {
        board3x3.getCell(0, 0).setFixedValue(5);
        Board copy = new Board(board3x3);
        copy.getCell(0, 0).setAsEmpty();
        assertEquals(5, board3x3.getCell(0, 0).getValue());
        assertEquals(0, copy.getCell(0, 0).getValue());
    }

    @Test
    public void squareFull_center_has8Neighbors() {
        Board b = new Board(3, 3, CellShape.SQUARE, new SquareFullAdjacencyStrategy());
        assertEquals(8, b.getNeighbors(new Position(1, 1)).size());
    }

    @Test
    public void cellShape_returnedCorrectly() {
        assertEquals(CellShape.SQUARE, board3x3.getCellShape());
    }
}
