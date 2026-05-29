package theknife.client.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import theknife.client.ClientTK;
import theknife.client.ServerConnection;
import theknife.client.SessioneCorrente;
import theknife.model.Recensione;
import theknife.model.Ristorante;
import theknife.shared.Request;
import theknife.shared.Response;

@SuppressWarnings("unused")
/**
 * DettaglioRistoranteController: Controller JavaFX per la schermata
 * DettaglioRistorante. Gestisce gli eventi dell'interfaccia grafica e comunica
 * con il server tramite ServerConnection.
 * 
 * @author Mirashaj Erik 760453 VA
 * @author GorchynskYi Igor 757184 VA
 * @author Kabuka Dan Mumanga 757708 VA
 * @author Mujeci Lorenzo 757597 VA
 */
public class DettaglioRistoranteController {

    @FXML
    private Label lblNome, lblCitta, lblIndirizzo, lblPrezzo, lblCucina, lblDelivery, lblPrenotazione, lblAvgRating,
            lblReviewCount, lblCoordinate, lblMessaggio, lblVotoSelezionato;
    @FXML
    private Label lblAvgRatingTop, lblAvgStarsTop;
    @FXML
    private Label lblCucinaInfo, lblPrezzoInfo, lblDeliveryInfo, lblPrenotazioneInfo, lblDescrizioneBrief;
    @FXML
    private Label lblIndirizzoInfo, lblCittaInfo, lblCucinaInfo2, lblPrezzoInfo2, lblLoginPrompt, lblNazioneInfo;
    @FXML
    private VBox vboxRecensioni;
    @FXML
    private VBox vboxFormRecensione;
    @FXML
    private Button btnPreferito;
    @FXML
    private Button btnPrenota;
    @FXML
    private Button star1, star2, star3, star4, star5;
    @FXML
    private HBox starsContainer;
    @FXML
    private TextArea txtTestoRecensione;
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

    private int selectedRating = 5;
    private boolean isPreferito = false;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");

