import static org.junit.Assert.*;
import org.junit.Test;

import domini.model.cell.Position;

public class PositionTest {

    @Test
    public void constructor_storesValues() {
        Position p = new Position(2, 5);
        assertEquals(2, p.row());
        assertEquals(5, p.col());
    }

    @Test
    public void equality_sameValues() {
        Position p1 = new Position(1, 3);
        Position p2 = new Position(1, 3);
        assertEquals(p1, p2);
    }

    @Test
    public void equality_differentValues() {
        Position p1 = new Position(1, 2);
        Position p2 = new Position(2, 1);
        assertNotEquals(p1, p2);
    }

    @Test
    public void hashCode_equalForSameValues() {
        Position p1 = new Position(4, 7);
        Position p2 = new Position(4, 7);
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    public void zeroPosition() {
        Position p = new Position(0, 0);
        assertEquals(0, p.row());
        assertEquals(0, p.col());
    }
}
