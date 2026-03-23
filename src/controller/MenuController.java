package controller;

import java.io.File;
import java.io.IOException;
import model.adjacency.*;
import model.algorithms.Generator;
import model.board.Board;
import model.cell.CellShape;
import model.game.Game;
import model.level.LevelCatalog;
import model.ranking.RankingEntry;
import model.user.User;
import persistence.game.GameSaver;
import persistence.level.LevelLoader;
import persistence.ranking.RankingRepository;
import persistence.user.UserRepository;
import view.ConsoleView;

import java.util.List;

public class MenuController {

    private final ConsoleView view;
    private final User currentUser;
    private final LevelCatalog catalog;
    private final UserRepository userRepo;
    private final RankingRepository rankingRepo;

    public MenuController(User currentUser, ConsoleView view) {
        this.view = view;
        this.currentUser = currentUser;
        this.catalog = new LevelLoader().loadAllLevels();
        this.userRepo = new UserRepository();
        this.rankingRepo = new RankingRepository();
    }

    public void showMainMenu() {
        boolean exit = false;
        while (!exit) {
            view.printMessage("\n=== MENU PRINCIPAL HIDATO ===");
            view.printUserProfile(currentUser);
            view.printMessage("1. Hidatos Pregenerados");
            view.printMessage("2. Partida Aleatoria");
            view.printMessage("3. Editor de Hidato");
            view.printMessage("4. Cargar Partida Guardada");
            view.printMessage("5. Ver Ranking");
            view.printMessage("6. Salir");

            String option = view.askString("Selecciona una opcion:");

            switch (option.trim()) {
                case "1" -> playPregenerated();
                case "2" -> playRandomGame();
                case "3" -> openEditor();
                case "4" -> loadGame();
                case "5" -> showRanking();
                case "6" -> {
                    exit = true;
                    handleExit();
                }
                default -> view.printMessage("Opcion no valida.");
            }
        }
    }

    private void playPregenerated() {
        if (catalog.size() == 0) {
            view.printMessage("No se encontraron niveles pregenerados en data/levels/.");
            return;
        }
        LevelSelectController lsc = new LevelSelectController(view, catalog, currentUser);
        lsc.showLevelSelect();
    }

    private void playRandomGame() {
        java.util.Random rnd = new java.util.Random();

        view.printMessage("\n--- Configuracion de Partida ---");

        // 1. Shape
        view.printMessage("Selecciona Forma de Celdas: [C]uadrado, [H]exagono, [T]riangulo, [R]andom");
        String shapeStr = view.askString(">").toUpperCase();
        CellShape shape;

        if (shapeStr.startsWith("R")) {
            int check = rnd.nextInt(3);
            if (check == 0) shape = CellShape.SQUARE;
            else if (check == 1) shape = CellShape.HEXAGON;
            else shape = CellShape.TRIANGLE;
            view.printMessage("-> Forma aleatoria elegida: " + shape);
        } else if (shapeStr.startsWith("H")) {
            shape = CellShape.HEXAGON;
        } else if (shapeStr.startsWith("T")) {
            shape = CellShape.TRIANGLE;
        } else {
            shape = CellShape.SQUARE;
        }

        // 2. Adjacency
        view.printMessage("Selecciona Adyacencia: [1] Lados (Ortogonal), [2] Lados+Vertices (Full), [R]andom");
        String adjStr = view.askString(">").toUpperCase();
        boolean fullAdj = false;

        if (adjStr.startsWith("R")) {
            fullAdj = rnd.nextBoolean();
            view.printMessage("-> Adyacencia aleatoria elegida: " + (fullAdj ? "Full (Diagonal)" : "Ortogonal"));
        } else {
            fullAdj = adjStr.startsWith("2");
        }

        // 3. Size
        view.printMessage("Tamano del lado (ej. 5 para 5x5) o [R]andom (3-8):");
        String sizeStr = view.askString(">").toUpperCase();
        int size = 5;

        if (sizeStr.startsWith("R")) {
            size = 3 + rnd.nextInt(6);
            view.printMessage("-> Tamano aleatorio elegido: " + size);
        } else {
            try {
                size = Integer.parseInt(sizeStr);
            } catch (Exception e) {
                view.printMessage("Tamano invalido, usando 5 por defecto.");
            }
        }

        // 4. Difficulty
        view.printMessage("Dificultad (0.0 facil - 1.0 dificil) o [R]andom:");
        String diffStr = view.askString(">").toUpperCase();
        double diff = 0.4;

        if (diffStr.startsWith("R")) {
            diff = 0.2 + (rnd.nextDouble() * 0.4);
            diff = Math.round(diff * 100.0) / 100.0;
            view.printMessage("-> Dificultad aleatoria elegida: " + diff);
        } else {
            try {
                double inputDiff = Double.parseDouble(diffStr);
                diff = 0.1 + (inputDiff * 0.6);
                diff = Math.round(diff * 100.0) / 100.0;
                if (diff < 0.1) diff = 0.1;
                if (diff > 0.8) diff = 0.8;
            } catch (Exception e) {
                view.printMessage("Dificultad invalida, usando media por defecto.");
                diff = 0.4;
            }
        }

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

        view.printMessage("Generando...");
        Generator generator = new Generator();
        Board board = generator.generatePuzzle(size, size, shape, strategy, diff);

        if (board != null) {
            Game game = new Game(board);
            GameController gc = new GameController(game, view);
            gc.play();
        } else {
            view.printMessage("Error al generar el tablero. Intenta con otros parametros.");
        }
    }

