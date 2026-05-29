package theknife.client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import theknife.client.ClientTK;
import theknife.client.SessioneCorrente;

/**
 * WelcomeController: Controller JavaFX per la schermata Welcome. Gestisce gli
 * eventi dell'interfaccia grafica e comunica con il server tramite
 * ServerConnection.
 * 
 * @author Mirashaj Erik 760453 VA
 * @author GorchynskYi Igor 757184 VA
 * @author Kabuka Dan Mumanga 757708 VA
 * @author Mujeci Lorenzo 757597 VA
 */
public class WelcomeController {

    @FXML
    private Button btnLogin;

    @FXML
    private Button btnRegistrazione;

    @FXML
    private Button btnGuest;

    @FXML
    private TextField txtCittaGuest;

    @FXML
    public void initialize() {
        String buttonStyle = "-fx-font-size: 16px; -fx-pref-height: 45px; -fx-pref-width: 220px; -fx-cursor: hand;";
        if (btnLogin != null)
            btnLogin.setStyle(buttonStyle);
        if (btnRegistrazione != null)
            btnRegistrazione.setStyle(buttonStyle);
        if (btnGuest != null)
            btnGuest.setStyle(buttonStyle);

        if (txtCittaGuest != null) {
            txtCittaGuest.setStyle("-fx-font-size: 14px; -fx-pref-height: 40px;");
        }
    }

    @FXML
    private void handleLogin() {
        ClientTK.loadScene("login.fxml", "TheKnife - Login");
    }

    @FXML
    private void handleRegistrazione() {
        ClientTK.loadScene("registrazione.fxml", "TheKnife - Register");
    }

    @FXML
    private void handleGuest() {
        String citta = (txtCittaGuest != null) ? txtCittaGuest.getText().trim() : "";
        if (!citta.isEmpty()) {
            SessioneCorrente.getInstance().setCittaGuest(citta);
            System.out.println("Guest accessing from city: " + citta);
        }
        ClientTK.loadScene("home.fxml", "TheKnife - Home");
    }

    @FXML
    private void handleConfermaGuest() {
        handleGuest();
    }
}
