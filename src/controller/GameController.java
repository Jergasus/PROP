package controller;

import java.io.IOException;
import java.time.LocalDateTime;
import model.algorithms.Solver;
import model.board.Board;
import model.cell.Cell;
import model.game.Game;
import model.level.Difficulty;
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
    private long accumulatedTime = 0;

    // Level-aware fields (null for random/custom games)
    private final String levelId;
    private final Difficulty difficulty;

    // Completion tracking
    private boolean completed = false;
    private long completionTimeMillis = 0;

    /** Constructor for random/custom games (no level tracking). */
    public GameController(Game game, ConsoleView view) {
        this(game, view, null, null);
    }

    /** Constructor for pregenerated level games (with level tracking). */
    public GameController(Game game, ConsoleView view, String levelId, Difficulty difficulty) {
        this.game = game;
        this.view = view;
        this.rankingManager = new RankingManager();
        this.gameSaver = new GameSaver();
        this.levelId = levelId;
        this.difficulty = difficulty;
        if (game.getElapsedTime() > 0) {
            this.accumulatedTime = game.getElapsedTime();
        }
    }

    public void play() {
        startTime = System.currentTimeMillis();
        view.printMessage("=== Iniciando Partida de Hidato ===");
        view.printMessage("Objetivo: Rellenar el tablero con valores del 1 al " + game.getMaxNumber());

        while (!game.isFinished()) {
            long elapsed = accumulatedTime + (System.currentTimeMillis() - startTime);
            view.printBoardWithTime(game.getBoard(), elapsed);

            ConsoleView.MoveInput input = view.askMove();
            if (input == null) {
                view.printMessage("Entrada invalida.");
                continue;
            }
            if (input.row() == -1) {
                view.printMessage("Juego pausado/terminado por usuario.");
                saveAndExit();
                return;
            }

            if (input.row() == -2) {
                if (game.undo()) view.printMessage("Deshacer realizado.");
                else view.printMessage("No hay movimientos para deshacer.");
                continue;
            }
            if (input.row() == -3) {
                if (game.redo()) view.printMessage("Rehacer realizado.");
                else view.printMessage("No hay movimientos para rehacer.");
                continue;
            }
            if (input.row() == -4) {
                surrender();
                return;
            }

            if (isValidMove(input.row(), input.col(), input.value())) {
                game.makeMove(input.row(), input.col(), input.value());
                view.printMessage("Movimiento realizado.");
            }
        }

        long totalDuration = accumulatedTime + (System.currentTimeMillis() - startTime);
        view.printBoard(game.getBoard());
        view.printMessage("Felicidades! Has completado el Hidato correctamente.");

        this.completed = true;
        this.completionTimeMillis = totalDuration;

        if (levelId == null) {
            // Random/custom game — use legacy score system
            saveScore();
        }
        // For level games, LevelSelectController handles scoring
    }

    public boolean wasCompleted() { return completed; }
    public long getCompletionTimeMillis() { return completionTimeMillis; }

    private void surrender() {
        String ans = view.askString("Estas seguro que quieres rendirte y ver la solucion? (s/n)");
        if (ans.equalsIgnoreCase("s")) {
            view.printMessage("Resetando estado a valores originales...");
            Board board = game.getBoard();

            for (int i = 0; i < board.getRows(); i++) {
                for (int j = 0; j < board.getCols(); j++) {
                    Cell c = board.getCell(i, j);
                    if (!c.isFixed() && !c.isVoid()) {
                        c.setAsEmpty();
                    }
                }
            }

            Solver solver = new Solver();
            if (solver.solve(board)) {
                view.printBoard(board);
                view.printMessage("Te has rendido. Aqui tienes la solucion.");
            } else {
                view.printMessage("Error inesperado: El tablero no tiene solucion desde el estado inicial.");
            }
        }
    }

    private void saveAndExit() {
        String filename = view.askString("Nombre del archivo para guardar (sin extension, Enter para 'saved_game'):");
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

        view.printMessage("Tiempo total: " + view.formatTime(totalDuration));
        String name = view.askString("Introduce tu nombre para el ranking:");

        Score score = new Score(name, totalDuration, "Normal", LocalDateTime.now());
        rankingManager.addScore(score);

        view.printRanking(rankingManager.getTopScores(10));
    }

    private boolean isValidMove(int row, int col, int value) {
        Board board = game.getBoard();
        Cell cell = board.getCell(row, col);

        if (cell == null) {
            view.printMessage("Error: Posicion fuera de rango.");
            return false;
        }

        if (cell.isVoid()) {
             view.printMessage("Error: Celda no jugable (Void).");
             return false;
        }

        if (cell.isFixed()) {
            view.printMessage("Error: No puedes modificar una celda fija.");
            return false;
        }

        if (value < 1 || value > game.getMaxNumber()) {
            view.printMessage("Error: Valor fuera de rango (1-" + game.getMaxNumber() + ").");
            return false;
        }

        if (isDuplicate(board, value) && cell.getValue() != value) {
             view.printMessage("Advertencia: El numero " + value + " ya esta en el tablero. Se permite corregir.");
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
