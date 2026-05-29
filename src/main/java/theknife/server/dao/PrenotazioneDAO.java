package theknife.server.dao;

import java.util.List;

import theknife.model.Prenotazione;

/**
 * Interfaccia DAO per le operazioni CRUD sulle prenotazioni nel database.
 * 
 * @author Mirashaj Erik 760453 VA
 * @author GorchynskYi Igor 757184 VA
 * @author Kabuka Dan Mumanga 757708 VA
 * @author Mujeci Lorenzo 757597 VA
 */
public interface PrenotazioneDAO {
    List<Prenotazione> findByUtente(int idUtente);

    List<Prenotazione> findByGestore(int idGestore);

    Prenotazione inserisci(Prenotazione p);

    boolean elimina(int idPrenotazione);

    Prenotazione aggiorna(Prenotazione p);
}
