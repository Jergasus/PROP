package model.cell;

import java.io.Serializable;

/**
 * Representa una celda individual en el tablero de Hidato.
 */
public class Cell implements Serializable {
    private int value; // 0 indica vacía
    private final Position position;
    private final CellShape shape;
    private boolean isFixed; // Si es true, es un número inicial dado por el problema
    private boolean isVoid;  // Si es true, es un "hueco" en el tablero (no jugable)

    public Cell(Position position, CellShape shape) {
        this.position = position;
        this.shape = shape;
        this.value = 0;
        this.isFixed = false;
        this.isVoid = false;
    }

    public int getValue() {
        return value;
    }

    public Cell(Cell other) {
        this.position = other.position; // Position is a record (immutable), safe to share reference
        this.shape = other.shape;
        this.value = other.value;
        this.isFixed = other.isFixed; // Assuming isFixed field exists
        this.isVoid = other.isVoid;
    }

    public void setValue(int value) {
        if (!isFixed && !isVoid) {
            this.value = value;
        }
    }

    /**
     * Usado para configurar el tablero o el generador
     */
    public void setFixedValue(int value) {
        this.value = value;
        this.isFixed = true;
        this.isVoid = false;
    }

    /**
     * Resetea la celda a estado vacío (0) y no fijo.
     */
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

    public boolean isEmpty() {
        return value == 0 && !isVoid;
    }

    public boolean isFixed() {
        return isFixed;
    }

    public boolean isVoid() {
        return isVoid;
    }

    public Position getPosition() {
        return position;
    }

    public CellShape getShape() {
        return shape;
    }
    
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
