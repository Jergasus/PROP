package model.algorithms;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.board.Board;
import model.cell.Cell;

public class Solver {

    /**
     * Resuelve el tablero usando Backtracking + dos optimizaciones:
     * 1. HashMap de pistas fijas para busqueda O(1) en vez de O(n^2).
     * 2. Warnsdorff's Rule: probar primero vecinos con menos opciones futuras.
     */
    public boolean solve(Board board) {
        Cell startCell = findStartCell(board);
        if (startCell == null) return false;
        int maxVal = countTotalCells(board);
        Map<Integer, Cell> fixedMap = buildFixedMap(board);
        return solveRecursive(board, startCell, 2, maxVal, fixedMap);
    }

    public int countSolutions(Board board, int limit) {
        Cell startCell = findStartCell(board);
        if (startCell == null) return 0;
        int maxVal = countTotalCells(board);
        Map<Integer, Cell> fixedMap = buildFixedMap(board);
        return countRecursive(board, startCell, 2, maxVal, limit, fixedMap);
    }

    // Optimizacion 1: construir mapa {valor -> celda} para lookup O(1)
    private Map<Integer, Cell> buildFixedMap(Board board) {
        Map<Integer, Cell> map = new HashMap<>();
        for (int i = 0; i < board.getRows(); i++) {
            for (int j = 0; j < board.getCols(); j++) {
                Cell c = board.getCell(i, j);
                if (c != null && c.getValue() > 0) {
                    map.put(c.getValue(), c);
                }
            }
        }
        return map;
    }

    private boolean solveRecursive(Board board, Cell currentCell, int nextVal, int maxVal, Map<Integer, Cell> fixedMap) {
        if (nextVal > maxVal) return true;

        // O(1): comprobar si nextVal ya es una pista fija
        Cell fixedNext = fixedMap.get(nextVal);
        if (fixedNext != null) {
            if (areNeighbors(board, currentCell, fixedNext)) {
                return solveRecursive(board, fixedNext, nextVal + 1, maxVal, fixedMap);
            } else {
                return false;
            }
        }

        // Optimizacion 2: Warnsdorff's Rule
        // Ordenar vecinos vacios por cuantas opciones libres tienen.
        // Primero los mas restringidos (menos opciones futuras) -> menos backtracking.
        List<Cell> neighbors = board.getNeighbors(currentCell.getPosition());
        neighbors.sort((a, b) -> {
            if (a.getValue() != 0) return 1;
            if (b.getValue() != 0) return -1;
            return countFreeNeighbors(board, a) - countFreeNeighbors(board, b);
        });

        for (Cell neighbor : neighbors) {
            if (neighbor.getValue() == 0) {
                neighbor.setValue(nextVal);
                fixedMap.put(nextVal, neighbor);

                if (solveRecursive(board, neighbor, nextVal + 1, maxVal, fixedMap)) {
                    return true;
                }

                neighbor.setValue(0);
                fixedMap.remove(nextVal);
            }
        }
        return false;
    }

    private int countRecursive(Board board, Cell currentCell, int nextVal, int maxVal, int limit, Map<Integer, Cell> fixedMap) {
        if (nextVal > maxVal) return 1;

        int count = 0;
        Cell fixedNext = fixedMap.get(nextVal);
        if (fixedNext != null) {
            if (areNeighbors(board, currentCell, fixedNext)) {
                return countRecursive(board, fixedNext, nextVal + 1, maxVal, limit, fixedMap);
            } else {
                return 0;
            }
        }

        List<Cell> neighbors = board.getNeighbors(currentCell.getPosition());
        for (Cell neighbor : neighbors) {
            if (neighbor.getValue() == 0) {
                neighbor.setValue(nextVal);
                fixedMap.put(nextVal, neighbor);

                count += countRecursive(board, neighbor, nextVal + 1, maxVal, limit, fixedMap);

                neighbor.setValue(0);
                fixedMap.remove(nextVal);

                if (count >= limit) return count;
            }
        }
        return count;
    }

    // Warnsdorff helper: cuantos vecinos vacios tiene una celda ..
    private int countFreeNeighbors(Board board, Cell cell) {
        int count = 0;
        for (Cell n : board.getNeighbors(cell.getPosition())) {
            if (n.getValue() == 0) count++;
        }
        return count;
    }

    private boolean areNeighbors(Board board, Cell c1, Cell c2) {
        return board.getNeighbors(c1.getPosition()).contains(c2);
    }

    private Cell findStartCell(Board board) {
        for (int i = 0; i < board.getRows(); i++) {
            for (int j = 0; j < board.getCols(); j++) {
                Cell cell = board.getCell(i, j);
                if (cell != null && cell.getValue() == 1) return cell;
            }
        }
        return null;
    }

    private int countTotalCells(Board board) {
        int count = 0;
        for (int i = 0; i < board.getRows(); i++) {
            for (int j = 0; j < board.getCols(); j++) {
                Cell c = board.getCell(i, j);
                if (c != null && !c.isVoid()) count++;
            }
        }
        return count;
    }
}
