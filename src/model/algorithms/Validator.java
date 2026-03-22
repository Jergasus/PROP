package model.algorithms;

import model.board.Board;
import model.cell.Cell;

/**
 * Validates Hidato board states.
 *
 * A valid Hidato solution requires that the numbers 1..N form a consecutive
 * path where every pair of successive numbers occupies adjacent cells
 * (according to the board's adjacency strategy).
 *
 * Optimisations applied:
 *  - The value→cell lookup table and the non-void cell count are built in a
 *    single O(N) grid pass (instead of calling getCellCount() separately).
 *  - The "all slots filled" check and the adjacency path walk are merged into
 *    one loop (instead of two sequential loops).
 *  - Adjacency is tested via Board.areAdjacent(), which delegates to
 *    AdjacencyStrategy.areAdjacent().  For Square/SquareFull geometries this
 *    is an O(1) arithmetic check that avoids allocating a neighbour list.
 */
public class Validator {

    /**
     * Returns true if the board is a complete, valid Hidato solution:
     *   - Every non-void cell is filled (no zeros).
     *   - Numbers 1..N form a continuous path where consecutive values are
     *     adjacent according to the board's adjacency strategy.
     *
     * @param board the board to validate
     * @return true if the board is a fully valid solution
     */
    public boolean isValidSolution(Board board) {
        // Single pass: build lookup AND count non-void cells simultaneously.
        int maxPossible = board.getRows() * board.getCols();
        Cell[] lookup = new Cell[maxPossible + 1];
        int cellCount = buildLookup(board, lookup, maxPossible);
        if (cellCount < 0) return false; // duplicate or out-of-range value

        if (cellCount == 0) return false;

        // Single path walk: checks both "every value is present" and
        // "consecutive values are adjacent" in one pass.
        // If lookup[v] is null for any v in 1..cellCount, the board is incomplete.
        for (int v = 1; v <= cellCount; v++) {
            if (lookup[v] == null) return false; // gap — cell not filled
            if (v < cellCount) {
                if (lookup[v + 1] == null) return false; // next step missing
                if (!board.areAdjacent(lookup[v].getPosition(), lookup[v + 1].getPosition())) {
                    return false; // consecutive values not adjacent
                }
            }
        }
        return true;
    }

    /**
     * Returns true if the current (possibly partial) board state has no rule
     * violations.  Empty cells are allowed; already-placed numbers must not
     * conflict.
     *
     * Specifically checks:
     *   - No duplicate values among non-void cells.
     *   - For every pair of consecutive values that are both already placed,
     *     the two cells must be adjacent.
     *
     * @param board the board to check
     * @return true if the partial state is consistent
     */
    public boolean isPartiallyValid(Board board) {
        int maxPossible = board.getRows() * board.getCols();
        Cell[] lookup = new Cell[maxPossible + 1];
        int cellCount = buildLookup(board, lookup, maxPossible);
        if (cellCount < 0) return false; // duplicate or out-of-range value

        if (cellCount == 0) return true;

        // For each placed value v, if v+1 is also placed they must be adjacent.
        for (int v = 1; v < cellCount; v++) {
            if (lookup[v] != null && lookup[v + 1] != null) {
                if (!board.areAdjacent(lookup[v].getPosition(), lookup[v + 1].getPosition())) {
                    return false;
                }
            }
        }
        return true;
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /**
     * Single-pass scan that builds a value→cell array and counts non-void cells.
     *
     * @param board      the board to scan
     * @param lookup     pre-allocated array of size (maxPossible + 1); index 0 unused
     * @param maxPossible upper bound for valid cell values (rows * cols)
     * @return the number of non-void cells, or -1 if a duplicate or
     *         out-of-range value is detected
     */
    private int buildLookup(Board board, Cell[] lookup, int maxPossible) {
        int cellCount = 0;
        for (int i = 0; i < board.getRows(); i++) {
            for (int j = 0; j < board.getCols(); j++) {
                Cell c = board.getCell(i, j);
                if (c == null || c.isVoid()) continue;
                cellCount++;
                int v = c.getValue();
                if (v == 0) continue; // empty — legal in partial boards
                if (v < 1 || v > maxPossible) return -1; // out of range
                if (lookup[v] != null) return -1;         // duplicate
                lookup[v] = c;
            }
        }
        return cellCount;
    }
}
