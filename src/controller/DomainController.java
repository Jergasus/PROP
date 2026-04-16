package controller;

import java.util.List;
import java.util.Map;
import model.board.Board;
import model.game.MoveInput;
import model.level.Level;
import model.ranking.RankingEntry;
import model.ranking.Score;
import model.user.LevelProgress;
import model.user.User;
import view.ConsoleView;

/**
 * Single mediator between domain controllers and the view layer.
 * All controllers communicate with the user through this class.
 */
public class DomainController {
    private final ConsoleView view;

    public DomainController(ConsoleView view) {
        this.view = view;
    }

    // -----------------------------------------------------------------------
    // Basic I/O
    // -----------------------------------------------------------------------

    public void printMessage(String message) {
        view.printMessage(message);
    }

    public String askString(String prompt) {
        return view.askString(prompt);
    }

    public boolean askYesNo(String prompt) {
        return view.askYesNo(prompt);
    }

    public MoveInput askMove() {
        return view.askMove();
    }

    public void consumeNewLine() {
        view.consumeNewLine();
    }

    // -----------------------------------------------------------------------
    // Board display
    // -----------------------------------------------------------------------

    public void printBoard(Board board) {
        view.printBoard(board);
    }

    public void printBoardWithTime(Board board, long elapsedMillis) {
        view.printBoardWithTime(board, elapsedMillis);
    }

    // -----------------------------------------------------------------------
    // Ranking
    // -----------------------------------------------------------------------

    public void printPointRanking(List<RankingEntry> entries) {
        view.printPointRanking(entries);
    }

    public void printRanking(List<Score> scores) {
        view.printRanking(scores);
    }

    // -----------------------------------------------------------------------
    // Stars, scoring, levels
    // -----------------------------------------------------------------------

    public void printStars(int stars) {
        view.printStars(stars);
    }

    public void printLevelResult(int stars, int pointsGained, long timeMillis) {
        view.printLevelResult(stars, pointsGained, timeMillis);
    }

    public void printLevelList(List<Level> levels, Map<String, LevelProgress> progress) {
        view.printLevelList(levels, progress);
    }

    // -----------------------------------------------------------------------
    // User profile
    // -----------------------------------------------------------------------

    public void printUserProfile(User user) {
        view.printUserProfile(user);
    }

    // -----------------------------------------------------------------------
    // Utilities
    // -----------------------------------------------------------------------

    public String formatTime(long millis) {
        return view.formatTime(millis);
    }
}
