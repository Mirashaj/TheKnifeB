package theknife.server.dao;

import java.util.List;
import java.util.Map;

import theknife.model.Ristorante;

/**
 * Interfaccia DAO per le operazioni CRUD sui ristoranti nel database.
 * 
 * @author Mirashaj Erik 760453 VA
 * @author GorchynskYi Igor 757184 VA
 * @author Kabuka Dan Mumanga 757708 VA
 * @author Mujeci Lorenzo 757597 VA
 */
public interface RistoranteDAO {
    List<Ristorante> cerca(Map<String, Object> filtri);

    Ristorante findById(int id);

    List<Ristorante> findByGestore(int idGestore);

    List<theknife.model.RiepilogoRistorante> riepilogoByGestore(int idGestore);

    List<Ristorante> findVicini(double lat, double lon, double raggioKm);

    Ristorante inserisci(Ristorante r);

    Ristorante modifica(Ristorante r);

    void aggiungiPreferito(int idUtente, int idRistorante);

    void rimuoviPreferito(int idUtente, int idRistorante);

    List<Ristorante> findPreferiti(int idUtente);

    boolean isPreferito(int idUtente, int idRistorante);
}
