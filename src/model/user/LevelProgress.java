package model.user;

/**
 * Tracks a player's best result on a specific pregenerated level.
 * Stores the best star rating, best time, and cumulative points awarded
 * (to enforce the "first-time bonus only" constraint).
 */
public class LevelProgress {
    private int bestStars;       // 0-3
    private long bestTimeMillis; // 0 if never completed
    private int pointsAwarded;   // 0-4, cumulative across all attempts

    /** Default: never played. */
    public LevelProgress() {
        this(0, 0, 0);
    }

    public LevelProgress(int bestStars, long bestTimeMillis, int pointsAwarded) {
        this.bestStars = bestStars;
        this.bestTimeMillis = bestTimeMillis;
        this.pointsAwarded = pointsAwarded;
    }

    public int getBestStars()       { return bestStars; }
    public long getBestTimeMillis() { return bestTimeMillis; }
    public int getPointsAwarded()   { return pointsAwarded; }

    public boolean isCompleted() { return bestTimeMillis > 0; }

    public void setBestStars(int bestStars)           { this.bestStars = bestStars; }
    public void setBestTimeMillis(long bestTimeMillis) { this.bestTimeMillis = bestTimeMillis; }
    public void setPointsAwarded(int pointsAwarded)   { this.pointsAwarded = pointsAwarded; }
}
