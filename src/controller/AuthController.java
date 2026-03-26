package controller;

import model.user.User;
import persistence.user.UserRepository;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class AuthController {
    private final DomainController domain;
    private final UserRepository userRepo;

    public AuthController(DomainController domain) {
        this.domain = domain;
        this.userRepo = new UserRepository();
    }

    public User authenticate() {
        while (true) {
            domain.printMessage("\n=== HIDATO - AUTENTICACION ===");
            domain.printMessage("1. Iniciar Sesion");
            domain.printMessage("2. Registrarse");
            String option = domain.askString("Selecciona una opcion:");

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
                    domain.printMessage("Opcion no valida.");
            }
        }
    }

    private User login() {
        String username = domain.askString("Usuario:").trim();
        if (username.isEmpty()) {
            domain.printMessage("Nombre de usuario vacio.");
            return null;
        }

        String password = domain.askString("Contrasena:");

        try {
            User user = userRepo.loadUser(username);
            if (user == null) {
                domain.printMessage("Usuario no encontrado.");
                return null;
            }

            String hash = hashPassword(password);
            if (!hash.equals(user.getPasswordHash())) {
                domain.printMessage("Contrasena incorrecta.");
                return null;
            }

            domain.printMessage("Bienvenido de nuevo, " + username + "!");
            domain.printUserProfile(user);
            return user;
        } catch (IOException e) {
            domain.printMessage("Error al cargar usuario: " + e.getMessage());
            return null;
        }
    }

    private User register() {
        String username = domain.askString("Elige un nombre de usuario:").trim();
        if (username.isEmpty()) {
            domain.printMessage("Nombre de usuario vacio.");
            return null;
        }

        if (userRepo.userExists(username)) {
            domain.printMessage("Ese nombre de usuario ya existe.");
            return null;
        }

        String password = domain.askString("Elige una contrasena:");
        if (password.trim().isEmpty()) {
            domain.printMessage("Contrasena vacia.");
            return null;
        }

        String hash = hashPassword(password);
        User user = new User(username, hash);

        try {
            userRepo.saveUser(user);
            domain.printMessage("Usuario registrado correctamente. Bienvenido, " + username + "!");
            return user;
        } catch (IOException e) {
            domain.printMessage("Error al guardar usuario: " + e.getMessage());
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
