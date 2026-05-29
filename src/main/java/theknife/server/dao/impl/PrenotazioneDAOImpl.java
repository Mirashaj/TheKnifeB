package theknife.server.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import theknife.model.Prenotazione;
import theknife.server.DatabaseConnection;
import theknife.server.dao.PrenotazioneDAO;

/**
 * Implementazione concreta di PrenotazioneDAO tramite JDBC e PostgreSQL.
 * 
 * @author Mirashaj Erik 760453 VA
 * @author GorchynskYi Igor 757184 VA
 * @author Kabuka Dan Mumanga 757708 VA
 * @author Mujeci Lorenzo 757597 VA
 */
public class PrenotazioneDAOImpl implements PrenotazioneDAO {

    @Override
    public Prenotazione aggiorna(Prenotazione p) {
        String query = "UPDATE Prenotazioni SET data_prenotazione = ?, posti = ?, stato = ? WHERE id = ? AND id_utente = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setTimestamp(1, p.getDataPrenotazione() != null ? Timestamp.valueOf(p.getDataPrenotazione()) : null);
            stmt.setInt(2, p.getPosti());
            stmt.setString(3, p.getStato());
            stmt.setInt(4, p.getId());
            stmt.setInt(5, p.getIdUtente());

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                return p;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Prenotazione> findByUtente(int idUtente) {

        List<Prenotazione> risultati = new ArrayList<>();
        String query = "SELECT p.*, r.nome AS nome_ristorante FROM Prenotazioni p " +
                "LEFT JOIN RistorantiTheKnife r ON r.id = p.id_ristorante " +
                "WHERE p.id_utente = ? AND p.data_prenotazione >= NOW() ORDER BY p.data_prenotazione DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idUtente);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Prenotazione p = new Prenotazione();
                p.setId(rs.getInt("id"));
                p.setIdUtente(rs.getInt("id_utente"));
                p.setIdRistorante(rs.getInt("id_ristorante"));
                Timestamp ts = rs.getTimestamp("data_prenotazione");
                if (ts != null)
                    p.setDataPrenotazione(ts.toLocalDateTime());
                p.setPosti(rs.getInt("posti"));
                p.setStato(rs.getString("stato"));
                p.setNomeRistorante(rs.getString("nome_ristorante"));
                risultati.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return risultati;
    }

    @Override
    public List<Prenotazione> findByGestore(int idGestore) {
        List<Prenotazione> risultati = new ArrayList<>();
        String query = "SELECT p.*, r.nome AS nome_ristorante FROM Prenotazioni p " +
                "JOIN RistorantiTheKnife r ON p.id_ristorante = r.id " +
                "WHERE r.id_gestore = ? AND p.data_prenotazione >= NOW() " +
                "ORDER BY p.data_prenotazione DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idGestore);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Prenotazione p = new Prenotazione();
                p.setId(rs.getInt("id"));
                p.setIdUtente(rs.getInt("id_utente"));
                p.setIdRistorante(rs.getInt("id_ristorante"));
                Timestamp ts = rs.getTimestamp("data_prenotazione");
                if (ts != null)
                    p.setDataPrenotazione(ts.toLocalDateTime());
                p.setPosti(rs.getInt("posti"));
                p.setStato(rs.getString("stato"));
                p.setNomeRistorante(rs.getString("nome_ristorante"));
                risultati.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return risultati;
    }

    @Override
    public Prenotazione inserisci(Prenotazione p) {
        String query = "INSERT INTO Prenotazioni (id_utente, id_ristorante, data_prenotazione, posti, stato) " +
                "VALUES (?, ?, ?, ?, ?) RETURNING id";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, p.getIdUtente());
            stmt.setInt(2, p.getIdRistorante());
            if (p.getDataPrenotazione() != null)
                stmt.setTimestamp(3, Timestamp.valueOf(p.getDataPrenotazione()));
            else
                stmt.setTimestamp(3, null);
            stmt.setInt(4, p.getPosti());
            stmt.setString(5, p.getStato());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                p.setId(rs.getInt("id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return p;
    }

    @Override
    public boolean elimina(int idPrenotazione) {
        String query = "DELETE FROM Prenotazioni WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idPrenotazione);
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
