
import org.junit.Test;
import static org.junit.Assert.*;

import domini.model.cell.Position;

public class PositionTest {

    @Test
    public void testRecordGetters() {
        Position pos = new Position(3, 4);
        assertEquals(3, pos.row());
        assertEquals(4, pos.col());
    }

    @Test
    public void testToString() {
        Position pos = new Position(5, 12);
        assertEquals("(5, 12)", pos.toString());
    }
    
    @Test
    public void testEqualsAndHashCode() {
        Position pos1 = new Position(2, 2);
        Position pos2 = new Position(2, 2);
        Position pos3 = new Position(3, 2);
        
        assertEquals(pos1, pos2);
        assertNotEquals(pos1, pos3);
        assertEquals(pos1.hashCode(), pos2.hashCode());
    }
}