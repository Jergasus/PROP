package domini.model.Hidato;

import domini.model.board.Board;

/**
 * Representa un cas d'Hidato amb el seu tauler i metadades associades.
 * Aquesta classe forma part del model de domini.
 */
public class Hidato {

    private final String name;
    private final String adjacencyDesc;
    private final boolean expectedSolvable;
    private final String description;
    private final Board board;

    /**
     * Constructor per defecte per crear un nou objecte Hidato.
     */
    public Hidato(String name, String adjacencyDesc, boolean expectedSolvable,
                  String description, Board board) {
        this.name             = name;
        this.adjacencyDesc    = adjacencyDesc;
        this.expectedSolvable = expectedSolvable;
        this.description      = description;
        this.board            = board;
    }

    // Getters utilitzats pel CtrlDomini per obtenir la informació del catàleg

    public String getName() { 
        return name; 
    }

    public String getAdjacencyDesc() { 
        return adjacencyDesc; 
    }

    public boolean isExpectedSolvable() { 
        return expectedSolvable; 
    }

    public String getDescription() { 
        return description; 
    }

    /**
     * Retorna una còpia del tauler per evitar que modificacions externes 
     * afectin l'estat original guardat al catàleg.
     */
    public Board getBoard() { 
        return new Board(board); 
    }
}