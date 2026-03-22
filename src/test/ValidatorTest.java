package test;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import model.adjacency.SquareAdjacencyStrategy;
import model.adjacency.SquareFullAdjacencyStrategy;
import model.algorithms.Validator;
import model.board.Board;
import model.cell.CellShape;

/**
 * JUnit 4 tests for Validator.
 *
 * Covers:
 *  - isValidSolution(): complete boards (correct, empty, duplicate, broken path, 8-way diagonal)
 *  - isPartiallyValid(): partial boards (no conflict, conflict, empty)
 */
public class ValidatorTest {

    private Validator validator;

    // -----------------------------------------------------------------------
    // Setup
    // -----------------------------------------------------------------------

    @Before
    public void setUp() {
        validator = new Validator();
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /** Creates an N×N square board (4-way) and pre-fills it with the given values. */
    private Board square4(int n, int[][] values) {
        Board b = new Board(n, n, CellShape.SQUARE, new SquareAdjacencyStrategy());
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                if (values[i][j] > 0) b.getCell(i, j).setFixedValue(values[i][j]);
        return b;
    }

    /** Creates an N×N square board (8-way) and pre-fills it with the given values. */
    private Board square8(int n, int[][] values) {
        Board b = new Board(n, n, CellShape.SQUARE, new SquareFullAdjacencyStrategy());
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                if (values[i][j] > 0) b.getCell(i, j).setFixedValue(values[i][j]);
        return b;
    }

    // -----------------------------------------------------------------------
    // isValidSolution — complete boards
    // -----------------------------------------------------------------------

    @Test
    public void validSolution_correct2x2() {
        // 1 2      path: (0,0)→(0,1)→(1,1)→(1,0) — all orthogonally adjacent
        // 4 3
        assertTrue(validator.isValidSolution(square4(2, new int[][]{{1,2},{4,3}})));
    }

    @Test
    public void validSolution_emptyBoardFails() {
        assertFalse(validator.isValidSolution(square4(2, new int[][]{{0,0},{0,0}})));
    }

    @Test
    public void validSolution_duplicateValueFails() {
        // 1 1 — value 1 appears twice
        // 3 4
        assertFalse(validator.isValidSolution(square4(2, new int[][]{{1,1},{3,4}})));
    }

    @Test
    public void validSolution_brokenPath4way() {
        // 1 3       1 at (0,0), 2 at (1,1): diagonal — NOT adjacent in 4-way
        // 4 2
        assertFalse(validator.isValidSolution(square4(2, new int[][]{{1,3},{4,2}})));
    }

    @Test
    public void validSolution_correct3x3Snake() {
        // 1 2 3
        // 6 5 4    each consecutive pair is orthogonally adjacent
        // 7 8 9
        assertTrue(validator.isValidSolution(square4(3, new int[][]{{1,2,3},{6,5,4},{7,8,9}})));
    }

    @Test
    public void validSolution_broken3x3Fails() {
        // 1 2 3
        // 6 5 4    6→7: (1,0)→(2,1) is diagonal in 4-way → INVALID
        // 9 7 8
        assertFalse(validator.isValidSolution(square4(3, new int[][]{{1,2,3},{6,5,4},{9,7,8}})));
    }

    @Test
    public void validSolution_8way_orthogonalOk() {
        // Same snake path is also valid with 8-way adjacency
        assertTrue(validator.isValidSolution(square8(2, new int[][]{{1,2},{4,3}})));
    }

    @Test
    public void validSolution_8way_diagonalAllowed() {
        // 1 3       1→2: (0,0)→(1,1) — diagonal, valid in 8-way
        // 4 2       2→3: (1,1)→(0,1) — orthogonal, valid
        //           3→4: (0,1)→(1,0) — diagonal, valid in 8-way
        assertTrue(validator.isValidSolution(square8(2, new int[][]{{1,3},{4,2}})));
    }

    @Test
    public void validSolution_boardWithVoids() {
        // 3×3 board with two void cells.
        // Non-void cells must form a valid path 1..7.
        // Layout (V=void):
        //  1  2  V
        //  6  3  V
        //  5  4  7   ← 6→7 is NOT adjacent (distance > 1), should fail
        Board b = new Board(3, 3, CellShape.SQUARE, new SquareAdjacencyStrategy());
        b.getCell(0, 2).setVoid(true);
        b.getCell(1, 2).setVoid(true);
        int[][] values = {{1,2,0},{6,3,0},{5,4,7}};
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                if (values[i][j] > 0 && !b.getCell(i,j).isVoid())
                    b.getCell(i, j).setFixedValue(values[i][j]);
        assertFalse(validator.isValidSolution(b));
    }

    // -----------------------------------------------------------------------
    // isPartiallyValid — partial boards
    // -----------------------------------------------------------------------

    @Test
    public void partiallyValid_emptyBoardIsOk() {
        // No numbers placed yet — no constraint can be violated
        assertTrue(validator.isPartiallyValid(square4(2, new int[][]{{0,0},{0,0}})));
    }

    @Test
    public void partiallyValid_noConflict() {
        // Only 1 placed — no consecutive pair to check
        assertTrue(validator.isPartiallyValid(square4(2, new int[][]{{1,0},{0,4}})));
    }

    @Test
    public void partiallyValid_conflictDetected() {
        // 1 at (0,0) and 2 at (1,1): diagonal in 4-way — consecutive but not adjacent
        assertFalse(validator.isPartiallyValid(square4(2, new int[][]{{1,0},{0,2}})));
    }

    @Test
    public void partiallyValid_gapAllowed() {
        // 1 at (0,0), 3 at (1,1): gap (2 missing) — no direct adjacency check needed
        assertTrue(validator.isPartiallyValid(square4(2, new int[][]{{1,0},{0,3}})));
    }

    @Test
    public void partiallyValid_conflict8way_diagonalOk() {
        // In 8-way, 1 at (0,0) and 2 at (1,1) ARE adjacent — no conflict
        assertTrue(validator.isPartiallyValid(square8(2, new int[][]{{1,0},{0,2}})));
    }
}
