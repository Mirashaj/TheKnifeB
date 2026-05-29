package theknife.server.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import theknife.model.Ristorante;
import theknife.server.DatabaseConnection;
import theknife.server.dao.RistoranteDAO;

/**
 * Implementazione concreta di RistoranteDAO tramite JDBC e PostgreSQL.
 * 
 * @author Mirashaj Erik 760453 VA
 * @author GorchynskYi Igor 757184 VA
 * @author Kabuka Dan Mumanga 757708 VA
 * @author Mujeci Lorenzo 757597 VA
 */
public class RistoranteDAOImpl implements RistoranteDAO {

    @Override
    public List<Ristorante> cerca(Map<String, Object> filtri) {
        List<Ristorante> risultati = new ArrayList<>();
        StringBuilder query = new StringBuilder(
                "SELECT r.id, r.nome, r.nazione, r.citta, r.indirizzo, r.latitudine, r.longitudine, " +
                        "r.prezzo_medio, r.delivery, r.prenotazione, r.tipo_cucina, r.descrizione, r.id_gestore, " +
                        "COALESCE(AVG(rec.stelle), 0) AS media_stelle, " +
                        "COUNT(rec.id) AS num_recensioni FROM RistorantiTheKnife r " +
                        "LEFT JOIN Recensioni rec ON rec.id_ristorante = r.id " +
                        "WHERE 1=1 ");

        // Luogo
        String luogo = (String) filtri.get("luogo");
        if (luogo != null && !luogo.isEmpty()) {
            query.append("AND (r.citta ILIKE '%' || ? || '%' OR r.nome ILIKE '%' || ? || '%') ");
        }

        // Tipo cucina
        if (filtri.containsKey("tipoCucina") && filtri.get("tipoCucina") != null) {
            query.append("AND r.tipo_cucina ILIKE '%' || ? || '%' ");
        }

        // Delivery
        if (filtri.containsKey("delivery")) {
            query.append("AND r.delivery = ? ");
        }

        Double lat = null, lon = null, raggio = null;
        if (filtri.containsKey("latitudine") && filtri.containsKey("longitudine") && filtri.containsKey("raggioKm")) {
            Object latObj = filtri.get("latitudine");
            Object lonObj = filtri.get("longitudine");
            Object ragObj = filtri.get("raggioKm");
            if (latObj != null && lonObj != null && ragObj != null) {
                lat = ((Number) latObj).doubleValue();
                lon = ((Number) lonObj).doubleValue();
                raggio = ((Number) ragObj).doubleValue();
                query.append("AND (6371 * acos(cos(radians(?)) * cos(radians(r.latitudine)) * " +
                        "cos(radians(r.longitudine) - radians(?)) + " +
                        "sin(radians(?)) * sin(radians(r.latitudine)))) <= ? ");
            }
        }

        query.append("GROUP BY r.id, r.nome, r.nazione, r.citta, r.indirizzo, r.latitudine, r.longitudine, " +
                "r.prezzo_medio, r.delivery, r.prenotazione, r.tipo_cucina, r.descrizione, r.id_gestore ");

        if (filtri.containsKey("stelleMin") && filtri.get("stelleMin") != null) {
            query.append("HAVING (AVG(rec.stelle) IS NULL OR AVG(rec.stelle) >= ?) ");
        }

        query.append("ORDER BY r.nome ");

        if (filtri.containsKey("limite")) {
            query.append("LIMIT ? ");
        }

        if (filtri.containsKey("offset")) {
            query.append("OFFSET ? ");
        }

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(query.toString())) {

            int paramIndex = 1;

            if (luogo != null && !luogo.isEmpty()) {
                stmt.setString(paramIndex++, luogo);
                stmt.setString(paramIndex++, luogo);
            }

            if (filtri.containsKey("tipoCucina") && filtri.get("tipoCucina") != null) {
                stmt.setString(paramIndex++, (String) filtri.get("tipoCucina"));
            }

            // Delivery
            if (filtri.containsKey("delivery")) {
                stmt.setBoolean(paramIndex++, (Boolean) filtri.get("delivery"));
            }

            if (lat != null && lon != null && raggio != null) {
                stmt.setDouble(paramIndex++, lat);
                stmt.setDouble(paramIndex++, lon);
                stmt.setDouble(paramIndex++, lat);
                stmt.setDouble(paramIndex++, raggio);
            }

            if (filtri.containsKey("stelleMin") && filtri.get("stelleMin") != null) {
                stmt.setDouble(paramIndex++, ((Number) filtri.get("stelleMin")).doubleValue());
            }

            if (filtri.containsKey("limite") && filtri.get("limite") != null) {
                stmt.setInt(paramIndex++, ((Number) filtri.get("limite")).intValue());
            }

            if (filtri.containsKey("offset") && filtri.get("offset") != null) {
                stmt.setInt(paramIndex++, ((Number) filtri.get("offset")).intValue());
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Ristorante r = mapRistorante(rs);
                risultati.add(r);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore DB durante ricerca ristoranti: " + e.getMessage(), e);
        }

        return risultati;
    }

