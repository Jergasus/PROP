package domini.controller;

import domini.model.board.Board;
import domini.model.Hidato.Hidato;
import domini.model.cell.CellShape;
import domini.model.adjacency.AdjacencyStrategy;
import domini.model.adjacency.HexagonalAdjacencyStrategy;
import domini.model.adjacency.SquareAdjacencyStrategy;
import domini.model.adjacency.SquareFullAdjacencyStrategy;
import domini.model.adjacency.TriangleAdjacencyStrategy;
import domini.algorithms.Solver;
import domini.algorithms.Validator;
import domini.stubs.StubGestorDades;

import java.util.ArrayList;
import java.util.List;

public class CtrlDomini {

    private final Solver solver = new Solver();
    private final Validator validator = new Validator();
    
    //Domini es comunica amb la Persistència a través del Stub
    private final StubGestorDades stubDades = new StubGestorDades();
    
    private final List<Hidato> catalog;
    private Board activeBoard;

    public CtrlDomini() {
        catalog = buildCatalog();
    }

    public int getCatalogSize()                  { return catalog.size(); }
    public String getCatalogCaseName(int i)         { return catalog.get(i).getName(); }
    public String getCatalogCaseAdjacency(int i)    { return catalog.get(i).getAdjacencyDesc(); }
    public boolean getCatalogCaseSolvable(int i)    { return catalog.get(i).isExpectedSolvable(); }
    public String getCatalogCaseDescription(int i)  { return catalog.get(i).getDescription(); }

    public void selectCase(int index) {
        activeBoard = catalog.get(index).getBoard();
    }

    public String getActiveBoardAsString() {
        if (activeBoard == null) return "(cap tauler seleccionat)";
        return activeBoard.toString();
    }

    public boolean isPartiallyValid() {
        if (activeBoard == null) return false;
        return validator.isPartiallyValid(activeBoard);
    }

    public boolean isValidSolution() {
        if (activeBoard == null) return false;
        return validator.isValidSolution(activeBoard);
    }

    public boolean hasSolution() {
        if (activeBoard == null) return false;
        Board copy = new Board(activeBoard);
        return solver.solve(copy);
    }

    public boolean solve() {
        if (activeBoard == null) return false;
        return solver.solve(activeBoard);
    }

    // --- MÈTODES PRIVATS PER CARREGAR I CONSTRUIR OBJECTES DEL DOMINI ---

    private List<Hidato> buildCatalog() {
        List<Hidato> list = new ArrayList<>();
        
        // 1. Demanem a l'stub quins fitxers hi ha disponibles
        String[] fileNames = stubDades.getLlistaHidatos();

        if (fileNames.length == 0) {
            System.err.println("[CtrlDomini] Cap hidato trobat pel gestor de dades.");
            return list;
        }

        // 2. Iterem i demanem el contingut en text pla per a cada fitxer
        for (String fName : fileNames) {
            try {
                String[] lines = stubDades.loadHidato(fName);
                if (lines.length >= 6) {
                    list.add(parse(lines));
                }
            } catch (Exception e) {
                System.err.println("[CtrlDomini] No s'ha pogut carregar " + fName + ": " + e.getMessage());
            }
        }
        return list;
    }

    // Processa un array de Strings i el converteix en un objecte Hidato
    private Hidato parse(String[] lines) {
        String name     = lines[0];
        String adjDesc  = lines[1];
        boolean solvable = lines[2].equalsIgnoreCase("SOLVABLE");
        String desc     = lines[3];

        String[] header = lines[4].split(",");
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
            default:
                shape    = CellShape.SQUARE;
                strategy = adjCode.equals("CA")
                    ? new SquareFullAdjacencyStrategy()
                    : new SquareAdjacencyStrategy();
        }

        Board board = new Board(rows, cols, shape, strategy);

        // Els valors del tauler comencen a l'índex 5 de l'array 'lines'
        for (int r = 0; r < rows; r++) {
            String[] tokens = lines[r + 5].split(",");
            for (int c = 0; c < cols; c++) {
                String token = tokens[c].trim();
                switch (token) {
                    case "#": 
                    case "*": 
                        board.getCell(r, c).setVoid(true); 
                        break;
                    case "?": 
                        break;
                    default:  
                        board.getCell(r, c).setFixedValue(Integer.parseInt(token));
                }
            }
        }

        return new Hidato(name, adjDesc, solvable, desc, board);
    }
}