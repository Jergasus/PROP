package model.algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.board.Board;
import model.cell.Cell;

/**
 * Solves Hidato puzzles using backtracking with two optimisations:
 *
 *  1. Clue map  — a HashMap<value, Cell> built once (O(N)) replaces the
 *     O(N) linear scan that would otherwise be needed for every fixed-clue
 *     look-up during the search.
 *
 *  2. Warnsdorff's heuristic — at each step the empty neighbours are tried
 *     in ascending order of their own free-neighbour count (most-constrained
 *     first).  This dramatically reduces the branching factor and the amount
 *     of backtracking needed.
 *
 * The initialisation (building the clue map and locating the start cell) is
 * done in a single O(N) pass instead of the two separate passes that a naive
 * implementation would require.
 */
public class Solver {

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /**
     * Fills {@code board} in-place with a valid Hidato solution.
     *
     * @return true if a solution was found and applied, false otherwise
     */
    public boolean solve(Board board) {
        int maxVal = board.getCellCount();
        Map<Integer, Cell> clueMap = new HashMap<>();
        Cell startCell = buildClueMap(board, clueMap); // single O(N) pass
        if (startCell == null) return false;
        return solveRecursive(board, startCell, 2, maxVal, clueMap);
    }

    /**
     * Counts the number of distinct solutions, stopping early once
     * {@code limit} solutions have been found.
     *
     * Used by the Generator to verify solution uniqueness efficiently
     * (call with limit=2: if the result is 1 the puzzle is unique).
     *
     * @return the number of solutions found, capped at {@code limit}
     */
    public int countSolutions(Board board, int limit) {
        int maxVal = board.getCellCount();
        Map<Integer, Cell> clueMap = new HashMap<>();
        Cell startCell = buildClueMap(board, clueMap);
        if (startCell == null) return 0;
        return countRecursive(board, startCell, 2, maxVal, limit, clueMap);
    }

    // -----------------------------------------------------------------------
    // Initialisation — single pass
    // -----------------------------------------------------------------------

    /**
     * Scans the board once to:
     *   (a) populate {@code clueMap} with every already-placed value, and
     *   (b) return the cell that holds value 1 (the path start).
     *
     * Doing both jobs in one pass avoids the duplicate O(N) scan that
     * separate findStartCell() and buildFixedMap() methods would require.
     *
     * @return the start cell (value == 1), or null if not present
     */
    private Cell buildClueMap(Board board, Map<Integer, Cell> clueMap) {
        Cell startCell = null;
        for (int i = 0; i < board.getRows(); i++) {
            for (int j = 0; j < board.getCols(); j++) {
                Cell c = board.getCell(i, j);
                if (c == null || c.isVoid() || c.getValue() == 0) continue;
                clueMap.put(c.getValue(), c);
                if (c.getValue() == 1) startCell = c;
            }
        }
        return startCell;
    }

    // -----------------------------------------------------------------------
    // Backtracking — solve
    // -----------------------------------------------------------------------

    private boolean solveRecursive(Board board, Cell current, int nextVal,
                                   int maxVal, Map<Integer, Cell> clueMap) {
        if (nextVal > maxVal) return true;

        // O(1) fixed-clue check: if nextVal is already placed, the path must
        // reach it directly from current — no branching needed.
        Cell fixedNext = clueMap.get(nextVal);
        if (fixedNext != null) {
            if (board.areAdjacent(current.getPosition(), fixedNext.getPosition())) {
                return solveRecursive(board, fixedNext, nextVal + 1, maxVal, clueMap);
            }
            return false;
        }

        // Warnsdorff's heuristic (fixed):
        //   Step 1 — pre-filter: collect only empty neighbours (avoids sorting
        //            cells that will never be tried).
        //   Step 2 — pre-compute each candidate's free-neighbour count exactly
        //            once before sorting (avoids the O(k log k) repeated calls
        //            that a comparator-based approach would incur).
        //   Step 3 — sort by score ascending (most constrained first).
        List<Cell> rawNeighbors = board.getNeighbors(current.getPosition());
        List<Cell> candidates = new ArrayList<>(rawNeighbors.size());
        int[] scores = new int[rawNeighbors.size()];
        int idx = 0;
        for (Cell n : rawNeighbors) {
            if (n.getValue() == 0) {
                candidates.add(n);
                scores[idx++] = countFreeNeighbors(board, n);
            }
        }
        // Attach pre-computed scores for the sort (identity map via index).
        // Build a small wrapper to avoid allocating a separate int[] during sort.
        int size = candidates.size();
        // Simple insertion sort — candidates list is small (≤ 8 elements),
        // so insertion sort outperforms Arrays.sort on tiny inputs with no
        // extra allocation.
        for (int i = 1; i < size; i++) {
            Cell keyCell = candidates.get(i);
            int keyScore = scores[i];
            int j = i - 1;
            while (j >= 0 && scores[j] > keyScore) {
                candidates.set(j + 1, candidates.get(j));
                scores[j + 1] = scores[j];
                j--;
            }
            candidates.set(j + 1, keyCell);
            scores[j + 1] = keyScore;
        }

        for (Cell candidate : candidates) {
            candidate.setValue(nextVal);
            clueMap.put(nextVal, candidate);

            if (solveRecursive(board, candidate, nextVal + 1, maxVal, clueMap)) {
                return true;
            }

            candidate.setValue(0);
            clueMap.remove(nextVal);
        }
        return false;
    }

    // -----------------------------------------------------------------------
    // Backtracking — count solutions
    // -----------------------------------------------------------------------

    private int countRecursive(Board board, Cell current, int nextVal,
                                int maxVal, int limit, Map<Integer, Cell> clueMap) {
        if (nextVal > maxVal) return 1;

        Cell fixedNext = clueMap.get(nextVal);
        if (fixedNext != null) {
            if (board.areAdjacent(current.getPosition(), fixedNext.getPosition())) {
                return countRecursive(board, fixedNext, nextVal + 1, maxVal, limit, clueMap);
            }
            return 0;
        }

        int count = 0;
        for (Cell neighbor : board.getNeighbors(current.getPosition())) {
            if (neighbor.getValue() != 0) continue;

            neighbor.setValue(nextVal);
            clueMap.put(nextVal, neighbor);

            count += countRecursive(board, neighbor, nextVal + 1, maxVal, limit, clueMap);

            neighbor.setValue(0);
            clueMap.remove(nextVal);

            if (count >= limit) return count; // early exit
        }
        return count;
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /** Counts the empty (value == 0) non-void neighbours of {@code cell}. */
    private int countFreeNeighbors(Board board, Cell cell) {
        int count = 0;
        for (Cell n : board.getNeighbors(cell.getPosition())) {
            if (n.getValue() == 0) count++;
        }
        return count;
    }
}
