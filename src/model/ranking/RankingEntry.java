package model.ranking;

/**
 * A single entry in the point-based global ranking.
 * One entry per user, sorted by totalPoints descending.
 */
public class RankingEntry {
    private final String username;
    private int totalPoints;

    public RankingEntry(String username, int totalPoints) {
        this.username = username;
        this.totalPoints = totalPoints;
    }

    public String getUsername()  { return username; }
    public int getTotalPoints() { return totalPoints; }
    public void setTotalPoints(int totalPoints) { this.totalPoints = totalPoints; }

    @Override
    public String toString() {
        return String.format("%-20s %d pts", username, totalPoints);
    }
}
