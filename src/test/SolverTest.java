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

public class SolverTest {

    private Solver   solver;
    private Validator validator;

    @Before
    public void setUp() {
        solver   = new Solver();
        validator = new Validator();
    }

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

    @Test
    public void solve_1x1_trivial() {
        Board b = new Board(1, 1, CellShape.SQUARE, new SquareAdjacencyStrategy());
        b.getCell(0, 0).setFixedValue(1);
        assertTrue(solver.solve(b));
        assertTrue(validator.isValidSolution(b));
    }

    @Test
    public void solve_2x2_withClues() {
        // 1 .
        // 4 .
        Board b = square4(2, new int[][]{{1,0},{4,0}});
        assertTrue(solver.solve(b));
        assertTrue(validator.isValidSolution(b));
    }

    @Test
    public void solve_3x3_sparseClues() {
        // 1 . .
        // . . .
        // . . 9
        Board b = square4(3, new int[][]{{1,0,0},{0,0,0},{0,0,9}});
        assertTrue(solver.solve(b));
        assertTrue(validator.isValidSolution(b));
    }

    @Test
    public void solve_3x3_denseClues() {
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
        // 1 .    diagonal step allowed in 8-way
        // . 4
        Board b = square8(2, new int[][]{{1,0},{0,4}});
        assertTrue(solver.solve(b));
        assertTrue(validator.isValidSolution(b));
    }

    @Test
    public void solve_resultMutatesBoard() {
        Board b = square4(3, new int[][]{{1,0,0},{0,0,0},{0,0,9}});
        solver.solve(b);
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                assertNotEquals("Cell (" + i + "," + j + ") still empty", 0, b.getCell(i,j).getValue());
    }

    @Test
    public void solve_unsolvable_noStart() {
        Board b = square4(2, new int[][]{{0,2},{3,4}});
        assertFalse(solver.solve(b));
    }

    @Test
    public void solve_unsolvable_fixedCluesNotAdjacent() {
        // only two non-void cells, diagonal in 4-way
        Board b = new Board(3, 3, CellShape.SQUARE, new SquareAdjacencyStrategy());
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                b.getCell(i, j).setVoid(true);
        b.getCell(1, 1).setFixedValue(1);
        b.getCell(2, 2).setFixedValue(2);
        assertFalse(solver.solve(b));
    }

    @Test
    public void solve_unsolvable_4way_corners() {
        Board b = new Board(2, 2, CellShape.SQUARE, new SquareAdjacencyStrategy());
        b.getCell(0, 1).setVoid(true);
        b.getCell(1, 0).setVoid(true);
        b.getCell(0, 0).setFixedValue(1);
        b.getCell(1, 1).setFixedValue(2);
        assertFalse(solver.solve(b));
    }

    @Test
    public void countSolutions_uniqueSolution() {
        // 1→2(1,0)→3(1,1)→4(0,1) is the only path
        Board b = square4(2, new int[][]{{1,4},{0,0}});
        assertEquals(1, solver.countSolutions(b, 2));
    }

    @Test
    public void countSolutions_fullyFixedBoard() {
        Board b = square4(2, new int[][]{{1,2},{4,3}});
        assertEquals(1, solver.countSolutions(b, 2));
    }

    @Test
    public void countSolutions_noSolution() {
        Board b = square4(2, new int[][]{{0,2},{3,4}});
        assertEquals(0, solver.countSolutions(b, 2));
    }

    @Test
    public void countSolutions_limitsEarlyExit() {
        Board b = square8(3, new int[][]{{1,0,0},{0,0,0},{0,0,0}});
        assertEquals(2, solver.countSolutions(b, 2));
    }

    @Test
    public void solve_doesNotOverwriteFixedCells() {
        Board b = square4(3, new int[][]{{1,0,0},{0,0,0},{0,0,9}});
        solver.solve(b);
        assertEquals(1, b.getCell(0, 0).getValue());
        assertEquals(9, b.getCell(2, 2).getValue());
        assertTrue(b.getCell(0, 0).isFixed());
        assertTrue(b.getCell(2, 2).isFixed());
    }
}
