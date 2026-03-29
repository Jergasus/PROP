package stubs;

// Stub for the persistence layer — to be replaced in delivery 2.
public class StubGestorDades {

    public String[] loadHidato(String path) { return new String[0]; }

    public boolean saveHidato(String path, String[] lines) { return false; }

    public String[] loadRanking() { return new String[0]; }

    public boolean saveRanking(String[] lines) { return false; }
}
