package controller;

import java.io.IOException;
import java.time.LocalDateTime;
import model.algorithms.Solver;
import model.board.Board;
import model.cell.Cell;
import model.game.Game;
import model.ranking.RankingManager;
import model.ranking.Score;
import persistence.game.GameSaver;
import view.ConsoleView;

public class GameController {
    private Game game;
    private final ConsoleView view;
    private final RankingManager rankingManager;
    private final GameSaver gameSaver;
    private long startTime;
    private long accumulatedTime = 0; // Tiempo de sesiones anteriores si se carga la partida

    public GameController(Game game, ConsoleView view) {
        this.game = game;
        this.view = view;
        this.rankingManager = new RankingManager();
        this.gameSaver = new GameSaver();
        if (game.getElapsedTime() > 0) {
            this.accumulatedTime = game.getElapsedTime();
        }
    }

    public void play() {
        startTime = System.currentTimeMillis();
        view.printMessage("=== Iniciando Partida de Hidato ===");
        view.printMessage("Objetivo: Rellenar el tablero con valores del 1 al " + game.getMaxNumber());

        while (!game.isFinished()) {
            view.printBoard(game.getBoard());
            
            ConsoleView.MoveInput input = view.askMove();
            if (input == null) {
                view.printMessage("Entrada inválida.");
                continue;
            }
            if (input.row() == -1) {
                view.printMessage("👋 Juego pausado/terminado por usuario.");
                saveAndExit();
                return;
            }

            // Comandos especiales: -2 Undo, -3 Redo, -4 Rendirse
            if (input.row() == -2) {
                if (game.undo()) view.printMessage("↩️ Deshacer realizado.");
                else view.printMessage("⚠️ No hay movimientos para deshacer.");
                continue;
            }
            if (input.row() == -3) {
                if (game.redo()) view.printMessage("↪️ Rehacer realizado.");
                else view.printMessage("⚠️ No hay movimientos para rehacer.");
                continue;
            }
            if (input.row() == -4) {
                surrender();
                return;
            }

            if (isValidMove(input.row(), input.col(), input.value())) {
                game.makeMove(input.row(), input.col(), input.value());
                view.printMessage("✅ Movimiento realizado.");
            }
        }

        view.printBoard(game.getBoard());
        view.printMessage("🎉 ¡Felicidades! Has completado el Hidato correctamente.");
        
        saveScore();
    }
    
    private void surrender() {
        String ans = view.askString("¿Estás seguro que quieres rendirte y ver la solución? (s/n)");
        if (ans.equalsIgnoreCase("s")) {
            view.printMessage("Resetando estado a valores originales...");
            Board board = game.getBoard();
            
            // 1. Limpiar celdas NO fijas
            for (int i = 0; i < board.getRows(); i++) {
                for (int j = 0; j < board.getCols(); j++) {
                    Cell c = board.getCell(i, j);
                    if (!c.isFixed() && !c.isVoid()) {
                        c.setAsEmpty();
                    }
                }
            }

            // 2. Resolver
            Solver solver = new Solver();
            if (solver.solve(board)) {
                view.printBoard(board);
                view.printMessage("🏳️ Te has rendido. Aquí tienes la solución.");
            } else {
                view.printMessage("❌ Error inesperado: El tablero no tiene solución desde el estado inicial.");
            }
        }
    }

    private void saveAndExit() {
        String filename = view.askString("Nombre del archivo para guardar (sin extensión, Enter para 'saved_game'):");
        if (filename.trim().isEmpty()) {
            filename = "saved_game";
        }
        
        try {
            long currentSessionTime = System.currentTimeMillis() - startTime;
            game.setElapsedTime(accumulatedTime + currentSessionTime);
            gameSaver.saveGame(game, filename + ".hidato");
            view.printMessage("Partida guardada en " + filename + ".hidato");
        } catch (IOException e) {
            view.printMessage("Error al guardar: " + e.getMessage());
        }
    }

    private void saveScore() {
        long endTime = System.currentTimeMillis();
        long totalDuration = accumulatedTime + (endTime - startTime);
        
        view.printMessage("Tiempo total: " + totalDuration + "ms");
        String name = view.askString("Introduce tu nombre para el ranking:");
        
        Score score = new Score(name, totalDuration, "Normal", LocalDateTime.now());
        rankingManager.addScore(score);
        
        view.printRanking(rankingManager.getTopScores(10));
    }

    private boolean isValidMove(int row, int col, int value) {
        Board board = game.getBoard();
        Cell cell = board.getCell(row, col);

        if (cell == null) {
            view.printMessage("❌ Error: Posición fuera de rango.");
            return false; 
        }

        if (cell.isVoid()) {
             view.printMessage("❌ Error: Celda no jugable (Void).");
             return false;
        }

        if (cell.isFixed()) {
            view.printMessage("❌ Error: No puedes modificar una celda fija.");
            return false;
        }

        if (value < 1 || value > game.getMaxNumber()) {
            view.printMessage("❌ Error: Valor fuera de rango (1-" + game.getMaxNumber() + ").");
            return false;
        }
        
        // Verificar si ya existe (opcional, simplifica testing permitir errores temporales)
        if (isDuplicate(board, value) && cell.getValue() != value) {
             view.printMessage("⚠️ Advertencia: El número " + value + " ya está en el tablero. Se permite corregir.");
        }

        return true;
    }
    
    private boolean isDuplicate(Board board, int value) {
        for(int i=0; i<board.getRows(); i++){
            for(int j=0; j<board.getCols(); j++){
                if(board.getCell(i,j).getValue() == value) return true;
            }
        }
        return false;
    }
}
