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

import java.util.List;

public class MenuController {

    private final DomainController domain;
    private final User currentUser;
    private final LevelCatalog catalog;
    private final UserRepository userRepo;
    private final RankingRepository rankingRepo;

    public MenuController(User currentUser, DomainController domain) {
        this.domain = domain;
        this.currentUser = currentUser;
        this.catalog = new LevelLoader().loadAllLevels();
        this.userRepo = new UserRepository();
        this.rankingRepo = new RankingRepository();
    }

    public void showMainMenu() {
        boolean exit = false;
        while (!exit) {
            domain.printMessage("\n=== MENU PRINCIPAL HIDATO ===");
            domain.printUserProfile(currentUser);
            domain.printMessage("1. Hidatos Pregenerados");
            domain.printMessage("2. Partida Aleatoria");
            domain.printMessage("3. Editor de Hidato");
            domain.printMessage("4. Cargar Partida Guardada");
            domain.printMessage("5. Ver Ranking");
            domain.printMessage("6. Salir");

            String option = domain.askString("Selecciona una opcion:");

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
                default -> domain.printMessage("Opcion no valida.");
            }
        }
    }

    private void playPregenerated() {
        if (catalog.size() == 0) {
            domain.printMessage("No se encontraron niveles pregenerados en data/levels/.");
            return;
        }
        LevelSelectController lsc = new LevelSelectController(domain, catalog, currentUser);
        lsc.showLevelSelect();
    }

    private void playRandomGame() {
        java.util.Random rnd = new java.util.Random();

        domain.printMessage("\n--- Configuracion de Partida ---");

        // 1. Shape
        domain.printMessage("Selecciona Forma de Celdas: [C]uadrado, [H]exagono, [T]riangulo, [R]andom");
        String shapeStr = domain.askString(">").toUpperCase();
        CellShape shape;

        if (shapeStr.startsWith("R")) {
            int check = rnd.nextInt(3);
            if (check == 0) shape = CellShape.SQUARE;
            else if (check == 1) shape = CellShape.HEXAGON;
            else shape = CellShape.TRIANGLE;
            domain.printMessage("-> Forma aleatoria elegida: " + shape);
        } else if (shapeStr.startsWith("H")) {
            shape = CellShape.HEXAGON;
        } else if (shapeStr.startsWith("T")) {
            shape = CellShape.TRIANGLE;
        } else {
            shape = CellShape.SQUARE;
        }

        // 2. Adjacency
        domain.printMessage("Selecciona Adyacencia: [1] Lados (Ortogonal), [2] Lados+Vertices (Full), [R]andom");
        String adjStr = domain.askString(">").toUpperCase();
        boolean fullAdj = false;

        if (adjStr.startsWith("R")) {
            fullAdj = rnd.nextBoolean();
            domain.printMessage("-> Adyacencia aleatoria elegida: " + (fullAdj ? "Full (Diagonal)" : "Ortogonal"));
        } else {
            fullAdj = adjStr.startsWith("2");
        }

        // 3. Size
        domain.printMessage("Tamano del lado (ej. 5 para 5x5) o [R]andom (3-8):");
        String sizeStr = domain.askString(">").toUpperCase();
        int size = 5;

        if (sizeStr.startsWith("R")) {
            size = 3 + rnd.nextInt(6);
            domain.printMessage("-> Tamano aleatorio elegido: " + size);
        } else {
            try {
                size = Integer.parseInt(sizeStr);
            } catch (Exception e) {
                domain.printMessage("Tamano invalido, usando 5 por defecto.");
            }
        }

        // 4. Difficulty
        domain.printMessage("Dificultad (0.0 facil - 1.0 dificil) o [R]andom:");
        String diffStr = domain.askString(">").toUpperCase();
        double diff = 0.4;

        if (diffStr.startsWith("R")) {
            diff = 0.2 + (rnd.nextDouble() * 0.4);
            diff = Math.round(diff * 100.0) / 100.0;
            domain.printMessage("-> Dificultad aleatoria elegida: " + diff);
        } else {
            try {
                double inputDiff = Double.parseDouble(diffStr);
                diff = 0.1 + (inputDiff * 0.6);
                diff = Math.round(diff * 100.0) / 100.0;
                if (diff < 0.1) diff = 0.1;
                if (diff > 0.8) diff = 0.8;
            } catch (Exception e) {
                domain.printMessage("Dificultad invalida, usando media por defecto.");
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

        // 5. Void cells
        domain.printMessage("Numero de celdas vacias/agujeros (0 para ninguna) o [R]andom:");
        String voidStr = domain.askString(">").toUpperCase();
        int numVoids = 0;
        if (voidStr.startsWith("R")) {
            numVoids = rnd.nextInt(Math.max(1, (size * size) / 4));
            domain.printMessage("-> Agujeros aleatorios: " + numVoids);
        } else {
            try {
                numVoids = Integer.parseInt(voidStr);
                if (numVoids < 0) numVoids = 0;
            } catch (Exception e) {
                numVoids = 0;
            }
        }

        domain.printMessage("Generando...");
        Generator generator = new Generator();
        Board board;
        if (numVoids > 0) {
            board = generator.generatePuzzle(size, size, shape, strategy, numVoids, diff);
        } else {
            board = generator.generatePuzzle(size, size, shape, strategy, diff);
        }

        if (board != null) {
            Game game = new Game(board);
            GameController gc = new GameController(game, domain);
            gc.play();
        } else {
            domain.printMessage("Error al generar el tablero. Intenta con otros parametros.");
        }
    }

    private void openEditor() {
        domain.printMessage("\n--- Editor de Hidato ---");
        domain.printMessage("Configura tu tablero vacio:");
        domain.printMessage("Selecciona Forma: [C]uadrado, [H]exagono, [T]riangulo");
        String shapeStr = domain.askString(">").toUpperCase();
        CellShape shape;
        if (shapeStr.startsWith("H")) {
            shape = CellShape.HEXAGON;
        } else if (shapeStr.startsWith("T")) {
            shape = CellShape.TRIANGLE;
        } else {
            shape = CellShape.SQUARE;
        }

        domain.printMessage("Tamano (ej. 5):");
        int size = 5;
        try {
            size = Integer.parseInt(domain.askString(">"));
        } catch (NumberFormatException e) {
            domain.printMessage("Tamano invalido, usando 5.");
        }

        AdjacencyStrategy strategy;
        if (shape == CellShape.HEXAGON) {
            strategy = new HexagonalAdjacencyStrategy();
        } else if (shape == CellShape.TRIANGLE) {
            strategy = new TriangleAdjacencyStrategy();
        } else {
            domain.printMessage("Adyacencia: [1] Lados (Ortogonal), [2] Lados+Vertices (Full/Diagonal)");
            String adjStr = domain.askString(">");
            strategy = adjStr.startsWith("2") ? new SquareFullAdjacencyStrategy() : new SquareAdjacencyStrategy();
        }

        Board emptyBoard = new Board(size, size, shape, strategy);
        EditorController editor = new EditorController(emptyBoard, domain);
        editor.startEditor();
    }

    private void loadGame() {
        domain.printMessage("Nombre del archivo a cargar (sin extension):");
        String name = domain.askString(">");
        File f = new File(name + ".hidato");

        if (!f.exists()) {
            domain.printMessage("No existe archivo " + name + ".hidato");
            return;
        }

        try {
            GameSaver saver = new GameSaver();
            Game game = saver.loadGame(name + ".hidato");
            if (game == null) {
                domain.printMessage("Error: partida corrupta o vacia.");
                return;
            }
            domain.printMessage("Partida cargada. Tiempo acumulado: " + domain.formatTime(game.getElapsedTime()));

            GameController gc = new GameController(game, domain);
            gc.play();
        } catch (Exception e) {
            domain.printMessage("Error cargando partida: " + e.getMessage());
        }
    }

    private void showRanking() {
        List<RankingEntry> entries = rankingRepo.loadRanking();
        domain.printPointRanking(entries);
    }

    private void handleExit() {
        if (currentUser.getTotalPoints() > 0) {
            boolean update = domain.askYesNo("Actualizar ranking global con tus " + currentUser.getTotalPoints() + " puntos?");
            if (update) {
                try {
                    rankingRepo.updateEntry(currentUser.getUsername(), currentUser.getTotalPoints());
                    domain.printMessage("Ranking actualizado.");
                    domain.printPointRanking(rankingRepo.loadRanking());
                } catch (IOException e) {
                    domain.printMessage("Error al actualizar ranking: " + e.getMessage());
                }
            }
        }

        // Save user profile on exit
        try {
            userRepo.saveUser(currentUser);
        } catch (IOException e) {
            domain.printMessage("Error al guardar perfil: " + e.getMessage());
        }

        domain.printMessage("Hasta luego, " + currentUser.getUsername() + "!");
    }
}
