package theknife.client.controller;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

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
import theknife.model.Prenotazione;
import theknife.model.Ristorante;
import theknife.shared.Request;
import theknife.shared.Response;

/**
 * PrenotazioniController: Controller JavaFX per la schermata Prenotazioni.
 * Gestisce gli eventi dell'interfaccia grafica e comunica con il server tramite
 * ServerConnection.
 * 
 * @author Mirashaj Erik 760453 VA
 * @author GorchynskYi Igor 757184 VA
 * @author Kabuka Dan Mumanga 757708 VA
 * @author Mujeci Lorenzo 757597 VA
 */
public class PrenotazioniController {

    @FXML
    private AnchorPane loginGatePanel;
    @FXML
    private AnchorPane contentPanel;
    @FXML
    private Button btnAccedi;
    @FXML
    private Button btnRegistrati;
    @FXML
    private TilePane tilePrenotazioni;
    @FXML
    private VBox emptyStateBox;
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

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM uuuu HH:mm", Locale.ITALIAN);

    private GridPane gridPrenotazioni;

    private String stars(double mediaStelle) {
        int value = (int) Math.round(mediaStelle);
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            b.append(i < value ? '★' : '☆');
        }
        return b.toString();
    }

    @FXML
    public void initialize() {
        updateAccountMenu();

        configureNavbar();

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

        if (tilePrenotazioni != null && tilePrenotazioni.getParent() instanceof Pane parent) {
            gridPrenotazioni = new GridPane();
            gridPrenotazioni.setHgap(12);
            gridPrenotazioni.setVgap(12);
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(25);
            gridPrenotazioni.getColumnConstraints().addAll(cc, cc, cc, cc);

            int index = parent.getChildren().indexOf(tilePrenotazioni);
            parent.getChildren().set(index, gridPrenotazioni);

            javafx.application.Platform.runLater(() -> {
                javafx.scene.Node n = gridPrenotazioni;
                while (n != null && !(n instanceof ScrollPane)) {
                    n = n.getParent();
                }
                if (n instanceof ScrollPane) {
                    ((ScrollPane) n).setFitToWidth(true);
                }
            });
        }

        loadPrenotazioni();
    }

    private void configureNavbar() {
        boolean gestore = SessioneCorrente.getInstance().isGestore();

        if (btnNavHome != null) {
            btnNavHome.setText("Home");
            btnNavHome.getStyleClass().setAll("tk-nav-item");
            btnNavHome.setOnAction(e -> handleHome());
        }

        if (btnNavSecondary != null) {
            btnNavSecondary.setText(gestore ? "Restaurants" : "Favorites");
            btnNavSecondary.getStyleClass().setAll("tk-nav-item");
            btnNavSecondary.setOnAction(e -> handlePreferiti());
        }
        if (btnNavThird != null) {
            btnNavThird.setText(gestore ? "Review" : "Reviews");
            btnNavThird.getStyleClass().setAll("tk-nav-item");
            btnNavThird.setOnAction(e -> handleMieRecensioni());
        }
        if (btnNavFourth != null) {
            btnNavFourth.setVisible(true);
            btnNavFourth.setManaged(true);
            btnNavFourth.setText("Bookings");
            btnNavFourth.getStyleClass().setAll("tk-nav-active");
            btnNavFourth.setOnAction(e -> handlePrenotazioni());
        }
    }

    private void loadPrenotazioni() {
        if (lblError != null)
            lblError.setVisible(false);
        new Thread(() -> {
            try {
                Request req;
                if (SessioneCorrente.getInstance().isGestore()) {
                    req = new Request("PRENOTAZIONI_GESTORE");
                    req.addParametro("idGestore", SessioneCorrente.getInstance().getUtenteLoggato().getId());
                } else {
                    req = new Request("VISUALIZZA_PRENOTAZIONI");
                    req.addParametro("idUtente", SessioneCorrente.getInstance().getUtenteLoggato().getId());
                }
                Response res = ServerConnection.getInstance().send(req);
                if (res.isSuccesso()) {
                    @SuppressWarnings("unchecked")
                    List<Prenotazione> list = (List<Prenotazione>) res.getPayload();
                    Platform.runLater(() -> {
                        if (gridPrenotazioni != null)
                            gridPrenotazioni.getChildren().clear();
                        if (list == null || list.isEmpty()) {
                            if (emptyStateBox != null) {
                                emptyStateBox.setVisible(true);
                                emptyStateBox.setManaged(true);
                            }
                        } else {
                            if (emptyStateBox != null) {
                                emptyStateBox.setVisible(false);
                                emptyStateBox.setManaged(false);
                            }
                            if (gridPrenotazioni != null) {
                                for (int i = 0; i < list.size(); i++) {
                                    VBox card = createBookingCard(list.get(i));
                                    card.setMaxWidth(Double.MAX_VALUE);
                                    GridPane.setHgrow(card, Priority.ALWAYS);
                                    gridPrenotazioni.add(card, i % 4, i / 4);
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
                    if (lblError != null) {
                        lblError.setText("Errore di connessione");
                        lblError.setVisible(true);
                        lblError.setManaged(true);
                    }
                });
            }
        }).start();
    }

    private VBox createBookingCard(Prenotazione p) {
        VBox card = new VBox(10);
        card.getStyleClass().addAll("tk-booking-card");

        HBox header = new HBox(10);
        VBox titleBox = new VBox(2);
        Label name = new Label(p.getNomeRistorante());
        name.getStyleClass().add("tk-card-title");
        name.setOnMouseClicked(e -> {
            theknife.model.Ristorante r = new theknife.model.Ristorante();
            r.setId(p.getIdRistorante());
            r.setNome(p.getNomeRistorante());
            SessioneCorrente.getInstance().setSelectedRistorante(r);
            ClientTK.loadScene("dettaglio_ristorante.fxml", "TheKnife - Dettaglio Ristorante");
        });

        Label date = new Label(p.getDataPrenotazione() != null ? p.getDataPrenotazione().format(formatter) : "");
        date.getStyleClass().add("tk-text-secondary");
        titleBox.getChildren().addAll(name, date);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(titleBox, spacer);

        Label seats = new Label("Posti: " + p.getPosti());
        seats.getStyleClass().add("tk-text-secondary");

        Label rating = new Label("...");
        rating.getStyleClass().add("tk-stars");
        loadRatingForCard(p.getIdRistorante(), rating);

        String statoOriginale = p.getStato() != null ? p.getStato().toUpperCase() : "ON HOLD";
        String displayStatus;
        if (statoOriginale.equals("ACCEPTED") || statoOriginale.equals("CONFERMATA")
                || "ACCETTATA".equals(statoOriginale)) {
            displayStatus = "ACCEPTED";
        } else if (statoOriginale.equals("DECLINED") || statoOriginale.equals("CANCELLED")
                || "RIFIUTATA".equals(statoOriginale)) {
            displayStatus = "DECLINED";
        } else {
            displayStatus = "ON HOLD";
        }
        Label status = new Label("State: " + displayStatus);
        status.getStyleClass().add("tk-text-secondary");
        if ("ACCEPTED".equals(displayStatus)) {
            status.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
        } else if ("DECLINED".equals(displayStatus)) {
            status.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        } else {
            status.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
        }

        HBox actions = new HBox(8);
        actions.setAlignment(javafx.geometry.Pos.BOTTOM_RIGHT);

        if (SessioneCorrente.getInstance().isGestore()) {
            Button btnAccept = new Button("Accept");
            btnAccept.getStyleClass().add("tk-btn-primary");
            btnAccept.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");

            Button btnDecline = new Button("Decline");
            btnDecline.getStyleClass().add("tk-btn-primary");
            btnDecline.setStyle("-fx-background-color: #C0392B; -fx-text-fill: white;");

            boolean isPending = (p.getStato() == null || "ON HOLD".equalsIgnoreCase(p.getStato())
                    || "IN ATTESA".equalsIgnoreCase(p.getStato()));

            if (!isPending) {
                btnAccept.setDisable(true);
                btnDecline.setDisable(true);
            }

            btnAccept.setOnAction(e -> updateBookingStatus(p, "ACCEPTED"));
            btnDecline.setOnAction(e -> updateBookingStatus(p, "DECLINED"));

            actions.getChildren().addAll(btnAccept, btnDecline);
        } else {
            Button btnEdit = new Button("Edit");
            btnEdit.getStyleClass().add("tk-btn-secondary");

            Button btnCancel = new Button("Delete");
            btnCancel.getStyleClass().add("tk-btn-primary");
            btnCancel.setStyle("-fx-background-color: #C0392B; -fx-text-fill: white;");

            actions.getChildren().addAll(btnEdit, btnCancel);

            btnCancel.setOnAction(e -> cancelBooking(p.getId(), card));
            btnEdit.setOnAction(e -> showEditBooking(card, p, seats, date, actions));
        }

        Region vSpacer = new Region();
        vSpacer.setPickOnBounds(false);
        VBox.setVgrow(vSpacer, Priority.ALWAYS);

        card.getChildren().addAll(header, seats, rating, status, vSpacer, actions);
        return card;
    }

    private void loadRatingForCard(int idRistorante, Label ratingLabel) {
        new Thread(() -> {
            try {
                Request req = new Request("DETTAGLIO_RISTORANTE");
                req.addParametro("idRistorante", idRistorante);
                Response res = ServerConnection.getInstance().send(req);
                if (res.isSuccesso()) {
                    Ristorante dettagli = (Ristorante) res.getPayload();
                    if (dettagli != null && dettagli.getNumRecensioni() > 0) {
                        Platform.runLater(() -> ratingLabel.setText(stars(dettagli.getMediaStelle())));
                    } else {
                        Platform.runLater(() -> ratingLabel.setText("No reviews"));
                    }
                } else {
                    Platform.runLater(() -> ratingLabel.setText(""));
                }
            } catch (Exception e) {
                Platform.runLater(() -> ratingLabel.setText(""));
            }
        }).start();
    }

    private void showEditBooking(VBox card, Prenotazione p, Label seatsLabel, Label dateLabel, HBox actions) {
        card.getChildren().removeIf(n -> "edit-box".equals(n.getUserData()));

        actions.setVisible(false);
        actions.setManaged(false);

        VBox editor = new VBox(8);
        editor.setUserData("edit-box");
        editor.setStyle("-fx-padding: 8 0 0 0;");

        javafx.scene.control.DatePicker datePicker = new javafx.scene.control.DatePicker();
        datePicker.setPrefWidth(140);
        if (p.getDataPrenotazione() != null) {
            datePicker.setValue(p.getDataPrenotazione().toLocalDate());
        }

        javafx.scene.control.TextField timeField = new javafx.scene.control.TextField();
        timeField.setPromptText("HH:mm");
        timeField.setPrefWidth(80);
        if (p.getDataPrenotazione() != null) {
            timeField.setText(
                    String.format("%02d:%02d", p.getDataPrenotazione().getHour(), p.getDataPrenotazione().getMinute()));
        }

        HBox dateBox = new HBox(6, new Label("Data:"), datePicker);
        dateBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        javafx.scene.control.TextField seatsField = new javafx.scene.control.TextField(String.valueOf(p.getPosti()));
        seatsField.setPrefWidth(80);
        seatsField.setPromptText("Posti");

        HBox timeAndSeatsBox = new HBox(10, new HBox(6, new Label("Ora:"), timeField),
                new HBox(6, new Label("Posti:"), seatsField));
        timeAndSeatsBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Button btnSave = new Button("Salva");
        btnSave.getStyleClass().add("tk-btn-primary");

        Button btnCancelEdit = new Button("Cancel");
        btnCancelEdit.getStyleClass().add("tk-btn-secondary");

        HBox buttonBox = new HBox(8, btnCancelEdit, btnSave);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        editor.getChildren().addAll(dateBox, timeAndSeatsBox, buttonBox);

        btnCancelEdit.setOnAction(ev -> {
            card.getChildren().remove(editor);
            actions.setVisible(true);
            actions.setManaged(true);
        });

        btnSave.setOnAction(ev -> {
            if (datePicker.getValue() == null) {
                showError("Seleziona una data.");
                return;
            }

            String time = timeField.getText() != null ? timeField.getText().trim() : "";
            java.time.LocalTime localTime;
            try {
                localTime = java.time.LocalTime.parse(time);
            } catch (Exception ex) {
                showError("Formato ora non valido (HH:mm).");
                return;
            }

            int newSeats;
            try {
                newSeats = Integer.parseInt(seatsField.getText().trim());
            } catch (Exception ex) {
                showError("Posti non validi.");
                return;
            }
            if (newSeats < 1) {
                showError("Posti devono essere >= 1.");
                return;
            }

            java.time.LocalDateTime newDate = java.time.LocalDateTime.of(datePicker.getValue(), localTime);
            if (newDate.isBefore(java.time.LocalDateTime.now())) {
                showError("La data e l'ora devono essere future.");
                return;
            }

            Request req = new Request("MODIFICA_PRENOTAZIONE");
            req.addParametro("idUtente", SessioneCorrente.getInstance().getUtenteLoggato().getId());
            req.addParametro("idPrenotazione", p.getId());
            req.addParametro("dataPrenotazione", newDate);
            req.addParametro("posti", newSeats);
            req.addParametro("stato", p.getStato() != null ? p.getStato() : "CONFERMATA");

            new Thread(() -> {
                try {
                    Response res = ServerConnection.getInstance().send(req);
                    if (res.isSuccesso()) {
                        Platform.runLater(() -> {
                            card.getChildren().remove(editor);
                            seatsLabel.setText("Posti: " + newSeats);
                            dateLabel.setText(newDate.format(formatter));
                            actions.setVisible(true);
                            actions.setManaged(true);
                        });
                    } else {
                        Platform.runLater(() -> showError(res.getMessaggio()));
                    }
                } catch (IOException | ClassNotFoundException ex) {
                    Platform.runLater(() -> showError("Errore di connessione"));
                }
            }).start();
        });

        card.getChildren().add(editor);
    }

    private void showError(String message) {
        if (lblError != null) {
            lblError.setText(message);
            lblError.setVisible(true);
            lblError.setManaged(true);
        }
    }

    private void cancelBooking(int idPrenotazione, VBox card) {
        new Thread(() -> {
            try {
                Request req = new Request("ELIMINA_PRENOTAZIONE");
                req.addParametro("idPrenotazione", idPrenotazione);
                Response res = ServerConnection.getInstance().send(req);
                if (res.isSuccesso()) {
                    Platform.runLater(this::loadPrenotazioni);
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

    private void updateBookingStatus(Prenotazione p, String nuovoStato) {
        new Thread(() -> {
            try {
                Request req = new Request("MODIFICA_PRENOTAZIONE");
                req.addParametro("idUtente", p.getIdUtente());
                req.addParametro("idPrenotazione", p.getId());
                req.addParametro("dataPrenotazione", p.getDataPrenotazione());
                req.addParametro("posti", p.getPosti());
                req.addParametro("stato", nuovoStato);

                Response res = ServerConnection.getInstance().send(req);
                if (res.isSuccesso()) {
                    Platform.runLater(this::loadPrenotazioni);
                } else {
                    Platform.runLater(() -> {
                        lblError.setText(res.getMessaggio());
                        lblError.setVisible(true);
                        lblError.setManaged(true);
                    });
                }
            } catch (IOException | ClassNotFoundException e) {
                Platform.runLater(() -> {
                    lblError.setText("errore di connessione");
                    lblError.setVisible(true);
                    lblError.setManaged(true);
                });
            }
        }).start();
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
        icon.setMinSize(18, 18);
        icon.setMaxSize(18, 18);
        return icon;
    }

    @FXML
    private void handleHome() {
        ClientTK.loadScene("home.fxml", "TheKnife - Home");
    }

    @FXML
    private void handlePreferiti() {
        if (SessioneCorrente.getInstance().isGestore()) {
            ClientTK.loadScene("dashboard_gestore.fxml", "TheKnife - Dashboard");
            return;
        }
        ClientTK.loadScene("preferiti.fxml", "TheKnife - Favorites");
    }

    @FXML
    private void handleMieRecensioni() {
        if (SessioneCorrente.getInstance().isGestore()) {
            ClientTK.loadScene("gestione_recensioni.fxml", "TheKnife - Review Management");
            return;
        }
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
