package test;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import model.adjacency.*;
import model.algorithms.Generator;
import model.algorithms.Solver;
import model.algorithms.Validator;
import model.board.Board;
import model.cell.CellShape;

/**
 * JUnit 4 tests for Generator.
 *
 * Covers:
 *  - generateFullBoard(): all cells filled, solution valid
 *  - generatePuzzle(): non-null, solvable, unique solution, has clues
 *  - Multiple geometries: square, hexagonal
 */
public class GeneratorTest {

    private Generator generator;
    private Solver    solver;
    private Validator validator;

    // -----------------------------------------------------------------------
    // Setup
    // -----------------------------------------------------------------------

    @Before
    public void setUp() {
        generator = new Generator();
        solver    = new Solver();
        validator = new Validator();
    }

    // -----------------------------------------------------------------------
    // generateFullBoard()
    // -----------------------------------------------------------------------

    @Test
    public void fullBoard_notNull() {
        Board b = generator.generateFullBoard(4, 4, CellShape.SQUARE, new SquareAdjacencyStrategy());
        assertNotNull("generateFullBoard must return a non-null board", b);
    }

    @Test
    public void fullBoard_allCellsFilled() {
        Board b = generator.generateFullBoard(4, 4, CellShape.SQUARE, new SquareAdjacencyStrategy());
        assertNotNull(b);
        for (int i = 0; i < b.getRows(); i++)
            for (int j = 0; j < b.getCols(); j++)
                if (!b.getCell(i, j).isVoid())
                    assertNotEquals("Cell (" + i + "," + j + ") must be filled",
                                    0, b.getCell(i, j).getValue());
    }

    @Test
    public void fullBoard_isValidSolution() {
        Board b = generator.generateFullBoard(4, 4, CellShape.SQUARE, new SquareAdjacencyStrategy());
        assertNotNull(b);
        assertTrue("generateFullBoard must produce a valid Hidato solution",
                   validator.isValidSolution(b));
    }

    @Test
    public void fullBoard_hex_notNull() {
        Board b = generator.generateFullBoard(3, 4, CellShape.HEXAGON, new HexagonalAdjacencyStrategy());
        assertNotNull(b);
        assertTrue(validator.isValidSolution(b));
    }

    // -----------------------------------------------------------------------
    // generatePuzzle()
    // -----------------------------------------------------------------------

    @Test
    public void puzzle_notNull() {
        Board b = generator.generatePuzzle(4, 4, CellShape.SQUARE, new SquareAdjacencyStrategy(), 0.4);
        assertNotNull("generatePuzzle must return a non-null board", b);
    }

    @Test
    public void puzzle_isSolvable() {
        Board puzzle = generator.generatePuzzle(4, 4, CellShape.SQUARE, new SquareAdjacencyStrategy(), 0.4);
        assertNotNull(puzzle);
        Board copy = new Board(puzzle); // solve on copy to preserve puzzle state
        assertTrue("Generated puzzle must be solvable", solver.solve(copy));
        assertTrue("Solved puzzle must be valid", validator.isValidSolution(copy));
    }

    @Test
    public void puzzle_uniqueSolution() {
        Board puzzle = generator.generatePuzzle(4, 4, CellShape.SQUARE, new SquareAdjacencyStrategy(), 0.4);
        assertNotNull(puzzle);
        assertEquals("Generated puzzle must have exactly one solution",
                     1, solver.countSolutions(puzzle, 2));
    }

    @Test
    public void puzzle_hasAtLeastOneClue() {
        // A puzzle must always keep value 1 (the start of the path).
        Board puzzle = generator.generatePuzzle(4, 4, CellShape.SQUARE, new SquareAdjacencyStrategy(), 0.4);
        assertNotNull(puzzle);
        boolean hasOne = false;
        for (int i = 0; i < puzzle.getRows() && !hasOne; i++)
            for (int j = 0; j < puzzle.getCols() && !hasOne; j++)
                if (puzzle.getCell(i, j).getValue() == 1) hasOne = true;
        assertTrue("Generated puzzle must retain the starting clue (value 1)", hasOne);
    }

    @Test
    public void puzzle_notFullyFilled() {
        // A puzzle with difficulty > 0 must have at least one empty cell.
        Board puzzle = generator.generatePuzzle(4, 4, CellShape.SQUARE, new SquareAdjacencyStrategy(), 0.5);
        assertNotNull(puzzle);
        boolean hasEmpty = false;
        for (int i = 0; i < puzzle.getRows() && !hasEmpty; i++)
            for (int j = 0; j < puzzle.getCols() && !hasEmpty; j++)
                if (!puzzle.getCell(i,j).isVoid() && puzzle.getCell(i,j).getValue() == 0) hasEmpty = true;
        assertTrue("Puzzle with difficulty > 0 must have empty cells", hasEmpty);
    }

    @Test
    public void puzzle_8way_uniqueSolution() {
        Board puzzle = generator.generatePuzzle(3, 3, CellShape.SQUARE, new SquareFullAdjacencyStrategy(), 0.3);
        assertNotNull(puzzle);
        assertEquals(1, solver.countSolutions(puzzle, 2));
    }

    @Test
    public void puzzle_hex_solvableAndUnique() {
        Board puzzle = generator.generatePuzzle(3, 3, CellShape.HEXAGON, new HexagonalAdjacencyStrategy(), 0.3);
        assertNotNull(puzzle);
        assertEquals(1, solver.countSolutions(puzzle, 2));
    }

    @Test
    public void puzzle_zeroDifficulty_stillSolvable() {
        // Difficulty 0 means no cells removed — should return a fully solved board.
        Board puzzle = generator.generatePuzzle(3, 3, CellShape.SQUARE, new SquareAdjacencyStrategy(), 0.0);
        assertNotNull(puzzle);
        Board copy = new Board(puzzle);
        assertTrue(solver.solve(copy));
    }
}
