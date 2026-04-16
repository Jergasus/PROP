package persistence;

import model.board.Board;
import java.io.IOException;

public interface BoardSerializer {
    /**
     * Guarda el tablero en un archivo.
     */
    void saveBoard(Board board, String filePath) throws IOException;

    /**
     * Carga un tablero desde un archivo.
     */
    Board loadBoard(String filePath) throws IOException;
}
