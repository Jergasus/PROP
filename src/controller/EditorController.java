package controller;

import model.algorithms.Solver;
import model.board.Board;
import model.cell.Cell;
import model.game.Game;
import persistence.game.GameSaver;
import view.ConsoleView;
import view.ConsoleView.MoveInput;

public class EditorController {
    private final Board board;
    private final ConsoleView view;
    private final Solver solver;
    private final GameSaver saver;

    public EditorController(Board board, ConsoleView view) {
        this.board = board;
        this.view = view;
        this.solver = new Solver();
        this.saver = new GameSaver();
    }

    public void startEditor() {
        String adjInfo = board.getAdjacencyStrategyName();
        boolean editing = true;
        while (editing) {
            view.printMessage("\n=== EDITOR DE HIDATO [Adyacencia: " + adjInfo + "] ===");
            view.printBoard(board);
            view.printMessage("Comandos:");
            view.printMessage("  [fila] [col] [valor] -> Poner número (1..N)");
            view.printMessage("  [fila] [col] -1      -> Poner agujero (Void)");
            view.printMessage("  [fila] [col] 0       -> Borrar celda (Empty)");
            view.printMessage("Acciones:");
            view.printMessage("  -1: Salir (Sin guardar)");
            view.printMessage("  -2: Validar y buscar soluciones");
            view.printMessage("  -3: Guardar como partida jugable");

            MoveInput input = view.askMove();
            if (input == null) {
                view.printMessage("Entrada inválida.");
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
            view.printMessage("Coordenadas fuera de rango.");
            return;
        }
        
        // Limpiar estado previo
        cell.setValue(0);
        cell.setVoid(false); // Reset to normal empty cell first
        
        if (val == -1) {
            cell.setVoid(true);
            view.printMessage("Celda (" + r + "," + c + ") marcada como VOID.");
        } else if (val == 0) {
            // Ya se limpió arriba
            view.printMessage("Celda (" + r + "," + c + ") limpiada.");
        } else if (val > 0) {
            // En el editor, todos los números que el creador pone son PISTAS FIJAS.
            // Usar setFixedValue para que surrender() y el juego no los borre/permita modificar.
            cell.setFixedValue(val);
            view.printMessage("Pista fija " + val + " colocada en (" + r + "," + c + ").");
        }
    }

    private void validateBoard() {
        view.printMessage("Analizando tablero...");
        // Validar conteo de celdas vs rango de números
        // El solver asume que el tablero está bien formado.
        int solutionCount = solver.countSolutions(board, 2);
        
        if (solutionCount == 0) {
            view.printMessage("❌ El tablero NO tiene solución.");
        } else if (solutionCount == 1) {
            view.printMessage("✅ El tablero es un Hidato Válido (Solución única).");
        } else {
            view.printMessage("⚠️ El tablero tiene MÚLTIPLES soluciones (" + solutionCount + "+).");
        }
    }

    private void saveAsGame() {
        String name = view.askString("Nombre del archivo (sin extensión):");
        if (name == null || name.trim().isEmpty()) return;

        // Crear una instancia de Game con este tablero
        Game newGame = new Game(board);
        // Guardar
        try {
            saver.saveGame(newGame, name + ".hidato");
            view.printMessage("Partida guardada correctamente en " + name + ".hidato");
        } catch (Exception e) {
            view.printMessage("Error al guardar: " + e.getMessage());
        }
    }
}
