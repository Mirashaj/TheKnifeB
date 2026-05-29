package theknife.client.controller;

import java.util.List;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import theknife.client.ClientTK;
import theknife.client.ServerConnection;
import theknife.client.SessioneCorrente;
import theknife.model.Recensione;
import theknife.shared.Request;
import theknife.shared.Response;

/**
 * GestioneRecensioniController: Controller JavaFX per la schermata
 * GestioneRecensioni. Gestisce gli eventi dell'interfaccia grafica e comunica
 * con il server tramite ServerConnection.
 * 
 * @author Mirashaj Erik 760453 VA
 * @author GorchynskYi Igor 757184 VA
 * @author Kabuka Dan Mumanga 757708 VA
 * @author Mujeci Lorenzo 757597 VA
 */
public class GestioneRecensioniController {

    @FXML
    private Label lblTitolo;
    @FXML
    private Label lblSottotitolo;
    @FXML
    private Label lblEmpty;
    @FXML
    private Label lblError;
    @FXML
    private VBox vboxRecensioni;
    @FXML
    private MenuButton accountMenuButton;
    @FXML
    private Button btnNavHome;
    @FXML
    private Button btnNavRestaurants;
    @FXML
    private Button btnNavReviews;
    @FXML
    private Button btnNavBookings;

    @FXML
    public void initialize() {
        if (!SessioneCorrente.getInstance().isUserLogged() || !SessioneCorrente.getInstance().isGestore()) {
            ClientTK.loadScene("welcome.fxml", "TheKnife - Welcome");
            return;
        }

        configureNavbar();
        updateAccountMenu();

        if (lblTitolo != null) {
            lblTitolo.setText("Restaurant reviews");
        }
        if (lblSottotitolo != null) {
            lblSottotitolo.setText(
                    "Reply to reviews left on your restaurants and modify your response when needed.");
        }

        caricaRecensioni();
        javafx.application.Platform.runLater(() -> {
            javafx.scene.Node n = vboxRecensioni;
            while (n != null && !(n instanceof ScrollPane)) {
                n = n.getParent();
            }
            if (n instanceof ScrollPane) {
                ((ScrollPane) n).setFitToWidth(true);
            }
        });
    }

