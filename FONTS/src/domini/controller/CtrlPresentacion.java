package domini.controller;

import domini.model.adjacency.*;
import domini.algorithms.Generator;
import domini.algorithms.Solver;
import domini.model.board.Board;
import domini.model.cell.Cell;
import domini.model.cell.CellShape;
import domini.model.game.Game;
import domini.model.level.*;
import domini.model.ranking.RankingEntry;
import domini.model.scoring.ScoringEngine;
import domini.model.user.*;
import persistence.HidatoFileParser;
import persistence.game.GameSaver;
import persistence.level.LevelLoader;
import persistence.ranking.RankingRepository;
import persistence.user.UserRepository;
import view.*;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.List;

public class CtrlPresentacion {

    private JFrame frame;
    private CardLayout cardLayout;
    private JPanel cardPanel;

    private VistaLogin vistaLogin;
    private VistaPrincipal vistaPrincipal;
    private VistaSeleccionNivel vistaSeleccionNivel;
    private VistaTauler vistaTauler;
    private VistaEditor vistaEditor;
    private VistaRanking vistaRanking;
    private VistaConfigAleatoria vistaConfigAleatoria;

    private User currentUser;
    private LevelCatalog catalog;
    private final UserRepository userRepo = new UserRepository();
    private final RankingRepository rankingRepo = new RankingRepository();
    private final GameSaver gameSaver = new GameSaver();

    private Game currentGame;
    private Difficulty currentDifficulty;
    private String currentLevelId;
    private long gameAccumulatedTime;

    // -----------------------------------------------------------------------
    // Initialization
    // -----------------------------------------------------------------------

