package domini.algorithms;

import domini.model.board.Board;
import domini.model.cell.Cell;

public class Validator {

    public boolean isValidSolution(Board board) {
        int maxPossible = board.getRows() * board.getCols();
        Cell[] lookup = new Cell[maxPossible + 1];
        int cellCount = buildLookup(board, lookup, maxPossible);
        if (cellCount <= 0) return false;

        for (int v = 1; v <= cellCount; v++) {
            if (lookup[v] == null) return false;
            if (v < cellCount) {
                if (lookup[v + 1] == null) return false;
                if (!board.areAdjacent(lookup[v].getPosition(), lookup[v + 1].getPosition())) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isPartiallyValid(Board board) {
        int maxPossible = board.getRows() * board.getCols();
        Cell[] lookup = new Cell[maxPossible + 1];
        int cellCount = buildLookup(board, lookup, maxPossible);
        if (cellCount < 0) return false;
        if (cellCount == 0) return true;

        for (int v = 1; v < cellCount; v++) {
            if (lookup[v] != null && lookup[v + 1] != null) {
                if (!board.areAdjacent(lookup[v].getPosition(), lookup[v + 1].getPosition())) {
                    return false;
                }
            }
        }
        return true;
    }

    private int buildLookup(Board board, Cell[] lookup, int maxPossible) {
        int cellCount = 0;
        for (int i = 0; i < board.getRows(); i++) {
            for (int j = 0; j < board.getCols(); j++) {
                Cell c = board.getCell(i, j);
                if (c == null || c.isVoid()) continue;
                cellCount++;
                int v = c.getValue();
                if (v == 0) continue;
                if (v < 1 || v > maxPossible) return -1;
                if (lookup[v] != null) return -1;
                lookup[v] = c;
            }
        }
        return cellCount;
    }
}
