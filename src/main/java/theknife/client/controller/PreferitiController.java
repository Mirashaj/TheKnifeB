package theknife.client.controller;

import java.io.IOException;
import java.util.List;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import theknife.client.ClientTK;
import theknife.client.ServerConnection;
import theknife.client.SessioneCorrente;
import theknife.model.Ristorante;
import theknife.shared.Request;
import theknife.shared.Response;

/**
 * PreferitiController: Controller JavaFX per la schermata Preferiti. Gestisce
 * gli eventi dell'interfaccia grafica e comunica con il server tramite
 * ServerConnection.
 * 
 * @author Mirashaj Erik 760453 VA
 * @author GorchynskYi Igor 757184 VA
 * @author Kabuka Dan Mumanga 757708 VA
 * @author Mujeci Lorenzo 757597 VA
 */
public class PreferitiController {

    @FXML
    private AnchorPane loginGatePanel;
    @FXML
    private AnchorPane contentPanel;
    @FXML
    private Button btnAccedi;
    @FXML
    private Button btnRegistrati;
    @FXML
    private TilePane tilePreferiti;
    @FXML
    private VBox emptyStateBox;
    @FXML
    private Label lblError;
    @FXML
    private MenuButton accountMenuButton;

    private GridPane gridPreferiti;

    @FXML
    public void initialize() {
        updateAccountMenu();

        if (btnAccedi != null) {
            btnAccedi.setOnAction(e -> ClientTK.loadScene("login.fxml", "TheKnife - Login"));
        }
        if (btnRegistrati != null) {
            btnRegistrati.setOnAction(e -> ClientTK.loadScene("registrazione.fxml", "TheKnife - Registration"));
        }

        if (!SessioneCorrente.getInstance().isUserLogged()) {
            if (loginGatePanel != null) {
                loginGatePanel.setVisible(true);
                loginGatePanel.setManaged(true);
            }
            if (contentPanel != null) {
                contentPanel.setVisible(false);
                contentPanel.setManaged(false);
            }
            return;
        }

        if (loginGatePanel != null) {
            loginGatePanel.setVisible(false);
            loginGatePanel.setManaged(false);
        }
        if (contentPanel != null) {
            contentPanel.setVisible(true);
            contentPanel.setManaged(true);
        }

        if (SessioneCorrente.getInstance().isGestore()) {
            ClientTK.loadScene("welcome.fxml", "TheKnife - Welcome");
            return;
        }

        if (tilePreferiti != null && tilePreferiti.getParent() instanceof Pane parent) {
            gridPreferiti = new GridPane();
            gridPreferiti.setHgap(12);
            gridPreferiti.setVgap(12);
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(25);
            gridPreferiti.getColumnConstraints().addAll(cc, cc, cc, cc);

            int index = parent.getChildren().indexOf(tilePreferiti);
            parent.getChildren().set(index, gridPreferiti);

            javafx.application.Platform.runLater(() -> {
                javafx.scene.Node n = gridPreferiti;
                while (n != null && !(n instanceof ScrollPane)) {
                    n = n.getParent();
                }
                if (n instanceof ScrollPane) {
                    ((ScrollPane) n).setFitToWidth(true);
                }
            });
        }

        loadPreferiti();
    }

    private void loadPreferiti() {
        lblError.setVisible(false);
        new Thread(() -> {
            try {
                Request req = new Request("VISUALIZZA_PREFERITI");
                req.addParametro("idUtente", SessioneCorrente.getInstance().getUtenteLoggato().getId());
                Response res = ServerConnection.getInstance().send(req);
                if (res.isSuccesso()) {
                    @SuppressWarnings("unchecked")
                    List<Ristorante> list = (List<Ristorante>) res.getPayload();
                    Platform.runLater(() -> {
                        if (gridPreferiti != null)
                            gridPreferiti.getChildren().clear();
                        if (list == null || list.isEmpty()) {
                            emptyStateBox.setVisible(true);
                            emptyStateBox.setManaged(true);
                        } else {
                            emptyStateBox.setVisible(false);
                            emptyStateBox.setManaged(false);
                            if (gridPreferiti != null) {
                                for (int i = 0; i < list.size(); i++) {
                                    VBox card = createCard(list.get(i));
                                    card.setMaxWidth(Double.MAX_VALUE);
                                    GridPane.setHgrow(card, Priority.ALWAYS);
                                    gridPreferiti.add(card, i % 4, i / 4);
                                }
                            }
                        }
                    });
                } else {
                    Platform.runLater(() -> {
                        lblError.setText(res.getMessaggio());
                        lblError.setVisible(true);
                        lblError.setManaged(true);
                    });
                }
            } catch (IOException | ClassNotFoundException e) {
                Platform.runLater(() -> {
                    lblError.setText("Errore di connessione");
                    lblError.setVisible(true);
                    lblError.setManaged(true);
                });
            }
        }).start();
    }

