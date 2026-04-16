package domini.model.cell;

import java.io.Serializable;

public class Cell implements Serializable {
    private int value;
    private final Position position;
    private final CellShape shape;
    private boolean isFixed;
    private boolean isVoid;

    public Cell(Position position, CellShape shape) {
        this.position = position;
        this.shape = shape;
        this.value = 0;
        this.isFixed = false;
        this.isVoid = false;
    }

    public Cell(Cell other) {
        this.position = other.position;
        this.shape = other.shape;
        this.value = other.value;
        this.isFixed = other.isFixed;
        this.isVoid = other.isVoid;
    }

    public int getValue() { return value; }

    public void setValue(int value) {
        if (!isFixed && !isVoid) this.value = value;
    }

    public void setFixedValue(int value) {
        this.value = value;
        this.isFixed = true;
        this.isVoid = false;
    }

    public void setAsEmpty() {
        this.value = 0;
        this.isFixed = false;
        this.isVoid = false;
    }

    public void setVoid(boolean isVoid) {
        this.isVoid = isVoid;
        if (isVoid) {
            this.value = 0;
            this.isFixed = false;
        }
    }

    public boolean isEmpty()  { return value == 0 && !isVoid; }
    public boolean isFixed()  { return isFixed; }
    public boolean isVoid()   { return isVoid; }
    public Position getPosition() { return position; }
    public CellShape getShape()   { return shape; }

    @Override
    public String toString() {
        if (isVoid) return " # ";
        if (shape == CellShape.TRIANGLE) {
            boolean pointsUp = (position.row() + position.col()) % 2 == 0;
            String ind = pointsUp ? "^" : "v";
            if (value == 0) return " " + ind + " ";
            return String.format("%s%-2d", ind, value);
        }
        if (value == 0) return " . ";
        return String.format("%3d", value);
    }
}
