package theknife.client.controller;

import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
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
 * HomeController: Controller JavaFX per la schermata Home. Gestisce gli eventi
 * dell'interfaccia grafica e comunica con il server tramite ServerConnection.
 * 
 * @author Mirashaj Erik 760453 VA
 * @author GorchynskYi Igor 757184 VA
 * @author Kabuka Dan Mumanga 757708 VA
 * @author Mujeci Lorenzo 757597 VA
 */
public class HomeController {

    private static final int RESULTS_PER_PAGE = 40;

    @FXML
    private TextField txtRicerca;

    @FXML
    private TextField txtCucina;

    @FXML
    private CheckBox chkDelivery;

    @FXML
    private CheckBox chkPrenotazione;

    @FXML
    private Label lblMessaggio;

    @FXML
    private TilePane flowRistoranti;

    @FXML
    private Button btnCaricaAltro;

    @FXML
    private VBox filterMenu;

    @FXML
    private TextField txtLatitudine;

    @FXML
    private TextField txtLongitudine;

    @FXML
    private Slider sliderStelle;

    @FXML
    private Label lblStelleMin;

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

    private final List<Ristorante> risultatiCorrenti = new ArrayList<>();

    private GridPane gridRistoranti;

    @FXML
    public void initialize() {
        txtRicerca.setOnAction(e -> handleCerca());

        if (sliderStelle != null && lblStelleMin != null) {
            sliderStelle.valueProperty().addListener((obs, oldValue, newValue) -> lblStelleMin
                    .setText(String.format("Min rating: %.1f", newValue.doubleValue())));
        }

        String cittaGuest = SessioneCorrente.getInstance().getCittaGuest();
        if (cittaGuest != null && !cittaGuest.isEmpty()) {
            txtRicerca.setText(cittaGuest);
        } else if (SessioneCorrente.getInstance().isUserLogged()) {
            String dom = SessioneCorrente.getInstance().getUtenteLoggato().getDomicilio();
            if (dom != null && !dom.isEmpty()) {
                txtRicerca.setText(dom);
            }
        }

        updateAccountMenu();
        configureNavbar();

        if (filterMenu != null) {
            filterMenu.setVisible(false);
            filterMenu.setManaged(false);
        }
        if (flowRistoranti != null && flowRistoranti.getParent() instanceof Pane parent) {
            gridRistoranti = new GridPane();
            gridRistoranti.setHgap(12);
            gridRistoranti.setVgap(12);
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(25);
            gridRistoranti.getColumnConstraints().addAll(cc, cc, cc, cc);

            int index = parent.getChildren().indexOf(flowRistoranti);
            parent.getChildren().set(index, gridRistoranti);

            javafx.application.Platform.runLater(() -> {
                javafx.scene.Node n = gridRistoranti;
                while (n != null && !(n instanceof ScrollPane)) {
                    n = n.getParent();
                }
                if (n instanceof ScrollPane) {
                    ((ScrollPane) n).setFitToWidth(true);
                }
            });
        }

        handleCerca();
    }

    private void updateAccountMenu() {
        if (accountMenuButton == null) {
            return;
        }

        accountMenuButton.setText("");
        accountMenuButton.getItems().clear();
        accountMenuButton.setGraphic(createAccountGraphic());

        if (SessioneCorrente.getInstance().isUserLogged()) {
            MenuItem settings = new MenuItem("Settings");
            settings.setOnAction(e -> handleSettings());

            MenuItem logout = new MenuItem("Logout");
            logout.setOnAction(e -> handleLogout());

            accountMenuButton.getItems().addAll(settings, logout);
        } else {
            MenuItem register = new MenuItem("Register");
            register.setOnAction(e -> handleRegister());

            MenuItem login = new MenuItem("Login");
            login.setOnAction(e -> handleLogin());

            MenuItem back = new MenuItem("Back");
            back.setOnAction(e -> handleBack());

            accountMenuButton.getItems().addAll(register, login, back);
        }
    }

