package theknife.server.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import theknife.model.Recensione;
import theknife.model.RispostaRecensione;
import theknife.server.DatabaseConnection;
import theknife.server.dao.RecensioneDAO;

/**
 * Implementazione concreta di RecensioneDAO tramite JDBC e PostgreSQL.
 * 
 * @author Mirashaj Erik 760453 VA
 * @author GorchynskYi Igor 757184 VA
 * @author Kabuka Dan Mumanga 757708 VA
 * @author Mujeci Lorenzo 757597 VA
 */
public class RecensioneDAOImpl implements RecensioneDAO {

    @Override
    public List<Recensione> findByRistorante(int idRistorante) {
        List<Recensione> risultati = new ArrayList<>();
        String query = "SELECT r.*, u.nome AS nome_utente, " +
                "rr.id AS rr_id, rr.id_gestore AS rr_id_gestore, rr.testo AS rr_testo, rr.data_risposta " +
                "FROM Recensioni r " +
                "LEFT JOIN Utenti u ON r.id_utente = u.id " +
                "LEFT JOIN RisposteRecensioni rr ON r.id = rr.id_recensione " +
                "WHERE r.id_ristorante = ? " +
                "ORDER BY r.data_inserimento DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idRistorante);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Recensione rec = mapRecensione(rs);
                risultati.add(rec);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return risultati;
    }

    @Override
    public List<Recensione> findByGestore(int idGestore) {
        List<Recensione> risultati = new ArrayList<>();
        String query = "SELECT rec.*, r.nome AS nome_ristorante, u.nome AS nome_utente, " +
                "rr.id AS rr_id, rr.id_gestore AS rr_id_gestore, rr.testo AS rr_testo, rr.data_risposta " +
                "FROM Recensioni rec " +
                "JOIN RistorantiTheKnife r ON r.id = rec.id_ristorante " +
                "LEFT JOIN Utenti u ON rec.id_utente = u.id " +
                "LEFT JOIN RisposteRecensioni rr ON rr.id_recensione = rec.id " +
                "WHERE r.id_gestore = ? " +
                "ORDER BY rec.data_inserimento DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idGestore);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                risultati.add(mapRecensione(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return risultati;
    }

    @Override
    public List<Recensione> findByUtente(int idUtente) {
        List<Recensione> risultati = new ArrayList<>();
        String query = "SELECT rec.*, r.nome AS nome_ristorante, rr.testo AS testo_risposta " +
                "FROM Recensioni rec " +
                "JOIN RistorantiTheKnife r ON r.id = rec.id_ristorante " +
                "LEFT JOIN RisposteRecensioni rr ON rr.id_recensione = rec.id " +
                "WHERE rec.id_utente = ? " +
                "ORDER BY rec.data_inserimento DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idUtente);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Recensione rec = new Recensione();
                rec.setId(rs.getInt("id"));
                rec.setIdRistorante(rs.getInt("id_ristorante"));
                rec.setIdUtente(rs.getInt("id_utente"));
                rec.setStelle(rs.getInt("stelle"));
                rec.setTesto(rs.getString("testo"));

                Timestamp ts = rs.getTimestamp("data_inserimento");
                if (ts != null) {
                    rec.setDataInserimento(ts.toLocalDateTime());
                }

                rec.setNomeRistorante(rs.getString("nome_ristorante"));

                if (rs.getString("testo_risposta") != null) {
                    RispostaRecensione risposta = new RispostaRecensione();
                    risposta.setTesto(rs.getString("testo_risposta"));
                    rec.setRisposta(risposta);
                }

                risultati.add(rec);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return risultati;
    }

    @Override
    public Recensione inserisci(Recensione r) {
        String query = "INSERT INTO Recensioni " +
                "(id_ristorante, id_utente, stelle, testo) " +
                "VALUES (?, ?, ?, ?) RETURNING id";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, r.getIdRistorante());
            stmt.setInt(2, r.getIdUtente());
            stmt.setInt(3, r.getStelle());
            stmt.setString(4, r.getTesto());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                r.setId(rs.getInt("id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return r;
    }

    @Override
    public Recensione modifica(int idRecensione, int stelle, String testo) {
        String query = "UPDATE Recensioni SET stelle = ?, testo = ? WHERE id = ? RETURNING *";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, stelle);
            stmt.setString(2, testo);
            stmt.setInt(3, idRecensione);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRecensione(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public boolean elimina(int idRecensione) {
        String query = "DELETE FROM Recensioni WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idRecensione);
            int rowsAffected = stmt.executeUpdate();

            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public RispostaRecensione rispondi(int idRecensione, int idGestore, String testo) {
        String query = "INSERT INTO RisposteRecensioni " +
                "(id_recensione, id_gestore, testo) " +
                "VALUES (?, ?, ?) " +
                "ON CONFLICT (id_recensione) DO UPDATE SET " +
                "id_gestore = EXCLUDED.id_gestore, " +
                "testo = EXCLUDED.testo, " +
                "data_risposta = NOW() RETURNING *";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idRecensione);
            stmt.setInt(2, idGestore);
            stmt.setString(3, testo);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                RispostaRecensione risposta = new RispostaRecensione();
                risposta.setId(rs.getInt("id"));
                risposta.setIdRecensione(rs.getInt("id_recensione"));
                risposta.setIdGestore(rs.getInt("id_gestore"));
                risposta.setTesto(rs.getString("testo"));

                Timestamp ts = rs.getTimestamp("data_risposta");
                if (ts != null) {
                    risposta.setDataRisposta(ts.toLocalDateTime());
                }

                return risposta;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public boolean eliminaRisposta(int idRisposta) {
        String query = "DELETE FROM RisposteRecensioni WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idRisposta);
            int rowsAffected = stmt.executeUpdate();

            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Helper per mappare un ResultSet a un oggetto Recensione.
     */
    private Recensione mapRecensione(ResultSet rs) throws SQLException {
        Recensione rec = new Recensione();
        rec.setId(rs.getInt("id"));
        rec.setIdRistorante(rs.getInt("id_ristorante"));
        rec.setIdUtente(rs.getInt("id_utente"));
        rec.setNomeUtente(rs.getString("nome_utente"));
        rec.setStelle(rs.getInt("stelle"));
        rec.setTesto(rs.getString("testo"));

        Timestamp ts = rs.getTimestamp("data_inserimento");
        if (ts != null) {
            rec.setDataInserimento(ts.toLocalDateTime());
        }

        // Mappa la risposta se presente
        int rrId = rs.getInt("rr_id");
        if (!rs.wasNull() && rrId > 0) {
            RispostaRecensione risposta = new RispostaRecensione();
            risposta.setId(rrId);
            risposta.setIdRecensione(rs.getInt("id"));
            risposta.setIdGestore(rs.getInt("rr_id_gestore"));
            risposta.setTesto(rs.getString("rr_testo"));

            Timestamp tsRisposta = rs.getTimestamp("data_risposta");
            if (tsRisposta != null) {
                risposta.setDataRisposta(tsRisposta.toLocalDateTime());
            }

            rec.setRisposta(risposta);
        }

        return rec;
    }
}
