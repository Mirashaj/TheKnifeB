package theknife.server;

import java.sql.Connection;
import java.sql.SQLException;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Singleton che gestisce il pool di connessioni JDBC al database PostgreSQL
 * tramite HikariCP.
 * 
 * @author Mirashaj Erik 760453 VA
 * @author GorchynskYi Igor 757184 VA
 * @author Kabuka Dan Mumanga 757708 VA
 * @author Mujeci Lorenzo 757597 VA
 */
public class DatabaseConnection {

    private static DatabaseConnection instance;
    private HikariDataSource dataSource;

    private DatabaseConnection(String host, String dbName, String user, String password) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://" + host + "/" + dbName);
        config.setUsername(user);
        config.setPassword(password);

        // Configurazioni ottimali di base per HikariCP
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);

        dataSource = new HikariDataSource(config);
    }

    /**
     * Inizializza il pool di connessioni.
     *
     * @param host     Host del database.
     * @param dbName   Nome del database.
     * @param user     Utente del database.
     * @param password Password del database.
     */
    public static synchronized void init(String host, String dbName, String user, String password) {
        if (instance == null) {
            instance = new DatabaseConnection(host, dbName, user, password);
        }
    }

    /**
     * Restituisce l'istanza Singleton della connessione.
     *
     * @return L'istanza di DatabaseConnection.
     */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            throw new IllegalStateException("DatabaseConnection non inizializzato. Chiamare init() prima.");
        }
        return instance;
    }

    /**
     * Restituisce una connessione attiva dal pool.
     *
     * @return Connection valida verso PostgreSQL.
     * @throws SQLException Se non è possibile ottenere la connessione.
     */
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Chiude il pool di connessioni liberando le risorse.
     */
    public void closePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
