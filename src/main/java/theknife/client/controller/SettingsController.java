package theknife.client.controller;

import java.io.IOException;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;
import theknife.client.ClientTK;
import theknife.client.ServerConnection;
import theknife.client.SessioneCorrente;
import theknife.model.Utente;
import theknife.shared.Request;
import theknife.shared.Response;

/**
 * SettingsController: Controller JavaFX per la schermata Settings. Gestisce gli
 * eventi dell'interfaccia grafica e comunica con il server tramite
 * ServerConnection.
 * 
 * @author Mirashaj Erik 760453 VA
 * @author GorchynskYi Igor 757184 VA
 * @author Kabuka Dan Mumanga 757708 VA
 * @author Mujeci Lorenzo 757597 VA
 */
public class SettingsController {

    @FXML
    private TextField txtNome;
    @FXML
    private TextField txtCognome;
    @FXML
    private TextField txtEmail;
    @FXML
    private PasswordField pfPassword;
    @FXML
    private PasswordField pfConfirmPassword;
    @FXML
    private Label lblError;
    @FXML
    private MenuButton accountMenuButton;
    @FXML
    private Button btnNavHome;
    @FXML
    private Button btnNavSecondary;
    @FXML
    private Button btnNavThird;
    @FXML
    private Button btnNavFourth;

    @FXML
    public void initialize() {
        if (!SessioneCorrente.getInstance().isUserLogged()) {
            ClientTK.loadScene("welcome.fxml", "TheKnife - Welcome");
            return;
        }

        updateAccountMenu();
        configureNavbar();
        populateUserDetails();
    }

    private void populateUserDetails() {
        Utente utente = SessioneCorrente.getInstance().getUtenteLoggato();
        if (utente != null) {
            if (txtNome != null)
                txtNome.setText(utente.getNome());
            if (txtCognome != null)
                txtCognome.setText(utente.getCognome());
            if (txtEmail != null)
                txtEmail.setText(utente.getEmail());
        }
    }

    @FXML
    private void handleSaveChanges() {
        if (lblError != null) {
            lblError.setVisible(false);
            lblError.setManaged(false);
        }

        String nome = txtNome != null ? txtNome.getText().trim() : "";
        String cognome = txtCognome != null ? txtCognome.getText().trim() : "";
        String email = txtEmail != null ? txtEmail.getText().trim() : "";
        String password = pfPassword != null ? pfPassword.getText() : "";
        String confirmPassword = pfConfirmPassword != null ? pfConfirmPassword.getText() : "";

        if (nome.isEmpty() || cognome.isEmpty() || email.isEmpty()) {
            showError("First Name, Last Name, and Email are required.");
            return;
        }

        if (!password.isEmpty() && !password.equals(confirmPassword)) {
            showError("Passwords do not match.");
            return;
        }

        Utente utente = SessioneCorrente.getInstance().getUtenteLoggato();

        new Thread(() -> {
            try {
                Request req = new Request("MODIFICA_UTENTE");
                req.addParametro("idUtente", utente.getId());
                req.addParametro("nome", nome);
                req.addParametro("cognome", cognome);
                req.addParametro("email", email);
                if (!password.isEmpty()) {
                    req.addParametro("password", password);
                }

                Response res = ServerConnection.getInstance().send(req);
                if (res.isSuccesso()) {
                    Platform.runLater(() -> {
                        Utente updatedUtente = (Utente) res.getPayload();
                        SessioneCorrente.getInstance().login(updatedUtente);
                        populateUserDetails();

                        Alert info = new Alert(Alert.AlertType.INFORMATION,
                                "Your profile has been updated successfully.");
                        info.setTitle("Profile Updated");
                        info.setHeaderText("Success");
                        info.showAndWait();

                        if (pfPassword != null)
                            pfPassword.clear();
                        if (pfConfirmPassword != null)
                            pfConfirmPassword.clear();
                    });
                } else {
                    Platform.runLater(() -> showError(res.getMessaggio()));
                }
            } catch (IOException | ClassNotFoundException e) {
                Platform.runLater(() -> showError("Connection error."));
            }
        }).start();
    }

