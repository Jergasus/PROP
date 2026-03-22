package controller;

import java.io.File;
import model.adjacency.*;
import model.algorithms.Generator;
import model.board.Board;
import model.cell.CellShape;
import model.game.Game;
import persistence.game.GameSaver;
import view.ConsoleView;

public class MenuController {

    private final ConsoleView view;
    // Removed direct Scanner usage, rely on ConsoleView

    public MenuController() {
        this.view = new ConsoleView();
    }

    public void showMainMenu() {
        boolean exit = false;
        while (!exit) {
            view.printMessage("\n=== MENÚ PRINCIPAL HIDATO ===");
            view.printMessage("1. Jugar Nueva Partida Aleatoria");
            view.printMessage("2. Editor de Hidato (Crear/Validar Propio)");
            view.printMessage("3. Cargar Partida Guardada");
            view.printMessage("4. Ver Ranking");
            view.printMessage("5. Salir");

            String option = view.askString("Selecciona una opción:");
            // askString now returns next(), so single chars work fine.

            switch (option) {
                case "1" ->
                    playRandomGame();
                case "2" ->
                    openEditor();
                case "3" ->
                    loadGame();
                case "4" ->
                    showRanking();
                case "5" ->
                    exit = true;
                default ->
                    view.printMessage("Opción no válida.");
            }
        }
    }

    private void playRandomGame() {
        java.util.Random rnd = new java.util.Random();

        // Configurar Partida
        view.printMessage("\n--- Configuración de Partida ---");

        // 1. FORMA
        view.printMessage("Selecciona Forma de Celdas: [C]uadrado, [H]exágono, [T]riángulo, [R]andom");
        String shapeStr = view.askString(">").toUpperCase();
        CellShape shape;

        if (shapeStr.startsWith("R")) {
            int check = rnd.nextInt(3); // 0, 1, 2
            if (check == 0) {
                shape = CellShape.SQUARE;
            } else if (check == 1) {
                shape = CellShape.HEXAGON;
            } else {
                shape = CellShape.TRIANGLE;
            }
            view.printMessage("-> Forma aleatoria elegida: " + shape);
        } else if (shapeStr.startsWith("H")) {
            shape = CellShape.HEXAGON;
        } else if (shapeStr.startsWith("T")) {
            shape = CellShape.TRIANGLE;
        } else {
            shape = CellShape.SQUARE;
        }

        // 2. ADYACENCIA
        view.printMessage("Selecciona Adyacencia: [1] Lados (Ortogonal), [2] Lados+Vértices (Full), [R]andom");
        String adjStr = view.askString(">").toUpperCase();
        boolean fullAdj = false;

        if (adjStr.startsWith("R")) {
            fullAdj = rnd.nextBoolean();
            view.printMessage("-> Adyacencia aleatoria elegida: " + (fullAdj ? "Full (Diagonal)" : "Ortogonal"));
        } else {
            fullAdj = adjStr.startsWith("2");
        }

        // 3. TAMAÑO
        view.printMessage("Tamaño del lado (ej. 5 para 5x5) o [R]andom (3-8):");
        String sizeStr = view.askString(">").toUpperCase();
        int size = 5;

        if (sizeStr.startsWith("R")) {
            size = 3 + rnd.nextInt(6); // 3 to 8
            view.printMessage("-> Tamaño aleatorio elegido: " + size);
        } else {
            try {
                size = Integer.parseInt(sizeStr);
            } catch (Exception e) {
                view.printMessage("Tamaño inválido, usando 5 por defecto.");
            }
        }

        // 4. DIFICULTAD
        view.printMessage("Dificultad (0.0 facil - 1.0 dificil) o [R]andom:");
        String diffStr = view.askString(">").toUpperCase();
        double diff = 0.4;

        if (diffStr.startsWith("R")) {
            // Random between 0.2 and 0.6
            diff = 0.2 + (rnd.nextDouble() * 0.4);
            // Redondear a 2 decimales para que se vea bonito
            diff = Math.round(diff * 100.0) / 100.0;
            view.printMessage("-> Dificultad aleatoria elegida: " + diff);
        } else {
            try {
                double inputDiff = Double.parseDouble(diffStr);
                // Escalar de [0, 1] a [0.1, 0.7] para el sistema interno
                // Fórmula: min + (val * (max - min))
                diff = 0.1 + (inputDiff * 0.6);
                
                // Redondear
                diff = Math.round(diff * 100.0) / 100.0;
                // Clamp por seguridad extrema
                if(diff < 0.1) diff = 0.1;
                if(diff > 0.8) diff = 0.8;
                
            } catch (Exception e) {
                view.printMessage("Dificultad inválida, usando media por defecto.");
                diff = 0.4;
            }
        }

        // Seleccionar estrategia de adyacencia según forma y preferencia del usuario
        AdjacencyStrategy strategy;
        if (shape == CellShape.SQUARE && fullAdj) {
            strategy = new SquareFullAdjacencyStrategy();
        } else if (shape == CellShape.HEXAGON) {
            strategy = new HexagonalAdjacencyStrategy();
        } else if (shape == CellShape.TRIANGLE) {
            strategy = new TriangleAdjacencyStrategy();
        } else {
            strategy = new SquareAdjacencyStrategy();
        }

        // Generar
        view.printMessage("Generando...");
        Generator generator = new Generator();
        Board board = generator.generatePuzzle(size, size, shape, strategy, diff);

        if (board != null) {

            Game game = new Game(board);
            GameController gc = new GameController(game, view);
            gc.play();
        } else {
            view.printMessage("Error al generar el tablero. Intenta con otros parámetros.");
        }
    }