    @FXML
    public void initialize() {
        initializeStarButtons();

        Ristorante r = SessioneCorrente.getInstance().getSelectedRistorante();
        if (r != null) {
            applyRestaurantToUI(r);

            loadFullRestaurantDetails(r.getId());
            loadReviews(r.getId());
            if (SessioneCorrente.getInstance().isUserLogged() && !SessioneCorrente.getInstance().isGestore()) {
                checkPreferito(r.getId());
            }
        }
        boolean customerActionsVisible = SessioneCorrente.getInstance().isUserLogged()
                && !SessioneCorrente.getInstance().isGestore();

        // Mostra form solo se loggato
        if (vboxFormRecensione != null && lblLoginPrompt != null) {
            vboxFormRecensione.setVisible(customerActionsVisible);
            vboxFormRecensione.setManaged(customerActionsVisible);
            lblLoginPrompt.setVisible(!customerActionsVisible);
            lblLoginPrompt.setManaged(!customerActionsVisible);
            if (!customerActionsVisible) {
                lblLoginPrompt.setText("Log in to leave a review");
            }
        }

        if (btnPrenota != null) {
            btnPrenota.setVisible(customerActionsVisible);
            btnPrenota.setManaged(customerActionsVisible);
        }

        if (btnPreferito != null) {
            btnPreferito.setVisible(customerActionsVisible);
            btnPreferito.setManaged(customerActionsVisible);
            btnPreferito.setDisable(!customerActionsVisible);
            btnPreferito.setText("Add to favorites");
            btnPreferito.setOnAction(e -> handlePreferito());
        }

        updateAccountMenu();
        configureNavbar();
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

    private void initializeStarButtons() {
        if (star1 == null || star2 == null || star3 == null || star4 == null || star5 == null) {
            return;
        }

        Button[] stars = { star1, star2, star3, star4, star5 };
        updateStarDisplay(selectedRating);

        for (int i = 0; i < stars.length; i++) {
            final int starIndex = i + 1;
            stars[i].setOnAction(e -> {
                selectedRating = starIndex;
                updateStarDisplay(starIndex);
            });
        }
    }

    private void updateStarDisplay(int rating) {
        if (star1 == null || star2 == null || star3 == null || star4 == null || star5 == null) {
            return;
        }

        Button[] stars = { star1, star2, star3, star4, star5 };
        for (int i = 0; i < stars.length; i++) {
            stars[i].setText(i < rating ? "★" : "☆");
        }

        if (lblVotoSelezionato != null) {
            lblVotoSelezionato.setText(String.valueOf(rating));
        }
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

    private void configureNavbar() {
        boolean gestore = SessioneCorrente.getInstance().isGestore();

        if (btnNavHome != null) {
            btnNavHome.getStyleClass().setAll("tk-nav-item");
        }

        if (btnNavSecondary != null) {
            btnNavSecondary.setText(gestore ? "Restaurants" : "Favorites");
            btnNavSecondary.getStyleClass().setAll("tk-nav-item");
        }

        if (btnNavThird != null) {
            btnNavThird.setText(gestore ? "Review" : "Reviews");
            btnNavThird.getStyleClass().setAll("tk-nav-item");
        }

        if (btnNavFourth != null) {
            btnNavFourth.setVisible(true);
            btnNavFourth.setManaged(true);
            btnNavFourth.setText("Bookings");
            btnNavFourth.getStyleClass().setAll("tk-nav-item");
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

    private void checkPreferito(int idRistorante) {
        if (!SessioneCorrente.getInstance().isUserLogged() || SessioneCorrente.getInstance().isGestore())
            return;
        new Thread(() -> {
            try {
                Request req = new Request("VERIFICA_PREFERITO");
                req.addParametro("idUtente", SessioneCorrente.getInstance().getUtenteLoggato().getId());
                req.addParametro("idRistorante", idRistorante);
                Response res = ServerConnection.getInstance().send(req);
                if (res.isSuccesso()) {
                    isPreferito = (Boolean) res.getPayload();
                    Platform.runLater(this::updatePreferitoBtn);
                }
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Errore nel controllo dei preferiti: " + e.getMessage());
            }
        }).start();
    }

    private void updatePreferitoBtn() {
        if (btnPreferito != null) {
            btnPreferito.setText(isPreferito ? "Remove from favorites" : "Add to favorites");
            btnPreferito.setOnAction(e -> togglePreferito());
        }
    }

    private void loadFullRestaurantDetails(int idRistorante) {
        new Thread(() -> {
            try {
                Request req = new Request("DETTAGLIO_RISTORANTE");
                req.addParametro("idRistorante", idRistorante);
                Response res = ServerConnection.getInstance().send(req);
                if (res.isSuccesso()) {
                    Ristorante dettagli = (Ristorante) res.getPayload();
                    if (dettagli != null) {
                        SessioneCorrente.getInstance().setSelectedRistorante(dettagli);
                        Platform.runLater(() -> applyRestaurantToUI(dettagli));
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Errore nel caricamento dettagli ristorante: " + e.getMessage());
            }
        }).start();
    }

    private void applyRestaurantToUI(Ristorante r) {
        if (r == null)
            return;
        if (lblNome != null)
            lblNome.setText(safeText(r.getNome()));
        if (lblCitta != null)
            lblCitta.setText(formatLocation(r));
        if (lblIndirizzo != null)
            lblIndirizzo.setText(safeText(r.getIndirizzo()));

        // Prepara indirizzi per visualizzazione
        String indirizzoFull = safeText(r.getIndirizzo());
        String citta = safeText(r.getCitta());
        String nazione = safeText(r.getNazione());

        String streetOnly = indirizzoFull;
        if (!indirizzoFull.isEmpty() && !citta.isEmpty()) {
            String lowerAddr = indirizzoFull.toLowerCase();
            String lowerCity = citta.toLowerCase();
            int idx = lowerAddr.indexOf(lowerCity);
            if (idx > 0) {
                streetOnly = indirizzoFull.substring(0, idx).trim();
                // Rimuovi virgole finali
                while (streetOnly.endsWith(",") || streetOnly.endsWith(" ")) {
                    streetOnly = streetOnly.substring(0, streetOnly.length() - 1).trim();
                }
            }
        }

        if (lblIndirizzoInfo != null)
            lblIndirizzoInfo.setText(streetOnly.isEmpty() ? indirizzoFull : streetOnly);
        if (lblCittaInfo != null)
            lblCittaInfo.setText(citta);
        if (lblNazioneInfo != null)
            lblNazioneInfo.setText(nazione);

        if (lblCucina != null)
            lblCucina.setText(safeText(r.getTipoCucina()));
        if (lblCucinaInfo != null)
            lblCucinaInfo.setText(safeText(r.getTipoCucina()));
        if (lblCucinaInfo2 != null)
            lblCucinaInfo2.setText(safeText(r.getTipoCucina()));

        if (lblPrezzo != null)
            lblPrezzo.setText(formatPrice(r.getPrezzoMedio()));
        if (lblPrezzoInfo != null)
            lblPrezzoInfo.setText(formatPrice(r.getPrezzoMedio()));
        if (lblPrezzoInfo2 != null)
            lblPrezzoInfo2.setText(String.format("%.0f€", r.getPrezzoMedio()));

        if (lblCoordinate != null)
            lblCoordinate.setText(String.format("%.4f, %.4f", r.getLatitudine(), r.getLongitudine()));

        if (lblDescrizioneBrief != null)
            lblDescrizioneBrief.setText(safeText(r.getDescrizione()));

        // Mostra badge se disponibili
        if (lblDelivery != null) {
            if (r.isDelivery()) {
                lblDelivery.setVisible(true);
                lblDelivery.setManaged(true);
                lblDelivery.setText("Delivery available");
                lblDelivery.getStyleClass().setAll("tk-badge", "tk-positive");
            } else {
                lblDelivery.setVisible(false);
                lblDelivery.setManaged(false);
            }
        }
        if (lblPrenotazione != null) {
            if (r.isPrenotazione()) {
                lblPrenotazione.setVisible(true);
                lblPrenotazione.setManaged(true);
                lblPrenotazione.setText("Booking available");
                lblPrenotazione.getStyleClass().setAll("tk-badge", "tk-positive");
            } else {
                lblPrenotazione.setVisible(false);
                lblPrenotazione.setManaged(false);
            }
        }

        // Stile pulsante prenotazione
        if (btnPrenota != null) {
            if (r.isPrenotazione()) {
                btnPrenota.setText("Book Now");
                btnPrenota.setDisable(false);
                btnPrenota.setStyle("-fx-background-color: #C0392B; -fx-text-fill: white;");
            } else {
                btnPrenota.setText("Booking not available");
                btnPrenota.setDisable(true);
                btnPrenota.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: #555555;");
            }
        }

        // Info consegna
        if (lblDeliveryInfo != null) {
            lblDeliveryInfo.setText(r.isDelivery() ? "Delivery available" : "Delivery not available");
        }

        // Info prenotazione nella sezione Services
        if (lblPrenotazioneInfo != null) {
            lblPrenotazioneInfo.setText(r.isPrenotazione() ? "Booking available" : "Booking not available");
        }

        // Voto e numero recensioni
        if (lblAvgRating != null) {
            double avg = r.getMediaStelle();
            if (avg > 0)
                lblAvgRating.setText(String.format("%.1f / 5.0", avg));
        }
        if (lblReviewCount != null) {
            int num = r.getNumRecensioni();
            if (num > 0)
                lblReviewCount.setText(String.valueOf(num));
        }
        if (lblAvgRatingTop != null) {
            double avg = r.getMediaStelle();
            if (avg > 0)
                lblAvgRatingTop.setText(String.format("%.1f", avg));
            else
                lblAvgRatingTop.setText("");
        }
        if (lblAvgStarsTop != null) {
            double avg = r.getMediaStelle();
            lblAvgStarsTop.setText(stars((int) Math.round(avg)));
        }
    }

    private void loadReviews(int ristoranteId) {
        new Thread(() -> {
            try {
                Request req = new Request("VISUALIZZA_RECENSIONI");
                req.addParametro("idRistorante", ristoranteId);
                Response res = ServerConnection.getInstance().send(req);
                if (res.isSuccesso()) {
                    @SuppressWarnings("unchecked")
                    List<Recensione> recensioni = (List<Recensione>) res.getPayload();
                    Platform.runLater(() -> {
                        double avg = recensioni.stream().mapToInt(Recensione::getStelle).average().orElse(0.0);
                        lblAvgRating.setText(String.format("%.1f / 5.0", avg));
                        lblReviewCount.setText(String.valueOf(recensioni.size()));
                        if (lblAvgRatingTop != null) {
                            lblAvgRatingTop.setText(String.format("%.1f", avg));
                        }
                        if (lblAvgStarsTop != null) {
                            lblAvgStarsTop.setText(stars((int) Math.round(avg)));
                        }
                        if (vboxRecensioni != null) {
                            vboxRecensioni.getChildren()
                                    .setAll(recensioni.stream().map(this::createReviewCard).toList());
                        }
                    });
                } else {
                    Platform.runLater(() -> {
                        lblAvgRating.setText("No reviews");
                        lblReviewCount.setText("0");
                        if (vboxRecensioni != null) {
                            vboxRecensioni.getChildren().setAll(createEmptyReviewsState("No reviews yet",
                                    "Be the first to share your experience."));
                        }
                    });
                }
            } catch (IOException | ClassNotFoundException e) {
                Platform.runLater(() -> {
                    lblAvgRating.setText("Error");
                    if (vboxRecensioni != null) {
                        vboxRecensioni.getChildren().setAll(createEmptyReviewsState("Could not load reviews",
                                "Check the connection and try again."));
                    }
                });
            }
        }).start();
    }

    @FXML
    private void handleAnnullaRecensione() {
        if (vboxFormRecensione != null) {
            vboxFormRecensione.setVisible(false);
            vboxFormRecensione.setManaged(false);
        }
        if (lblMessaggio != null) {
            lblMessaggio.setText("Review cancelled.");
        }
        // Resetta stelle e testo
        selectedRating = 5;
        updateStarDisplay(5);
        if (txtTestoRecensione != null) {
            txtTestoRecensione.clear();
        }
    }

    @FXML
    private void handleInviaRecensione() {
        try {
            if (!SessioneCorrente.getInstance().isUserLogged()) {
                if (lblMessaggio != null)
                    lblMessaggio.setText("Please login to submit a review.");
                return;
            }
            Ristorante r = SessioneCorrente.getInstance().getSelectedRistorante();
            int stelle = selectedRating;
            String testo = txtTestoRecensione != null ? txtTestoRecensione.getText() : "";

            Request req = new Request("AGGIUNGI_RECENSIONE");
            req.addParametro("idUtente", SessioneCorrente.getInstance().getUtenteLoggato().getId());
            req.addParametro("idRistorante", r.getId());
            req.addParametro("stelle", stelle);
            req.addParametro("testo", testo);

            Response res = ServerConnection.getInstance().send(req);
            if (res.isSuccesso()) {
                if (vboxFormRecensione != null) {
                    vboxFormRecensione.setVisible(false);
                    vboxFormRecensione.setManaged(false);
                }
                if (txtTestoRecensione != null)
                    txtTestoRecensione.clear();
                loadReviews(r.getId());
                if (lblMessaggio != null)
                    lblMessaggio.setText("Review submitted.");
            }

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Errore nell'invio della recensione: " + e.getMessage());
        }
    }

    @FXML
    private void handlePreferito() {
        if (!SessioneCorrente.getInstance().isUserLogged() || SessioneCorrente.getInstance().isGestore()) {
            if (lblMessaggio != null) {
                lblMessaggio.setText("Login as customer to use favorites.");
            }
            return;
        }
        togglePreferito();
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
    private void handleAggiungiRecensione() {
        if (!SessioneCorrente.getInstance().isUserLogged()) {
            if (lblLoginPrompt != null) {
                lblLoginPrompt.setText("Log in to leave a review");
            }
            return;
        }
        if (vboxFormRecensione != null) {
            vboxFormRecensione.setVisible(true);
            vboxFormRecensione.setManaged(true);
        }
    }

    @FXML
    private void handlePrenotazioni() {
        ClientTK.loadScene("prenotazioni.fxml", "TheKnife - Bookings");
    }

    @FXML
    private void handlePrenota() {
        if (!SessioneCorrente.getInstance().isUserLogged()) {
            if (lblMessaggio != null)
                lblMessaggio.setText("Please login to make a booking.");
            return;
        }
        if (SessioneCorrente.getInstance().isGestore()) {
            if (lblMessaggio != null)
                lblMessaggio.setText("Gestores cannot make bookings.");
            return;
        }

        Ristorante r = SessioneCorrente.getInstance().getSelectedRistorante();
        if (r == null)
            return;
        if (!r.isPrenotazione()) {
            if (lblMessaggio != null)
                lblMessaggio.setText("Booking is not available for this restaurant.");
            return;
        }

        showBookingWindow(r);
    }

    private void showBookingWindow(Ristorante ristorante) {
        Stage dialog = new Stage(StageStyle.UNDECORATED);
        Window owner = btnPrenota != null && btnPrenota.getScene() != null ? btnPrenota.getScene().getWindow() : null;
        if (owner != null) {
            dialog.initOwner(owner);
        }
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle("New booking");
        dialog.setResizable(false);

        VBox card = new VBox(16);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setPrefWidth(500);
        card.getStyleClass().add("tk-card");
        card.setPadding(new Insets(20));

        Label title = new Label("Book " + safeText(ristorante.getNome()));
        title.getStyleClass().add("tk-section-title");
        title.setWrapText(true);

        Label subtitle = new Label("Enter date, time and number of people.");
        subtitle.getStyleClass().add("tk-text-secondary");
        subtitle.setWrapText(true);

        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.getStyleClass().add("tk-input");
        datePicker.setMaxWidth(Double.MAX_VALUE);

        TextField timeField = new TextField(LocalTime.now().plusHours(1).withMinute(0).withSecond(0).withNano(0)
                .format(DateTimeFormatter.ofPattern("HH:mm")));
        timeField.setPromptText("HH:mm");
        timeField.getStyleClass().add("tk-input");
        timeField.setMaxWidth(Double.MAX_VALUE);

        TextField peopleField = new TextField("2");
        peopleField.setPromptText("Number of people");
        peopleField.getStyleClass().add("tk-input");
        peopleField.setMaxWidth(Double.MAX_VALUE);

        VBox dateBox = buildBookingField("Date", datePicker);
        VBox timeBox = buildBookingField("Time", timeField);
        VBox peopleBox = buildBookingField("Number of people", peopleField);

        Label statusLabel = new Label();
        statusLabel.getStyleClass().add("tk-text-secondary");
        statusLabel.setStyle("-fx-text-fill: #ff9b9b;");
        statusLabel.setWrapText(true);
        statusLabel.setVisible(false);
        statusLabel.setManaged(false);

        Button btnIndietro = new Button("Back");
        btnIndietro.getStyleClass().add("tk-btn-secondary");
        btnIndietro.setOnAction(e -> dialog.close());

        Button btnConferma = new Button("Confirm");
        btnConferma.getStyleClass().add("tk-btn-primary");
        btnConferma.setOnAction(e -> {
            LocalDate data = datePicker.getValue();
            if (data == null) {
                showBookingStatus(statusLabel, "Please select a valid date.");
                return;
            }

            LocalTime ora;
            try {
                ora = LocalTime.parse(timeField.getText().trim(), DateTimeFormatter.ofPattern("HH:mm"));
            } catch (DateTimeParseException ex) {
                showBookingStatus(statusLabel, "Please enter a valid time in HH:mm format.");
                return;
            }

            int numeroPersone;
            try {
                numeroPersone = Integer.parseInt(peopleField.getText().trim());
            } catch (NumberFormatException ex) {
                showBookingStatus(statusLabel, "Please enter a valid number of people.");
                return;
            }

            if (numeroPersone <= 0) {
                showBookingStatus(statusLabel, "Number of people must be greater than zero.");
                return;
            }

            LocalDateTime dataPrenotazione = LocalDateTime.of(data, ora);
            if (dataPrenotazione.isBefore(LocalDateTime.now())) {
                showBookingStatus(statusLabel, "Date and time must be in the future.");
                return;
            }

            btnConferma.setDisable(true);
            showBookingStatus(statusLabel, "Saving...");
            submitBooking(ristorante, dataPrenotazione, numeroPersone, dialog, statusLabel, btnConferma);
        });

        HBox buttons = new HBox(10, btnIndietro, btnConferma);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        card.getChildren().addAll(title, subtitle, dateBox, timeBox, peopleBox, statusLabel, buttons);

        StackPane root = new StackPane(card);
        root.getStyleClass().add("tk-root");
        StackPane.setMargin(card, new Insets(8));

        Scene scene = new Scene(root, 500, 420);
        // fai espandere la card per occupare la maggior parte della larghezza della
        // finestra e evitare barre laterali visibili
        card.prefWidthProperty().bind(scene.widthProperty().subtract(48));
        scene.getStylesheets().add(ClientTK.class.getResource("/theknife/styles/theme.css").toExternalForm());
        dialog.setScene(scene);
        dialog.setOnShown(e -> dialog.centerOnScreen());
        dialog.showAndWait();
    }

    private VBox buildBookingField(String labelText, javafx.scene.Node control) {
        Label label = new Label(labelText);
        label.getStyleClass().add("tk-text-secondary");

        VBox box = new VBox(6, label, control);
        box.setFillWidth(true);
        return box;
    }

    private void showBookingStatus(Label statusLabel, String message) {
        if (statusLabel == null) {
            return;
        }
        statusLabel.setText(message);
        statusLabel.setVisible(true);
        statusLabel.setManaged(true);
    }

    private void submitBooking(Ristorante ristorante, LocalDateTime dataPrenotazione, int posti, Stage dialog,
            Label statusLabel, Button btnConferma) {
        new Thread(() -> {
            try {
                Request req = new Request("AGGIUNGI_PRENOTAZIONE");
                req.addParametro("idUtente", SessioneCorrente.getInstance().getUtenteLoggato().getId());
                req.addParametro("idRistorante", ristorante.getId());
                req.addParametro("dataPrenotazione", dataPrenotazione);
                req.addParametro("posti", posti);
                req.addParametro("stato", "ON HOLD");

                Response res = ServerConnection.getInstance().send(req);
                Platform.runLater(() -> {
                    if (res.isSuccesso()) {
                        if (lblMessaggio != null)
                            lblMessaggio.setText("Booking confirmed.");
                        dialog.close();
                    } else {
                        showBookingStatus(statusLabel, res.getMessaggio());
                        btnConferma.setDisable(false);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showBookingStatus(statusLabel, "Error: " + e.getMessage());
                    btnConferma.setDisable(false);
                });
            }
        }).start();
    }

    private void togglePreferito() {
        try {
            Ristorante r = SessioneCorrente.getInstance().getSelectedRistorante();
            String command = isPreferito ? "RIMUOVI_PREFERITO" : "AGGIUNGI_PREFERITO";
            Request req = new Request(command);
            req.addParametro("idUtente", SessioneCorrente.getInstance().getUtenteLoggato().getId());
            req.addParametro("idRistorante", r.getId());

            Response res = ServerConnection.getInstance().send(req);
            if (res.isSuccesso()) {
                isPreferito = !isPreferito;
                updatePreferitoBtn();
                if (lblMessaggio != null) {
                    lblMessaggio.setText(isPreferito ? "Added to favorites." : "Removed from favorites.");
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Errore nella modifica dei preferiti: " + e.getMessage());
        }
    }

    @FXML
    private void handleIndietro() {
        ClientTK.loadScene("home.fxml", "TheKnife - Home");
    }

    private VBox createReviewCard(Recensione recensione) {
        VBox card = new VBox(8);
        // Usa tk-card per lo stile di base e separa dal layout delle pagine recensione
        card.getStyleClass().addAll("tk-card", "tk-review-card-detail");
        card.setMaxWidth(Double.MAX_VALUE);

        HBox header = new HBox(10);
        Label avatar = new Label(initials(recensione.getNomeUtente()));
        avatar.getStyleClass().add("tk-badge");
        avatar.setMinWidth(34);
        avatar.setAlignment(javafx.geometry.Pos.CENTER);

        VBox meta = new VBox(2);
        Label user = new Label(recensione.getNomeUtente());
        user.getStyleClass().add("tk-card-title");
        Label date = new Label(
                recensione.getDataInserimento() != null ? recensione.getDataInserimento().format(formatter) : "");
        date.getStyleClass().add("tk-text-secondary");
        meta.getChildren().addAll(user, date);

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        Label rating = new Label(stars(recensione.getStelle()));
        rating.getStyleClass().add("tk-stars");
        header.getChildren().addAll(avatar, meta, spacer, rating);

        Label text = new Label(recensione.getTesto());
        text.setWrapText(true);
        text.getStyleClass().add("tk-text-secondary");

        card.getChildren().addAll(header, text);

        if (recensione.getRisposta() != null && recensione.getRisposta().getTesto() != null
                && !recensione.getRisposta().getTesto().isBlank()) {
            VBox reply = new VBox(4);
            reply.getStyleClass().add("tk-review-reply");
            Label replyTitle = new Label("Manager reply");
            replyTitle.getStyleClass().add("tk-card-title");
            Label replyText = new Label(recensione.getRisposta().getTesto());
            replyText.setWrapText(true);
            replyText.getStyleClass().add("tk-text-secondary");
            reply.getChildren().addAll(replyTitle, replyText);
            card.getChildren().add(reply);
        }

        return card;
    }

    private VBox createEmptyReviewsState(String title, String subtitle) {
        VBox empty = new VBox(4);
        empty.getStyleClass().add("tk-empty-state");
        Label lblTitle = new Label(title);
        lblTitle.getStyleClass().add("tk-section-title");
        Label lblSubtitle = new Label(subtitle);
        lblSubtitle.getStyleClass().add("tk-text-secondary");
        lblSubtitle.setWrapText(true);
        empty.getChildren().addAll(lblTitle, lblSubtitle);
        return empty;
    }

    private String formatLocation(Ristorante r) {
        String city = safeText(r.getCitta());
        String country = safeText(r.getNazione());
        if (city.isEmpty()) {
            return country;
        }
        return country.isEmpty() ? city : city + ", " + country;
    }

    private String formatPrice(double price) {
        if (price <= 0) {
            return "Price on request";
        }
        if (price < 50) {
            return "€ Low";
        }
        if (price < 100) {
            return "€€ Medium";
        }
        return "€€€ High";
    }

    private String stars(int value) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            builder.append(i < value ? '★' : '☆');
        }
        return builder.toString();
    }

    private String initials(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return "?";
        }
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }

    private String safeText(String value) {
        return value == null ? "" : value.trim();
    }
}