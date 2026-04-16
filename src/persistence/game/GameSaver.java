package persistence.game;

import model.game.Game;
import java.io.*;

public class GameSaver {
    
    public void saveGame(Game game, String filePath) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(game);
        }
    }

    public Game loadGame(String filePath) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            return (Game) ois.readObject();
        }
    }
}
