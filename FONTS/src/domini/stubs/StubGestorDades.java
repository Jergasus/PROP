package domini.stubs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class StubGestorDades {

    // Simula una consulta al gestor de dades que retorna 
    // la llista d'Hidatos amb la seva informacio descriptiva.
    // Format de retorn: { { "nom_fitxer.csv", "Nom Puzzle", "Adjacencia", "Solvable?", "Descripcio" }, ... }
    public String[][] getDadesCataleg() {
        return new String[][] {
            {"01_square_4way_3x3.csv", "Quadrat 3x3 (4-way)", "Quadrat - Nomes costats", "SOLVABLE", "Hidato 3x3 classic amb solucio."},
            {"02_square_8way_3x4_pdf.csv", "Quadrat 3x4 (8-way)", "Quadrat - Costats i cantonades", "SOLVABLE", "Exemple del PDF de l'assignatura."},
            {"03_square_8way_5x5_diamond_pdf.csv", "Rombe 5x5 (8-way)", "Quadrat - Costats i cantonades", "SOLVABLE", "Tauler quadrat en forma de rombe (PDF)."},
            {"04_hexagon_4x3_pdf.csv", "Hexagonal 4x3", "Hexagon", "SOLVABLE", "Exemple de tauler hexagonal del PDF."},
            {"05_triangle_4x4.csv", "Triangular 4x4", "Triangle", "SOLVABLE", "Exemple de tauler amb celles triangulars."},
            {"06_square_4way_unsolvable.csv", "Quadrat 4-way Impossible", "Quadrat - Nomes costats", "UNSOLVABLE", "Prova negativa: tauler sense solucio."},
            {"07_square_8way_unsolvable.csv", "Quadrat 8-way Impossible", "Quadrat - Costats i cantonades", "UNSOLVABLE", "Prova negativa: tauler sense solucio."},
            {"08_triangle_unsolvable.csv", "Triangular Impossible", "Triangle", "UNSOLVABLE", "Prova negativa: tauler sense solucio."}
        };
    }

    // Obre el fitxer .csv i retorna exclusivament les linies pures del tauler (capcalera + graella)
    public String[] loadHidato(String fileName) throws Exception {
        // Construim la ruta relativa cap als fitxers de proves per complir la normativa (no paths absoluts)
        String basePath = ".." + File.separator + "EXE" + File.separator + "DriverHidato" + File.separator + "hidatos" + File.separator;
        File file = new File(basePath + fileName);
        
        if (!file.exists()) {
            throw new Exception("El fitxer " + fileName + " no s'ha trobat a la ruta: " + basePath);
        }

        List<String> lines = new ArrayList<>();
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                // Ignorem linies buides per evitar errors de lectura
                if (!line.isEmpty()) {
                    lines.add(line);
                }
            }
        }
        
        return lines.toArray(new String[0]);
    }
}