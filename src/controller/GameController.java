package controller;

import java.io.IOException;
import java.time.LocalDateTime;
import model.algorithms.Solver;
import model.board.Board;
import model.cell.Cell;
import model.game.Game;
import model.game.MoveInput;
import model.level.Difficulty;
import model.ranking.RankingManager;
import model.ranking.Score;
import persistence.game.GameSaver;

public class GameController {
    private Game game;
    private final DomainController domain;
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
    public GameController(Game game, DomainController domain) {
        this(game, domain, null, null);
    }

    /** Constructor for pregenerated level games (with level tracking). */
    public GameController(Game game, DomainController domain, String levelId, Difficulty difficulty) {
        this.game = game;
        this.domain = domain;
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
        domain.printMessage("=== Iniciando Partida de Hidato ===");
        domain.printMessage("Objetivo: Rellenar el tablero con valores del 1 al " + game.getMaxNumber());

        while (!game.isFinished()) {
            long elapsed = accumulatedTime + (System.currentTimeMillis() - startTime);
            domain.printBoardWithTime(game.getBoard(), elapsed);

            MoveInput input = domain.askMove();
            if (input == null) {
                domain.printMessage("Entrada invalida.");
                continue;
            }
            if (input.row() == -1) {
                domain.printMessage("Juego pausado/terminado por usuario.");
                saveAndExit();
                return;
            }

            if (input.row() == -2) {
                if (game.undo()) domain.printMessage("Deshacer realizado.");
                else domain.printMessage("No hay movimientos para deshacer.");
                continue;
            }
            if (input.row() == -3) {
                if (game.redo()) domain.printMessage("Rehacer realizado.");
                else domain.printMessage("No hay movimientos para rehacer.");
                continue;
            }
            if (input.row() == -4) {
                surrender();
                return;
            }

            if (isValidMove(input.row(), input.col(), input.value())) {
                game.makeMove(input.row(), input.col(), input.value());
                domain.printMessage("Movimiento realizado.");
            }
        }

        long totalDuration = accumulatedTime + (System.currentTimeMillis() - startTime);
        domain.printBoard(game.getBoard());
        domain.printMessage("Felicidades! Has completado el Hidato correctamente.");

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
        String ans = domain.askString("Estas seguro que quieres rendirte y ver la solucion? (s/n)");
        if (ans.equalsIgnoreCase("s")) {
            domain.printMessage("Resetando estado a valores originales...");
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
                domain.printBoard(board);
                domain.printMessage("Te has rendido. Aqui tienes la solucion.");
            } else {
                domain.printMessage("Error inesperado: El tablero no tiene solucion desde el estado inicial.");
            }
        }
    }

    private void saveAndExit() {
        String filename = domain.askString("Nombre del archivo para guardar (sin extension, Enter para 'saved_game'):");
        if (filename.trim().isEmpty()) {
            filename = "saved_game";
        }

        try {
            long currentSessionTime = System.currentTimeMillis() - startTime;
            game.setElapsedTime(accumulatedTime + currentSessionTime);
            gameSaver.saveGame(game, filename + ".hidato");
            domain.printMessage("Partida guardada en " + filename + ".hidato");
        } catch (IOException e) {
            domain.printMessage("Error al guardar: " + e.getMessage());
        }
    }

    private void saveScore() {
        long endTime = System.currentTimeMillis();
        long totalDuration = accumulatedTime + (endTime - startTime);

        domain.printMessage("Tiempo total: " + domain.formatTime(totalDuration));
        String name = domain.askString("Introduce tu nombre para el ranking:");

        Score score = new Score(name, totalDuration, "Normal", LocalDateTime.now());
        rankingManager.addScore(score);

        domain.printRanking(rankingManager.getTopScores(10));
    }

    private boolean isValidMove(int row, int col, int value) {
        Board board = game.getBoard();
        Cell cell = board.getCell(row, col);

        if (cell == null) {
            domain.printMessage("Error: Posicion fuera de rango.");
            return false;
        }

        if (cell.isVoid()) {
             domain.printMessage("Error: Celda no jugable (Void).");
             return false;
        }

        if (cell.isFixed()) {
            domain.printMessage("Error: No puedes modificar una celda fija.");
            return false;
        }

        if (value < 1 || value > game.getMaxNumber()) {
            domain.printMessage("Error: Valor fuera de rango (1-" + game.getMaxNumber() + ").");
            return false;
        }

        if (isDuplicate(board, value) && cell.getValue() != value) {
             domain.printMessage("Advertencia: El numero " + value + " ya esta en el tablero. Se permite corregir.");
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
