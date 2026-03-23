package controller;

import model.user.User;
import persistence.user.UserRepository;
import view.ConsoleView;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class AuthController {
    private final ConsoleView view;
    private final UserRepository userRepo;

    public AuthController(ConsoleView view) {
        this.view = view;
        this.userRepo = new UserRepository();
    }

    public User authenticate() {
        while (true) {
            view.printMessage("\n=== HIDATO - AUTENTICACION ===");
            view.printMessage("1. Iniciar Sesion");
            view.printMessage("2. Registrarse");
            String option = view.askString("Selecciona una opcion:");

            switch (option.trim()) {
                case "1": {
                    User user = login();
                    if (user != null) return user;
                    break;
                }
                case "2": {
                    User user = register();
                    if (user != null) return user;
                    break;
                }
                default:
                    view.printMessage("Opcion no valida.");
            }
        }
    }

    private User login() {
        String username = view.askString("Usuario:").trim();
        if (username.isEmpty()) {
            view.printMessage("Nombre de usuario vacio.");
            return null;
        }

        String password = view.askString("Contrasena:");

        try {
            User user = userRepo.loadUser(username);
            if (user == null) {
                view.printMessage("Usuario no encontrado.");
                return null;
            }

            String hash = hashPassword(password);
            if (!hash.equals(user.getPasswordHash())) {
                view.printMessage("Contrasena incorrecta.");
                return null;
            }

            view.printMessage("Bienvenido de nuevo, " + username + "!");
            view.printUserProfile(user);
            return user;
        } catch (IOException e) {
            view.printMessage("Error al cargar usuario: " + e.getMessage());
            return null;
        }
    }

    private User register() {
        String username = view.askString("Elige un nombre de usuario:").trim();
        if (username.isEmpty()) {
            view.printMessage("Nombre de usuario vacio.");
            return null;
        }

        if (userRepo.userExists(username)) {
            view.printMessage("Ese nombre de usuario ya existe.");
            return null;
        }

        String password = view.askString("Elige una contrasena:");
        if (password.trim().isEmpty()) {
            view.printMessage("Contrasena vacia.");
            return null;
        }

        String hash = hashPassword(password);
        User user = new User(username, hash);

        try {
            userRepo.saveUser(user);
            view.printMessage("Usuario registrado correctamente. Bienvenido, " + username + "!");
            return user;
        } catch (IOException e) {
            view.printMessage("Error al guardar usuario: " + e.getMessage());
            return null;
        }
    }

    static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
