package controller;

import model.algorithms.Solver;
import model.board.Board;
import model.cell.Cell;
import model.game.Game;
import model.game.MoveInput;
import persistence.HidatoFileParser;
import persistence.game.GameSaver;

import java.io.IOException;

public class EditorController {
    private final Board board;
    private final DomainController domain;
    private final Solver solver;
    private final GameSaver saver;
    private final HidatoFileParser formatParser;

    public EditorController(Board board, DomainController domain) {
        this.board = board;
        this.domain = domain;
        this.solver = new Solver();
        this.saver = new GameSaver();
        this.formatParser = new HidatoFileParser();
    }

    public void startEditor() {
        String adjInfo = board.getAdjacencyStrategyName();
        boolean editing = true;
        while (editing) {
            domain.printMessage("\n=== EDITOR DE HIDATO [Adyacencia: " + adjInfo + "] ===");
            domain.printBoard(board);
            domain.printMessage("Comandos:");
            domain.printMessage("  [fila] [col] [valor] -> Poner número (1..N)");
            domain.printMessage("  [fila] [col] -1      -> Poner agujero (Void)");
            domain.printMessage("  [fila] [col] 0       -> Borrar celda (Empty)");
            domain.printMessage("Acciones:");
            domain.printMessage("  -1: Salir (Sin guardar)");
            domain.printMessage("  -2: Validar, analizar y resolver");
            domain.printMessage("  -3: Guardar como partida jugable (.hidato)");
            domain.printMessage("  -4: Exportar en formato estándar (.txt)");

            MoveInput input = domain.askMove();
            if (input == null) { domain.printMessage("Entrada inválida."); continue; }

            int r = input.row();
            switch (r) {
                case -1 -> editing = false;
                case -2 -> validateAndSolve();
                case -3 -> saveAsGame();
                case -4 -> exportAsText();
                default -> modifyCell(r, input.col(), input.value());
            }
        }
    }

    private void modifyCell(int r, int c, int val) {
        Cell cell = board.getCell(r, c);
        if (cell == null) { domain.printMessage("Coordenadas fuera de rango."); return; }

        cell.setAsEmpty();
        if (val == -1) {
            cell.setVoid(true);
            domain.printMessage("Celda (" + r + "," + c + ") marcada como VOID.");
        } else if (val == 0) {
            domain.printMessage("Celda (" + r + "," + c + ") limpiada.");
        } else if (val > 0) {
            cell.setFixedValue(val);
            domain.printMessage("Pista fija " + val + " colocada en (" + r + "," + c + ").");
        }
    }

    // -----------------------------------------------------------------------
    // Validate, analyse and optionally show solution (with machine time)
    // -----------------------------------------------------------------------

    private void validateAndSolve() {
        domain.printMessage("Analizando tablero...");

        long start = System.currentTimeMillis();
        int solutionCount = solver.countSolutions(board, 2);
        long machineTime = System.currentTimeMillis() - start;

        domain.printMessage("Tiempo de análisis (máquina): " + domain.formatTime(machineTime));

        if (solutionCount == 0) {
            domain.printMessage("El tablero NO tiene solución.");
        } else if (solutionCount == 1) {
            domain.printMessage("El tablero es un Hidato válido (solución única).");
            String ans = domain.askString("¿Mostrar la solución? (s/n)");
            if (ans.equalsIgnoreCase("s")) {
                Board solutionBoard = new Board(board);
                long solveStart = System.currentTimeMillis();
                solver.solve(solutionBoard);
                long solveTime = System.currentTimeMillis() - solveStart;
                domain.printBoard(solutionBoard);
                domain.printMessage("Tiempo de resolución (máquina): " + domain.formatTime(solveTime));
            }
        } else {
            domain.printMessage("El tablero tiene MÚLTIPLES soluciones (" + solutionCount + "+). Añade más pistas para hacerlo único.");
        }
    }

    // -----------------------------------------------------------------------
    // Save as playable game (Java serialization)
    // -----------------------------------------------------------------------

    private void saveAsGame() {
        String name = domain.askString("Nombre del archivo (sin extensión):");
        if (name == null || name.trim().isEmpty()) return;

        try {
            saver.saveGame(new Game(board), name + ".hidato");
            domain.printMessage("Partida guardada en " + name + ".hidato");
        } catch (Exception e) {
            domain.printMessage("Error al guardar: " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Export in standard spec format (readable text, shareable as level file)
    // -----------------------------------------------------------------------

    private void exportAsText() {
        String name = domain.askString("Nombre del archivo de exportación (sin extensión):");
        if (name == null || name.trim().isEmpty()) return;

        try {
            formatParser.saveBoard(board, name + ".txt");
            domain.printMessage("Exportado en formato estándar: " + name + ".txt");
        } catch (IOException e) {
            domain.printMessage("Error al exportar: " + e.getMessage());
        }
    }
}