    private void configureNavbar() {
        boolean gestore = SessioneCorrente.getInstance().isGestore();

        if (btnNavHome != null) {
            btnNavHome.getStyleClass().setAll("tk-nav-active");
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

    @FXML
    private void handleLogin() {
        ClientTK.loadScene("login.fxml", "TheKnife - Login");
    }

    @FXML
    private void handleRegister() {
        ClientTK.loadScene("registrazione.fxml", "TheKnife - Registration");
    }

    @FXML
    private void handleLogout() {
        SessioneCorrente.getInstance().logout();
        ClientTK.loadScene("welcome.fxml", "TheKnife - Welcome");
    }

    @FXML
    private void handleMyAccount() {
        if (SessioneCorrente.getInstance().isUserLogged() && SessioneCorrente.getInstance().isGestore()) {
            ClientTK.loadScene("dashboard_gestore.fxml", "TheKnife - Dashboard");
            return;
        }
        if (SessioneCorrente.getInstance().isUserLogged()) {
            ClientTK.loadScene("mie_recensioni.fxml", "TheKnife - My Reviews");
            return;
        }
        ClientTK.loadScene("login.fxml", "TheKnife - Login");
    }

    @FXML
    private void handleBack() {
        ClientTK.loadScene("welcome.fxml", "TheKnife - Welcome");
    }

    @FXML
    private void handleSettings() {
        ClientTK.loadScene("settings.fxml", "TheKnife - Settings");
    }

    @FXML
    private void handlePrenotazioni() {
        ClientTK.loadScene("prenotazioni.fxml", "TheKnife - Bookings");
    }

    @FXML
    private void handleToggleFilters() {
        if (filterMenu == null)
            return;
        boolean show = !filterMenu.isVisible();
        filterMenu.setVisible(show);
        filterMenu.setManaged(show);
    }

    @FXML
    private void handleCerca() {
        risultatiCorrenti.clear();
        if (gridRistoranti != null) {
            gridRistoranti.getChildren().clear();
        }
        eseguiRicerca(0);
    }

    private void eseguiRicerca(int offset) {
        new Thread(() -> {
            String luogo = txtRicerca.getText().trim();
            String cucinaVal = (txtCucina != null) ? txtCucina.getText().trim() : "";
            boolean delivery = chkDelivery.isSelected();
            boolean prenotazione = chkPrenotazione != null && chkPrenotazione.isSelected();
            double stelleMin = sliderStelle != null ? sliderStelle.getValue() : 0.0;

            javafx.application.Platform.runLater(() -> lblMessaggio.setText("Searching..."));

            try {
                Request request = new Request("CERCA_RISTORANTE");
                if (!luogo.isEmpty())
                    request.addParametro("luogo", luogo);
                if (!cucinaVal.isEmpty())
                    request.addParametro("tipoCucina", cucinaVal);
                if (delivery)
                    request.addParametro("delivery", true);
                if (prenotazione)
                    request.addParametro("prenotazione", true);
                if (stelleMin > 0)
                    request.addParametro("stelleMin", stelleMin);

                if (txtLatitudine != null && !txtLatitudine.getText().trim().isEmpty() &&
                        txtLongitudine != null && !txtLongitudine.getText().trim().isEmpty()) {
                    try {
                        request.addParametro("latitudine", Double.parseDouble(txtLatitudine.getText().trim()));
                        request.addParametro("longitudine", Double.parseDouble(txtLongitudine.getText().trim()));
                    } catch (NumberFormatException ex) {
                        javafx.application.Platform.runLater(() -> lblMessaggio.setText("Invalid coordinates."));
                        return;
                    }
                }

                request.addParametro("limite", RESULTS_PER_PAGE);
                request.addParametro("offset", offset);

                Response response = ServerConnection.getInstance().send(request);

                javafx.application.Platform.runLater(() -> {
                    if (response.isSuccesso()) {
                        @SuppressWarnings("unchecked")
                        List<Ristorante> nuoviRisultati = (List<Ristorante>) response.getPayload();
                        risultatiCorrenti.addAll(nuoviRisultati);
                        renderVisibleResults(nuoviRisultati);
                        lblMessaggio.setText(String.format("Found %d restaurants.", risultatiCorrenti.size()));
                    } else {
                        lblMessaggio.setText("Error: " + response.getMessaggio());
                    }
                });

            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    lblMessaggio.setText("Connection error.");
                });
            }
        }).start();
    }

    @FXML
    private void handleLoadMore() {
        eseguiRicerca(risultatiCorrenti.size());
    }

