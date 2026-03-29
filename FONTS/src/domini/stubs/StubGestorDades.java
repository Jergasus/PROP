package domini.stubs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StubGestorDades {
    private static final String HIDATOS_DIR = "EXE/DriverHidato/hidatos"; 

    public String[] getLlistaHidatos() {
        File dir = new File(HIDATOS_DIR);
        File[] files = dir.listFiles((d, name) -> name.endsWith(".csv") || name.endsWith(".txt"));
        if (files == null) return new String[0];
        
        String[] names = new String[files.length];
        for (int i = 0; i < files.length; i++) names[i] = files[i].getName();
        Arrays.sort(names);
        return names;
    }

    public String[] loadHidato(String fileName) throws IOException {
        File file = new File(HIDATOS_DIR, fileName);
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) lines.add(line.trim());
            }
        }
        return lines.toArray(new String[0]);
    }
}