    private VBox createCard(Ristorante r) {
        VBox card = new VBox(8);
        card.getStyleClass().add("tk-card");

        Label title = new Label(r.getNome());
        title.getStyleClass().add("tk-card-title");

        Label cuisine = new Label(r.getTipoCucina());
        cuisine.getStyleClass().add("tk-text-secondary");

        Label rating = new Label(stars((int) Math.round(r.getMediaStelle())));
        rating.getStyleClass().add("tk-stars");

        HBox actions = new HBox(8);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button btnRemove = new Button("Delete");
        btnRemove.getStyleClass().add("tk-btn-primary");
        btnRemove.setStyle("-fx-background-color: #C0392B; -fx-text-fill: white;");
        btnRemove.setOnAction(e -> {
            removePreferito(r.getId(), card);
        });
        actions.getChildren().addAll(spacer, btnRemove);

        card.getChildren().addAll(title, cuisine, rating, actions);

        card.setOnMouseClicked(e -> {
            SessioneCorrente.getInstance().setSelectedRistorante(r);
            ClientTK.loadScene("dettaglio_ristorante.fxml", "TheKnife - Dettaglio Ristorante");
        });

        return card;
    }

    private void removePreferito(int idRistorante, VBox card) {
        new Thread(() -> {
            try {
                Request req = new Request("RIMUOVI_PREFERITO");
                req.addParametro("idUtente", SessioneCorrente.getInstance().getUtenteLoggato().getId());
                req.addParametro("idRistorante", idRistorante);
                Response res = ServerConnection.getInstance().send(req);
                if (res.isSuccesso()) {
                    Platform.runLater(() -> {
                        loadPreferiti();
                    });
                } else {
                    Platform.runLater(() -> {
                        lblError.setText(res.getMessaggio());
                        lblError.setVisible(true);
                        lblError.setManaged(true);
                    });
                }
            } catch (IOException | ClassNotFoundException e) {
                Platform.runLater(() -> {
                    lblError.setText("Errore di connessione");
                    lblError.setVisible(true);
                    lblError.setManaged(true);
                });
            }
        }).start();
    }

    private String stars(int value) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < 5; i++)
            b.append(i < value ? '★' : '☆');
        return b.toString();
    }

    private void updateAccountMenu() {
        if (accountMenuButton == null)
            return;
        accountMenuButton.setText("");
        accountMenuButton.getItems().clear();
        accountMenuButton.setGraphic(createAccountGraphic());

        if (SessioneCorrente.getInstance().isUserLogged()) {
            MenuItem settings = new MenuItem("Settings");
            settings.setOnAction(e -> ClientTK.loadScene("settings.fxml", "TheKnife - Settings"));
            MenuItem logout = new MenuItem("Logout");
            logout.setOnAction(e -> {
                SessioneCorrente.getInstance().logout();
                ClientTK.loadScene("welcome.fxml", "TheKnife - Welcome");
            });
            accountMenuButton.getItems().addAll(settings, logout);
        } else {
            MenuItem register = new MenuItem("Register");
            register.setOnAction(e -> ClientTK.loadScene("registrazione.fxml", "TheKnife - Registration"));
            MenuItem login = new MenuItem("Login");
            login.setOnAction(e -> ClientTK.loadScene("login.fxml", "TheKnife - Login"));
            MenuItem back = new MenuItem("Back");
            back.setOnAction(e -> ClientTK.loadScene("welcome.fxml", "TheKnife - Welcome"));
            accountMenuButton.getItems().addAll(register, login, back);
        }
    }

    private StackPane createAccountGraphic() {
        SVGPath svgPath = new SVGPath();
        svgPath.setContent(
                "M12.12 12.78C12.05 12.77 11.96 12.77 11.88 12.78C10.12 12.72 8.71997 11.28 8.71997 9.50998C8.71997 7.69998 10.18 6.22998 12 6.22998C13.81 6.22998 15.28 7.69998 15.28 9.50998C15.27 11.28 13.88 12.72 12.12 12.78Z M18.74 19.3801C16.96 21.0101 14.6 22.0001 12 22.0001C9.40001 22.0001 7.04001 21.0101 5.26001 19.3801C5.36001 18.4401 5.96001 17.5201 7.03001 16.8001C9.77001 14.9801 14.25 14.9801 16.97 16.8001C18.04 17.5201 18.64 18.4401 18.74 19.3801Z M12 22C17.5228 22 22 17.5228 22 12C22 6.47715 17.5228 2 12 2C6.47715 2 2 6.47715 2 12C2 17.5228 6.47715 22 12 22Z");
        svgPath.getStyleClass().add("tk-account-menu-icon");
        StackPane icon = new StackPane(svgPath);
        icon.setPrefSize(18, 18);
        return icon;
    }

    @FXML
    private void handleHome() {
        ClientTK.loadScene("home.fxml", "TheKnife - Home");
    }

    @FXML
    private void handlePreferiti() {
        ClientTK.loadScene("preferiti.fxml", "TheKnife - Favorites");
    }

    @FXML
    private void handleMieRecensioni() {
        ClientTK.loadScene("mie_recensioni.fxml", "TheKnife - My Reviews");
    }

    @FXML
    private void handlePrenotazioni() {
        ClientTK.loadScene("prenotazioni.fxml", "TheKnife - Bookings");
    }

    @FXML
    private void handleIndietro() {
        ClientTK.loadScene("home.fxml", "TheKnife - Home");
    }
}
