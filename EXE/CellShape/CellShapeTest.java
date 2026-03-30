import org.junit.Test;
import static org.junit.Assert.*;

import domini.model.cell.CellShape;

public class CellShapeTest {

    @Test
    public void testEnumValues() {
        CellShape[] shapes = CellShape.values();
        assertEquals(3, shapes.length);
        assertEquals(CellShape.SQUARE, shapes[0]);
        assertEquals(CellShape.HEXAGON, shapes[1]);
        assertEquals(CellShape.TRIANGLE, shapes[2]);
    }

    @Test
    public void testValueOf() {
        assertEquals(CellShape.SQUARE, CellShape.valueOf("SQUARE"));
        assertEquals(CellShape.HEXAGON, CellShape.valueOf("HEXAGON"));
        assertEquals(CellShape.TRIANGLE, CellShape.valueOf("TRIANGLE"));
    }
}