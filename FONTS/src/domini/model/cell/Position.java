package domini.model.cell;

import java.io.Serializable;

public record Position(int row, int col) implements Serializable {
    @Override
    public String toString() {
        return String.format("(%d, %d)", row, col);
    }
}
