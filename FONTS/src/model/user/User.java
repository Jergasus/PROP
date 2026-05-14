package model.user;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a registered player with their profile data.
 * Stores authentication credentials and per-level progression.
 */
public class User {
    private final String username;
    private final String passwordHash; // SHA-256 hex string
    private int totalPoints;
    private final Map<String, LevelProgress> levelProgress; // key = levelId

    public User(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.totalPoints = 0;
        this.levelProgress = new HashMap<>();
    }

    public String getUsername()     { return username; }
    public String getPasswordHash() { return passwordHash; }
    public int getTotalPoints()    { return totalPoints; }
    public void setTotalPoints(int totalPoints) { this.totalPoints = totalPoints; }

    public void addPoints(int delta) {
        if (delta > 0) this.totalPoints += delta;
    }

    /**
     * Returns the progress for a level, or a fresh (never-played) entry
     * if the player hasn't attempted it yet.
     */
    public LevelProgress getLevelProgress(String levelId) {
        return levelProgress.getOrDefault(levelId, new LevelProgress());
    }

    public void updateLevelProgress(String levelId, LevelProgress progress) {
        levelProgress.put(levelId, progress);
    }

    public Map<String, LevelProgress> getAllLevelProgress() {
        return levelProgress;
    }
}
