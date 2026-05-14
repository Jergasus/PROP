package model.ranking;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Score implements Serializable {
    private String playerName;
    private long timeMillis;
    private String difficulty;
    private LocalDateTime date;
    
    public Score(String playerName, long timeMillis, String difficulty, LocalDateTime date) {
        this.playerName = playerName;
        this.timeMillis = timeMillis;
        this.difficulty = difficulty;
        this.date = date;
    }

    public long timeMillis() { return timeMillis; }

    @Override
    public String toString() {
        return String.format("%-15s | %d ms | %s | %s", playerName, timeMillis, difficulty, date.toLocalDate());
    }
}
