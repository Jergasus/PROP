
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import domini.model.adjacency.SquareAdjacencyStrategy;
import domini.model.adjacency.SquareFullAdjacencyStrategy;
import domini.model.adjacency.HexagonalAdjacencyStrategy;
import domini.model.adjacency.TriangleAdjacencyStrategy;
import domini.model.board.Board;
import domini.model.cell.Cell;
import domini.model.cell.CellShape;
import domini.model.cell.Position;

import java.util.List;

public class BoardTest {

    private Board board3x3;

    @Before
    public void setUp() {
        board3x3 = new Board(3, 3, CellShape.SQUARE, new SquareAdjacencyStrategy());
    }

    @Test
    public void dimensions_areCorrect() {
        assertEquals("El nombre de files ha de ser 3", 3, board3x3.getRows());
        assertEquals("El nombre de columnes ha de ser 3", 3, board3x3.getCols());
    }

    @Test
    public void getCellShape_returnsCorrectShape() {
        assertEquals("La geometria del tauler base ha de ser SQUARE", CellShape.SQUARE, board3x3.getCellShape());
        Board hex = new Board(3, 3, CellShape.HEXAGON, new HexagonalAdjacencyStrategy());
        assertEquals("La geometria del tauler hex ha de ser HEXAGON", CellShape.HEXAGON, hex.getCellShape());
    }

    // Comprova que la còpia és independent de l'original
    @Test
    public void copyConstructor_isDeepCopy() {
        board3x3.getCell(0, 0).setFixedValue(5);
        Board copy = new Board(board3x3);

        assertEquals("La còpia ha de preservar els valors fixats prèviament", 5, copy.getCell(0, 0).getValue());

        copy.getCell(0, 0).setAsEmpty();
        assertEquals("Modificar la cel·la de la còpia no ha d'alterar l'original", 5, board3x3.getCell(0, 0).getValue());
    }

    @Test
    public void getCell_returnsCell() {
        assertNotNull("La cel·la superior esquerra (0,0) ha d'existir", board3x3.getCell(0, 0));
        assertNotNull("La cel·la inferior dreta (2,2) ha d'existir", board3x3.getCell(2, 2));
    }

    // Ha de retornar null en lloc de llançar IndexOutOfBounds
    @Test
    public void getCell_outOfBounds_returnsNull() {
        assertNull("Una fila negativa ha de retornar null", board3x3.getCell(-1, 0));
        assertNull("Una columna igual o major a l'amplada ha de retornar null", board3x3.getCell(0, 3));
        assertNull("Una coordenada (3,3) en un tauler de 3x3 ha de retornar null", board3x3.getCell(3, 3));
    }

    @Test
    public void getCellCount_allPlayable() {
        assertEquals("En un tauler de 3x3 sense forats hi ha d'haver 9 cel·les jugables", 9, board3x3.getCellCount());
    }

    @Test
    public void getCellCount_withVoids() {
        board3x3.getCell(0, 0).setVoid(true);
        board3x3.getCell(1, 1).setVoid(true);
        assertEquals("Si es marquen 2 forats, el recompte ha de ser de 7 cel·les", 7, board3x3.getCellCount());
    }

    @Test
    public void getNeighbors_4way_centerCell() {
        List<Cell> neighbors = board3x3.getNeighbors(new Position(1, 1));
        assertEquals("El centre d'un tauler de 4 costats ha de tenir exactament 4 veïns", 4, neighbors.size());
    }

    @Test
    public void getNeighbors_4way_cornerCell() {
        List<Cell> neighbors = board3x3.getNeighbors(new Position(0, 0));
        assertEquals("Una cantonada en un tauler de 4 costats ha de tenir exactament 2 veïns", 2, neighbors.size());
    }

    @Test
    public void getNeighbors_excludesVoidCells() {
        board3x3.getCell(0, 1).setVoid(true);
        board3x3.getCell(1, 0).setVoid(true);
        List<Cell> neighbors = board3x3.getNeighbors(new Position(0, 0));
        assertEquals("Si s'anul·len els dos únics veïns de la cantonada, la llista de veïns ha de ser buida", 0, neighbors.size());
    }

    @Test
    public void areAdjacent_4way_orthogonal() {
        assertTrue("Les posicions (0,0) i (0,1) són adjacents horitzontalment", board3x3.areAdjacent(new Position(0, 0), new Position(0, 1)));
        assertTrue("Les posicions (1,1) i (2,1) són adjacents verticalment", board3x3.areAdjacent(new Position(1, 1), new Position(2, 1)));
    }

    @Test
    public void areAdjacent_4way_diagonal_false() {
        assertFalse("En adjacència de 4 costats, les posicions diagonals (0,0) i (1,1) NO han de ser adjacents", board3x3.areAdjacent(new Position(0, 0), new Position(1, 1)));
    }

    @Test
    public void areAdjacent_8way_diagonalAllowed() {
        Board b = new Board(3, 3, CellShape.SQUARE, new SquareFullAdjacencyStrategy());
        assertTrue("L'estratègia de 8 costats ha de considerar adjacents les cel·les diagonals", b.areAdjacent(new Position(0, 0), new Position(1, 1)));
    }

    @Test
    public void getNeighbors_hexBoard_evenRow() {
        Board hex = new Board(3, 3, CellShape.HEXAGON, new HexagonalAdjacencyStrategy());
        List<Cell> neighbors = hex.getNeighbors(new Position(0, 1));
        assertFalse("El càlcul de veïns en un tauler hexagonal no ha de retornar una llista buida per una posició vàlida", neighbors.isEmpty());
    }

    @Test
    public void getNeighbors_triangleBoard() {
        Board t = new Board(2, 3, CellShape.TRIANGLE, new TriangleAdjacencyStrategy());
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 3; j++) {
                assertFalse("En un tauler triangular inicial sense forats, cap cel·la ha de tenir 0 veïns", t.getNeighbors(new Position(i, j)).isEmpty());
            }
        }
    }

    @Test
    public void pruneDeadEnds_removesCorners_onTriangleBoard() {
        Board t = new Board(2, 3, CellShape.TRIANGLE, new TriangleAdjacencyStrategy());
        t.pruneDeadEnds();

        for (int i = 0; i < t.getRows(); i++) {
            for (int j = 0; j < t.getCols(); j++) {
                Cell c = t.getCell(i, j);
                if (!c.isVoid()) {
                    assertTrue("Després de la poda, cap cel·la jugable ha de tenir menys de 2 veïns", t.getNeighbors(c.getPosition()).size() >= 2);
                }
            }
        }
    }

    @Test
    public void isConnected_fullBoard() {
        assertTrue("Un tauler completament lliure es considera connectat", board3x3.isConnected());
    }

    @Test
    public void isConnected_afterVoidSplitsBoard() {
        // Buidem tota la columna del mig per separar l'esquerra de la dreta
        for (int i = 0; i < 3; i++) board3x3.getCell(i, 1).setVoid(true);
        assertFalse("Si es divideix el tauler en dues meitats incomunicades, isConnected ha de retornar fals", board3x3.isConnected());
    }

    @Test
    public void isConnected_singleCell() {
        Board b = new Board(1, 1, CellShape.SQUARE, new SquareAdjacencyStrategy());
        assertTrue("Un tauler d'1x1 es considera connectat per definició", b.isConnected());
    }
}
