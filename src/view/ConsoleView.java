package view;

import java.util.List;
import java.util.Map;
import java.util.Scanner;
import model.board.Board;
import model.game.MoveInput;
import model.level.Level;
import model.ranking.RankingEntry;
import model.user.LevelProgress;
import model.user.User;

public class ConsoleView {
    private final Scanner scanner;

    public ConsoleView() {
        this.scanner = new Scanner(System.in);
    }

    // -----------------------------------------------------------------------
    // Ranking (point-based)
    // -----------------------------------------------------------------------

    public void printPointRanking(List<RankingEntry> entries) {
        System.out.println("\n=== RANKING GLOBAL ===");
        if (entries.isEmpty()) {
            System.out.println("No hay puntuaciones registradas.");
        } else {
            System.out.println(String.format("  %-4s %-20s %s", "#", "Jugador", "Puntos"));
            System.out.println("  " + "-".repeat(35));
            int rank = 1;
            for (RankingEntry e : entries) {
                System.out.printf("  %-4d %-20s %d pts%n", rank++, e.getUsername(), e.getTotalPoints());
            }
        }
        System.out.println("======================\n");
    }

    /** Legacy ranking for backward compat with random games. */
    public void printRanking(java.util.List<model.ranking.Score> scores) {
        System.out.println("\n=== TOP SCORES ===");
        if (scores.isEmpty()) {
            System.out.println("No hay puntuaciones registradas.");
        } else {
            System.out.println(String.format("%-15s | %-10s | %-10s | %s", "Nombre", "Tiempo", "Dificil", "Fecha"));
            System.out.println("-".repeat(60));
            for (model.ranking.Score s : scores) {
                System.out.println(s);
            }
        }
        System.out.println("==================\n");
    }

    // -----------------------------------------------------------------------
    // Timer and board display
    // -----------------------------------------------------------------------

    public void printBoardWithTime(Board board, long elapsedMillis) {
        System.out.println(board.toString());
        System.out.println("Tiempo: " + formatTime(elapsedMillis));
        System.out.println();
    }

    public String formatTime(long millis) {
        long totalSeconds = millis / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        if (minutes > 0) return minutes + "m " + seconds + "s";
        return seconds + "s";
    }

    // -----------------------------------------------------------------------
    // Stars and scoring display
    // -----------------------------------------------------------------------

    public void printStars(int stars) {
        StringBuilder sb = new StringBuilder("Estrellas: ");
        for (int i = 0; i < 3; i++) sb.append(i < stars ? "* " : "- ");
        System.out.println(sb.toString().trim());
    }

    public void printLevelResult(int stars, int pointsGained, long timeMillis) {
        System.out.println("\n=== RESULTADO ===");
        System.out.println("Tiempo: " + formatTime(timeMillis));
        printStars(stars);
        System.out.println("Puntos ganados: +" + pointsGained);
        System.out.println("=================\n");
    }

    // -----------------------------------------------------------------------
    // Level selection
    // -----------------------------------------------------------------------

    public void printLevelList(List<Level> levels, Map<String, LevelProgress> progress) {
        for (int i = 0; i < levels.size(); i++) {
            Level l = levels.get(i);
            LevelProgress lp = progress.getOrDefault(l.getLevelId(), new LevelProgress());
            String starStr = buildStarString(lp.getBestStars());
            String timeStr = lp.isCompleted() ? "(" + formatTime(lp.getBestTimeMillis()) + ")" : "(sin jugar)";
            System.out.printf("  %d. %-12s [%s] %s%n", i + 1, l.getDisplayName(), starStr, timeStr);
        }
    }

    private String buildStarString(int stars) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) sb.append(i < stars ? "*" : "-");
        return sb.toString();
    }

    // -----------------------------------------------------------------------
    // User profile
    // -----------------------------------------------------------------------

    public void printUserProfile(User user) {
        System.out.println("Jugador: " + user.getUsername() + " | Puntos totales: " + user.getTotalPoints());
    }

    // -----------------------------------------------------------------------
    // Yes/No prompt
    // -----------------------------------------------------------------------

    public boolean askYesNo(String prompt) {
        System.out.println(prompt + " [s/N]");
        String ans = scanner.nextLine().trim();
        return ans.equalsIgnoreCase("s") || ans.equalsIgnoreCase("si") || ans.equalsIgnoreCase("y");
    }

    public String askString(String prompt) {
        System.out.println(prompt);
        // Si hay una linea vacia pendiente por enter anteriores, la consumimos?
        // Es delicado. Mejor usar next() si queremos palabra, o nextLine.
        // Pero tú quieres permitir Enter vacío.
        
        // Versión simple: next() es bloqueante, no permite vacíos.
        // Si queremos permitir vacíos (Enter), necesitamos nextLine().
        // Probamos leer linea.
        String str = scanner.nextLine();
        
        // Si venimos de un nextInt(), el \n se queda. str será "".
        // Eso "simularía" que el usuario le dió a Enter.
        // Pero si ES el usuario dando Enter a propósito, también es "".
        
        // En este caso concreto (guardar partida), que sea "" está bien (usa default).
        return str;
    }
    
    // Método auxiliar para limpiar buffer si sabemos que venimos de ints
    public void consumeNewLine() {
        if(scanner.hasNextLine()) scanner.nextLine();
    }

    public void printMessage(String message) {
        System.out.println(message);
    }

    public void printBoard(Board board) {
        System.out.println(board.toString());
    }

    public MoveInput askMove() {
        System.out.println("Introduce jugada (fila columna valor).");
        System.out.println("Opciones: -1 (Salir), -2 (Undo), -3 (Redo), -4 (Rendirse/Solucion)");
        try {
            if (!scanner.hasNextInt()) {
                if(scanner.hasNext()){
                    String cmd = scanner.next();
                    // Permite comandos de texto si se desea, por ahora devolvemos null
                }
                return null;
            }
            int r = scanner.nextInt();
            
            // Si es un comando de salida (-1) o similar, consumimos el resto de la linea
            // para que no afecte al siguiente scanner.nextLine() del askString().
            if (r < 0) {
                if(scanner.hasNextLine()) scanner.nextLine(); // Limpiar el buffer aquí
                return new MoveInput(r, 0, 0);
            }
            
            // Comandos rápidos sin argumentos adicionales (ej: -1, -2, -3, -4)
            
            // Si es un movimiento normal, esperamos col y val
            if (scanner.hasNextInt()) {
                int c = scanner.nextInt();
                if (scanner.hasNextInt()) {
                    int v = scanner.nextInt();
                    return new MoveInput(r, c, v);
                }
            }
            return null;
        } catch (Exception e) {
            scanner.nextLine(); 
            return null;
        }
    }
}
