package controller;

import model.board.Board;
import model.game.Game;
import model.level.Difficulty;
import model.level.Level;
import model.level.LevelCatalog;
import model.scoring.ScoringEngine;
import model.user.LevelProgress;
import model.user.User;
import persistence.user.UserRepository;
import view.ConsoleView;

import java.io.IOException;
import java.util.List;

public class LevelSelectController {
    private final ConsoleView view;
    private final LevelCatalog catalog;
    private final User currentUser;
    private final UserRepository userRepo;

    public LevelSelectController(ConsoleView view, LevelCatalog catalog, User currentUser) {
        this.view = view;
        this.catalog = catalog;
        this.currentUser = currentUser;
        this.userRepo = new UserRepository();
    }

    public void showLevelSelect() {
        while (true) {
            view.printMessage("\n=== HIDATOS PREGENERADOS ===");
            view.printUserProfile(currentUser);

            for (Difficulty d : Difficulty.values()) {
                List<Level> levels = catalog.getLevelsByDifficulty(d);
                if (levels.isEmpty()) continue;
                view.printMessage("\n--- " + d.getDisplayName() + " ---");
                view.printLevelList(levels, currentUser.getAllLevelProgress());
            }

            view.printMessage("\n  0. Volver al menu principal");
            String input = view.askString("Selecciona un nivel (numero) o 0 para volver:");

            int choice;
            try {
                choice = Integer.parseInt(input.trim());
            } catch (NumberFormatException e) {
                view.printMessage("Entrada no valida.");
                continue;
            }

            if (choice == 0) return;

            List<Level> allLevels = catalog.getAllLevels();
            if (choice < 1 || choice > allLevels.size()) {
                view.printMessage("Nivel no valido.");
                continue;
            }

            Level selected = allLevels.get(choice - 1);
            playLevel(selected);
        }
    }

    private void playLevel(Level level) {
        view.printMessage("\nIniciando nivel: " + level.getDisplayName()
                + " (" + level.getDifficulty().getDisplayName() + ")");

        // Deep copy the board so the original level stays untouched
        Board puzzleCopy = new Board(level.getBoard());
        Game game = new Game(puzzleCopy);
        game.setLevelId(level.getLevelId());

        GameController gc = new GameController(game, view, level.getLevelId(), level.getDifficulty());
        gc.play();

        if (gc.wasCompleted()) {
            handleLevelCompletion(level.getLevelId(), level.getDifficulty(), gc.getCompletionTimeMillis());
        }
    }

    private void handleLevelCompletion(String levelId, Difficulty difficulty, long timeMillis) {
        int stars = ScoringEngine.calculateStars(difficulty, timeMillis);
        LevelProgress previousProgress = currentUser.getLevelProgress(levelId);
        int pointsGained = ScoringEngine.calculateNewPoints(stars, previousProgress);
        LevelProgress newProgress = ScoringEngine.updateProgress(previousProgress, stars, timeMillis);

        currentUser.updateLevelProgress(levelId, newProgress);
        currentUser.addPoints(pointsGained);

        view.printLevelResult(stars, pointsGained, timeMillis);

        // Persist user progress
        try {
            userRepo.saveUser(currentUser);
        } catch (IOException e) {
            view.printMessage("Error al guardar progreso: " + e.getMessage());
        }
    }
}
