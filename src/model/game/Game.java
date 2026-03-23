package model.game;

import model.algorithms.Validator;
import model.board.Board;
import model.cell.Cell;

import java.util.Stack;
import java.io.Serializable;

public class Game implements Serializable {
    private final Board board;
    private final int maxNumber;
    private final Stack<Move> undoStack;
    private final Stack<Move> redoStack;
    private long elapsedTimeMillis;
    private String levelId; // null for random/custom games

    public Game(Board board) {
        this.board = board;
        this.maxNumber = calculateMaxNumber();
        this.undoStack = new Stack<>();
        this.redoStack = new Stack<>();
        this.elapsedTimeMillis = 0;
    }
    
    public void setElapsedTime(long millis) {
        this.elapsedTimeMillis = millis;
    }
    
    public long getElapsedTime() {
        return elapsedTimeMillis;
    }

    public String getLevelId() { return levelId; }
    public void setLevelId(String levelId) { this.levelId = levelId; }

    public void makeMove(int row, int col, int value) {
        Cell cell = board.getCell(row, col);
        int prevValue = cell.getValue();
        
        cell.setValue(value);
        
        undoStack.push(new Move(cell.getPosition(), prevValue, value));
        redoStack.clear(); // Nuevo movimiento limpia el futuro
    }

    public boolean undo() {
        if (undoStack.isEmpty()) return false;

        Move move = undoStack.pop();
        Cell cell = board.getCell(move.position());
        cell.setValue(move.previousValue());
        
        redoStack.push(move);
        return true;
    }


    public boolean redo() {
        if (redoStack.isEmpty()) return false;

        Move move = redoStack.pop();
        Cell cell = board.getCell(move.position());
        cell.setValue(move.newValue());
        
        undoStack.push(move);
        return true;
    }

    public Board getBoard() {
        return board;
    }

    public int getMaxNumber() {
        return maxNumber;
    }

    private int calculateMaxNumber() {
        return board.getCellCount();
    }

    public boolean isFinished() {
        // Quick check: every non-void cell must be filled
        for (int i = 0; i < board.getRows(); i++) {
            for (int j = 0; j < board.getCols(); j++) {
                Cell c = board.getCell(i, j);
                if (!c.isVoid() && c.getValue() == 0) return false;
            }
        }
        // Full check: the filled numbers form a valid consecutive path
        return new Validator().isValidSolution(board);
    }
}
