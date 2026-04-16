package persistence.ranking;

import model.ranking.RankingEntry;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Text-file persistence for the global point-based ranking.
 *
 * File format (data/ranking.txt):
 *   username,totalPoints
 *   ...
 * Sorted by totalPoints descending.
 */
public class RankingRepository {

    private static final String RANKING_FILE = "data/ranking.txt";

    public RankingRepository() {
        ensureDirectoryExists();
    }

    public List<RankingEntry> loadRanking() {
        List<RankingEntry> entries = new ArrayList<>();
        Path path = Paths.get(RANKING_FILE);
        if (!Files.exists(path)) return entries;

        try {
            for (String raw : Files.readAllLines(path)) {
                String line = raw.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split(",");
                if (parts.length < 2) continue;
                String username = parts[0].trim();
                int points = Integer.parseInt(parts[1].trim());
                entries.add(new RankingEntry(username, points));
            }
        } catch (IOException e) {
            // return what we have so far
        }

        entries.sort((a, b) -> Integer.compare(b.getTotalPoints(), a.getTotalPoints()));
        return entries;
    }

    public void saveRanking(List<RankingEntry> entries) throws IOException {
        ensureDirectoryExists();
        entries.sort((a, b) -> Integer.compare(b.getTotalPoints(), a.getTotalPoints()));
        try (PrintWriter w = new PrintWriter(new FileWriter(RANKING_FILE))) {
            for (RankingEntry e : entries) {
                w.printf("%s,%d%n", e.getUsername(), e.getTotalPoints());
            }
        }
    }

    /**
     * Updates (or creates) a ranking entry for the given user, then saves.
     */
    public void updateEntry(String username, int totalPoints) throws IOException {
        List<RankingEntry> entries = loadRanking();
        boolean found = false;
        for (RankingEntry e : entries) {
            if (e.getUsername().equals(username)) {
                e.setTotalPoints(totalPoints);
                found = true;
                break;
            }
        }
        if (!found) {
            entries.add(new RankingEntry(username, totalPoints));
        }
        saveRanking(entries);
    }

    private void ensureDirectoryExists() {
        try {
            Files.createDirectories(Paths.get("data"));
        } catch (IOException e) {
            // directory may already exist
        }
    }
}
