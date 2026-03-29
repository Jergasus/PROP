package controller;

import model.board.Board;

public class HidatoCaseController {

    private final String name;
    private final String adjacencyDesc;
    private final boolean expectedSolvable;
    private final String description;
    private final Board board;

    public HidatoCaseController(String name, String adjacencyDesc, boolean expectedSolvable,
                                String description, Board board) {
        this.name             = name;
        this.adjacencyDesc    = adjacencyDesc;
        this.expectedSolvable = expectedSolvable;
        this.description      = description;
        this.board            = board;
    }

    public String  getName()            { return name; }
    public String  getAdjacencyDesc()   { return adjacencyDesc; }
    public boolean isExpectedSolvable() { return expectedSolvable; }
    public String  getDescription()     { return description; }

    public Board getBoard() { return new Board(board); }
}
