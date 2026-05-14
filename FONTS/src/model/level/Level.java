package model.level;

import model.board.Board;

/**
 * A pregenerated Hidato puzzle with metadata.
 * The board is in its unsolved (puzzle) state — some cells empty, clues fixed.
 */
public class Level {
    private final String levelId;       // e.g. "easy_01"
    private final Difficulty difficulty;
    private final Board board;          // puzzle board (unsolved)
    private final String displayName;   // e.g. "Easy 1"

    public Level(String levelId, Difficulty difficulty, Board board, String displayName) {
        this.levelId = levelId;
        this.difficulty = difficulty;
        this.board = board;
        this.displayName = displayName;
    }

    public String getLevelId()       { return levelId; }
    public Difficulty getDifficulty() { return difficulty; }
    public Board getBoard()          { return board; }
    public String getDisplayName()   { return displayName; }
}
