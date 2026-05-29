package theknife.client.controller;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import theknife.client.ClientTK;
import theknife.client.ServerConnection;
import theknife.model.Utente;
import theknife.shared.Request;
import theknife.shared.Response;

/**
 * LoginController: Controller JavaFX per la schermata Login. Gestisce gli
 * eventi dell'interfaccia grafica e comunica con il server tramite
 * ServerConnection.
 * 
 * @author Mirashaj Erik 760453 VA
 * @author GorchynskYi Igor 757184 VA
 * @author Kabuka Dan Mumanga 757708 VA
 * @author Mujeci Lorenzo 757597 VA
 */
public class LoginController {

    @FXML
    private TextField tfEmail;

    @FXML
    private PasswordField pfPassword;

    @FXML
    private Label lblErrore;

    @FXML
    public void initialize() {
        tfEmail.setStyle("-fx-font-size: 13px; -fx-pref-height: 40px;");
        pfPassword.setStyle("-fx-font-size: 13px; -fx-pref-height: 40px;");
        tfEmail.setAlignment(Pos.CENTER_LEFT);
        pfPassword.setAlignment(Pos.CENTER_LEFT);
    }

    @FXML
    private void handleLogin() {
        String email = tfEmail.getText().trim();
        String password = pfPassword.getText();

        if (email.isEmpty() || password.isEmpty()) {
            lblErrore.setText("Email and password are required.");
            return;
        }

        try {
            ServerConnection.init("localhost", 5000);

            Request request = new Request("LOGIN");
            request.addParametro("email", email);
            request.addParametro("password", password);

            Response response = ServerConnection.getInstance().send(request);

            if (response.isSuccesso()) {
                Utente utente = (Utente) response.getPayload();
                System.out.println("Login eseguito: " + utente);

                theknife.client.SessioneCorrente.getInstance().login(utente);

                ClientTK.loadScene("home.fxml", "TheKnife - Home");
            } else {
                lblErrore.setText("Invalid credentials.");
            }
        } catch (Exception e) {
            lblErrore.setText("Connection error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleIndietro() {
        ClientTK.loadScene("welcome.fxml", "TheKnife - Welcome");
    }

    @FXML
    private void handleRegistrati() {
        ClientTK.loadScene("registrazione.fxml", "TheKnife - Register");
    }
}
