package test;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import model.cell.Cell;
import model.cell.CellShape;
import model.cell.Position;

public class CellTest {

    private Cell cell;

    @Before
    public void setUp() {
        cell = new Cell(new Position(0, 0), CellShape.SQUARE);
    }

    @Test
    public void newCell_isEmpty() {
        assertEquals(0, cell.getValue());
        assertFalse(cell.isFixed());
        assertFalse(cell.isVoid());
        assertTrue(cell.isEmpty());
    }

    @Test
    public void setValue_updatesValue() {
        cell.setValue(5);
        assertEquals(5, cell.getValue());
    }

    @Test
    public void setFixedValue_marksFixed() {
        cell.setFixedValue(3);
        assertEquals(3, cell.getValue());
        assertTrue(cell.isFixed());
        assertFalse(cell.isVoid());
    }

    @Test
    public void setValue_doesNotOverwriteFixed() {
        cell.setFixedValue(7);
        cell.setValue(99);
        assertEquals(7, cell.getValue());
    }

    @Test
    public void setVoid_clearsValueAndFixed() {
        cell.setFixedValue(4);
        cell.setVoid(true);
        assertTrue(cell.isVoid());
        assertEquals(0, cell.getValue());
        assertFalse(cell.isFixed());
    }

    @Test
    public void setVoid_false_restoresPlayable() {
        cell.setVoid(true);
        cell.setVoid(false);
        assertFalse(cell.isVoid());
    }

    @Test
    public void setValue_doesNotWorkOnVoid() {
        cell.setVoid(true);
        cell.setValue(5);
        assertEquals(0, cell.getValue());
    }

    @Test
    public void setAsEmpty_resetsCell() {
        cell.setFixedValue(9);
        cell.setAsEmpty();
        assertEquals(0, cell.getValue());
        assertFalse(cell.isFixed());
        assertFalse(cell.isVoid());
    }

    @Test
    public void isEmpty_falseWhenHasValue() {
        cell.setValue(1);
        assertFalse(cell.isEmpty());
    }

    @Test
    public void isEmpty_falseWhenVoid() {
        cell.setVoid(true);
        assertFalse(cell.isEmpty());
    }

    @Test
    public void getPosition_returnsCorrectPosition() {
        Cell c = new Cell(new Position(2, 3), CellShape.SQUARE);
        assertEquals(2, c.getPosition().row());
        assertEquals(3, c.getPosition().col());
    }

    @Test
    public void getShape_returnsCorrectShape() {
        Cell hex = new Cell(new Position(0, 0), CellShape.HEXAGON);
        assertEquals(CellShape.HEXAGON, hex.getShape());
    }

    @Test
    public void copyConstructor_deepCopiesState() {
        cell.setFixedValue(5);
        Cell copy = new Cell(cell);
        assertEquals(5, copy.getValue());
        assertTrue(copy.isFixed());
        // mutating copy does not affect original
        copy.setAsEmpty();
        assertEquals(5, cell.getValue());
        assertTrue(cell.isFixed());
    }

    @Test
    public void toString_voidCell() {
        cell.setVoid(true);
        assertEquals(" # ", cell.toString());
    }

    @Test
    public void toString_emptySquareCell() {
        assertEquals(" . ", cell.toString());
    }

    @Test
    public void toString_squareCellWithValue() {
        cell.setValue(7);
        assertEquals("  7", cell.toString());
    }

    @Test
    public void toString_triangleEmptyPointingUp() {
        // (row+col) % 2 == 0 → points up
        Cell t = new Cell(new Position(0, 0), CellShape.TRIANGLE);
        assertEquals(" ^ ", t.toString());
    }

    @Test
    public void toString_triangleEmptyPointingDown() {
        // (row+col) % 2 == 1 → points down
        Cell t = new Cell(new Position(0, 1), CellShape.TRIANGLE);
        assertEquals(" v ", t.toString());
    }
}
