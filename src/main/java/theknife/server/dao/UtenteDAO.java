package theknife.server.dao;

import theknife.model.Utente;

/**
 * Interfaccia DAO per le operazioni CRUD sugli utenti nel database.
 * 
 * @author Mirashaj Erik 760453 VA
 * @author GorchynskYi Igor 757184 VA
 * @author Kabuka Dan Mumanga 757708 VA
 * @author Mujeci Lorenzo 757597 VA
 */
public interface UtenteDAO {
    Utente findByEmail(String email);

    Utente inserisci(Utente u);

    boolean esisteEmail(String email);

    Utente modifica(Utente u);

    boolean elimina(int idUtente);
}