    private void caricaRecensioni() {
        if (lblError != null) {
            lblError.setVisible(false);
            lblError.setManaged(false);
        }

        new Thread(() -> {
            try {
                Request req = new Request("RECENSIONI_GESTORE");
                req.addParametro("idGestore", SessioneCorrente.getInstance().getUtenteLoggato().getId());
                Response res = ServerConnection.getInstance().send(req);
                if (res.isSuccesso() && vboxRecensioni != null) {
                    @SuppressWarnings("unchecked")
                    List<Recensione> recensioni = (List<Recensione>) res.getPayload();
                    Platform.runLater(() -> popolaUI(recensioni));
                } else if (lblError != null) {
                    Platform.runLater(() -> {
                        lblError.setText(res.getMessaggio());
                        lblError.setVisible(true);
                        lblError.setManaged(true);
                    });
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    if (lblError != null) {
                        lblError.setText("error connecting to the server");
                        lblError.setVisible(true);
                        lblError.setManaged(true);
                    }
                });
            }
        }).start();
    }

    private void popolaUI(List<Recensione> recensioni) {
        vboxRecensioni.getChildren().clear();
        if (recensioni == null) {
            recensioni = List.of();
        }
        boolean empty = recensioni.isEmpty();

        if (lblEmpty != null) {
            lblEmpty.setVisible(empty);
            lblEmpty.setManaged(empty);
        }

        if (empty) {
            return;
        }

        for (Recensione recensione : recensioni) {
            vboxRecensioni.getChildren().add(createReviewCard(recensione));
        }
    }

    private VBox createReviewCard(Recensione recensione) {
        VBox card = new VBox(12);
        card.getStyleClass().addAll("tk-owner-review-card");

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        StackPane avatar = new StackPane(new Label(initials(recensione.getNomeUtente())));
        avatar.getStyleClass().add("tk-result-thumb");
        avatar.setPrefSize(42, 42);
        avatar.setMinSize(42, 42);
        avatar.setMaxSize(42, 42);

        VBox meta = new VBox(2);
        Label restaurant = new Label(recensione.getNomeRistorante());
        restaurant.getStyleClass().add("tk-card-title");
        Label user = new Label(recensione.getNomeUtente());
        user.getStyleClass().add("tk-text-secondary");
        meta.getChildren().addAll(restaurant, user);

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        Label rating = new Label(stars(recensione.getStelle()));
        rating.getStyleClass().add("tk-stars");
        header.getChildren().addAll(avatar, meta, spacer, rating);

        Label date = new Label(recensione.getDataInserimento() != null
                ? recensione.getDataInserimento().format(java.time.format.DateTimeFormatter.ofPattern("d MMM yyyy"))
                : "");
        date.getStyleClass().add("tk-text-secondary");

        Label text = new Label(recensione.getTesto());
        text.setWrapText(true);
        text.getStyleClass().add("tk-text-secondary");

        card.getChildren().addAll(header, date, text);

        Region vSpacer = new Region();
        vSpacer.setPickOnBounds(false);
        VBox.setVgrow(vSpacer, Priority.ALWAYS);
        card.getChildren().add(vSpacer);

        boolean hasReply = recensione.getRisposta() != null && recensione.getRisposta().getTesto() != null
                && !recensione.getRisposta().getTesto().isBlank();

        VBox replyPreview = new VBox(4);
        replyPreview.getStyleClass().add("tk-review-reply");
        if (hasReply) {
            Label replyTitle = new Label("Your response");
            replyTitle.getStyleClass().add("tk-card-title");
            Label replyText = new Label(recensione.getRisposta().getTesto());
            replyText.setWrapText(true);
            replyText.getStyleClass().add("tk-text-secondary");
            replyPreview.getChildren().addAll(replyTitle, replyText);
            card.getChildren().add(replyPreview);
        }

        HBox actions = new HBox(8);
        actions.setAlignment(Pos.BOTTOM_RIGHT);
        Button btnModifica = new Button(hasReply ? "Modify response" : "Reply");
        btnModifica.getStyleClass().add(hasReply ? "tk-btn-secondary" : "tk-btn-primary");
        actions.getChildren().add(btnModifica);

        Button btnElimina = new Button("Delete");
        btnElimina.getStyleClass().add("tk-btn-primary");
        if (hasReply) {
            actions.getChildren().add(btnElimina);
        }

        card.getChildren().add(actions);

        btnModifica.setOnAction(e -> {
            openReplyEditor(card, recensione, replyPreview, actions, hasReply);
        });

        btnElimina.setOnAction(e -> {
            confirmDeleteReply(card, recensione, replyPreview, actions);
        });

        return card;
    }

    private void openReplyEditor(VBox card, Recensione recensione, VBox replyPreview, HBox actions, boolean hasReply) {
        VBox editor = new VBox(8);
        TextArea txtRisposta = new TextArea(hasReply ? recensione.getRisposta().getTesto() : "");
        txtRisposta.setPromptText("Write a response...");
        txtRisposta.setWrapText(true);
        txtRisposta.setPrefRowCount(3);
        txtRisposta.getStyleClass().add("tk-input");

        HBox editorActions = new HBox(8);
        Button btnSalva = new Button("Save");
        btnSalva.getStyleClass().add("tk-btn-primary");
        Button btnAnnulla = new Button("Cancel");
        btnAnnulla.getStyleClass().add("tk-btn-secondary");

        editorActions.setAlignment(Pos.BOTTOM_RIGHT);
        editorActions.getChildren().addAll(btnSalva, btnAnnulla);
        editor.getChildren().addAll(txtRisposta, editorActions);

        int indexActions = card.getChildren().indexOf(actions);
        if (hasReply) {
            int indexPreview = card.getChildren().indexOf(replyPreview);
            card.getChildren().set(indexPreview, editor);
            card.getChildren().remove(actions);
        } else {
            card.getChildren().set(indexActions, editor);
        }

        btnAnnulla.setOnAction(e -> {
            int idx = card.getChildren().indexOf(editor);
            if (hasReply) {
                card.getChildren().set(idx, replyPreview);
                card.getChildren().add(idx + 1, actions);
            } else {
                card.getChildren().set(idx, actions);
            }
        });

        btnSalva.setOnAction(e -> {
            String newText = txtRisposta.getText().trim();
            if (newText.isEmpty()) {
                return;
            }
            new Thread(() -> {
                try {
                    Request req = new Request("RISPONDI_RECENSIONE");
                    req.addParametro("idRecensione", recensione.getId());
                    req.addParametro("idGestore", SessioneCorrente.getInstance().getUtenteLoggato().getId());
                    req.addParametro("testo", newText);

                    Response res = ServerConnection.getInstance().send(req);
                    if (res.isSuccesso()) {
                        Platform.runLater(() -> caricaRecensioni());
                    } else {
                        Platform.runLater(() -> {
                            if (lblError != null) {
                                lblError.setText(res.getMessaggio());
                                lblError.setVisible(true);
                                lblError.setManaged(true);
                            }
                        });
                    }
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        if (lblError != null) {
                            lblError.setText("Errore di connessione");
                            lblError.setVisible(true);
                            lblError.setManaged(true);
                        }
                    });
                }
            }).start();
        });
    }

    private void confirmDeleteReply(VBox card, Recensione recensione, VBox replyPreview, HBox actions) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Sei sicuro di voler eliminare questa risposta?",
                javafx.scene.control.ButtonType.YES, javafx.scene.control.ButtonType.NO);
        alert.setTitle("Conferma eliminazione");
        alert.showAndWait().ifPresent(b -> {
            if (b == javafx.scene.control.ButtonType.YES) {
                new Thread(() -> {
                    try {
                        Request req = new Request("ELIMINA_RISPOSTA");
                        req.addParametro("idRisposta", recensione.getRisposta().getId());
                        Response res = ServerConnection.getInstance().send(req);
                        if (res.isSuccesso()) {
                            Platform.runLater(() -> caricaRecensioni());
                        } else {
                            Platform.runLater(() -> {
                                if (lblError != null) {
                                    lblError.setText(res.getMessaggio());
                                    lblError.setVisible(true);
                                    lblError.setManaged(true);
                                }
                            });
                        }
                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            if (lblError != null) {
                                lblError.setText("Errore di connessione");
                                lblError.setVisible(true);
                                lblError.setManaged(true);
                            }
                        });
                    }
                }).start();
            }
        });
    }

    private String initials(String name) {
        if (name == null || name.isBlank()) {
            return "U";
        }
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }

    private String stars(int value) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            builder.append(i < value ? '★' : '☆');
        }
        return builder.toString();
    }

    private void configureNavbar() {
        if (btnNavHome != null) {
            btnNavHome.setText("Home");
            btnNavHome.getStyleClass().setAll("tk-nav-item");
        }
        if (btnNavRestaurants != null) {
            btnNavRestaurants.setText("Restaurants");
            btnNavRestaurants.getStyleClass().setAll("tk-nav-item");
        }
        if (btnNavReviews != null) {
            btnNavReviews.setText("Review");
            btnNavReviews.getStyleClass().setAll("tk-nav-active");
        }
        if (btnNavBookings != null) {
            btnNavBookings.setText("Bookings");
            btnNavBookings.getStyleClass().setAll("tk-nav-item");
        }
    }

    private void updateAccountMenu() {
        if (accountMenuButton == null) {
            return;
        }

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
    private void handleIndietro() {
        ClientTK.loadScene("dashboard_gestore.fxml", "TheKnife - Dashboard");
    }

    @FXML
    private void handleHome() {
        ClientTK.loadScene("home.fxml", "TheKnife - Home");
    }

    @FXML
    private void handleRestaurants() {
        ClientTK.loadScene("dashboard_gestore.fxml", "TheKnife - Dashboard");
    }

    @FXML
    private void handleReviews() {
        ClientTK.loadScene("gestione_recensioni.fxml", "TheKnife - Review Management");
    }

    @FXML
    private void handleBookings() {
        ClientTK.loadScene("prenotazioni.fxml", "TheKnife - Bookings");
    }
}
