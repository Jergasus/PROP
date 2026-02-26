package model.game;

import model.board.Board;
import model.cell.Cell;

import java.util.Stack;
import java.util.List;
import java.io.Serializable;

public class Game implements Serializable {
    private final Board board;
    private final int maxNumber;
    private final Stack<Move> undoStack;
    private final Stack<Move> redoStack;
    private long elapsedTimeMillis;

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

    private boolean validatePath() {
        // Buscar el 1
        Cell current = null;
        for (int i = 0; i < board.getRows(); i++) {
            for (int j = 0; j < board.getCols(); j++) {
                if (board.getCell(i, j).getValue() == 1) {
                    current = board.getCell(i, j);
                    break;
                }
            }
        }
        if (current == null) return false;

        // Seguir el camino hasta maxNumber
        for (int nextVal = 2; nextVal <= maxNumber; nextVal++) {
            List<Cell> neighbors = board.getNeighbors(current.getPosition());
            boolean foundNext = false;
            for (Cell neighbor : neighbors) {
                if (neighbor.getValue() == nextVal) {
                    current = neighbor;
                    foundNext = true;
                    break;
                }
            }
            if (!foundNext) return false; // El camino se rompe
        }
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
        int count = 0;
        for (int i = 0; i < board.getRows(); i++) {
            for (int j = 0; j < board.getCols(); j++) {
                if (!board.getCell(i, j).isVoid()) {
                    count++;
                }
            }
        }
        return count;
    }

    public boolean isFinished() {
        for (int i = 0; i < board.getRows(); i++) {
            for (int j = 0; j < board.getCols(); j++) {
                Cell c = board.getCell(i, j);
                if (!c.isVoid() && c.getValue() == 0) {
                    return false;
                }
            }
        }
        return true;
    }
}