    private void openEditor() {
        view.printMessage("\n--- Editor de Hidato ---");
        view.printMessage("Configura tu tablero vacio:");
        view.printMessage("Selecciona Forma: [C]uadrado, [H]exagono, [T]riangulo");
        String shapeStr = view.askString(">").toUpperCase();
        CellShape shape;
        if (shapeStr.startsWith("H")) {
            shape = CellShape.HEXAGON;
        } else if (shapeStr.startsWith("T")) {
            shape = CellShape.TRIANGLE;
        } else {
            shape = CellShape.SQUARE;
        }

        view.printMessage("Tamano (ej. 5):");
        int size = 5;
        try {
            size = Integer.parseInt(view.askString(">"));
        } catch (NumberFormatException e) {
            view.printMessage("Tamano invalido, usando 5.");
        }

        AdjacencyStrategy strategy;
        if (shape == CellShape.HEXAGON) {
            strategy = new HexagonalAdjacencyStrategy();
        } else if (shape == CellShape.TRIANGLE) {
            strategy = new TriangleAdjacencyStrategy();
        } else {
            view.printMessage("Adyacencia: [1] Lados (Ortogonal), [2] Lados+Vertices (Full/Diagonal)");
            String adjStr = view.askString(">");
            strategy = adjStr.startsWith("2") ? new SquareFullAdjacencyStrategy() : new SquareAdjacencyStrategy();
        }

        Board emptyBoard = new Board(size, size, shape, strategy);
        EditorController editor = new EditorController(emptyBoard, view);
        editor.startEditor();
    }

    private void loadGame() {
        view.printMessage("Nombre del archivo a cargar (sin extension):");
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
                view.printMessage("Error: partida corrupta o vacia.");
                return;
            }
            view.printMessage("Partida cargada. Tiempo acumulado: " + view.formatTime(game.getElapsedTime()));

            GameController gc = new GameController(game, view);
            gc.play();
        } catch (Exception e) {
            view.printMessage("Error cargando partida: " + e.getMessage());
        }
    }

    private void showRanking() {
        List<RankingEntry> entries = rankingRepo.loadRanking();
        view.printPointRanking(entries);
    }

    private void handleExit() {
        if (currentUser.getTotalPoints() > 0) {
            boolean update = view.askYesNo("Actualizar ranking global con tus " + currentUser.getTotalPoints() + " puntos?");
            if (update) {
                try {
                    rankingRepo.updateEntry(currentUser.getUsername(), currentUser.getTotalPoints());
                    view.printMessage("Ranking actualizado.");
                    view.printPointRanking(rankingRepo.loadRanking());
                } catch (IOException e) {
                    view.printMessage("Error al actualizar ranking: " + e.getMessage());
                }
            }
        }

        // Save user profile on exit
        try {
            userRepo.saveUser(currentUser);
        } catch (IOException e) {
            view.printMessage("Error al guardar perfil: " + e.getMessage());
        }

        view.printMessage("Hasta luego, " + currentUser.getUsername() + "!");
    }
}
