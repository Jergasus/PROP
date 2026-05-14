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

    private final String levelId;
    private final Difficulty difficulty;

    private boolean completed = false;
    private long completionTimeMillis = 0;

    public GameController(Game game, DomainController domain) {
        this(game, domain, null, null);
    }

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

            switch (input.row()) {
                case -1 -> { saveAndExit(); return; }
                case -2 -> { if (game.undo()) domain.printMessage("Deshacer realizado."); else domain.printMessage("No hay movimientos para deshacer."); }
                case -3 -> { if (game.redo()) domain.printMessage("Rehacer realizado."); else domain.printMessage("No hay movimientos para rehacer."); }
                case -4 -> { surrender(); return; }
                case -5 -> giveHint();
                default -> {
                    if (isValidMove(input.row(), input.col(), input.value())) {
                        game.makeMove(input.row(), input.col(), input.value());
                        domain.printMessage("Movimiento realizado.");
                    }
                }
            }
        }

        long totalDuration = accumulatedTime + (System.currentTimeMillis() - startTime);
        domain.printBoard(game.getBoard());
        domain.printMessage("Felicidades! Has completado el Hidato correctamente.");
        domain.printMessage("Tu tiempo: " + domain.formatTime(totalDuration));

        this.completed = true;
        this.completionTimeMillis = totalDuration;

        if (levelId == null) {
            saveScore();
        }
    }

    public boolean wasCompleted() { return completed; }
    public long getCompletionTimeMillis() { return completionTimeMillis; }

    // -----------------------------------------------------------------------
    // Hint: reveal the position of the lowest unfilled number from the solution
    // -----------------------------------------------------------------------

    private void giveHint() {
        Board cleanCopy = new Board(game.getBoard());
        for (int i = 0; i < cleanCopy.getRows(); i++) {
            for (int j = 0; j < cleanCopy.getCols(); j++) {
                Cell c = cleanCopy.getCell(i, j);
                if (!c.isFixed() && !c.isVoid()) c.setAsEmpty();
            }
        }

        Solver solver = new Solver();
        long start = System.currentTimeMillis();
        boolean solved = solver.solve(cleanCopy);
        long machineTime = System.currentTimeMillis() - start;

        domain.printMessage("(Tiempo máquina — pista: " + domain.formatTime(machineTime) + ")");

        if (!solved) {
            domain.printMessage("No se puede calcular pista: el tablero no tiene solución desde las pistas iniciales.");
            return;
        }

        Board current = game.getBoard();
        for (int v = 1; v <= game.getMaxNumber(); v++) {
            for (int i = 0; i < current.getRows(); i++) {
                for (int j = 0; j < current.getCols(); j++) {
                    Cell sol = cleanCopy.getCell(i, j);
                    Cell cur = current.getCell(i, j);
                    if (sol.getValue() == v && !cur.isFixed() && cur.getValue() != v) {
                        domain.printMessage("Pista: el número " + v + " pertenece a fila " + i + ", columna " + j + ".");
                        return;
                    }
                }
            }
        }
        domain.printMessage("No hay más pistas disponibles.");
    }

    // -----------------------------------------------------------------------
    // Surrender: reset user moves and show solution with machine time
    // -----------------------------------------------------------------------

    private void surrender() {
        String ans = domain.askString("Estas seguro que quieres rendirte y ver la solucion? (s/n)");
        if (!ans.equalsIgnoreCase("s")) return;

        domain.printMessage("Resetando estado a valores originales...");
        Board board = game.getBoard();

        for (int i = 0; i < board.getRows(); i++) {
            for (int j = 0; j < board.getCols(); j++) {
                Cell c = board.getCell(i, j);
                if (!c.isFixed() && !c.isVoid()) c.setAsEmpty();
            }
        }

        Solver solver = new Solver();
        long start = System.currentTimeMillis();
        if (solver.solve(board)) {
            long machineTime = System.currentTimeMillis() - start;
            domain.printBoard(board);
            domain.printMessage("Te has rendido. Aqui tienes la solucion.");
            domain.printMessage("Tiempo de resolución (máquina): " + domain.formatTime(machineTime));
        } else {
            domain.printMessage("Error inesperado: el tablero no tiene solución desde el estado inicial.");
        }
    }

    // -----------------------------------------------------------------------
    // Save / exit
    // -----------------------------------------------------------------------

    private void saveAndExit() {
        String filename = domain.askString("Nombre del archivo para guardar (sin extension, Enter para 'saved_game'):");
        if (filename.trim().isEmpty()) filename = "saved_game";

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

    // -----------------------------------------------------------------------
    // Move validation
    // -----------------------------------------------------------------------

    private boolean isValidMove(int row, int col, int value) {
        Board board = game.getBoard();
        Cell cell = board.getCell(row, col);

        if (cell == null) { domain.printMessage("Error: Posicion fuera de rango."); return false; }
        if (cell.isVoid()) { domain.printMessage("Error: Celda no jugable (Void)."); return false; }
        if (cell.isFixed()) { domain.printMessage("Error: No puedes modificar una celda fija."); return false; }
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
        for (int i = 0; i < board.getRows(); i++)
            for (int j = 0; j < board.getCols(); j++)
                if (board.getCell(i, j).getValue() == value) return true;
        return false;
    }
}
