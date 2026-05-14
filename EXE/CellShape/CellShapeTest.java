import static org.junit.Assert.*;
import org.junit.Test;

import domini.model.cell.CellShape;

public class CellShapeTest {

    @Test
    public void threeShapesExist() {
        CellShape[] shapes = CellShape.values();
        assertEquals(3, shapes.length);
    }

    @Test
    public void squareShapeExists() {
        CellShape s = CellShape.SQUARE;
        assertNotNull(s);
    }

    @Test
    public void hexagonShapeExists() {
        CellShape s = CellShape.HEXAGON;
        assertNotNull(s);
    }

    @Test
    public void triangleShapeExists() {
        CellShape s = CellShape.TRIANGLE;
        assertNotNull(s);
    }

    @Test
    public void valueOf_square() {
        assertEquals(CellShape.SQUARE, CellShape.valueOf("SQUARE"));
    }

    @Test
    public void valueOf_hexagon() {
        assertEquals(CellShape.HEXAGON, CellShape.valueOf("HEXAGON"));
    }

    @Test
    public void valueOf_triangle() {
        assertEquals(CellShape.TRIANGLE, CellShape.valueOf("TRIANGLE"));
    }
}