    @Override
    public Ristorante findById(int id) {
        String query = "SELECT r.id, r.nome, r.nazione, r.citta, r.indirizzo, r.latitudine, r.longitudine, " +
                "r.prezzo_medio, r.delivery, r.prenotazione, r.tipo_cucina, r.descrizione, r.id_gestore, " +
                "COALESCE(AVG(rec.stelle), 0) AS media_stelle, " +
                "COUNT(rec.id) AS num_recensioni " +
                "FROM RistorantiTheKnife r " +
                "LEFT JOIN Recensioni rec ON rec.id_ristorante = r.id " +
                "WHERE r.id = ? " +
                "GROUP BY r.id, r.nome, r.nazione, r.citta, r.indirizzo, r.latitudine, r.longitudine, " +
                "r.prezzo_medio, r.delivery, r.prenotazione, r.tipo_cucina, r.descrizione, r.id_gestore";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapRistorante(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore DB findById ristorante: " + e.getMessage(), e);
        }

        return null;
    }

    @Override
    public List<Ristorante> findByGestore(int idGestore) {
        List<Ristorante> risultati = new ArrayList<>();
        String query = "SELECT r.id, r.nome, r.nazione, r.citta, r.indirizzo, r.latitudine, r.longitudine, " +
                "r.prezzo_medio, r.delivery, r.prenotazione, r.tipo_cucina, r.descrizione, r.id_gestore, " +
                "COALESCE(AVG(rec.stelle), 0) AS media_stelle, " +
                "COUNT(rec.id) AS num_recensioni " +
                "FROM RistorantiTheKnife r " +
                "LEFT JOIN Recensioni rec ON rec.id_ristorante = r.id " +
                "WHERE r.id_gestore = ? " +
                "GROUP BY r.id, r.nome, r.nazione, r.citta, r.indirizzo, r.latitudine, r.longitudine, " +
                "r.prezzo_medio, r.delivery, r.prenotazione, r.tipo_cucina, r.descrizione, r.id_gestore " +
                "ORDER BY r.nome";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idGestore);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Ristorante r = mapRistorante(rs);
                risultati.add(r);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore DB findByGestore ristoranti: " + e.getMessage(), e);
        }

        return risultati;
    }

    @Override
    public List<Ristorante> findVicini(double lat, double lon, double raggioKm) {
        List<Ristorante> risultati = new ArrayList<>();
        String query = "SELECT r.id, r.nome, r.nazione, r.citta, r.indirizzo, r.latitudine, r.longitudine, " +
                "r.prezzo_medio, r.delivery, r.prenotazione, r.tipo_cucina, r.descrizione, r.id_gestore, " +
                "COALESCE(AVG(rec.stelle), 0) AS media_stelle, " +
                "COUNT(rec.id) AS num_recensioni, " +
                "(6371 * acos(cos(radians(?)) * cos(radians(r.latitudine)) * " +
                "cos(radians(r.longitudine) - radians(?)) + " +
                "sin(radians(?)) * sin(radians(r.latitudine)))) AS distanza_km " +
                "FROM RistorantiTheKnife r " +
                "LEFT JOIN Recensioni rec ON rec.id_ristorante = r.id " +
                "GROUP BY r.id, r.nome, r.nazione, r.citta, r.indirizzo, r.latitudine, r.longitudine, " +
                "r.prezzo_medio, r.delivery, r.prenotazione, r.tipo_cucina, r.descrizione, r.id_gestore " +
                "HAVING (6371 * acos(cos(radians(?)) * cos(radians(r.latitudine)) * " +
                "cos(radians(r.longitudine) - radians(?)) + " +
                "sin(radians(?)) * sin(radians(r.latitudine)))) <= ? " +
                "ORDER BY distanza_km";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setDouble(1, lat);
            stmt.setDouble(2, lon);
            stmt.setDouble(3, lat);
            stmt.setDouble(4, lat);
            stmt.setDouble(5, lon);
            stmt.setDouble(6, lat);
            stmt.setDouble(7, raggioKm);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Ristorante r = mapRistorante(rs);
                risultati.add(r);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore DB findVicini ristoranti: " + e.getMessage(), e);
        }

        return risultati;
    }

    /**
     * Helper per mappare un ResultSet a un oggetto Ristorante.
     */
    @Override
    public Ristorante inserisci(Ristorante r) {
        String query = "INSERT INTO RistorantiTheKnife " +
                "(nome, nazione, citta, indirizzo, latitudine, longitudine, prezzo_medio, delivery, prenotazione, tipo_cucina, descrizione, id_gestore) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, r.getNome());
            stmt.setString(2, r.getNazione());
            stmt.setString(3, r.getCitta());
            stmt.setString(4, r.getIndirizzo());
            stmt.setDouble(5, r.getLatitudine());
            stmt.setDouble(6, r.getLongitudine());
            stmt.setDouble(7, r.getPrezzoMedio());
            stmt.setBoolean(8, r.isDelivery());
            stmt.setBoolean(9, r.isPrenotazione());
            stmt.setString(10, r.getTipoCucina());
            stmt.setString(11, r.getDescrizione());
            if (r.getIdGestore() > 0) {
                stmt.setInt(12, r.getIdGestore());
            } else {
                stmt.setNull(12, java.sql.Types.INTEGER);
            }

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                r.setId(rs.getInt("id"));
                return r;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore in fase di inserimento ristorante: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public Ristorante modifica(Ristorante r) {
        String query = "UPDATE RistorantiTheKnife SET " +
                "nome = ?, nazione = ?, citta = ?, indirizzo = ?, latitudine = ?, longitudine = ?, " +
                "prezzo_medio = ?, delivery = ?, prenotazione = ?, tipo_cucina = ?, descrizione = ?, id_gestore = ? " +
                "WHERE id = ? RETURNING id";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, r.getNome());
            stmt.setString(2, r.getNazione());
            stmt.setString(3, r.getCitta());
            stmt.setString(4, r.getIndirizzo());
            stmt.setDouble(5, r.getLatitudine());
            stmt.setDouble(6, r.getLongitudine());
            stmt.setDouble(7, r.getPrezzoMedio());
            stmt.setBoolean(8, r.isDelivery());
            stmt.setBoolean(9, r.isPrenotazione());
            stmt.setString(10, r.getTipoCucina());
            stmt.setString(11, r.getDescrizione());
            if (r.getIdGestore() > 0) {
                stmt.setInt(12, r.getIdGestore());
            } else {
                stmt.setNull(12, java.sql.Types.INTEGER);
            }
            stmt.setInt(13, r.getId());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return r;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore in fase di modifica ristorante: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public List<theknife.model.RiepilogoRistorante> riepilogoByGestore(int idGestore) {
        List<theknife.model.RiepilogoRistorante> risultati = new ArrayList<>();
        String query = "SELECT r.id, r.nome, " +
                "COALESCE(AVG(rec.stelle), 0) AS media_stelle, " +
                "COUNT(rec.id) AS num_recensioni " +
                "FROM RistorantiTheKnife r " +
                "LEFT JOIN Recensioni rec ON rec.id_ristorante = r.id " +
                "WHERE r.id_gestore = ? " +
                "GROUP BY r.id, r.nome " +
                "ORDER BY r.nome";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idGestore);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                theknife.model.RiepilogoRistorante rr = new theknife.model.RiepilogoRistorante();
                rr.setId(rs.getInt("id"));
                rr.setNome(rs.getString("nome"));
                rr.setMediaStelle(rs.getDouble("media_stelle"));
                rr.setNumRecensioni(rs.getInt("num_recensioni"));
                risultati.add(rr);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore DB riepilogoByGestore: " + e.getMessage(), e);
        }

        return risultati;
    }

    @Override
    public void aggiungiPreferito(int idUtente, int idRistorante) {
        String query = "INSERT INTO Preferiti (id_utente, id_ristorante) VALUES (?, ?) ON CONFLICT DO NOTHING";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, idUtente);
            stmt.setInt(2, idRistorante);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore DB aggiungiPreferito: " + e.getMessage(), e);
        }
    }

    @Override
    public void rimuoviPreferito(int idUtente, int idRistorante) {
        String query = "DELETE FROM Preferiti WHERE id_utente = ? AND id_ristorante = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, idUtente);
            stmt.setInt(2, idRistorante);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore DB rimuoviPreferito: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Ristorante> findPreferiti(int idUtente) {
        List<Ristorante> risultati = new ArrayList<>();
        String query = "SELECT r.id, r.nome, r.nazione, r.citta, r.indirizzo, r.latitudine, r.longitudine, " +
                "r.prezzo_medio, r.delivery, r.prenotazione, r.tipo_cucina, r.descrizione, r.id_gestore, " +
                "COALESCE(AVG(rec.stelle), 0) AS media_stelle, " +
                "COUNT(rec.id) AS num_recensioni " +
                "FROM RistorantiTheKnife r " +
                "INNER JOIN Preferiti p ON p.id_ristorante = r.id " +
                "LEFT JOIN Recensioni rec ON rec.id_ristorante = r.id " +
                "WHERE p.id_utente = ? " +
                "GROUP BY r.id, r.nome, r.nazione, r.citta, r.indirizzo, r.latitudine, r.longitudine, " +
                "r.prezzo_medio, r.delivery, r.prenotazione, r.tipo_cucina, r.descrizione, r.id_gestore " +
                "ORDER BY r.nome";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, idUtente);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                risultati.add(mapRistorante(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore DB findPreferiti: " + e.getMessage(), e);
        }
        return risultati;
    }

    @Override
    public boolean isPreferito(int idUtente, int idRistorante) {
        String query = "SELECT COUNT(*) FROM Preferiti WHERE id_utente = ? AND id_ristorante = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, idUtente);
            stmt.setInt(2, idRistorante);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore DB isPreferito: " + e.getMessage(), e);
        }
        return false;
    }

    private Ristorante mapRistorante(ResultSet rs) throws SQLException {
        Ristorante r = new Ristorante();
        r.setId(rs.getInt("id"));
        r.setNome(rs.getString("nome"));
        r.setNazione(rs.getString("nazione"));
        r.setCitta(rs.getString("citta"));
        r.setIndirizzo(rs.getString("indirizzo"));
        r.setLatitudine(rs.getDouble("latitudine"));
        r.setLongitudine(rs.getDouble("longitudine"));

        r.setDelivery(rs.getBoolean("delivery"));
        r.setPrenotazione(rs.getBoolean("prenotazione"));
        r.setPrezzoMedio(rs.getDouble("prezzo_medio"));
        r.setTipoCucina(rs.getString("tipo_cucina"));
        r.setDescrizione(rs.getString("descrizione"));
        r.setIdGestore(rs.getInt("id_gestore"));

        try {
            r.setMediaStelle(rs.getDouble("media_stelle"));
        } catch (SQLException e) {
            r.setMediaStelle(0);
        }

        try {
            r.setNumRecensioni(rs.getInt("num_recensioni"));
        } catch (SQLException e) {
            r.setNumRecensioni(0);
        }

        return r;
    }

    private boolean hasColumn(ResultSet rs, String columnLabel) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            String label = metaData.getColumnLabel(i);
            String name = metaData.getColumnName(i);
            if (columnLabel.equalsIgnoreCase(label) || columnLabel.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }
}
