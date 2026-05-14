package model.cell;

import java.io.Serializable;

/**
 * Representa una coordenada en el tablero (fila, columna).
 * Usamos un 'record' de Java (JDK 21) que es inmutable y conciso.
 */
public record Position(int row, int col) implements Serializable {
    @Override
    public String toString() {
        return String.format("(%d, %d)", row, col);
    }
}
