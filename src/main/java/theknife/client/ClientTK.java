package theknife.client;

import java.io.IOException;

import atlantafx.base.theme.PrimerDark;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import theknife.shared.Request;

/**
 * Punto di ingresso dell'applicazione client JavaFX. Gestisce il caricamento
 * delle scene FXML e l'inizializzazione della connessione al server.
 * 
 * @author Mirashaj Erik 760453 VA
 * @author GorchynskYi Igor 757184 VA
 * @author Kabuka Dan Mumanga 757708 VA
 * @author Mujeci Lorenzo 757597 VA
 */
public class ClientTK extends Application {

    private static Stage primaryStage;

    /**
     * Avvia l'applicazione JavaFX e tenta la connessione al server.
     *
     * @param stage Lo stage principale fornito da JavaFX.
     * @throws Exception Se si verifica un errore durante l'avvio.
     */
    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        primaryStage.setMaximized(true);

        Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());

        // inizializza la connessione al server
        try {
            ServerConnection.init("localhost", 5000);
        } catch (IOException e) {
            System.err.println("Connection error to the server: " + e.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Connection Error");
            alert.setHeaderText("Unable to connect to the server. Please ensure that serverTK is running.");
            alert.showAndWait();
            Platform.exit();
            return;
        }

        primaryStage.setOnCloseRequest(event -> {
            try {
                if (ServerConnection.getInstance().isConnected()) {
                    ServerConnection.getInstance().send(new Request("DISCONNECT"));
                    ServerConnection.getInstance().disconnect();
                }
            } catch (Exception e) {
                /* ignora */ }
        });

        // carica la schermata di benvenuto
        loadScene("welcome.fxml", "TheKnife - Welcome");
    }

    /**
     * Carica una schermata FXML e la visualizza nello stage principale.
     *
     * @param fxmlFile Il nome del file FXML da caricare.
     * @param title    Il titolo della finestra.
     */
    public static void loadScene(String fxmlFile, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    ClientTK.class.getResource("/theknife/views/" + fxmlFile));
            Pane root = loader.load();

            if (primaryStage.getScene() == null) {
                Scene scene = new Scene(root);
                scene.getStylesheets().add(ClientTK.class.getResource("/theknife/styles/theme.css").toExternalForm());
                primaryStage.setScene(scene);
            } else {
                primaryStage.getScene().setRoot(root);
            }

            primaryStage.setTitle(title);
            primaryStage.setMaximized(true);
            if (!primaryStage.isShowing()) {
                primaryStage.show();
            }
        } catch (IOException e) {
            System.err.println("Error loading the screen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Mostra un errore e termina l'applicazione.
     *
     * @param titolo    Titolo dell'errore.
     * @param messaggio Messaggio di dettaglio dell'errore.
     */
    private void showErrorAndExit(String titolo, String messaggio) {
        System.err.println(titolo + ": " + messaggio);
        System.exit(1);
    }

    /**
     * Entry point dell'applicazione.
     *
     * @param args Argomenti da riga di comando.
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     *
     * @throws Exception se si verifica un errore mentre la chiude
     */
    @Override
    public void stop() throws Exception {
        // disconnetti dal server quando l'app si chiude
        try {
            ServerConnection.getInstance().disconnect();
        } catch (IllegalStateException e) {
            // disconnesso
        }
        super.stop();
    }
}
