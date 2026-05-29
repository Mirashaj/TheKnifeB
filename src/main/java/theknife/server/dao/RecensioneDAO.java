package theknife.server.dao;

import java.util.List;

import theknife.model.Recensione;
import theknife.model.RispostaRecensione;

/**
 * Interfaccia DAO per le operazioni CRUD sulle recensioni nel database.
 * 
 * @author Mirashaj Erik 760453 VA
 * @author GorchynskYi Igor 757184 VA
 * @author Kabuka Dan Mumanga 757708 VA
 * @author Mujeci Lorenzo 757597 VA
 */
public interface RecensioneDAO {
    List<Recensione> findByRistorante(int idRistorante);

    List<Recensione> findByGestore(int idGestore);

    List<Recensione> findByUtente(int idUtente);

    Recensione inserisci(Recensione r);

    Recensione modifica(int idRecensione, int stelle, String testo);

    boolean elimina(int idRecensione);

    RispostaRecensione rispondi(int idRecensione, int idGestore, String testo);

    boolean eliminaRisposta(int idRisposta);
}
