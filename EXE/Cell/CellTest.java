import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import domini.model.cell.Cell;
import domini.model.cell.CellShape;
import domini.model.cell.Position;

public class CellTest {

    private Cell cell;

    @Before
    public void setUp() {
        cell = new Cell(new Position(0, 0), CellShape.SQUARE);
    }

    @Test
    public void newCell_isEmpty() {
        assertTrue(cell.isEmpty());
        assertEquals(0, cell.getValue());
        assertFalse(cell.isFixed());
        assertFalse(cell.isVoid());
    }

    @Test
    public void setFixedValue_marksFixed() {
        cell.setFixedValue(7);
        assertEquals(7, cell.getValue());
        assertTrue(cell.isFixed());
        assertFalse(cell.isVoid());
        assertFalse(cell.isEmpty());
    }

    @Test
    public void setValue_onNonFixed_works() {
        cell.setValue(3);
        assertEquals(3, cell.getValue());
        assertFalse(cell.isFixed());
    }

    @Test
    public void setValue_onFixed_ignored() {
        cell.setFixedValue(5);
        cell.setValue(9);
        assertEquals(5, cell.getValue());
    }

    @Test
    public void setAsEmpty_resetsState() {
        cell.setFixedValue(4);
        cell.setAsEmpty();
        assertEquals(0, cell.getValue());
        assertFalse(cell.isFixed());
        assertTrue(cell.isEmpty());
    }

    @Test
    public void setVoid_marksVoid() {
        cell.setVoid(true);
        assertTrue(cell.isVoid());
        assertFalse(cell.isEmpty());
        assertEquals(0, cell.getValue());
    }

    @Test
    public void setVoid_false_restores() {
        cell.setVoid(true);
        cell.setVoid(false);
        assertFalse(cell.isVoid());
        assertTrue(cell.isEmpty());
    }

    @Test
    public void getPosition_correct() {
        assertEquals(0, cell.getPosition().row());
        assertEquals(0, cell.getPosition().col());
    }

    @Test
    public void getShape_correct() {
        assertEquals(CellShape.SQUARE, cell.getShape());
    }

    @Test
    public void copyConstructor_isIndependent() {
        cell.setFixedValue(3);
        Cell copy = new Cell(cell);
        copy.setAsEmpty();
        assertEquals(3, cell.getValue());
        assertEquals(0, copy.getValue());
    }
}
