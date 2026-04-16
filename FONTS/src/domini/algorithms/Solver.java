package domini.algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import domini.model.board.Board;
import domini.model.cell.Cell;

public class Solver {

    public boolean solve(Board board) {
        int maxVal = board.getCellCount();
        Map<Integer, Cell> clueMap = new HashMap<>();
        Cell startCell = buildClueMap(board, clueMap);
        if (startCell == null) return false;
        return solveRecursive(board, startCell, 2, maxVal, clueMap);
    }

    public int countSolutions(Board board, int limit) {
        int maxVal = board.getCellCount();
        Map<Integer, Cell> clueMap = new HashMap<>();
        Cell startCell = buildClueMap(board, clueMap);
        if (startCell == null) return 0;
        return countRecursive(board, startCell, 2, maxVal, limit, clueMap);
    }

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

    private boolean solveRecursive(Board board, Cell current, int nextVal,
                                   int maxVal, Map<Integer, Cell> clueMap) {
        if (nextVal > maxVal) return true;

        Cell fixedNext = clueMap.get(nextVal);
        if (fixedNext != null) {
            if (board.areAdjacent(current.getPosition(), fixedNext.getPosition())) {
                return solveRecursive(board, fixedNext, nextVal + 1, maxVal, clueMap);
            }
            return false;
        }

        // Warnsdorff: try candidates in ascending order of their free-neighbor count
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
        int size = candidates.size();
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

            if (count >= limit) return count;
        }
        return count;
    }

    private int countFreeNeighbors(Board board, Cell cell) {
        int count = 0;
        for (Cell n : board.getNeighbors(cell.getPosition())) {
            if (n.getValue() == 0) count++;
        }
        return count;
    }
}
