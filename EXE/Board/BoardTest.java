package test;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import model.adjacency.SquareAdjacencyStrategy;
import model.adjacency.SquareFullAdjacencyStrategy;
import model.adjacency.HexagonalAdjacencyStrategy;
import model.adjacency.TriangleAdjacencyStrategy;
import model.board.Board;
import model.cell.Cell;
import model.cell.CellShape;
import model.cell.Position;

import java.util.List;

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
    public void getCell_returnsCell() {
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
    public void getCellCount_withVoids() {
        board3x3.getCell(0, 0).setVoid(true);
        board3x3.getCell(1, 1).setVoid(true);
        assertEquals(7, board3x3.getCellCount());
    }

    @Test
    public void getNeighbors_4way_centerCell() {
        List<Cell> neighbors = board3x3.getNeighbors(new Position(1, 1));
        assertEquals(4, neighbors.size());
    }

    @Test
    public void getNeighbors_4way_cornerCell() {
        List<Cell> neighbors = board3x3.getNeighbors(new Position(0, 0));
        assertEquals(2, neighbors.size());
    }

    @Test
    public void getNeighbors_excludesVoidCells() {
        board3x3.getCell(0, 1).setVoid(true);
        board3x3.getCell(1, 0).setVoid(true);
        List<Cell> neighbors = board3x3.getNeighbors(new Position(0, 0));
        assertEquals(0, neighbors.size());
    }

    @Test
    public void areAdjacent_4way_orthogonal() {
        assertTrue(board3x3.areAdjacent(new Position(0, 0), new Position(0, 1)));
        assertTrue(board3x3.areAdjacent(new Position(1, 1), new Position(2, 1)));
    }

    @Test
    public void areAdjacent_4way_diagonal_false() {
        assertFalse(board3x3.areAdjacent(new Position(0, 0), new Position(1, 1)));
    }

    @Test
    public void areAdjacent_8way_diagonalAllowed() {
        Board b = new Board(3, 3, CellShape.SQUARE, new SquareFullAdjacencyStrategy());
        assertTrue(b.areAdjacent(new Position(0, 0), new Position(1, 1)));
    }

    @Test
    public void copyConstructor_isDeepCopy() {
        board3x3.getCell(0, 0).setFixedValue(5);
        Board copy = new Board(board3x3);
        assertEquals(5, copy.getCell(0, 0).getValue());
        // mutating copy does not affect original
        copy.getCell(0, 0).setAsEmpty();
        assertEquals(5, board3x3.getCell(0, 0).getValue());
    }

    @Test
    public void pruneDeadEnds_removesCorners_onHexBoard() {
        // Triangle board with no void cells will have many degree-1 cells
        Board t = new Board(2, 3, CellShape.TRIANGLE, new TriangleAdjacencyStrategy());
        int pruned = t.pruneDeadEnds();
        assertTrue(pruned >= 0);
        // After pruning, no remaining cell should have fewer than 2 neighbors
        for (int i = 0; i < t.getRows(); i++) {
            for (int j = 0; j < t.getCols(); j++) {
                Cell c = t.getCell(i, j);
                if (!c.isVoid()) {
                    assertTrue(t.getNeighbors(c.getPosition()).size() >= 2);
                }
            }
        }
    }

    @Test
    public void isConnected_fullBoard() {
        assertTrue(board3x3.isConnected());
    }

    @Test
    public void isConnected_afterVoidSplitsBoard() {
        // void the entire middle column — splits left column from right
        for (int i = 0; i < 3; i++) board3x3.getCell(i, 1).setVoid(true);
        assertFalse(board3x3.isConnected());
    }

    @Test
    public void isConnected_singleCell() {
        Board b = new Board(1, 1, CellShape.SQUARE, new SquareAdjacencyStrategy());
        assertTrue(b.isConnected());
    }

    @Test
    public void getCellShape_returnsCorrectShape() {
        assertEquals(CellShape.SQUARE, board3x3.getCellShape());
        Board hex = new Board(3, 3, CellShape.HEXAGON, new HexagonalAdjacencyStrategy());
        assertEquals(CellShape.HEXAGON, hex.getCellShape());
    }

    @Test
    public void getNeighbors_hexBoard_evenRow() {
        Board hex = new Board(3, 3, CellShape.HEXAGON, new HexagonalAdjacencyStrategy());
        // Center of even row (row 0) should have up to 4 in-bounds neighbors
        List<Cell> neighbors = hex.getNeighbors(new Position(0, 1));
        assertFalse(neighbors.isEmpty());
    }

    @Test
    public void getNeighbors_triangleBoard() {
        Board t = new Board(2, 3, CellShape.TRIANGLE, new TriangleAdjacencyStrategy());
        // Every cell should have at least 1 neighbor
        for (int i = 0; i < 2; i++)
            for (int j = 0; j < 3; j++)
                assertFalse(t.getNeighbors(new Position(i, j)).isEmpty());
    }
}
