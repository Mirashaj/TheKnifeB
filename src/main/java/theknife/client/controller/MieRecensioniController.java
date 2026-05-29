package theknife.client.controller;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
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
import theknife.model.Recensione;
import theknife.shared.Request;
import theknife.shared.Response;

/**
 * MieRecensioniController: Controller JavaFX per la schermata MieRecensioni.
 * Gestisce gli eventi dell'interfaccia grafica e comunica con il server tramite
 * ServerConnection.
 * 
 * @author Mirashaj Erik 760453 VA
 * @author GorchynskYi Igor 757184 VA
 * @author Kabuka Dan Mumanga 757708 VA
 * @author Mujeci Lorenzo 757597 VA
 */
public class MieRecensioniController {

    @FXML
    private AnchorPane loginGatePanel;
    @FXML
    private AnchorPane contentPanel;
    @FXML
    private Button btnAccedi;
    @FXML
    private Button btnRegistrati;
    @FXML
    private TilePane tileRecensioni;
    @FXML
    private VBox emptyStateBox;
    @FXML
    private Label lblError;
    @FXML
    private MenuButton accountMenuButton;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM uuuu", Locale.ITALIAN);

    private GridPane gridRecensioni;

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

        if (tileRecensioni != null && tileRecensioni.getParent() instanceof Pane parent) {
            gridRecensioni = new GridPane();
            gridRecensioni.setHgap(12);
            gridRecensioni.setVgap(12);
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(25);
            gridRecensioni.getColumnConstraints().addAll(cc, cc, cc, cc);

            int index = parent.getChildren().indexOf(tileRecensioni);
            parent.getChildren().set(index, gridRecensioni);

            javafx.application.Platform.runLater(() -> {
                javafx.scene.Node n = gridRecensioni;
                while (n != null && !(n instanceof ScrollPane)) {
                    n = n.getParent();
                }
                if (n instanceof ScrollPane) {
                    ((ScrollPane) n).setFitToWidth(true);
                }
            });
        }

