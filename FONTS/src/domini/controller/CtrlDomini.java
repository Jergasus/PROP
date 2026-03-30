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
        
        // 1. Demanem a l'stub (el nostre gestor de dades) tota la informació del catàleg
        String[][] dadesCataleg = stubDades.getDadesCataleg();

        if (dadesCataleg.length == 0) {
            System.err.println("[CtrlDomini] Cap hidato trobat pel gestor de dades.");
            return list;
        }

        // 2. Iterem sobre la informació rebuda i demanem el contingut CSV del tauler
        for (String[] infoHidato : dadesCataleg) {
            try {
                String fileName = infoHidato[0];
                String[] csvLines = stubDades.loadHidato(fileName);
                
                // Un hidato vàlid necessita almenys capçalera + 1 fila de tauler
                if (csvLines.length >= 2) { 
                    list.add(parse(infoHidato, csvLines));
                }
            } catch (Exception e) {
                System.err.println("[CtrlDomini] No s'ha pogut carregar " + infoHidato[0] + ": " + e.getMessage());
            }
        }
        return list;
    }

    
    // Processa la informació del catàleg i les línies pures del CSV
    private Hidato parse(String[] infoHidato, String[] csvLines) {
        String name      = infoHidato[1];
        String adjDesc   = infoHidato[2];
        boolean solvable = infoHidato[3].equalsIgnoreCase("SOLVABLE");
        String desc      = infoHidato[4];

        // Línia 0 del CSV: <tipus_cella>,<tipus_adjacencia>,<files>,<columnes>
        String[] header = csvLines[0].split(",");
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

        // Els valors del tauler comencen a l'índex 1 de l'array 'csvLines' ara!
        for (int r = 0; r < rows; r++) {
            String[] tokens = csvLines[r + 1].split(",");
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