    private void renderVisibleResults(List<Ristorante> nuoviRisultati) {
        if (gridRistoranti == null) {
            return;
        }

        if (risultatiCorrenti.isEmpty()) {
            gridRistoranti.getChildren().clear();
            gridRistoranti.add(
                    createEmptyState("No restaurants found", "Try widening your filters."),
                    0, 0);
            GridPane.setColumnSpan(gridRistoranti.getChildren().get(0), 4);
            refreshLoadMoreButton(0);
            return;
        }

        for (int i = 0; i < nuoviRisultati.size(); i++) {
            int totalIndex = risultatiCorrenti.size() - nuoviRisultati.size() + i;
            VBox card = createRestaurantCard(nuoviRisultati.get(i));
            card.setMaxWidth(Double.MAX_VALUE);
            GridPane.setHgrow(card, Priority.ALWAYS);
            gridRistoranti.add(card, totalIndex % 4, totalIndex / 4);
        }
        refreshLoadMoreButton(nuoviRisultati.size());
    }

    private void refreshLoadMoreButton(int addedCount) {
        if (btnCaricaAltro == null) {
            return;
        }
        boolean hasMore = addedCount == RESULTS_PER_PAGE;
        btnCaricaAltro.setVisible(hasMore);
        btnCaricaAltro.setManaged(hasMore);
    }

    private VBox createRestaurantCard(Ristorante r) {
        VBox card = new VBox(12);
        card.getStyleClass().addAll("tk-card", "tk-result-card");

        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label(getCuisineIcon(r.getTipoCucina()));
        icon.getStyleClass().add("tk-result-thumb");
        icon.setMinSize(44, 44);
        icon.setPrefSize(44, 44);
        icon.setAlignment(javafx.geometry.Pos.CENTER);

        Region topSpacer = new Region();
        HBox.setHgrow(topSpacer, javafx.scene.layout.Priority.ALWAYS);

        HBox ratingRow = createRatingRow(r);

        topRow.getChildren().addAll(icon, topSpacer, ratingRow);

        VBox titleBox = new VBox(2);
        Label lblNome = new Label(r.getNome());
        lblNome.getStyleClass().add("tk-card-title");
        lblNome.setWrapText(true);

        Label lblMeta = new Label((safeText(r.getCitta()) + ", " + safeText(r.getNazione())).replaceAll("^, |, $", ""));
        lblMeta.getStyleClass().add("tk-text-secondary");
        lblMeta.setWrapText(true);

        titleBox.getChildren().addAll(lblNome, lblMeta);

        Label lblCucina = new Label("Cuisine: " + safeText(r.getTipoCucina()));
        lblCucina.getStyleClass().add("tk-text-secondary");
        lblCucina.setWrapText(true);

        Label lblIndirizzo = new Label(safeText(r.getIndirizzo()));
        lblIndirizzo.getStyleClass().add("tk-text-secondary");
        lblIndirizzo.setWrapText(true);

        javafx.scene.layout.FlowPane tagsRow = new javafx.scene.layout.FlowPane(8, 8);
        Label lblPrezzo = new Label(formatPrice(r.getPrezzoMedio()));
        lblPrezzo.getStyleClass().addAll("tk-badge", getPriceStyleClass(r.getPrezzoMedio()));
        tagsRow.getChildren().add(lblPrezzo);

        if (r.isDelivery()) {
            Label lblDelivery = new Label("Delivery");
            lblDelivery.getStyleClass().add("tk-chip");
            tagsRow.getChildren().add(lblDelivery);
        }

        if (r.isPrenotazione()) {
            Label lblBooking = new Label("Booking");
            lblBooking.getStyleClass().add("tk-chip");
            tagsRow.getChildren().add(lblBooking);
        }

        card.setOnMouseClicked(event -> {
            SessioneCorrente.getInstance().setSelectedRistorante(r);
            ClientTK.loadScene("dettaglio_ristorante.fxml", "TheKnife - " + r.getNome());
        });

        card.getChildren().addAll(topRow, titleBox, lblCucina, lblIndirizzo, tagsRow);
        return card;
    }