        loadRecensioni();
    }

    private void loadRecensioni() {
        lblError.setVisible(false);
        new Thread(() -> {
            try {
                Request req = new Request("MIE_RECENSIONI");
                req.addParametro("idUtente", SessioneCorrente.getInstance().getUtenteLoggato().getId());
                Response res = ServerConnection.getInstance().send(req);
                if (res.isSuccesso()) {
                    @SuppressWarnings("unchecked")
                    List<Recensione> list = (List<Recensione>) res.getPayload();
                    Platform.runLater(() -> {
                        if (gridRecensioni != null)
                            gridRecensioni.getChildren().clear();
                        if (list == null || list.isEmpty()) {
                            emptyStateBox.setVisible(true);
                            emptyStateBox.setManaged(true);
                        } else {
                            emptyStateBox.setVisible(false);
                            emptyStateBox.setManaged(false);
                            if (gridRecensioni != null) {
                                for (int i = 0; i < list.size(); i++) {
                                    VBox card = createReviewCard(list.get(i));
                                    card.setMaxWidth(Double.MAX_VALUE);
                                    GridPane.setHgrow(card, Priority.ALWAYS);
                                    gridRecensioni.add(card, i % 4, i / 4);
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

    private VBox createReviewCard(Recensione recensione) {
        VBox card = new VBox(8);
        card.getStyleClass().addAll("tk-review-card", "tk-review-card-detail");

        HBox header = new HBox(10);
        Label name = new Label(recensione.getNomeRistorante());
        name.getStyleClass().add("tk-card-title");
        name.setOnMouseClicked(e -> {
            theknife.model.Ristorante r = new theknife.model.Ristorante();
            r.setId(recensione.getIdRistorante());
            r.setNome(recensione.getNomeRistorante());
            SessioneCorrente.getInstance().setSelectedRistorante(r);
            ClientTK.loadScene("dettaglio_ristorante.fxml", "TheKnife - Dettaglio Ristorante");
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label rating = new Label(stars(recensione.getStelle()));
        rating.getStyleClass().add("tk-stars");
        header.getChildren().addAll(name, spacer, rating);

        Label date = new Label(
                recensione.getDataInserimento() != null ? recensione.getDataInserimento().format(formatter) : "");
        date.getStyleClass().add("tk-text-secondary");

        Label text = new Label(recensione.getTesto());
        text.setWrapText(true);
        text.getStyleClass().add("tk-text-secondary");

        HBox actions = new HBox(8);
        actions.setAlignment(javafx.geometry.Pos.BOTTOM_RIGHT);
        Button btnModifica = new Button("Edit");
        btnModifica.getStyleClass().add("tk-btn-secondary");
        Button btnElimina = new Button("Delete");
        btnElimina.getStyleClass().add("tk-btn-primary");
        actions.getChildren().addAll(btnModifica, btnElimina);

        btnModifica.setOnAction(e -> openEditInline(card, recensione, text));
        btnElimina.setOnAction(e -> confirmDelete(card, recensione.getId()));

        card.getChildren().addAll(header, date, text);

        Region vSpacer = new Region();
        vSpacer.setPickOnBounds(false);
        VBox.setVgrow(vSpacer, Priority.ALWAYS);
        card.getChildren().add(vSpacer);

        if (recensione.getRisposta() != null && recensione.getRisposta().getTesto() != null
                && !recensione.getRisposta().getTesto().isBlank()) {
            VBox reply = new VBox(6);
            reply.getStyleClass().add("tk-review-reply");
            Label replyTitle = new Label("Risposta del ristorante");
            replyTitle.getStyleClass().add("tk-card-title");
            Label replyText = new Label(recensione.getRisposta().getTesto());
            replyText.setWrapText(true);
            replyText.getStyleClass().add("tk-text-secondary");
            reply.getChildren().addAll(replyTitle, replyText);
            card.getChildren().add(reply);
        }

        card.getChildren().add(actions);
        return card;
    }

    private void openEditInline(VBox card, Recensione recensione, Label originalText) {
        VBox editBox = new VBox(8);
        editBox.getStyleClass().add("tk-review-card");

        final int[] selected = new int[] { Math.max(1, recensione.getStelle()) };
        HBox starsBox = new HBox(6);
        for (int i = 1; i <= 5; i++) {
            Button starBtn = new Button(i <= selected[0] ? "★" : "☆");
            starBtn.getStyleClass().add("tk-stars");
            final int val = i;
            starBtn.setOnAction(ev -> {
                selected[0] = val;
                for (Node n : starsBox.getChildren()) {
                    if (n instanceof Button) {
                        Button b = (Button) n;
                        int idx = starsBox.getChildren().indexOf(b) + 1;
                        b.setText(idx <= selected[0] ? "★" : "☆");
                    }
                }
            });
            starsBox.getChildren().add(starBtn);
        }

        TextArea txt = new TextArea(recensione.getTesto());
        txt.setWrapText(true);
        Button btnSave = new Button("Save");
        btnSave.getStyleClass().add("tk-btn-primary");
        Button btnCancel = new Button("Cancel");
        btnCancel.getStyleClass().add("tk-btn-secondary");

        HBox actions = new HBox(8, btnSave, btnCancel);
        actions.setAlignment(javafx.geometry.Pos.BOTTOM_RIGHT);
        editBox.getChildren().addAll(starsBox, txt, actions);

        int col = -1, row = -1;
        if (gridRecensioni != null && gridRecensioni.getChildren().contains(card)) {
            col = GridPane.getColumnIndex(card);
            row = GridPane.getRowIndex(card);
            gridRecensioni.getChildren().remove(card);
            editBox.setMaxWidth(Double.MAX_VALUE);
            GridPane.setHgrow(editBox, Priority.ALWAYS);
            gridRecensioni.add(editBox, col, row);
        }

        final int fCol = col;
        final int fRow = row;

        btnCancel.setOnAction(e -> {
            if (fCol >= 0 && gridRecensioni != null) {
                Platform.runLater(() -> {
                    gridRecensioni.getChildren().remove(editBox);
                    gridRecensioni.add(card, fCol, fRow);
                });
            }
        });

        btnSave.setOnAction(e -> {
            recensione.setStelle(selected[0]);
            recensione.setTesto(txt.getText());
            Platform.runLater(() -> loadRecensioni());

            new Thread(() -> {
                try {
                    Request req = new Request("MODIFICA_RECENSIONE");
                    req.addParametro("idRecensione", recensione.getId());
                    req.addParametro("stelle", selected[0]);
                    req.addParametro("testo", txt.getText());
                    Response res = ServerConnection.getInstance().send(req);
                } catch (IOException | ClassNotFoundException ex) {
                }
            }).start();
        });
    }

    private void confirmDelete(VBox card, int idRecensione) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this review?",
                ButtonType.YES, ButtonType.NO);
        alert.setTitle("Confirm Deletion");
        alert.showAndWait().ifPresent(b -> {
            if (b == ButtonType.YES) {
                new Thread(() -> {
                    try {
                        Request req = new Request("ELIMINA_RECENSIONE");
                        req.addParametro("idRecensione", idRecensione);
                        Response res = ServerConnection.getInstance().send(req);
                        if (res.isSuccesso()) {
                            Platform.runLater(() -> {
                                loadRecensioni();
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
        });
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