    private void openEditor() {
        view.printMessage("\n--- Editor de Hidato ---");
        view.printMessage("Configura tu tablero vacío:");
        view.printMessage("Selecciona Forma: [C]adrado, [H]exágono, [T]riángulo");
        String shapeStr = view.askString(">").toUpperCase();
        CellShape shape;
        if (shapeStr.startsWith("H")) {
            shape = CellShape.HEXAGON;
        } else if (shapeStr.startsWith("T")) {
            shape = CellShape.TRIANGLE;
        } else {
            shape = CellShape.SQUARE;
        }

        view.printMessage("Tamaño (ej. 5):");
        int size = 5;
        try {
            size = Integer.parseInt(view.askString(">"));
        } catch (NumberFormatException e) {
            view.printMessage("Tamaño inválido, usando 5.");
        }

        // Adyacencia (solo relevante para cuadrado)
        AdjacencyStrategy strategy;
        if (shape == CellShape.HEXAGON) {
            strategy = new HexagonalAdjacencyStrategy();
        } else if (shape == CellShape.TRIANGLE) {
            strategy = new TriangleAdjacencyStrategy();
        } else {
            view.printMessage("Adyacencia: [1] Lados (Ortogonal), [2] Lados+Vértices (Full/Diagonal)");
            String adjStr = view.askString(">");
            strategy = adjStr.startsWith("2") ? new SquareFullAdjacencyStrategy() : new SquareAdjacencyStrategy();
            view.printMessage("-> Adyacencia: " + (adjStr.startsWith("2") ? "Full (8 vecinos)" : "Ortogonal (4 vecinos)"));
        }

        Board emptyBoard = new Board(size, size, shape, strategy);
        EditorController editor = new EditorController(emptyBoard, view);
        editor.startEditor();
    }

    private void loadGame() {
        view.printMessage("Nombre del archivo a cargar (sin extensión):");
        String name = view.askString(">");
        File f = new File(name + ".hidato");

        if (!f.exists()) {
            view.printMessage("No existe archivo " + name + ".hidato");
            return;
        }

        try {
            GameSaver saver = new GameSaver();
            Game game = saver.loadGame(name + ".hidato");
            if (game == null) {
                view.printMessage("Error: partida corrupta o vacía.");
                return;
            }
            view.printMessage("Partida cargada. Tiempo acumulado: " + game.getElapsedTime() + "ms");

            GameController gc = new GameController(game, view);
            gc.play();
        } catch (Exception e) {
            view.printMessage("Error cargando partida: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showRanking() {
        model.ranking.RankingManager rm = new model.ranking.RankingManager();
        view.printRanking(rm.getTopScores(10));
    }
}
