package view;

import java.util.Scanner;
import model.board.Board;

public class ConsoleView {
    private final Scanner scanner;

    public ConsoleView() {
        this.scanner = new Scanner(System.in);
    }

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

    public record MoveInput(int row, int col, int value) {}

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
