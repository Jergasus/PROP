package model.ranking;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RankingManager {
    private static final String FILE_PATH = "ranking.dat";
    private List<Score> scores;

    public RankingManager() {
        this.scores = loadScores();
    }

    public void addScore(Score score) {
        scores.add(score);
        scores.sort(Comparator.comparingLong(Score::timeMillis));
        saveScores();
    }

    public List<Score> getTopScores(int checkMax) {
        return scores.subList(0, Math.min(scores.size(), checkMax));
    }

    private void saveScores() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
            oos.writeObject(scores);
        } catch (IOException e) {
            System.err.println("Error guardando ranking: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private List<Score> loadScores() {
        File file = new File(FILE_PATH);
        if (!file.exists()) return new ArrayList<>();

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (List<Score>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return new ArrayList<>();
        }
    }
}
