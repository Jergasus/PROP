import controller.AuthController;
import controller.DomainController;
import controller.MenuController;
import model.user.User;
import view.ConsoleView;

public class Main {
    public static void main(String[] args) {
        ConsoleView view = new ConsoleView();
        DomainController domain = new DomainController(view);

        AuthController auth = new AuthController(domain);
        User user = auth.authenticate();

        MenuController menu = new MenuController(user, domain);
        menu.showMainMenu();
    }
}