    @FXML
    private void handleDeleteAccount() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure you want to permanently delete your account? This action cannot be undone.",
                ButtonType.YES, ButtonType.NO);
        alert.setTitle("Confirm Account Deletion");
        alert.setHeaderText("Delete Account");
        alert.showAndWait().ifPresent(b -> {
            if (b == ButtonType.YES) {
                executeDelete();
            }
        });
    }

    private void executeDelete() {
        if (lblError != null) {
            lblError.setVisible(false);
            lblError.setManaged(false);
        }
        new Thread(() -> {
            try {
                Request req = new Request("ELIMINA_UTENTE");
                req.addParametro("idUtente", SessioneCorrente.getInstance().getUtenteLoggato().getId());
                Response res = ServerConnection.getInstance().send(req);
                if (res.isSuccesso()) {
                    Platform.runLater(() -> {
                        SessioneCorrente.getInstance().logout();
                        Alert info = new Alert(Alert.AlertType.INFORMATION);
                        info.setTitle("Account Deleted");
                        info.setHeaderText(null);
                        info.setContentText("Your account and all associated data have been deleted.");
                        info.showAndWait();
                        ClientTK.loadScene("welcome.fxml", "TheKnife - Welcome");
                    });
                } else {
                    Platform.runLater(() -> showError(res.getMessaggio()));
                }
            } catch (IOException | ClassNotFoundException e) {
                Platform.runLater(() -> showError("Connection error."));
            }
        }).start();
    }

    private void showError(String message) {
        if (lblError != null) {
            lblError.setText(message);
            lblError.setVisible(true);
            lblError.setManaged(true);
        }
    }

    private void configureNavbar() {
        boolean gestore = SessioneCorrente.getInstance().isGestore();

        if (btnNavHome != null) {
            btnNavHome.getStyleClass().setAll("tk-nav-item");
            btnNavHome.setOnAction(e -> ClientTK.loadScene("home.fxml", "TheKnife - Home"));
        }

        if (btnNavSecondary != null) {
            btnNavSecondary.setText(gestore ? "Restaurants" : "Favorites");
            btnNavSecondary.getStyleClass().setAll("tk-nav-item");
            btnNavSecondary.setOnAction(e -> handleNavSecondary());
        }

        if (btnNavThird != null) {
            btnNavThird.setText(gestore ? "Review" : "Reviews");
            btnNavThird.getStyleClass().setAll("tk-nav-item");
            btnNavThird.setOnAction(e -> handleNavThird());
        }

        if (btnNavFourth != null) {
            btnNavFourth.setVisible(true);
            btnNavFourth.setManaged(true);
            btnNavFourth.setText("Bookings");
            btnNavFourth.getStyleClass().setAll("tk-nav-item");
            btnNavFourth.setOnAction(e -> ClientTK.loadScene("prenotazioni.fxml", "TheKnife - Bookings"));
        }
    }

    private void updateAccountMenu() {
        if (accountMenuButton == null)
            return;
        accountMenuButton.setText("");
        accountMenuButton.getItems().clear();
        accountMenuButton.setGraphic(createAccountGraphic());

        MenuItem settings = new MenuItem("Settings");
        settings.setOnAction(e -> ClientTK.loadScene("settings.fxml", "TheKnife - Settings"));
        MenuItem logout = new MenuItem("Logout");
        logout.setOnAction(e -> {
            SessioneCorrente.getInstance().logout();
            ClientTK.loadScene("welcome.fxml", "TheKnife - Welcome");
        });
        accountMenuButton.getItems().addAll(settings, logout);
    }

    private StackPane createAccountGraphic() {
        SVGPath svgPath = new SVGPath();
        svgPath.setContent(
                "M12.12 12.78C12.05 12.77 11.96 12.77 11.88 12.78C10.12 12.72 8.71997 11.28 8.71997 9.50998C8.71997 7.69998 10.18 6.22998 12 6.22998C13.81 6.22998 15.28 7.69998 15.28 9.50998C15.27 11.28 13.88 12.72 12.12 12.78Z M18.74 19.3801C16.96 21.0101 14.6 22.0001 12 22.0001C9.40001 22.0001 7.04001 21.0101 5.26001 19.3801C5.36001 18.4401 5.96001 17.5201 7.03001 16.8001C9.77001 14.9801 14.25 14.9801 16.97 16.8001C18.04 17.5201 18.64 18.4401 18.74 19.3801Z M12 22C17.5228 22 22 17.5228 22 12C22 6.47715 17.5228 2 12 2C6.47715 2 2 6.47715 2 12C2 17.5228 6.47715 22 12 22Z");
        svgPath.getStyleClass().add("tk-account-menu-icon");
        StackPane icon = new StackPane(svgPath);
        icon.setPrefSize(18, 18);
        icon.setMinSize(18, 18);
        icon.setMaxSize(18, 18);
        return icon;
    }

    @FXML
    private void handleNavSecondary() {
        if (SessioneCorrente.getInstance().isGestore()) {
            ClientTK.loadScene("dashboard_gestore.fxml", "TheKnife - Dashboard");
        } else {
            ClientTK.loadScene("preferiti.fxml", "TheKnife - Favorites");
        }
    }

    @FXML
    private void handleNavThird() {
        if (SessioneCorrente.getInstance().isGestore()) {
            ClientTK.loadScene("gestione_recensioni.fxml", "TheKnife - Review Management");
        } else {
            ClientTK.loadScene("mie_recensioni.fxml", "TheKnife - My Reviews");
        }
    }
}