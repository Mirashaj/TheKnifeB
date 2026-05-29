package theknife.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Punto di ingresso del server. Accetta connessioni TCP in entrata e crea un
 * thread ClientHandler per ogni client connesso.
 * 
 * @author Mirashaj Erik 760453 VA
 * @author GorchynskYi Igor 757184 VA
 * @author Kabuka Dan Mumanga 757708 VA
 * @author Mujeci Lorenzo 757597 VA
 */
public class ServerTK {

    private static final int DEFAULT_PORT = 5000;
    private ServerSocket serverSocket;

    /**
     * Inizializza il server sulla porta specificata.
     *
     * @param porta
     * @throws IOException
     */
    public ServerTK(int porta) throws IOException {
        this.serverSocket = new ServerSocket(porta);
    }

    /**
     * Avvia il server in ascolto su un thread principale.
     */
    public void avvia() {
        System.out.println("Server TheKnife avviato sulla porta: " + serverSocket.getLocalPort());

        try {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nuova connessione da: " + clientSocket.getInetAddress());

                // Crea un nuovo thread per gestire il client
                ClientHandler handler = new ClientHandler(clientSocket);
                Thread clientThread = new Thread(handler);
                clientThread.start();
            }
        } catch (IOException e) {
            System.err.println("Errore durante l'accettazione della connessione: " + e.getMessage());
        }
    }

    /**
     * Chiude il server e tutte le connessioni aperte.
     */
    public void chiudi() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                System.out.println("Server chiuso.");
            }
        } catch (IOException e) {
            System.err.println("Errore durante la chiusura del server: " + e.getMessage());
        }

        // Chiudi il pool di connessioni
        DatabaseConnection.getInstance().closePool();
    }

    /**
     * Metodo main - inizializza il server con parametri da riga di comando o GUI.
     *
     * @param args
     */
    public static void main(String[] args) {
        String host = "localhost";
        String dbName = "dbtk";
        String user = "postgres";
        String password = null;
        int porta = DEFAULT_PORT;
        boolean passwordProvided = false;

        // Parsing dei parametri da riga di comando
        for (int i = 0; i < args.length; i++) {
            if ("--host".equals(args[i]) && i + 1 < args.length) {
                host = args[i + 1];
            } else if ("--db".equals(args[i]) && i + 1 < args.length) {
                dbName = args[i + 1];
            } else if ("--user".equals(args[i]) && i + 1 < args.length) {
                user = args[i + 1];
            } else if ("--password".equals(args[i]) && i + 1 < args.length) {
                password = args[i + 1];
                passwordProvided = true;
            } else if ("--port".equals(args[i]) && i + 1 < args.length) {
                try {
                    porta = Integer.parseInt(args[i + 1]);
                } catch (NumberFormatException e) {
                    System.err.println("Porta non valida. Uso porta di default: " + DEFAULT_PORT);
                    porta = DEFAULT_PORT;
                }
            }
        }

        if (!passwordProvided) {
            java.io.Console console = System.console();
            if (console != null) {
                char[] pwdArray = console.readPassword("Inserisci la password per il database (utente %s): ", user);
                password = pwdArray != null ? new String(pwdArray) : "";
            } else {
                System.out.print("Inserisci la password per il database (utente " + user + "): ");
                java.util.Scanner scanner = new java.util.Scanner(System.in);
                if (scanner.hasNextLine()) {
                    password = scanner.nextLine();
                } else {
                    password = "";
                }
            }
        }

        // Se nessun argomento, mostra di fatto un menu interattivo
        if (args.length == 0) {
            System.out.println("=== Configurazione Server TheKnife ===");
            System.out.println("Utilizzo parametri di default:");
            System.out.println("  Host DB: " + host);
            System.out.println("  Nome DB: " + dbName);
            System.out.println("  User: " + user);
            System.out.println("  Port Server: " + porta);
            System.out.println();
        }

        try {
            // Inizializza il connection pool
            DatabaseConnection.init(host, dbName, user, password);
            System.out.println("Connessione al database inizializzata.");

            // Avvia il backfill in background senza bloccare il server
            Thread backfillThread = new Thread(() -> backfillRestaurantDescriptions());
            backfillThread.setDaemon(true);
            backfillThread.start();

            // Avvia il server
            ServerTK server = new ServerTK(porta);

            // Gestisce l'arresto graceful
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nArresto server in corso...");
                server.chiudi();
            }));

            server.avvia();

        } catch (Exception e) {
            System.err.println("Errore durante l'avvio del server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void backfillRestaurantDescriptions() {
        Path csvPath = Paths.get("sql", "michelin_my_maps.csv");

        // Log minimo: solo errori o riepilogo breve.
        if (!Files.exists(csvPath)) {
            System.out.println("Backfill: CSV not found at " + csvPath.toAbsolutePath() + ". Skipping backfill.");
            // Prova percorsi alternativi solo per diagnosi
            Path altPath1 = Paths.get(".", "sql", "michelin_my_maps.csv");
            Path altPath2 = Paths.get("..", "sql", "michelin_my_maps.csv");
            if (Files.exists(altPath1) || Files.exists(altPath2)) {
                System.out.println("  Note: CSV exists at alternative path(s): " + Files.exists(altPath1) + ", "
                        + Files.exists(altPath2));
            }
            return;
        }

        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            if (!hasDescriptionColumn(conn)) {
                System.out.println("'descrizione' non presente nel DB.");
                return;
            }

            String updateSql = "UPDATE RistorantiTheKnife " +
                    "SET descrizione = ? " +
                    "WHERE nome = ? AND indirizzo = ? AND (descrizione IS NULL OR descrizione = '')";

            java.nio.charset.CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(java.nio.charset.CodingErrorAction.IGNORE)
                    .onUnmappableCharacter(java.nio.charset.CodingErrorAction.IGNORE);

            try (PreparedStatement stmt = conn.prepareStatement(updateSql);
                    BufferedReader reader = new BufferedReader(
                            new java.io.InputStreamReader(Files.newInputStream(csvPath), decoder))) {

                String header = reader.readLine();
                if (header == null) {
                    System.out.println("CSV vuoto.");
                    return;
                }

                String line;
                int toProcess = 0;
                int skipped = 0;
                while ((line = reader.readLine()) != null) {
                    if (line.isBlank())
                        continue;

                    List<String> columns = parseCsvLine(line);
                    if (columns.size() < 14) {
                        skipped++;
                        continue;
                    }

                    String name = columns.get(0).trim();
                    String address = columns.get(1).trim();
                    String description = columns.get(13).trim();

                    if (name.isEmpty() || address.isEmpty() || description.isEmpty()) {
                        skipped++;
                        continue;
                    }

                    stmt.setString(1, description);
                    stmt.setString(2, name);
                    stmt.setString(3, address);
                    stmt.addBatch();
                    toProcess++;
                }

                if (toProcess == 0 && skipped == 0) {
                    // Niente da fare resta silenzioso
                    return;
                }

                int[] results = stmt.executeBatch();
                int affected = 0;
                for (int result : results) {
                    if (result > 0)
                        affected += result;
                }

                System.out.println(
                        String.format("riassunto: processed=%d, updated=%d, skipped=%d", toProcess, affected, skipped));
            }
        } catch (Exception e) {
            System.err.println("Errore nel backfill: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static boolean hasDescriptionColumn(Connection conn) throws SQLException {
        DatabaseMetaData metaData = conn.getMetaData();
        try (ResultSet rs = metaData.getColumns(null, null, "ristorantitheknife", "descrizione")) {
            if (rs.next()) {
                return true;
            }
        }
        try (ResultSet rs = metaData.getColumns(null, null, "RistorantiTheKnife", "descrizione")) {
            return rs.next();
        }
    }

    private static List<String> parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (inQuotes) {
                if (ch == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        current.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    current.append(ch);
                }
            } else {
                if (ch == '"') {
                    inQuotes = true;
                } else if (ch == ',') {
                    values.add(current.toString());
                    current.setLength(0);
                } else {
                    current.append(ch);
                }
            }
        }

        values.add(current.toString());
        return values;
    }
}
