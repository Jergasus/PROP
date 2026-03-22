package test;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import model.adjacency.SquareAdjacencyStrategy;
import model.adjacency.SquareFullAdjacencyStrategy;
import model.algorithms.Solver;
import model.algorithms.Validator;
import model.board.Board;
import model.cell.CellShape;
import model.cell.Position;

/**
 * JUnit 4 tests for Solver.
 *
 * Covers:
 *  - solve(): trivial, sparse clues, 8-way, unsolvable boards
 *  - countSolutions(): unique, multiple, zero solutions
 */
public class SolverTest {

    private Solver   solver;
    private Validator validator;

    // -----------------------------------------------------------------------
    // Setup
    // -----------------------------------------------------------------------

    @Before
    public void setUp() {
        solver   = new Solver();
        validator = new Validator();
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private Board square4(int n, int[][] values) {
        Board b = new Board(n, n, CellShape.SQUARE, new SquareAdjacencyStrategy());
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                if (values[i][j] > 0) b.getCell(i, j).setFixedValue(values[i][j]);
        return b;
    }

    private Board square8(int n, int[][] values) {
        Board b = new Board(n, n, CellShape.SQUARE, new SquareFullAdjacencyStrategy());
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                if (values[i][j] > 0) b.getCell(i, j).setFixedValue(values[i][j]);
        return b;
    }

    // -----------------------------------------------------------------------
    // solve() — basic cases
    // -----------------------------------------------------------------------

    @Test
    public void solve_1x1_trivial() {
        // A single-cell board with value 1 is already a complete Hidato.
        Board b = new Board(1, 1, CellShape.SQUARE, new SquareAdjacencyStrategy());
        b.getCell(0, 0).setFixedValue(1);
        assertTrue(solver.solve(b));
        assertTrue(validator.isValidSolution(b));
    }

    @Test
    public void solve_2x2_withClues() {
        // 1 .       Solver must place 2 and 3
        // 4 .
        Board b = square4(2, new int[][]{{1,0},{4,0}});
        assertTrue(solver.solve(b));
        assertTrue(validator.isValidSolution(b));
    }

    @Test
    public void solve_3x3_sparseClues() {
        // 1 . .     Only first and last cell are fixed.
        // . . .     Solver has maximum freedom.
        // . . 9
        Board b = square4(3, new int[][]{{1,0,0},{0,0,0},{0,0,9}});
        assertTrue(solver.solve(b));
        assertTrue(validator.isValidSolution(b));
    }

    @Test
    public void solve_3x3_denseClues() {
        // Provide all but one clue — solver just fills the gap.
        // 1 2 3
        // 6 . 4
        // 7 8 9
        Board b = square4(3, new int[][]{{1,2,3},{6,0,4},{7,8,9}});
        assertTrue(solver.solve(b));
        assertEquals(5, b.getCell(1, 1).getValue());
        assertTrue(validator.isValidSolution(b));
    }

    @Test
    public void solve_8way_diagonalPath() {
        // 1 .       In 8-way, path can go diagonal.
        // . 4       1→2→3→4 with diagonal steps allowed.
        Board b = square8(2, new int[][]{{1,0},{0,4}});
        assertTrue(solver.solve(b));
        assertTrue(validator.isValidSolution(b));
    }

    @Test
    public void solve_resultMutatesBoard() {
        // After solve(), all non-void cells must be filled (no zeros).
        Board b = square4(3, new int[][]{{1,0,0},{0,0,0},{0,0,9}});
        solver.solve(b);
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                assertNotEquals("Cell (" + i + "," + j + ") still empty", 0, b.getCell(i,j).getValue());
    }

    // -----------------------------------------------------------------------
    // solve() — unsolvable boards
    // -----------------------------------------------------------------------

    @Test
    public void solve_unsolvable_noStart() {
        // Board has no cell with value 1 — solver cannot begin.
        Board b = square4(2, new int[][]{{0,2},{3,4}});
        assertFalse(solver.solve(b));
    }

    @Test
    public void solve_unsolvable_fixedCluesNotAdjacent() {
        // 1 and 2 are both fixed but placed diagonally — not adjacent in 4-way.
        // Only two non-void cells exist so the solver cannot route around them.
        Board b = new Board(3, 3, CellShape.SQUARE, new SquareAdjacencyStrategy());
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                b.getCell(i, j).setVoid(true);
        b.getCell(1, 1).setFixedValue(1); // at (1,1)
        b.getCell(2, 2).setFixedValue(2); // at (2,2) — diagonal from (1,1)
        assertFalse(solver.solve(b));
    }

    @Test
    public void solve_unsolvable_4way_corners() {
        // In 4-way, the path from (0,0) cannot jump to (1,1).
        // Force: 1 at (0,0), 2 at (1,1), rest void.
        Board b = new Board(2, 2, CellShape.SQUARE, new SquareAdjacencyStrategy());
        b.getCell(0, 1).setVoid(true);
        b.getCell(1, 0).setVoid(true);
        b.getCell(0, 0).setFixedValue(1);
        b.getCell(1, 1).setFixedValue(2);
        assertFalse(solver.solve(b));
    }

    // -----------------------------------------------------------------------
    // countSolutions()
    // -----------------------------------------------------------------------

    @Test
    public void countSolutions_uniqueSolution() {
        // 1 at (0,0), 4 at (0,1).
        // Only valid path: 1(0,0)→2(1,0)→3(1,1)→4(0,1)
        Board b = square4(2, new int[][]{{1,4},{0,0}});
        assertEquals(1, solver.countSolutions(b, 2));
    }

    @Test
    public void countSolutions_fullyFixedBoard() {
        // All values in place — exactly one solution exists.
        Board b = square4(2, new int[][]{{1,2},{4,3}});
        assertEquals(1, solver.countSolutions(b, 2));
    }

    @Test
    public void countSolutions_noSolution() {
        // No cell with value 1 — zero solutions.
        Board b = square4(2, new int[][]{{0,2},{3,4}});
        assertEquals(0, solver.countSolutions(b, 2));
    }

    @Test
    public void countSolutions_limitsEarlyExit() {
        // A very open 3×3 board with only value 1 fixed likely has many solutions.
        // countSolutions(limit=2) must return exactly 2 (stops counting after limit).
        Board b = square8(3, new int[][]{{1,0,0},{0,0,0},{0,0,0}});
        int count = solver.countSolutions(b, 2);
        assertEquals(2, count); // stopped early at the limit
    }

    // -----------------------------------------------------------------------
    // solve() — fixed cells must not be overwritten
    // -----------------------------------------------------------------------

    @Test
    public void solve_doesNotOverwriteFixedCells() {
        // 1 . .
        // . . .
        // . . 9    After solve, cell (0,0) must still be 1 and cell (2,2) must still be 9.
        Board b = square4(3, new int[][]{{1,0,0},{0,0,0},{0,0,9}});
        solver.solve(b);
        assertEquals(1, b.getCell(0, 0).getValue());
        assertEquals(9, b.getCell(2, 2).getValue());
        assertTrue(b.getCell(0, 0).isFixed());
        assertTrue(b.getCell(2, 2).isFixed());
    }
}
