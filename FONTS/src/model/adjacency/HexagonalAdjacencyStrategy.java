package model.adjacency;

import model.cell.Position;
import java.util.ArrayList;
import java.util.List;

/**
 * Adyacencia para celdas Hexagonales.
 * Asumimos una malla "offset" (columnas desplazadas en filas impares).
 * 
 *     / \ / \
 *    |0,0|0,1|...   (Fila Par)
 *     \ / \ /
 *      |1,0|1,1|... (Fila Impar: desplazada a la derecha medio hexágono)
 *     / \ / \
 * 
 */
public class HexagonalAdjacencyStrategy implements AdjacencyStrategy {

    // Diferencias de coordenadas para filas PARES
    private static final int[][] EVEN_ROW_DIRS = {
        {-1, -1}, {-1, 0}, // Arriba-Izq, Arriba-Der
        {0, -1}, {0, 1},   // Izq, Der
        {1, -1}, {1, 0}    // Abajo-Izq, Abajo-Der
    };

    // Diferencias de coordenadas para filas IMPARES
    private static final int[][] ODD_ROW_DIRS = {
        {-1, 0}, {-1, 1},  // Arriba-Izq, Arriba-Der
        {0, -1}, {0, 1},   // Izq, Der
        {1, 0}, {1, 1}     // Abajo-Izq, Abajo-Der
    };

    @Override
    public List<Position> getNeighbors(Position pos, int rows, int cols) {
        List<Position> neighbors = new ArrayList<>();
        int r = pos.row();
        int c = pos.col();

        // Seleccionar direcciones según si la fila es par o impar
        int[][] directions = (r % 2 == 0) ? EVEN_ROW_DIRS : ODD_ROW_DIRS;

        for (int[] dir : directions) {
            int nr = r + dir[0];
            int nc = c + dir[1];

            // Validar límites
            if (nr >= 0 && nr < rows && nc >= 0 && nc < cols) {
                neighbors.add(new Position(nr, nc));
            }
        }
        return neighbors;
    }
}