    private VBox createEmptyState(String title, String subtitle) {
        VBox empty = new VBox(6);
        empty.getStyleClass().add("tk-empty-state");
        Label lblTitle = new Label(title);
        lblTitle.getStyleClass().add("tk-section-title");
        Label lblSubtitle = new Label(subtitle);
        lblSubtitle.getStyleClass().add("tk-text-secondary");
        lblSubtitle.setWrapText(true);
        empty.getChildren().addAll(lblTitle, lblSubtitle);
        empty.setPrefWidth(320);
        return empty;
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

    private String getPriceStyleClass(double price) {
        if (price <= 0) {
            return "tk-price-request";
        }
        if (price < 50) {
            return "tk-price-low";
        }
        if (price < 100) {
            return "tk-price-medium";
        }
        return "tk-price-high";
    }

    private String getCuisineIcon(String cuisine) {
        if (cuisine == null) {
            return "🍽";
        }
        String normalized = cuisine.toLowerCase();
        if (normalized.contains("japan"))
            return "🍣";
        if (normalized.contains("sea"))
            return "🦐";
        if (normalized.contains("grill") || normalized.contains("meat"))
            return "🥩";
        if (normalized.contains("creative"))
            return "✨";
        if (normalized.contains("ital"))
            return "🍝";
        if (normalized.contains("span"))
            return "🥘";
        if (normalized.contains("indian"))
            return "🍛";
        if (normalized.contains("burger") || normalized.contains("american"))
            return "🍔";
        if (normalized.contains("asian") || normalized.contains("chinese"))
            return "🍥";
        if (normalized.contains("vegetarian") || normalized.contains("vegan"))
            return "🥦";
        if (normalized.contains("pizza"))
            return "🍕";
        if (normalized.contains("mexican"))
            return "🌮";
        if (normalized.contains("mediterranean"))
            return "🥗";
        if (normalized.contains("thai"))
            return "🍜";
        if (normalized.contains("french"))
            return "🥖";
        if (normalized.contains("hot"))
            return "♨️";
        if (normalized.contains("dessert") || normalized.contains("sweet"))
            return "🍰";
        if (normalized.contains("sushi"))
            return "🍣";
        return "🍽";
    }

    private HBox createRatingRow(Ristorante r) {
        double value = r != null ? r.getMediaStelle() : 0.0;
        int recensioni = r != null ? r.getNumRecensioni() : 0;

        HBox ratingRow = new HBox(2);
        ratingRow.setAlignment(Pos.CENTER_LEFT);

        // Mostra voto se disponibile, altrimenti "Nessun voto"
        if (recensioni <= 0 && value <= 0.0001) {
            Label noRating = new Label("No reviews");
            noRating.getStyleClass().add("tk-stars");
            ratingRow.getChildren().add(noRating);
            return ratingRow;
        }

        // Genera stelle visive
        int fullStars = (int) value;
        boolean hasHalfStar = (value % 1) >= 0.5;
        int emptyStars = 5 - fullStars - (hasHalfStar ? 1 : 0);

        for (int i = 0; i < fullStars; i++) {
            ratingRow.getChildren().add(createStarLabel("★", false));
        }
        if (hasHalfStar) {
            ratingRow.getChildren().add(createStarLabel("⯪", true));
        }
        for (int i = 0; i < emptyStars; i++) {
            ratingRow.getChildren().add(createStarLabel("☆", false));
        }
        Label valueLabel = new Label(String.format(" %.1f", value));
        valueLabel.getStyleClass().add("tk-stars");
        ratingRow.getChildren().add(valueLabel);

        return ratingRow;

    }

    private Label createStarLabel(String symbol, boolean isHalfStar) {
        Label star = new Label(symbol);
        star.getStyleClass().add("tk-stars");
        if (isHalfStar) {
            star.getStyleClass().add("tk-star-half");
        }
        return star;
    }

    private String safeText(String value) {
        return value == null ? "" : value.trim();
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
    private void handleHome() {
        ClientTK.loadScene("home.fxml", "TheKnife - Home");
    }

    @FXML
    private void handleIndietro() {
        if (SessioneCorrente.getInstance().isUserLogged() &&
                "gestore".equals(SessioneCorrente.getInstance().getUtenteLoggato().getRuolo())) {
            ClientTK.loadScene("dashboard_gestore.fxml", "TheKnife - Dashboard");
        } else {
            ClientTK.loadScene("welcome.fxml", "TheKnife - Welcome");
        }
    }
}
