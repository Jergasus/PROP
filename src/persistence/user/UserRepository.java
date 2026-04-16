package persistence.user;

import model.user.LevelProgress;
import model.user.User;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * Reads and writes User profiles as structured text files.
 *
 * File format (data/users/<username>.txt):
 *   USERNAME:<name>
 *   PASSWORD_HASH:<sha256hex>
 *   TOTAL_POINTS:<int>
 *   LEVEL:<levelId>,<bestStars>,<bestTimeMillis>,<pointsAwarded>
 *   LEVEL:...
 */
public class UserRepository {

    private static final String USERS_DIR = "data/users";

    public UserRepository() {
        ensureDirectoryExists();
    }

    public boolean userExists(String username) {
        return Files.exists(getUserPath(username));
    }

    public void saveUser(User user) throws IOException {
        ensureDirectoryExists();
        try (PrintWriter w = new PrintWriter(new FileWriter(getUserPath(user.getUsername()).toFile()))) {
            w.println("USERNAME:" + user.getUsername());
            w.println("PASSWORD_HASH:" + user.getPasswordHash());
            w.println("TOTAL_POINTS:" + user.getTotalPoints());
            for (Map.Entry<String, LevelProgress> entry : user.getAllLevelProgress().entrySet()) {
                LevelProgress lp = entry.getValue();
                w.printf("LEVEL:%s,%d,%d,%d%n",
                        entry.getKey(),
                        lp.getBestStars(),
                        lp.getBestTimeMillis(),
                        lp.getPointsAwarded());
            }
        }
    }

    /**
     * Loads a user profile from disk.
     * Returns null if the file does not exist.
     */
    public User loadUser(String username) throws IOException {
        Path path = getUserPath(username);
        if (!Files.exists(path)) return null;

        List<String> lines = Files.readAllLines(path);
        String name = null;
        String hash = null;
        int totalPoints = 0;
        User user = null;

        for (String raw : lines) {
            String line = raw.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;

            if (line.startsWith("USERNAME:")) {
                name = line.substring("USERNAME:".length()).trim();
            } else if (line.startsWith("PASSWORD_HASH:")) {
                hash = line.substring("PASSWORD_HASH:".length()).trim();
            } else if (line.startsWith("TOTAL_POINTS:")) {
                totalPoints = Integer.parseInt(line.substring("TOTAL_POINTS:".length()).trim());
            } else if (line.startsWith("LEVEL:")) {
                if (user == null && name != null && hash != null) {
                    user = new User(name, hash);
                    user.setTotalPoints(totalPoints);
                }
                parseLevelLine(user, line.substring("LEVEL:".length()).trim());
            }
        }

        if (user == null && name != null && hash != null) {
            user = new User(name, hash);
            user.setTotalPoints(totalPoints);
        }
        return user;
    }

    private void parseLevelLine(User user, String data) {
        // Format: levelId,bestStars,bestTimeMillis,pointsAwarded
        String[] parts = data.split(",");
        if (parts.length < 4 || user == null) return;
        String levelId = parts[0].trim();
        int bestStars = Integer.parseInt(parts[1].trim());
        long bestTime = Long.parseLong(parts[2].trim());
        int pointsAwarded = Integer.parseInt(parts[3].trim());
        user.updateLevelProgress(levelId, new LevelProgress(bestStars, bestTime, pointsAwarded));
    }

    private Path getUserPath(String username) {
        return Paths.get(USERS_DIR, username + ".txt");
    }

    private void ensureDirectoryExists() {
        try {
            Files.createDirectories(Paths.get(USERS_DIR));
        } catch (IOException e) {
            // directory may already exist
        }
    }
}
