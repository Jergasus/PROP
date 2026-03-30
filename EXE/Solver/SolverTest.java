
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import domini.model.cell.Position;
import domini.model.cell.CellShape;
import domini.model.board.Board;
import domini.model.adjacency.*;
import domini.algorithms.Solver;
import domini.algorithms.Validator;

public class SolverTest {

    private Solver solver;
    private Validator validator;

    @Before
    public void setUp() {
        solver = new Solver();
        validator = new Validator();
    }

    // --- HELPERS ---
    private Board square4(int n, int[][] values) {
        Board b = new Board(n, n, CellShape.SQUARE, new SquareAdjacencyStrategy());
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                if (values[i][j] > 0) b.getCell(new Position(i, j)).setFixedValue(values[i][j]);
        return b;
    }

    private Board square8(int n, int[][] values) {
        Board b = new Board(n, n, CellShape.SQUARE, new SquareFullAdjacencyStrategy());
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                if (values[i][j] > 0) b.getCell(new Position(i, j)).setFixedValue(values[i][j]);
        return b;
    }

    // --- TOTS ELS TEUS TESTS ORIGINALS (CORREGITS) ---

    @Test
    public void solve_1x1_trivial() {
        Board b = new Board(1, 1, CellShape.SQUARE, new SquareAdjacencyStrategy());
        b.getCell(new Position(0, 0)).setFixedValue(1);
        assertTrue(solver.solve(b));
    }

    @Test
    public void solve_2x2_withClues() {
        Board b = square4(2, new int[][]{{1,0},{4,0}});
        assertTrue(solver.solve(b));
    }

    @Test
    public void solve_3x3_sparseClues() {
        Board b = square4(3, new int[][]{{1,0,0},{0,0,0},{0,0,9}});
        assertTrue(solver.solve(b));
    }

    @Test
    public void solve_3x3_denseClues() {
        Board b = square4(3, new int[][]{{1,2,3},{6,0,4},{7,8,9}});
        assertTrue(solver.solve(b));
        assertEquals(5, b.getCell(new Position(1, 1)).getValue());
    }

    @Test
    public void solve_8way_diagonalPath() {
        Board b = square8(2, new int[][]{{1,0},{0,4}});
        assertTrue(solver.solve(b));
    }

    @Test
    public void solve_unsolvable_noStart() {
        Board b = square4(2, new int[][]{{0,2},{3,4}});
        assertFalse(solver.solve(b));
    }

    @Test
    public void solve_hexagonal_solvable() {
        Board b = new Board(4, 3, CellShape.HEXAGON, new HexagonalAdjacencyStrategy());
        b.getCell(new Position(0, 0)).setVoid(true);
        b.getCell(new Position(2, 0)).setFixedValue(1);
        b.getCell(new Position(2, 2)).setFixedValue(8);
        assertTrue(solver.solve(b));
    }

    @Test
    public void solve_triangle_solvable() {
        Board b = new Board(2, 3, CellShape.TRIANGLE, new TriangleAdjacencyStrategy());
        b.getCell(new Position(0, 0)).setFixedValue(1);
        b.getCell(new Position(0, 1)).setFixedValue(6);
        assertTrue(solver.solve(b));
    }

    @Test
    public void countSolutions_uniqueSolution() {
        Board b = square4(2, new int[][]{{1,4},{0,0}});
        assertEquals(1, solver.countSolutions(b, 2));
    }

    @Test
    public void countSolutions_limitsEarlyExit() {
        Board b = square8(3, new int[][]{{1,0,0},{0,0,0},{0,0,0}});
        assertEquals(2, solver.countSolutions(b, 2));
    }

    // --- ELS NOUS CASOS EXTREMS AFEGITS ---

    @Test
    public void solve_unsolvable_isolatedIsland() {
        Board b = new Board(3, 3, CellShape.SQUARE, new SquareAdjacencyStrategy());
        b.getCell(new Position(0, 1)).setVoid(true);
        b.getCell(new Position(1, 0)).setVoid(true);
        b.getCell(new Position(2, 1)).setVoid(true);
        b.getCell(new Position(1, 2)).setVoid(true);
        b.getCell(new Position(0, 0)).setFixedValue(1);
        b.getCell(new Position(2, 2)).setFixedValue(5);
        assertFalse("Hauria de fallar per cella central inaccessibe", solver.solve(b));
    }

    @Test
    public void solve_unsolvable_impossibleDistance() {
        Board b = new Board(3, 3, CellShape.SQUARE, new SquareAdjacencyStrategy());
        b.getCell(new Position(0, 0)).setFixedValue(1);
        b.getCell(new Position(0, 2)).setFixedValue(2);
        assertFalse("Distancia impossible per 4-way", solver.solve(b));
    }

    @Test
    public void solve_alreadySolvedBoard() {
        int[][] values = {{1, 2}, {4, 3}};
        Board b = square4(2, values);
        assertTrue(solver.solve(b));
    }
}