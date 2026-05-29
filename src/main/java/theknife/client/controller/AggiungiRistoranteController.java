package theknife.client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;
import theknife.client.ClientTK;
import theknife.client.ServerConnection;
import theknife.client.SessioneCorrente;
import theknife.shared.Request;
import theknife.shared.Response;

/**
 * AggiungiRistoranteController: Controller JavaFX per la schermata
 * AggiungiRistorante. Gestisce gli eventi dell'interfaccia grafica e comunica
 * con il server tramite ServerConnection.
 * 
 * @author Mirashaj Erik 760453 VA
 * @author GorchynskYi Igor 757184 VA
 * @author Kabuka Dan Mumanga 757708 VA
 * @author Mujeci Lorenzo 757597 VA
 */
public class AggiungiRistoranteController {

    @FXML
    private TextField txtNome, txtIndirizzo, txtCitta, txtNazione, txtLatitudine, txtLongitudine, txtPrezzo, txtCucina;
    @FXML
    private CheckBox chkDelivery, chkPrenotazione;
    @FXML
    private Label lblErrore;
    @FXML
    private Button btnNavHome;
    @FXML
    private Button btnNavRestaurants;
    @FXML
    private Button btnNavReviews;
    @FXML
    private Button btnNavBookings;
    @FXML
    private MenuButton accountMenuButton;

    @FXML
    public void initialize() {
        if (!SessioneCorrente.getInstance().isUserLogged() || !SessioneCorrente.getInstance().isGestore()) {
            ClientTK.loadScene("welcome.fxml", "TheKnife - Welcome");
            return;
        }

        if (isEditMode()) {
            fillFormFromSelected();
        }

        updateAccountMenu();

        if (btnNavRestaurants != null) {
            btnNavRestaurants.setText("Restaurants");
            btnNavRestaurants.getStyleClass().setAll("tk-nav-active");
        }
        if (btnNavReviews != null) {
            btnNavReviews.setText("Review");
            btnNavReviews.getStyleClass().setAll("tk-nav-item");
        }
        if (btnNavBookings != null) {
            btnNavBookings.setText("Bookings");
            btnNavBookings.getStyleClass().setAll("tk-nav-item");
        }
    }

    private boolean isEditMode() {
        return SessioneCorrente.getInstance() != null
                && SessioneCorrente.getInstance().getSelectedRistorante() != null
                && SessioneCorrente.getInstance().getSelectedRistorante().getId() > 0;
    }

    private void fillFormFromSelected() {
        var r = SessioneCorrente.getInstance().getSelectedRistorante();
        if (r == null)
            return;
        if (txtNome != null)
            txtNome.setText(r.getNome());
        if (txtIndirizzo != null)
            txtIndirizzo.setText(r.getIndirizzo());
        if (txtCitta != null)
            txtCitta.setText(r.getCitta());
        if (txtNazione != null)
            txtNazione.setText(r.getNazione());
        if (txtLatitudine != null)
            txtLatitudine.setText(String.valueOf(r.getLatitudine()));
        if (txtLongitudine != null)
            txtLongitudine.setText(String.valueOf(r.getLongitudine()));
        if (txtPrezzo != null)
            txtPrezzo.setText(String.valueOf(r.getPrezzoMedio()));
        if (txtCucina != null)
            txtCucina.setText(r.getTipoCucina());
        if (chkDelivery != null)
            chkDelivery.setSelected(r.isDelivery());
        if (chkPrenotazione != null)
            chkPrenotazione.setSelected(r.isPrenotazione());
    }

    @FXML
    private void handleSalva() {
        try {
            String nome = txtNome.getText().trim();
            String indirizzo = txtIndirizzo.getText().trim();
            String citta = txtCitta.getText().trim();
            String nazione = txtNazione.getText().trim();
            String cucina = txtCucina != null ? txtCucina.getText().trim() : "";

            if (nome.isEmpty() || indirizzo.isEmpty() || citta.isEmpty() || nazione.isEmpty()) {
                if (lblErrore != null)
                    lblErrore.setText("Fill in all required fields.");
                return;
            }

            double lat = Double.parseDouble(txtLatitudine.getText().trim());
            double lon = Double.parseDouble(txtLongitudine.getText().trim());
            double prezzo = Double.parseDouble(txtPrezzo.getText().trim());

            String requestType = isEditMode() ? "MODIFICA_RISTORANTE" : "AGGIUNGI_RISTORANTE";
            Request req = new Request(requestType);

            req.addParametro("nome", nome);
            req.addParametro("indirizzo", indirizzo);
            req.addParametro("citta", citta);
            req.addParametro("nazione", nazione);
            req.addParametro("latitudine", lat);
            req.addParametro("longitudine", lon);
            req.addParametro("prezzoMedio", prezzo);
            req.addParametro("tipoCucina", cucina);
            if (chkDelivery != null)
                req.addParametro("delivery", chkDelivery.isSelected());
            if (chkPrenotazione != null)
                req.addParametro("prenotazione", chkPrenotazione.isSelected());
            req.addParametro("idGestore", SessioneCorrente.getInstance().getUtenteLoggato().getId());
            if (isEditMode()) {
                req.addParametro("idRistorante", SessioneCorrente.getInstance().getSelectedRistorante().getId());
            }

            Response res = ServerConnection.getInstance().send(req);

            if (res.isSuccesso()) {
                if (SessioneCorrente.getInstance().getSelectedRistorante() != null) {
                    SessioneCorrente.getInstance().setSelectedRistorante(null);
                }
                ClientTK.loadScene("dashboard_gestore.fxml", "TheKnife - Dashboard");

            } else if (lblErrore != null) {
                lblErrore.setText("Error: " + res.getMessaggio());
            }
        } catch (NumberFormatException e) {
            if (lblErrore != null)
                lblErrore.setText("Invalid coordinates or price.");
        } catch (Exception e) {
            if (lblErrore != null)
                lblErrore.setText("Connection error.");
        }
    }

    @FXML
    private void handleAnnulla() {
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
}
