package theknife.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import theknife.shared.Request;
import theknife.shared.Response;

/**
 * Singleton che gestisce la connessione TCP del client verso il server.
 * Implementa l'invio e la ricezione di oggetti serializzati.
 * 
 * @author Mirashaj Erik 760453 VA
 * @author GorchynskYi Igor 757184 VA
 * @author Kabuka Dan Mumanga 757708 VA
 * @author Mujeci Lorenzo 757597 VA
 */
public class ServerConnection {

    private static ServerConnection instance;
    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;

    private String host;
    private int port;

    private ServerConnection(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Inizializza la connessione Singleton al server.
     *
     * @param host L'host del server a cui connettersi.
     * @param port La porta del server.
     * @return L'istanza Singleton inizializzata.
     * @throws IOException Se si verifica un errore durante la connessione.
     */
    public static synchronized ServerConnection init(String host, int port) throws IOException {
        if (instance == null) {
            instance = new ServerConnection(host, port);
            instance.connect();
        }
        return instance;
    }

    /**
     * Restituisce l'istanza Singleton della connessione.
     *
     * @return L'istanza di ServerConnection.
     */
    public static synchronized ServerConnection getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ServerConnection non inizializzata. Chiamare init() prima.");
        }
        return instance;
    }

    /**
     * Stabilisce la connessione TCP con il server.
     *
     * @throws IOException Se c'è un problema di rete o I/O.
     */
    private void connect() throws IOException {
        this.socket = new Socket(host, port);
        this.oos = new ObjectOutputStream(socket.getOutputStream());
        this.oos.flush();
        this.ois = new ObjectInputStream(socket.getInputStream());
        System.out.println("Connesso al server: " + host + ":" + port);
    }

    /**
     * Invia una Request al server e attende la Response.
     *
     * @param request L'oggetto Request da inviare.
     * @return L'oggetto Response restituito dal server.
     * @throws IOException            Se si verifica un errore di I/O.
     * @throws ClassNotFoundException Se la risposta ricevuta non è riconosciuta.
     */
    public synchronized Response send(Request request) throws IOException, ClassNotFoundException {
        if (socket == null || socket.isClosed()) {
            throw new IOException("Connessione con il server non attiva.");
        }

        // invia la request
        oos.writeObject(request);
        oos.flush();

        // attende la response
        Object responseObj = ois.readObject();
        if (responseObj instanceof Response) {
            return (Response) responseObj;
        }

        throw new ClassNotFoundException("Response non riconosciuta dal server.");
    }

    /**
     * Chiude la connessione con il server.
     */
    public synchronized void disconnect() {
        try {
            if (ois != null)
                ois.close();
            if (oos != null)
                oos.close();
            if (socket != null)
                socket.close();
            System.out.println("Disconnesso dal server.");
        } catch (IOException e) {
            System.err.println("Errore durante la disconnessione: " + e.getMessage());
        } finally {
            instance = null;
        }
    }

    /**
     * Verifica se la connessione è attiva.
     *
     * @return true se connesso, false altrimenti.
     */
    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }
}
