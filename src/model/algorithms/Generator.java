package model.algorithms;

import model.board.Board;
import model.cell.Cell;
import model.cell.CellShape;
import model.cell.Position;
import model.adjacency.SquareAdjacencyStrategy;
import model.adjacency.HexagonalAdjacencyStrategy;
import model.adjacency.TriangleAdjacencyStrategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Generator {

    private final Random random = new Random();

    /**
     * Genera un puzzle de Hidato con solución única.
     * @param difficulty Porcentaje de celdas a intentar vaciar (0.0 a 1.0).
     */
    public Board generatePuzzle(int rows, int cols, CellShape shape, double difficulty) {
        // 1. Generar tablero completo válido
        Board board = generateFullBoard(rows, cols, shape);
        if (board == null) return null;

        // 2. Preparar lista de celdas candidatas a vaciar
        List<Cell> candidates = new ArrayList<>();
        int maxVal = board.getRows() * board.getCols(); // Aproximado
        
        // Marcar inicialmente todas las celdas NO vacías como fijas
        // para que al quitar una, el resto sigan siendo restricciones.
        for (int i = 0; i < board.getRows(); i++) {
            for (int j = 0; j < board.getCols(); j++) {
                Cell c = board.getCell(i, j);
                if (!c.isVoid()) {
                    c.setFixedValue(c.getValue());
                    // Excluir start (1) y end (max) de ser eliminados
                    // (Aunque en Hidato hard a veces no estan, aquí simplificamos)
                    if (c.getValue() != 1 && c.getValue() != maxVal) {
                        candidates.add(c);
                    }
                }
            }
        }
        
        // 3. Mezclar candidatos
        Collections.shuffle(candidates, random);

        // 4. Intentar vaciar celdas una a una
        Solver solver = new Solver();
        int targetToRemove = (int) (candidates.size() * difficulty);
        int removedCount = 0;

        for (Cell c : candidates) {
            if (removedCount >= targetToRemove) break;

            int originalValue = c.getValue();
            
            // Tentativamente vaciar (desfijar y poner a 0)
            c.setAsEmpty(); 

            // Verificar si sigue teniendo solución única
            // IMPORTANTE: countSolutions(limit=2) devuelve 0, 1 o 2.
            int solutions = solver.countSolutions(board, 2);
            
            if (solutions != 1) {
                // Si se rompe a 0 o >1 soluciones, deshacemos
                c.setFixedValue(originalValue);
            } else {
                // Éxito, se queda vacía
                removedCount++;
            }
        }

        return board;
    }

    /**
     * Genera un tablero de Hidato válido y completo (sin huecos vacíos).
     */
    public Board generateFullBoard(int rows, int cols, CellShape shape) {
        // 1. Crear tablero vacío con la estrategia adecuada
        // NOTA: Para SquareFullAdjacencyStrategy, necesitaríamos un parámetro extra.
        // Asumimos estrategias base por forma.
        Board board;
        if (shape == CellShape.HEXAGON) {
            board = new Board(rows, cols, shape, new HexagonalAdjacencyStrategy());
        } else if (shape == CellShape.TRIANGLE) {
            board = new Board(rows, cols, shape, new TriangleAdjacencyStrategy());
        } else {
            board = new Board(rows, cols, shape, new SquareAdjacencyStrategy());
        }

        // 2. Elegir posición inicial aleatoria
        int startR = random.nextInt(rows);
        int startC = random.nextInt(cols);
        Cell startCell = board.getCell(startR, startC);
        startCell.setValue(1); 
        // 3. Usar el Solver
        Solver solver = new Solver();
        if (solver.solve(board)) {
            return board;
        } else {
            return null; 
        }
    }
}
