package model.scoring;

import model.level.Difficulty;
import model.user.LevelProgress;

/**
 * Static scoring logic for the star/point progression system.
 *
 * Rules:
 *  - 1 base point for completing a level + 1 extra point per star earned.
 *  - Star bonus points are only awarded the FIRST time they are achieved.
 *  - Absolute maximum per level: 4 points (1 base + 3 stars).
 */
public class ScoringEngine {

    private ScoringEngine() {} // static-only class

    /**
     * Delegates star calculation to the Difficulty enum.
     */
    public static int calculateStars(Difficulty difficulty, long completionTimeMillis) {
        return difficulty.calculateStars(completionTimeMillis);
    }

    /**
     * Calculates how many NEW points the player earns on this attempt.
     * Returns a delta (0 to 4) that should be added to the player's total.
     *
     * Formula:
     *   totalThisRun = 1 (base) + starsEarned
     *   delta = max(0, totalThisRun - previousProgress.getPointsAwarded())
     */
    public static int calculateNewPoints(int starsEarned, LevelProgress previousProgress) {
        int totalThisRun = 1 + starsEarned;
        int previouslyAwarded = previousProgress.getPointsAwarded();
        return Math.max(0, totalThisRun - previouslyAwarded);
    }

    /**
     * Returns an updated LevelProgress with the best values from both
     * the previous record and the current attempt.
     */
    public static LevelProgress updateProgress(LevelProgress prev, int starsEarned, long timeMillis) {
        int newPointsTotal = Math.max(prev.getPointsAwarded(), 1 + starsEarned);
        int newBestStars = Math.max(prev.getBestStars(), starsEarned);
        long newBestTime = prev.getBestTimeMillis() == 0
                ? timeMillis
                : Math.min(prev.getBestTimeMillis(), timeMillis);

        return new LevelProgress(newBestStars, newBestTime, newPointsTotal);
    }
}
