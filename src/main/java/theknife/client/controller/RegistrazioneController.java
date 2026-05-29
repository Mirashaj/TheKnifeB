package theknife.client.controller;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import theknife.client.ClientTK;
import theknife.client.ServerConnection;
import theknife.client.SessioneCorrente;
import theknife.model.Utente;
import theknife.shared.Request;
import theknife.shared.Response;

/**
 * RegistrazioneController: Controller JavaFX per la schermata Registrazione.
 * Gestisce gli eventi dell'interfaccia grafica e comunica con il server tramite
 * ServerConnection.
 * 
 * @author Mirashaj Erik 760453 VA
 * @author GorchynskYi Igor 757184 VA
 * @author Kabuka Dan Mumanga 757708 VA
 * @author Mujeci Lorenzo 757597 VA
 */
public class RegistrazioneController {

    @FXML
    private TextField tfNome;

    @FXML
    private TextField tfCognome;

    @FXML
    private TextField tfEmail;

    @FXML
    private PasswordField pfPassword;

    @FXML
    private PasswordField pfConfirmPassword;

    @FXML
    private RadioButton rbCliente;

    @FXML
    private RadioButton rbGestore;

    @FXML
    private TextField tfDomicilio;

    @FXML
    private Label lblErrore;

    @FXML
    public void initialize() {
        String fieldStyle = "-fx-font-size: 13px; -fx-pref-height: 40px;";
        tfNome.setStyle(fieldStyle);
        tfCognome.setStyle(fieldStyle);
        tfEmail.setStyle(fieldStyle);
        pfPassword.setStyle(fieldStyle);
        tfDomicilio.setStyle(fieldStyle);
        tfNome.setAlignment(Pos.CENTER_LEFT);
        tfCognome.setAlignment(Pos.CENTER_LEFT);
        tfEmail.setAlignment(Pos.CENTER_LEFT);
        pfPassword.setAlignment(Pos.CENTER_LEFT);
        tfDomicilio.setAlignment(Pos.CENTER_LEFT);
    }

    @FXML
    private void handleRegistra() {
        String nome = tfNome.getText().trim();
        String cognome = tfCognome.getText().trim();
        String email = tfEmail.getText().trim();
        String password = pfPassword.getText();
        String confirmPassword = pfConfirmPassword.getText();
        String ruolo = (rbGestore != null && rbGestore.isSelected()) ? "gestore" : "cliente";
        String domicilio = tfDomicilio.getText().trim();

        if (nome.isEmpty() || cognome.isEmpty() || email.isEmpty() || password.isEmpty() || domicilio.isEmpty()) {
            lblErrore.setText("Fill in all required fields, including location.");
            return;
        }

        int atIndex = email.indexOf('@');
        if (atIndex < 1 || !email.substring(atIndex).contains(".")) {
            lblErrore.setText("Please enter a valid email address.");
            return;
        }

        if (password.length() < 8) {
            lblErrore.setText("Password must be at least 8 characters long.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            lblErrore.setText("Passwords do not match.");
            return;
        }

        try {
            ServerConnection.init("localhost", 5000);

            Request request = new Request("REGISTRAZIONE");
            request.addParametro("nome", nome);
            request.addParametro("cognome", cognome);
            request.addParametro("email", email);
            request.addParametro("password", password);
            request.addParametro("ruolo", ruolo);
            request.addParametro("domicilio", domicilio);

            Response response = ServerConnection.getInstance().send(request);

            if (response.isSuccesso()) {
                Utente utente = (Utente) response.getPayload();
                SessioneCorrente.getInstance().login(utente);
                ClientTK.loadScene("home.fxml", "TheKnife - Home");
            } else {
                lblErrore.setText("Registration failed: " + response.getMessaggio());
            }
        } catch (Exception e) {
            lblErrore.setText("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleIndietro() {
        ClientTK.loadScene("welcome.fxml", "TheKnife - Welcome");
    }

    @FXML
    private void handleAccedi() {
        ClientTK.loadScene("login.fxml", "TheKnife - Login");
    }
}
