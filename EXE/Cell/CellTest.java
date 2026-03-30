
import org.junit.Test;
import static org.junit.Assert.*;

// IMPORTS AFEGITS PER TROBAR EL MODEL
import domini.model.cell.Cell;
import domini.model.cell.Position;
import domini.model.cell.CellShape;

public class CellTest {

    @Test
    public void testConstructor() {
        Position pos = new Position(1, 1);
        Cell cell = new Cell(pos, CellShape.SQUARE);
        
        assertEquals(pos, cell.getPosition());
        assertEquals(CellShape.SQUARE, cell.getShape());
        assertEquals(0, cell.getValue());
        assertTrue(cell.isEmpty());
        assertFalse(cell.isFixed());
        assertFalse(cell.isVoid());
    }

    @Test
    public void testCopyConstructor() {
        Cell original = new Cell(new Position(2, 2), CellShape.HEXAGON);
        original.setFixedValue(5);
        
        Cell copy = new Cell(original);
        assertEquals(original.getPosition(), copy.getPosition());
        assertEquals(original.getShape(), copy.getShape());
        assertEquals(original.getValue(), copy.getValue());
        assertTrue(copy.isFixed());
    }

    @Test
    public void testSetValueNormal() {
        Cell cell = new Cell(new Position(0, 0), CellShape.SQUARE);
        cell.setValue(3);
        
        assertEquals(3, cell.getValue());
        assertFalse(cell.isEmpty());
        assertFalse(cell.isFixed());
    }

    @Test
    public void testSetValueOnFixedOrVoidIgnored() {
        Cell cellFixed = new Cell(new Position(0, 0), CellShape.SQUARE);
        cellFixed.setFixedValue(5);
        cellFixed.setValue(9); // No hauria de fer res
        assertEquals(5, cellFixed.getValue());

        Cell cellVoid = new Cell(new Position(0, 1), CellShape.SQUARE);
        cellVoid.setVoid(true);
        cellVoid.setValue(9); // No hauria de fer res
        assertEquals(0, cellVoid.getValue());
    }

    @Test
    public void testSetAsEmpty() {
        Cell cell = new Cell(new Position(0, 0), CellShape.SQUARE);
        cell.setFixedValue(5);
        cell.setAsEmpty();
        
        assertTrue(cell.isEmpty());
        assertFalse(cell.isFixed());
        assertEquals(0, cell.getValue());
    }

    @Test
    public void testSetVoid() {
        Cell cell = new Cell(new Position(0, 0), CellShape.SQUARE);
        cell.setFixedValue(5); // Donem un valor abans
        cell.setVoid(true);
        
        assertTrue(cell.isVoid());
        assertFalse(cell.isFixed());
        assertEquals(0, cell.getValue()); // El valor s'ha de resetejar a 0
    }

    @Test
    public void testToStringSquare() {
        Cell cell = new Cell(new Position(0, 0), CellShape.SQUARE);
        assertEquals(" . ", cell.toString()); // Buida
        
        cell.setValue(5);
        assertEquals("  5", cell.toString()); // Amb valor
        
        cell.setVoid(true);
        assertEquals(" # ", cell.toString()); // Forat
    }

    @Test
    public void testToStringTriangle() {
        // Triangle que apunta cap amunt (0+0 = parell)
        Cell cellUp = new Cell(new Position(0, 0), CellShape.TRIANGLE);
        assertEquals(" ^ ", cellUp.toString());
        cellUp.setValue(3);
        assertEquals("^3 ", cellUp.toString()); 

        // Triangle que apunta cap avall (0+1 = senar)
        Cell cellDown = new Cell(new Position(0, 1), CellShape.TRIANGLE);
        assertEquals(" v ", cellDown.toString());
        cellDown.setValue(10);
        assertEquals("v10", cellDown.toString());
    }
}