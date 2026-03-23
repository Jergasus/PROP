import controller.AuthController;
import controller.MenuController;
import model.user.User;
import view.ConsoleView;

public class Main {
    public static void main(String[] args) {
        ConsoleView view = new ConsoleView();

        AuthController auth = new AuthController(view);
        User user = auth.authenticate();

        MenuController menu = new MenuController(user, view);
        menu.showMainMenu();
    }
}
