
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import domini.model.cell.Position;
import domini.model.cell.CellShape;
import domini.model.board.Board;
import domini.model.adjacency.*;
import domini.algorithms.Validator;

public class ValidatorTest {

    private Validator validator;

    @Before
    public void setUp() {
        validator = new Validator();
    }

    // Helper per crear taulers de prova ràpidament
    private Board createBoard(int rows, int cols, CellShape shape, AdjacencyStrategy strategy, int[][] values) {
        Board b = new Board(rows, cols, shape, strategy);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (values[i][j] == -1) {
                    b.getCell(new Position(i, j)).setVoid(true);
                } else if (values[i][j] > 0) {
                    b.getCell(new Position(i, j)).setValue(values[i][j]);
                }
            }
        }
        return b;
    }

    // --- TESTS DE CASOS VÀLIDS ---

    @Test
    public void isValid_Square4_Success() {
        // 1 2
        // 4 3
        int[][] vals = {{1, 2}, {4, 3}};
        Board b = createBoard(2, 2, CellShape.SQUARE, new SquareAdjacencyStrategy(), vals);
        assertTrue("Hauria de ser una solucio valida (4-way)", validator.isValidSolution(b));
    }

    @Test
    public void isValid_Square8_DiagonalSuccess() {
        // 1 4
        // 3 2  (1 i 2 estan en diagonal, valid en 8-way)
        int[][] vals = {{1, 4}, {3, 2}};
        Board b = createBoard(2, 2, CellShape.SQUARE, new SquareFullAdjacencyStrategy(), vals);
        assertTrue("Hauria de ser valida en 8-way per la diagonal", validator.isValidSolution(b));
    }

    // --- TESTS DE CASOS LÍMIT I ERRORS (INVALID SOLUTIONS) ---

    @Test
    public void isInvalid_BrokenPath() {
        // 1 2
        // 4 5 (Falta el 3)
        int[][] vals = {{1, 2}, {4, 5}};
        Board b = createBoard(2, 2, CellShape.SQUARE, new SquareAdjacencyStrategy(), vals);
        assertFalse("Hauria de fallar perque el cami esta trencat (falta el 3)", validator.isValidSolution(b));
    }

    @Test
    public void isInvalid_DuplicateNumbers() {
        // 1 2
        // 2 3 (El 2 esta repetit)
        int[][] vals = {{1, 2}, {2, 3}};
        Board b = createBoard(2, 2, CellShape.SQUARE, new SquareAdjacencyStrategy(), vals);
        assertFalse("Hauria de fallar per numeros repetits", validator.isValidSolution(b));
    }

    @Test
    public void isInvalid_EmptyCells() {
        // 1 2
        // 0 3 (La cella amb 0 es considera buida)
        int[][] vals = {{1, 2}, {0, 3}};
        Board b = createBoard(2, 2, CellShape.SQUARE, new SquareAdjacencyStrategy(), vals);
        assertFalse("Una solucio no pot tenir celles buides (valor 0)", validator.isValidSolution(b));
    }

    @Test
    public void isInvalid_JumpTooLarge() {
        // 1 3 (Salts de 1 a 3 no permesos en 4-way si no hi ha el 2)
        // 4 2
        int[][] vals = {{1, 3}, {4, 2}};
        Board b = createBoard(2, 2, CellShape.SQUARE, new SquareAdjacencyStrategy(), vals);
        assertFalse("Celles consecutives han d'estar adjacents", validator.isValidSolution(b));
    }

    @Test
    public void isInvalid_WrongTopology() {
        // 1 2 (En triangles o hexagons l'adjacencia canvia)
        // 4 3
        int[][] vals = {{1, 2}, {4, 3}};
        // Suposem que en la topologia triangular (0,0) i (1,1) no fossin adjacents
        Board b = createBoard(2, 2, CellShape.TRIANGLE, new TriangleAdjacencyStrategy(), vals);
        // El resultat dependra de la teva implementacio de TriangleAdjacencyStrategy
        // pero serveix per testejar que el validador consulta l'estrategia correctament.
        boolean res = validator.isValidSolution(b);
        // Si no son adjacents, ha de ser false
        assertFalse("Hauria de fallar si la topologia no permet el pas", res);
    }

    @Test
    public void isValid_WithVoids() {
        // 1  2
        // V  3  (V = Void)
        int[][] vals = {{1, 2}, {-1, 3}};
        Board b = createBoard(2, 2, CellShape.SQUARE, new SquareAdjacencyStrategy(), vals);
        assertTrue("Hauria de ser valida ignorant la cella void", validator.isValidSolution(b));
    }
}