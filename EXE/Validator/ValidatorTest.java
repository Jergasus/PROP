package test;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import domini.model.adjacency.SquareAdjacencyStrategy;
import domini.model.adjacency.SquareFullAdjacencyStrategy;
import domini.algorithms.Validator;
import domini.model.board.Board;
import domini.model.cell.CellShape;

public class ValidatorTest {

    private Validator validator;

    @Before
    public void setUp() {
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
    public void validSolution_correct2x2() {
        // 1 2
        // 4 3
        assertTrue(validator.isValidSolution(square4(2, new int[][]{{1,2},{4,3}})));
    }

    @Test
    public void validSolution_emptyBoardFails() {
        assertFalse(validator.isValidSolution(square4(2, new int[][]{{0,0},{0,0}})));
    }

    @Test
    public void validSolution_duplicateValueFails() {
        assertFalse(validator.isValidSolution(square4(2, new int[][]{{1,1},{3,4}})));
    }

    @Test
    public void validSolution_brokenPath4way() {
        // 1→2 would require diagonal step, not valid in 4-way
        assertFalse(validator.isValidSolution(square4(2, new int[][]{{1,3},{4,2}})));
    }

    @Test
    public void validSolution_correct3x3Snake() {
        // 1 2 3
        // 6 5 4
        // 7 8 9
        assertTrue(validator.isValidSolution(square4(3, new int[][]{{1,2,3},{6,5,4},{7,8,9}})));
    }

    @Test
    public void validSolution_broken3x3Fails() {
        // 6→7 is diagonal in 4-way
        assertFalse(validator.isValidSolution(square4(3, new int[][]{{1,2,3},{6,5,4},{9,7,8}})));
    }

    @Test
    public void validSolution_8way_orthogonalOk() {
        assertTrue(validator.isValidSolution(square8(2, new int[][]{{1,2},{4,3}})));
    }

    @Test
    public void validSolution_8way_diagonalAllowed() {
        // 1→2 diagonal, valid in 8-way
        assertTrue(validator.isValidSolution(square8(2, new int[][]{{1,3},{4,2}})));
    }

    @Test
    public void validSolution_boardWithVoids() {
        // 6→7 not adjacent (distance > 1)
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

    @Test
    public void partiallyValid_emptyBoardIsOk() {
        assertTrue(validator.isPartiallyValid(square4(2, new int[][]{{0,0},{0,0}})));
    }

    @Test
    public void partiallyValid_noConflict() {
        assertTrue(validator.isPartiallyValid(square4(2, new int[][]{{1,0},{0,4}})));
    }

    @Test
    public void partiallyValid_conflictDetected() {
        // 1 at (0,0) and 2 at (1,1): diagonal in 4-way
        assertFalse(validator.isPartiallyValid(square4(2, new int[][]{{1,0},{0,2}})));
    }

    @Test
    public void partiallyValid_gapAllowed() {
        // gap between 1 and 3 — no direct adjacency check needed
        assertTrue(validator.isPartiallyValid(square4(2, new int[][]{{1,0},{0,3}})));
    }

    @Test
    public void partiallyValid_conflict8way_diagonalOk() {
        // diagonal is fine in 8-way
        assertTrue(validator.isPartiallyValid(square8(2, new int[][]{{1,0},{0,2}})));
    }
}
