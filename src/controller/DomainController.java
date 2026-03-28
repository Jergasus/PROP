package controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import model.adjacency.*;
import model.algorithms.Solver;
import model.algorithms.Validator;
import model.board.Board;
import model.cell.CellShape;

/**
 * Mediator between the driver (UI) and the domain layer (Solver, Validator).
 *
 * The driver must not instantiate Solver or Validator directly.
 * All puzzle operations go through this class.
 *
 * Board format used in the catalog (matches the official PDF spec):
 *   Line 0: TYPE,ADJ,rows,cols
 *            TYPE: Q=square  H=hexagon  T=triangle
 *            ADJ:  C=sides   CA=sides+angles
 *   Lines 1..rows:  comma-separated cell tokens per row
 *            integer → fixed clue value
 *            #       → outside area (void)
 *            *       → inaccessible (also void)
 *            ?       → empty cell to fill
 */
public class DomainController {

    private final Solver solver = new Solver();
    private final Validator validator = new Validator();
    private final List<HidatoCaseController> catalog;

    public DomainController() {
        catalog = buildCatalog();
    }

    // ------------------------------------------------------------------ //
    //  Public API — called only by the driver                             //
    // ------------------------------------------------------------------ //

    public List<HidatoCaseController> getCatalog() {
        return Collections.unmodifiableList(catalog);
    }

    public HidatoCaseController getCase(int index) {
        return catalog.get(index);
    }

    /** True if the board has no contradictions (consecutive clues adjacent, no duplicates). */
    public boolean isPartiallyValid(Board board) {
        return validator.isPartiallyValid(board);
    }

    /** True only if the board is fully filled and forms a valid Hidato solution. */
    public boolean isValidSolution(Board board) {
        return validator.isValidSolution(board);
    }

    /** Solves the board in-place. Returns true if a solution was found. */
    public boolean solve(Board board) {
        return solver.solve(board);
    }

    /** Counts distinct solutions up to {@code limit}. */
    public int countSolutions(Board board, int limit) {
        return solver.countSolutions(board, limit);
    }

    // ------------------------------------------------------------------ //
    //  Predefined catalog                                                 //
    // ------------------------------------------------------------------ //

    private List<HidatoCaseController> buildCatalog() {
        List<HidatoCaseController> list = new ArrayList<>();

        // ── SOLVABLE ────────────────────────────────────────────────────

        list.add(parse(
            "Square 4-way  ·  3×3 straight path",
            "Square — sides only",
            true,
            "Minimal 3×3 board. Clues 1 (top-left) and 9 (bottom-right) force a single winding path.",
            new String[]{
                "Q,C,3,3",
                "1,?,?",
                "?,?,?",
                "?,?,9"
            }
        ));

        list.add(parse(
            "Square 8-way  ·  3×4 L-shape  (PDF example)",
            "Square — sides + diagonals",
            true,
            "Official PDF example. L-shaped playable area; clues 1, 7 and 9 anchor the path.",
            new String[]{
                "Q,CA,3,4",
                "#,1,?,#",
                "?,?,?,?",
                "7,?,9,#"
            }
        ));

        list.add(parse(
            "Square 8-way  ·  5×5 diamond  (PDF example)",
            "Square — sides + diagonals",
            true,
            "Diamond-shaped 5×5 board from the spec. Four fixed clues (1, 3, 8, 11). " +
            "Inaccessible cells (*) are treated as void.",
            new String[]{
                "Q,CA,5,5",
                "#,#,1,#,#",
                "#,?,*,?,#",
                "8,?,?,?,3",
                "#,?,11,*,#",
                "#,#,?,#,#"
            }
        ));

        list.add(parse(
            "Hexagon sides  ·  4×3 offset grid  (PDF example)",
            "Hexagonal — sides only",
            true,
            "Offset hexagonal grid from the official PDF. Eight playable cells; " +
            "clues 1 and 8 anchor the start and end.",
            new String[]{
                "H,C,4,3",
                "#,*,?",
                "?,?,*",
                "1,?,8",
                "?,?,#"
            }
        ));

        list.add(parse(
            "Triangle sides  ·  4×4 pruned corners",
            "Triangle — sides only",
            true,
            "4×4 triangular grid. Corner cells (0,3) and (3,3) have only one neighbour " +
            "and are pre-voided. Value 1 placed at top-left.",
            new String[]{
                "T,C,4,4",
                "1,?,?,#",
                "?,?,?,?",
                "?,?,?,?",
                "?,?,?,#"
            }
        ));

        // ── UNSOLVABLE ──────────────────────────────────────────────────

        list.add(parse(
            "Square 4-way  ·  Non-adjacent 1 and 2  [UNSOLVABLE]",
            "Square — sides only",
            false,
            "Clues 1 and 2 are placed two rows apart with only 4-way adjacency. " +
            "They cannot be neighbours, so no valid path exists.",
            new String[]{
                "Q,C,3,3",
                "?,1,?",
                "?,?,?",
                "?,2,?"
            }
        ));

        list.add(parse(
            "Square 8-way  ·  Trapped endpoint  [UNSOLVABLE]",
            "Square — sides + diagonals",
            false,
            "Void barriers isolate clue 3 at (0,2). After the forced move 1→2→3, " +
            "cell (0,2) has no free neighbours — the path cannot continue.",
            new String[]{
                "Q,CA,3,3",
                "1,#,3",
                "#,?,#",
                "?,?,?"
            }
        ));

        list.add(parse(
            "Triangle sides  ·  Barrier splits 1 and 2  [UNSOLVABLE]",
            "Triangle — sides only",
            false,
            "Clues 1 (col 0) and 2 (col 4) are separated by a full void barrier in row 0. " +
            "They cannot be adjacent, making the puzzle unsolvable.",
            new String[]{
                "T,C,3,5",
                "1,#,#,#,2",
                "?,?,?,?,?",
                "#,#,#,#,#"
            }
        ));

        return list;
    }

    // ------------------------------------------------------------------ //
    //  Board parsing (PDF format → Board)                                 //
    // ------------------------------------------------------------------ //

    private HidatoCaseController parse(String name, String adjDesc, boolean solvable,
                             String description, String[] lines) {
        String[] header = lines[0].split(",");
        String typeCode = header[0].trim();
        String adjCode  = header[1].trim();
        int rows        = Integer.parseInt(header[2].trim());
        int cols        = Integer.parseInt(header[3].trim());

        CellShape shape;
        AdjacencyStrategy strategy;
        switch (typeCode) {
            case "H":
                shape    = CellShape.HEXAGON;
                strategy = new HexagonalAdjacencyStrategy();
                break;
            case "T":
                shape    = CellShape.TRIANGLE;
                strategy = new TriangleAdjacencyStrategy();
                break;
            default: // "Q"
                shape    = CellShape.SQUARE;
                strategy = adjCode.equals("CA")
                    ? new SquareFullAdjacencyStrategy()
                    : new SquareAdjacencyStrategy();
        }

        Board board = new Board(rows, cols, shape, strategy);

        for (int r = 0; r < rows; r++) {
            String[] tokens = lines[r + 1].split(",");
            for (int c = 0; c < cols; c++) {
                String token = tokens[c].trim();
                switch (token) {
                    case "#":
                    case "*":
                        board.getCell(r, c).setVoid(true);
                        break;
                    case "?":
                        break; // already empty by default
                    default:
                        board.getCell(r, c).setFixedValue(Integer.parseInt(token));
                }
            }
        }

        return new HidatoCaseController(name, adjDesc, solvable, description, board);
    }
}
