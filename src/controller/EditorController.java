package controller;

import model.algorithms.Solver;
import model.board.Board;
import model.cell.Cell;
import model.game.Game;
import model.game.MoveInput;
import persistence.game.GameSaver;

public class EditorController {
    private final Board board;
    private final DomainController domain;
    private final Solver solver;
    private final GameSaver saver;

    public EditorController(Board board, DomainController domain) {
        this.board = board;
        this.domain = domain;
        this.solver = new Solver();
        this.saver = new GameSaver();
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
            domain.printMessage("  -2: Validar y buscar soluciones");
            domain.printMessage("  -3: Guardar como partida jugable");

            MoveInput input = domain.askMove();
            if (input == null) {
                domain.printMessage("Entrada inválida.");
                continue;
            }

            int r = input.row();
            if (r == -1) {
                editing = false;
            } else if (r == -2) {
                validateBoard();
            } else if (r == -3) {
                saveAsGame();
            } else {
                // Modificar celda
                int c = input.col();
                int v = input.value();
                modifyCell(r, c, v);
            }
        }
    }

    private void modifyCell(int r, int c, int val) {
        Cell cell = board.getCell(r, c);
        if (cell == null) {
            domain.printMessage("Coordenadas fuera de rango.");
            return;
        }
        
        // Limpiar estado previo (setAsEmpty handles fixed cells correctly)
        cell.setAsEmpty();
        
        if (val == -1) {
            cell.setVoid(true);
            domain.printMessage("Celda (" + r + "," + c + ") marcada como VOID.");
        } else if (val == 0) {
            // Ya se limpió arriba
            domain.printMessage("Celda (" + r + "," + c + ") limpiada.");
        } else if (val > 0) {
            // En el editor, todos los números que el creador pone son PISTAS FIJAS.
            // Usar setFixedValue para que surrender() y el juego no los borre/permita modificar.
            cell.setFixedValue(val);
            domain.printMessage("Pista fija " + val + " colocada en (" + r + "," + c + ").");
        }
    }

    private void validateBoard() {
        domain.printMessage("Analizando tablero...");
        // Validar conteo de celdas vs rango de números
        // El solver asume que el tablero está bien formado.
        int solutionCount = solver.countSolutions(board, 2);
        
        if (solutionCount == 0) {
            domain.printMessage("❌ El tablero NO tiene solución.");
        } else if (solutionCount == 1) {
            domain.printMessage("✅ El tablero es un Hidato Válido (Solución única).");
        } else {
            domain.printMessage("⚠️ El tablero tiene MÚLTIPLES soluciones (" + solutionCount + "+).");
        }
    }

    private void saveAsGame() {
        String name = domain.askString("Nombre del archivo (sin extensión):");
        if (name == null || name.trim().isEmpty()) return;

        // Crear una instancia de Game con este tablero
        Game newGame = new Game(board);
        // Guardar
        try {
            saver.saveGame(newGame, name + ".hidato");
            domain.printMessage("Partida guardada correctamente en " + name + ".hidato");
        } catch (Exception e) {
            domain.printMessage("Error al guardar: " + e.getMessage());
        }
    }
}
