package model.level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds all pregenerated levels and provides lookup methods.
 */
public class LevelCatalog {
    private final List<Level> levels;
    private final Map<String, Level> byId;

    public LevelCatalog(List<Level> levels) {
        this.levels = new ArrayList<>(levels);
        this.byId = new HashMap<>();
        for (Level l : levels) {
            byId.put(l.getLevelId(), l);
        }
    }

    public List<Level> getLevelsByDifficulty(Difficulty d) {
        List<Level> result = new ArrayList<>();
        for (Level l : levels) {
            if (l.getDifficulty() == d) result.add(l);
        }
        return result;
    }

    public Level getLevel(String levelId) {
        return byId.get(levelId);
    }

    public List<Level> getAllLevels() {
        return levels;
    }

    public int size() {
        return levels.size();
    }
}