    public void inicializarPresentacion() {
        catalog = new LevelLoader().loadAllLevels();

        frame = new JFrame("Hidato");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) { salir(); }
        });
        frame.setSize(960, 720);
        frame.setLocationRelativeTo(null);
        frame.setMinimumSize(new Dimension(800, 600));

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        vistaLogin           = new VistaLogin(this);
        vistaPrincipal       = new VistaPrincipal(this);
        vistaSeleccionNivel  = new VistaSeleccionNivel(this);
        vistaTauler          = new VistaTauler(this);
        vistaEditor          = new VistaEditor(this);
        vistaRanking         = new VistaRanking(this);
        vistaConfigAleatoria = new VistaConfigAleatoria(this);

        cardPanel.add(vistaLogin,           "login");
        cardPanel.add(vistaPrincipal,       "menu");
        cardPanel.add(vistaSeleccionNivel,  "levels");
        cardPanel.add(vistaTauler,          "game");
        cardPanel.add(vistaEditor,          "editor");
        cardPanel.add(vistaRanking,         "ranking");
        cardPanel.add(vistaConfigAleatoria, "config");

        frame.add(cardPanel);
        frame.setVisible(true);
        mostrarLogin();
    }

    // -----------------------------------------------------------------------
    // Navigation
    // -----------------------------------------------------------------------

    public void mostrarLogin() {
        cardLayout.show(cardPanel, "login");
    }

    public void mostrarMenuPrincipal() {
        vistaPrincipal.actualizar();
        cardLayout.show(cardPanel, "menu");
    }

    public void mostrarSeleccionNivel() {
        vistaSeleccionNivel.actualizar();
        cardLayout.show(cardPanel, "levels");
    }

    public void mostrarJuego() {
        vistaTauler.iniciarPartida(currentGame, gameAccumulatedTime);
        cardLayout.show(cardPanel, "game");
    }

    public void mostrarEditor(Board board) {
        vistaEditor.iniciarEditor(board);
        cardLayout.show(cardPanel, "editor");
    }

    public void mostrarRanking() {
        vistaRanking.actualizar();
        cardLayout.show(cardPanel, "ranking");
    }

    public void mostrarConfigAleatoria() {
        cardLayout.show(cardPanel, "config");
    }

    // -----------------------------------------------------------------------
    // Authentication
    // -----------------------------------------------------------------------

    public String login(String username, String password) {
        if (username.isBlank()) return "Usuario vacío.";
        try {
            User user = userRepo.loadUser(username);
            if (user == null) return "Usuario no encontrado.";
            if (!AuthController.hashPassword(password).equals(user.getPasswordHash()))
                return "Contraseña incorrecta.";
            currentUser = user;
            return null;
        } catch (IOException e) {
            return "Error: " + e.getMessage();
        }
    }

    public String register(String username, String password) {
        if (username.isBlank()) return "Usuario vacío.";
        if (password.isBlank()) return "Contraseña vacía.";
        if (userRepo.userExists(username)) return "Ese nombre ya existe.";
        String hash = AuthController.hashPassword(password);
        User user = new User(username, hash);
        try {
            userRepo.saveUser(user);
            currentUser = user;
            return null;
        } catch (IOException e) {
            return "Error: " + e.getMessage();
        }
    }

    // -----------------------------------------------------------------------
    // Getters
    // -----------------------------------------------------------------------

    public User getCurrentUser()   { return currentUser; }
    public LevelCatalog getCatalog() { return catalog; }
    public Game getGame()          { return currentGame; }

    // -----------------------------------------------------------------------
    // Level play
    // -----------------------------------------------------------------------

    public void jugarNivel(Level level) {
        currentGame           = new Game(new Board(level.getBoard()));
        currentGame.setLevelId(level.getLevelId());
        currentLevelId        = level.getLevelId();
        currentDifficulty     = level.getDifficulty();
        gameAccumulatedTime   = 0;
        mostrarJuego();
    }

    public void jugarPartidaAleatoria(Board board) {
        currentGame         = new Game(board);
        currentLevelId      = null;
        currentDifficulty   = null;
        gameAccumulatedTime = 0;
        mostrarJuego();
    }

    // -----------------------------------------------------------------------
    // Game actions (called from VistaTauler)
    // -----------------------------------------------------------------------

    public boolean hacerMovimiento(int row, int col, int value) {
        Board board = currentGame.getBoard();
        Cell cell = board.getCell(row, col);
        if (cell == null || cell.isVoid() || cell.isFixed()) return false;
        if (value < 0 || value > currentGame.getMaxNumber()) return false;
        currentGame.makeMove(row, col, value);
        return true;
    }

    public boolean deshacer() { return currentGame.undo(); }
    public boolean rehacer()  { return currentGame.redo(); }
    public boolean juegoTerminado() { return currentGame.isFinished(); }

    public String pedirPista() {
        Board clean = new Board(currentGame.getBoard());
        for (int i = 0; i < clean.getRows(); i++)
            for (int j = 0; j < clean.getCols(); j++) {
                Cell c = clean.getCell(i, j);
                if (!c.isFixed() && !c.isVoid()) c.setAsEmpty();
            }
        Solver solver = new Solver();
        long t = System.currentTimeMillis();
        if (!solver.solve(clean)) return "No se puede calcular pista.";
        long mt = System.currentTimeMillis() - t;

        Board curr = currentGame.getBoard();
        for (int v = 1; v <= currentGame.getMaxNumber(); v++)
            for (int i = 0; i < curr.getRows(); i++)
                for (int j = 0; j < curr.getCols(); j++) {
                    Cell sol = clean.getCell(i, j);
                    Cell cur = curr.getCell(i, j);
                    if (sol.getValue() == v && !cur.isFixed() && cur.getValue() != v)
                        return "El número " + v + " va en fila " + i + ", col " + j
                             + "  [máquina: " + formatTime(mt) + "]";
                }
        return "No hay más pistas disponibles.";
    }

    public Board rendirse() {
        Board board = currentGame.getBoard();
        for (int i = 0; i < board.getRows(); i++)
            for (int j = 0; j < board.getCols(); j++) {
                Cell c = board.getCell(i, j);
                if (!c.isFixed() && !c.isVoid()) c.setAsEmpty();
            }
        Solver solver = new Solver();
        long t = System.currentTimeMillis();
        boolean solved = solver.solve(board);
        long mt = System.currentTimeMillis() - t;
        if (solved) {
            JOptionPane.showMessageDialog(frame,
                "Solución encontrada  [máquina: " + formatTime(mt) + "]",
                "Rendirse", JOptionPane.INFORMATION_MESSAGE);
        }
        return solved ? board : null;
    }

    public String guardarPartida(String name, long elapsed) {
        if (name.isBlank()) name = "saved_game";
        try {
            currentGame.setElapsedTime(elapsed);
            gameSaver.saveGame(currentGame, name + ".hidato");
            return null;
        } catch (IOException e) {
            return e.getMessage();
        }
    }

    public String cargarPartida(String path) {
        try {
            Game g = gameSaver.loadGame(path);
            if (g == null) return "Partida corrupta.";
            currentGame         = g;
            currentLevelId      = g.getLevelId();
            currentDifficulty   = null;
            gameAccumulatedTime = g.getElapsedTime();
            return null;
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    public void completarNivel(long timeMillis) {
        if (currentLevelId == null || currentDifficulty == null) return;
        int stars = ScoringEngine.calculateStars(currentDifficulty, timeMillis);
        LevelProgress prev = currentUser.getLevelProgress(currentLevelId);
        int pts = ScoringEngine.calculateNewPoints(stars, prev);
        currentUser.updateLevelProgress(currentLevelId, ScoringEngine.updateProgress(prev, stars, timeMillis));
        currentUser.addPoints(pts);
        try { userRepo.saveUser(currentUser); } catch (IOException ignored) {}
    }

    public int getStarsForTime(long timeMillis) {
        if (currentDifficulty == null) return 0;
        return ScoringEngine.calculateStars(currentDifficulty, timeMillis);
    }

    // -----------------------------------------------------------------------
    // Ranking
    // -----------------------------------------------------------------------

    public List<RankingEntry> getRanking() { return rankingRepo.loadRanking(); }

    public void actualizarRanking() {
        if (currentUser == null) return;
        try { rankingRepo.updateEntry(currentUser.getUsername(), currentUser.getTotalPoints()); }
        catch (IOException ignored) {}
    }

    // -----------------------------------------------------------------------
    // Generator
    // -----------------------------------------------------------------------

    public Board generarPartidaAleatoria(CellShape shape, boolean fullAdj,
                                         int size, double diff, int numVoids) {
        AdjacencyStrategy strategy;
        if      (shape == CellShape.SQUARE && fullAdj) strategy = new SquareFullAdjacencyStrategy();
        else if (shape == CellShape.HEXAGON)           strategy = new HexagonalAdjacencyStrategy();
        else if (shape == CellShape.TRIANGLE)          strategy = new TriangleAdjacencyStrategy();
        else                                           strategy = new SquareAdjacencyStrategy();
        Generator gen = new Generator();
        return numVoids > 0
            ? gen.generatePuzzle(size, size, shape, strategy, numVoids, diff)
            : gen.generatePuzzle(size, size, shape, strategy, diff);
    }

    // -----------------------------------------------------------------------
    // Editor
    // -----------------------------------------------------------------------

    public int[] validarEditor(Board board) {
        Solver solver = new Solver();
        long t = System.currentTimeMillis();
        int count = solver.countSolutions(board, 2);
        long mt = System.currentTimeMillis() - t;
        return new int[]{count, (int) mt};
    }

    public Board resolverEditor(Board board) {
        Board copy = new Board(board);
        return new Solver().solve(copy) ? copy : null;
    }

    public String guardarEditorComoPartida(Board board, String name) {
        try { gameSaver.saveGame(new Game(board), name + ".hidato"); return null; }
        catch (Exception e) { return e.getMessage(); }
    }

    public String exportarEditor(Board board, String name) {
        try { new HidatoFileParser().saveBoard(board, name + ".txt"); return null; }
        catch (IOException e) { return e.getMessage(); }
    }

    public Board crearTableroEditor(CellShape shape, AdjacencyStrategy strategy, int size) {
        return new Board(size, size, shape, strategy);
    }

    // -----------------------------------------------------------------------
    // Exit
    // -----------------------------------------------------------------------

    public void salir() {
        actualizarRanking();
        if (currentUser != null) {
            try { userRepo.saveUser(currentUser); } catch (IOException ignored) {}
        }
        System.exit(0);
    }

    // -----------------------------------------------------------------------
    // Utility
    // -----------------------------------------------------------------------

    public String formatTime(long millis) {
        long s = millis / 1000;
        long m = s / 60;
        s %= 60;
        return m > 0 ? m + "m " + s + "s" : s + "s";
    }

    public JFrame getFrame() { return frame; }
}
