package model.level;

/**
 * Difficulty tiers for pregenerated Hidato levels.
 * Each tier defines time thresholds for the 3-star rating system.
 */
public enum Difficulty {
    EASY   (60_000, 120_000, 180_000, "Facil"),
    MEDIUM (120_000, 240_000, 360_000, "Medio"),
    HARD   (180_000, 360_000, 540_000, "Dificil");

    private final long threeStarMillis;
    private final long twoStarMillis;
    private final long oneStarMillis;
    private final String displayName;

    Difficulty(long threeStar, long twoStar, long oneStar, String displayName) {
        this.threeStarMillis = threeStar;
        this.twoStarMillis = twoStar;
        this.oneStarMillis = oneStar;
        this.displayName = displayName;
    }

    /**
     * Returns 3, 2, 1, or 0 stars based on completion time.
     */
    public int calculateStars(long completionTimeMillis) {
        if (completionTimeMillis <= threeStarMillis) return 3;
        if (completionTimeMillis <= twoStarMillis)   return 2;
        if (completionTimeMillis <= oneStarMillis)    return 1;
        return 0;
    }

    public long getThreeStarMillis() { return threeStarMillis; }
    public long getTwoStarMillis()   { return twoStarMillis; }
    public long getOneStarMillis()   { return oneStarMillis; }
    public String getDisplayName()   { return displayName; }
}
