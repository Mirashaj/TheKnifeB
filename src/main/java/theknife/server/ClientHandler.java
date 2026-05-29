package theknife.server;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Map;

import org.mindrot.jbcrypt.BCrypt;

import theknife.model.Prenotazione;
import theknife.model.Recensione;
import theknife.model.RiepilogoRistorante;
import theknife.model.RispostaRecensione;
import theknife.model.Ristorante;
import theknife.model.Utente;
import theknife.server.dao.impl.PrenotazioneDAOImpl;
import theknife.server.dao.impl.RecensioneDAOImpl;
import theknife.server.dao.impl.RistoranteDAOImpl;
import theknife.server.dao.impl.UtenteDAOImpl;
import theknife.shared.Request;
import theknife.shared.Response;

/**
 * Gestore della comunicazione con un singolo client connesso. Esegue in un
 * thread separato e smista le richieste ai DAO appropriati.
 * 
 * @author Mirashaj Erik 760453 VA
 * @author GorchynskYi Igor 757184 VA
 * @author Kabuka Dan Mumanga 757708 VA
 * @author Mujeci Lorenzo 757597 VA
 */
public class ClientHandler implements Runnable {

    private Socket socket;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;

    private RistoranteDAOImpl ristoranteDAO = new RistoranteDAOImpl();
    private UtenteDAOImpl utenteDAO = new UtenteDAOImpl();
    private RecensioneDAOImpl recensioneDAO = new RecensioneDAOImpl();
    private PrenotazioneDAOImpl prenotazioneDAO = new PrenotazioneDAOImpl();

    /**
     * Costruttore per inizializzare la gestione del client.
     *
     * @param socket Il socket connesso al client.
     */
    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    /**
     * Esegue il ciclo principale di ascolto e gestione delle richieste provenienti
     * dal client.
     */
    @Override
    public void run() {
        try {
            // Inizializza gli stream di I/O
            oos = new ObjectOutputStream(socket.getOutputStream());
            oos.flush();
            ois = new ObjectInputStream(socket.getInputStream());

            System.out.println("ClientHandler avviato per il client: " + socket.getInetAddress());

            // Loop di ricezione e elaborazione delle request
            while (true) {
                Object obj = ois.readObject();
                if (!(obj instanceof Request)) {
                    continue;
                }

                Request request = (Request) obj;
                Response response = elaboraRequest(request);

                // Invia la response al client
                oos.writeObject(response);
                oos.flush();

                // Se è una richiesta di disconnessione, esci dal loop
                if ("DISCONNECT".equals(request.getTipo())) {
                    break;
                }
            }

        } catch (EOFException e) {
            System.out.println("Client disconnesso.");
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Errore nella comunicazione con il client: " + e.getMessage());
        } finally {
            chiudi();
        }
    }

    /**
     * Elabora la request ricevuta dal client.
     *
     * @param request L'oggetto Request ricevuto.
     * @return L'oggetto Response da rispedire al client.
     */
    private Response elaboraRequest(Request request) {
        String tipo = request.getTipo();
        Map<String, Object> params = request.getParametri();

        System.out.println("[DEBUG] Received request type: " + tipo);

        try {
            switch (tipo) {
                case "LOGIN":
                    return handleLogin(params);

                case "REGISTRAZIONE":
                    return handleRegistrazione(params);

                case "CERCA_RISTORANTE":
                    return handleCercaRistorante(params);

                case "DETTAGLIO_RISTORANTE":
                    return handleDettaglioRistorante(params);

                case "VISUALIZZA_RECENSIONI":
                    return handleVisualizzaRecensioni(params);

                case "AGGIUNGI_PREFERITO":
                    return handleAggiungiPreferito(params);

                case "RIMUOVI_PREFERITO":
                    return handleRimuoviPreferito(params);

                case "VISUALIZZA_PREFERITI":
                    return handleVisualizzaPreferiti(params);

                case "AGGIUNGI_RECENSIONE":
                    return handleAggiungiRecensione(params);

                case "MODIFICA_RECENSIONE":
                    return handleModificaRecensione(params);

                case "ELIMINA_RECENSIONE":
                    return handleEliminaRecensione(params);

                case "AGGIUNGI_RISTORANTE":
                    return handleAggiungiRistorante(params);

                case "MODIFICA_RISTORANTE":
                    return handleModificaRistorante(params);

                case "RIEPILOGO_GESTORE":
                    return handleRiepilogoGestore(params);

                case "RISTORANTI_GESTORE":
                    return handleRistorantiGestore(params);

                case "RECENSIONI_GESTORE":
                    return handleRecensioniGestore(params);

                case "RISPONDI_RECENSIONE":
                    return handleRispondiRecensione(params);

                case "ELIMINA_RISPOSTA":
                    return handleEliminaRisposta(params);

                case "RISTORANTI_VICINI":
                    return handleRistorantiVicini(params);

                case "VERIFICA_PREFERITO":
                    return handleVerificaPreferito(params);

                case "MIE_RECENSIONI":
                    return handleMieRecensioni(params);

                case "AGGIUNGI_PRENOTAZIONE":
                    return handleAggiungiPrenotazione(params);

                case "VISUALIZZA_PRENOTAZIONI":
                    return handleVisualizzaPrenotazioni(params);

                case "ELIMINA_PRENOTAZIONE":
                    return handleEliminaPrenotazione(params);

                case "MODIFICA_PRENOTAZIONE":
                    return handleModificaPrenotazione(params);

                case "PRENOTAZIONI_GESTORE":
                    return handlePrenotazioniGestore(params);

                case "MODIFICA_UTENTE":
                    return handleModificaUtente(params);

                case "ELIMINA_UTENTE":
                    return handleEliminaUtente(params);

                case "DISCONNECT":
                    return new Response(true, "Disconnect confirmed.", null);

                default:
                    return new Response(false, "Unknown request: " + tipo, null);
            }
        } catch (Exception e) {
            return new Response(false, "Error: " + e.getMessage(), null);
        }
    }

    private Response handleLogin(Map<String, Object> params) {
        String email = (String) params.get("email");
        String password = (String) params.get("password");

        if (email == null || password == null) {
            return new Response(false, "Email and password are required.", null);
        }

        Utente utente = utenteDAO.findByEmail(email);
        if (utente == null) {
            return new Response(false, "Invalid email or password.", null);
        }

        // Verifica la password con BCrypt
        if (!BCrypt.checkpw(password, utente.getPasswordHash())) {
            return new Response(false, "Invalid email or password.", null);
        }

        return new Response(true, "Login successful.", utente);
    }

    private Response handleRegistrazione(Map<String, Object> params) {
        String nome = (String) params.get("nome");
        String cognome = (String) params.get("cognome");
        String email = (String) params.get("email");
        String password = (String) params.get("password");
        String ruolo = (String) params.get("ruolo");

        if (nome == null || cognome == null || email == null || password == null || ruolo == null) {
            return new Response(false, "Missing required fields.", null);
        }

        if (utenteDAO.esisteEmail(email)) {
            return new Response(false, "Email already registered.", null);
        }

        Utente utente = new Utente();
        utente.setNome(nome);
        utente.setCognome(cognome);
        utente.setEmail(email);
        utente.setPasswordHash(BCrypt.hashpw(password, BCrypt.gensalt()));
        utente.setRuolo(ruolo);

        if (params.containsKey("dataNascita")) {
            utente.setDataNascita((java.time.LocalDate) params.get("dataNascita"));
        }

        if (params.containsKey("domicilio")) {
            utente.setDomicilio((String) params.get("domicilio"));
        }

        utente = utenteDAO.inserisci(utente);

        return new Response(true, "Registration completed.", utente);
    }

    private Response handleCercaRistorante(Map<String, Object> params) {
        List<Ristorante> risultati = ristoranteDAO.cerca(params);
        return new Response(true, "Search completed.", risultati);
    }

    private Response handleDettaglioRistorante(Map<String, Object> params) {
        Integer idRistorante = (Integer) params.get("idRistorante");
        if (idRistorante == null) {
            return new Response(false, "Missing restaurant ID.", null);
        }

        Ristorante r = ristoranteDAO.findById(idRistorante);
        if (r == null) {
            return new Response(false, "Restaurant not found.", null);
        }

        return new Response(true, "Restaurant details.", r);
    }

    private Response handleVisualizzaRecensioni(Map<String, Object> params) {
        Integer idRistorante = (Integer) params.get("idRistorante");
        if (idRistorante == null) {
            return new Response(false, "Missing restaurant ID.", null);
        }

        List<Recensione> recensioni = recensioneDAO.findByRistorante(idRistorante);
        return new Response(true, "Reviews retrieved.", recensioni);
    }

    private Response handleAggiungiPreferito(Map<String, Object> params) {
        try {
            Integer idUtente = (Integer) params.get("idUtente");
            Integer idRistorante = (Integer) params.get("idRistorante");

            if (idUtente == null || idRistorante == null) {
                return new Response(false, "Missing required parameters.", null);
            }

            ristoranteDAO.aggiungiPreferito(idUtente, idRistorante);
            return new Response(true, "Favorite added.", true);
        } catch (Exception e) {
            return new Response(false, "Error: " + e.getMessage(), null);
        }
    }

    private Response handleRimuoviPreferito(Map<String, Object> params) {
        try {
            Integer idUtente = (Integer) params.get("idUtente");
            Integer idRistorante = (Integer) params.get("idRistorante");

            if (idUtente == null || idRistorante == null) {
                return new Response(false, "Missing required parameters.", null);
            }

            ristoranteDAO.rimuoviPreferito(idUtente, idRistorante);
            return new Response(true, "Favorite removed.", true);
        } catch (Exception e) {
            return new Response(false, "Error: " + e.getMessage(), null);
        }
    }

    private Response handleVisualizzaPreferiti(Map<String, Object> params) {
        try {
            Integer idUtente = (Integer) params.get("idUtente");

            if (idUtente == null) {
                return new Response(false, "Missing user ID.", null);
            }

            List<Ristorante> lista = ristoranteDAO.findPreferiti(idUtente);
            return new Response(true, "Favorites retrieved.", lista);
        } catch (Exception e) {
            return new Response(false, "Error: " + e.getMessage(), null);
        }
    }

    private Response handleAggiungiRecensione(Map<String, Object> params) {
        Integer idUtente = (Integer) params.get("idUtente");
        Integer idRistorante = (Integer) params.get("idRistorante");
        Integer stelle = (Integer) params.get("stelle");
        String testo = (String) params.get("testo");

        if (idUtente == null || idRistorante == null || stelle == null) {
            return new Response(false, "Missing required parameters.", null);
        }

        Recensione r = new Recensione();
        r.setIdUtente(idUtente);
        r.setIdRistorante(idRistorante);
        r.setStelle(stelle);
        r.setTesto(testo);

        r = recensioneDAO.inserisci(r);
        if (r.getId() > 0) {
            return new Response(true, "Review added.", r);
        }

        return new Response(false, "Error adding review.", null);
    }

    private Response handleModificaRecensione(Map<String, Object> params) {
        Integer idRecensione = (Integer) params.get("idRecensione");
        Integer stelle = (Integer) params.get("stelle");
        String testo = (String) params.get("testo");

        if (idRecensione == null || stelle == null) {
            return new Response(false, "Missing required parameters.", null);
        }

        Recensione r = recensioneDAO.modifica(idRecensione, stelle, testo);
        if (r != null) {
            return new Response(true, "Review updated.", r);
        }

        return new Response(false, "Error updating review.", null);
    }

    private Response handleEliminaRecensione(Map<String, Object> params) {
        Integer idRecensione = (Integer) params.get("idRecensione");
        if (idRecensione == null) {
            return new Response(false, "Missing review ID.", null);
        }

        boolean result = recensioneDAO.elimina(idRecensione);
        if (result) {
            return new Response(true, "Review deleted.", true);
        }

        return new Response(false, "Error deleting review.", null);
    }

    private Response handleAggiungiRistorante(Map<String, Object> params) {
        try {
            String nome = (String) params.get("nome");
            String nazione = (String) params.get("nazione");
            String citta = (String) params.get("citta");
            String indirizzo = (String) params.get("indirizzo");
            Number latitudineObj = (Number) params.get("latitudine");
            Number longitudineObj = (Number) params.get("longitudine");
            Number prezzoMedioObj = (Number) params.get("prezzoMedio");
            Boolean delivery = (Boolean) params.get("delivery");
            Boolean prenotazione = (Boolean) params.get("prenotazione");
            String tipoCucina = (String) params.get("tipoCucina");
            Integer idGestore = (Integer) params.get("idGestore");

            if (nome == null || citta == null || nazione == null || indirizzo == null || latitudineObj == null
                    || longitudineObj == null) {
                return new Response(false, "Missing required parameters.", null);
            }

            Ristorante ristorante = new Ristorante();
            ristorante.setNome(nome);
            ristorante.setNazione(nazione);
            ristorante.setCitta(citta);
            ristorante.setIndirizzo(indirizzo);
            ristorante.setLatitudine(latitudineObj.doubleValue());
            ristorante.setLongitudine(longitudineObj.doubleValue());
            if (prezzoMedioObj != null) {
                ristorante.setPrezzoMedio(prezzoMedioObj.doubleValue());
            }
            if (delivery != null)
                ristorante.setDelivery(delivery);
            if (prenotazione != null)
                ristorante.setPrenotazione(prenotazione);
            ristorante.setTipoCucina(tipoCucina);
            if (idGestore != null) {
                ristorante.setIdGestore(idGestore);
            }

            Ristorante inserito = ristoranteDAO.inserisci(ristorante);
            return new Response(true, "Restaurant added.", inserito);
        } catch (Exception e) {
            return new Response(false, "Error: " + e.getMessage(), null);
        }
    }

    private Response handleModificaRistorante(Map<String, Object> params) {
        try {
            Integer idRistorante = (Integer) params.get("idRistorante");
            String nome = (String) params.get("nome");
            String nazione = (String) params.get("nazione");
            String citta = (String) params.get("citta");
            String indirizzo = (String) params.get("indirizzo");
            Number latitudineObj = (Number) params.get("latitudine");
            Number longitudineObj = (Number) params.get("longitudine");
            Number prezzoMedioObj = (Number) params.get("prezzoMedio");
            Boolean delivery = (Boolean) params.get("delivery");
            Boolean prenotazione = (Boolean) params.get("prenotazione");
            String tipoCucina = (String) params.get("tipoCucina");
            Integer idGestore = (Integer) params.get("idGestore");

            if (idRistorante == null || nome == null || citta == null || nazione == null || indirizzo == null
                    || latitudineObj == null || longitudineObj == null) {
                return new Response(false, "Missing required parameters.", null);
            }

            Ristorante ristorante = ristoranteDAO.findById(idRistorante);
            if (ristorante == null) {
                return new Response(false, "Restaurant not found.", null);
            }

            if (idGestore != null && ristorante.getIdGestore() != idGestore) {
                return new Response(false, "Unauthorized to modify this restaurant.", null);
            }

            ristorante.setNome(nome);
            ristorante.setNazione(nazione);
            ristorante.setCitta(citta);
            ristorante.setIndirizzo(indirizzo);
            ristorante.setLatitudine(latitudineObj.doubleValue());
            ristorante.setLongitudine(longitudineObj.doubleValue());
            if (prezzoMedioObj != null)
                ristorante.setPrezzoMedio(prezzoMedioObj.doubleValue());
            if (delivery != null)
                ristorante.setDelivery(delivery);
            if (prenotazione != null)
                ristorante.setPrenotazione(prenotazione);
            if (tipoCucina != null)
                ristorante.setTipoCucina(tipoCucina);

            Ristorante aggiornato = ristoranteDAO.modifica(ristorante);
            if (aggiornato != null) {
                return new Response(true, "Restaurant updated.", aggiornato);
            }
            return new Response(false, "Unable to modify restaurant.", null);
        } catch (Exception e) {
            return new Response(false, "Error: " + e.getMessage(), null);
        }
    }

    private Response handleRiepilogoGestore(Map<String, Object> params) {
        try {
            Integer idGestore = (Integer) params.get("idGestore");
            if (idGestore == null) {
                return new Response(false, "Missing manager ID.", null);
            }

            List<RiepilogoRistorante> lista = ristoranteDAO.riepilogoByGestore(idGestore);
            return new Response(true, "Manager summary.", lista);
        } catch (Exception e) {
            return new Response(false, "Error: " + e.getMessage(), null);
        }
    }

    private Response handleRistorantiGestore(Map<String, Object> params) {
        try {
            Integer idGestore = (Integer) params.get("idGestore");
            if (idGestore == null) {
                return new Response(false, "Missing manager ID.", null);
            }

            List<Ristorante> lista = ristoranteDAO.findByGestore(idGestore);
            return new Response(true, "Manager restaurants.", lista);
        } catch (Exception e) {
            return new Response(false, "Error: " + e.getMessage(), null);
        }
    }

    private Response handleRecensioniGestore(Map<String, Object> params) {
        try {
            Integer idGestore = (Integer) params.get("idGestore");
            if (idGestore == null) {
                return new Response(false, "Missing manager ID.", null);
            }

            List<Recensione> lista = recensioneDAO.findByGestore(idGestore);
            return new Response(true, "Manager reviews.", lista);
        } catch (Exception e) {
            return new Response(false, "Error: " + e.getMessage(), null);
        }
    }

    private Response handleRispondiRecensione(Map<String, Object> params) {
        Integer idRecensione = (Integer) params.get("idRecensione");
        Integer idGestore = (Integer) params.get("idGestore");
        String testo = (String) params.get("testo");

        if (idRecensione == null || idGestore == null || testo == null) {
            return new Response(false, "Missing required parameters.", null);
        }

        RispostaRecensione risposta = recensioneDAO.rispondi(idRecensione, idGestore, testo);
        if (risposta != null) {
            return new Response(true, "Reply added.", risposta);
        }

        return new Response(false, "Error adding reply.", null);
    }

    private Response handleEliminaRisposta(Map<String, Object> params) {
        Integer idRisposta = (Integer) params.get("idRisposta");
        if (idRisposta == null) {
            return new Response(false, "Missing reply ID.", null);
        }

        boolean result = recensioneDAO.eliminaRisposta(idRisposta);
        if (result) {
            return new Response(true, "Reply deleted.", true);
        }

        return new Response(false, "Error deleting reply.", null);
    }

    private Response handleVerificaPreferito(Map<String, Object> params) {
        try {
            Integer idUtente = (Integer) params.get("idUtente");
            Integer idRistorante = (Integer) params.get("idRistorante");

            if (idUtente == null || idRistorante == null) {
                return new Response(false, "Missing required parameters.", null);
            }

            boolean isPreferito = ristoranteDAO.isPreferito(idUtente, idRistorante);
            return new Response(true, "Ok.", isPreferito);
        } catch (Exception e) {
            return new Response(false, "Error: " + e.getMessage(), null);
        }
    }

    private Response handleMieRecensioni(Map<String, Object> params) {
        try {
            Integer idUtente = (Integer) params.get("idUtente");
            if (idUtente == null) {
                return new Response(false, "Missing user ID.", null);
            }

            List<Recensione> lista = recensioneDAO.findByUtente(idUtente);
            return new Response(true, "User reviews.", lista);
        } catch (Exception e) {
            return new Response(false, "Error: " + e.getMessage(), null);
        }
    }

    private Response handleAggiungiPrenotazione(Map<String, Object> params) {
        try {
            Integer idUtente = (Integer) params.get("idUtente");
            Integer idRistorante = (Integer) params.get("idRistorante");
            java.time.LocalDateTime data = (java.time.LocalDateTime) params.get("dataPrenotazione");
            Number postiObj = (Number) params.get("posti");

            if (idUtente == null || idRistorante == null) {
                return new Response(false, "Missing required parameters.", null);
            }

            int posti = (postiObj != null) ? postiObj.intValue() : 1;

            Prenotazione p = new Prenotazione();
            p.setIdUtente(idUtente);
            p.setIdRistorante(idRistorante);
            p.setDataPrenotazione(data);
            p.setPosti(posti);
            p.setStato(params.get("stato") != null ? (String) params.get("stato") : "CONFERMATA");

            p = prenotazioneDAO.inserisci(p);
            if (p.getId() > 0) {
                return new Response(true, "Booking added.", p);
            }

            return new Response(false, "Error adding booking.", null);
        } catch (Exception e) {
            return new Response(false, "Error: " + e.getMessage(), null);
        }
    }

    private Response handleVisualizzaPrenotazioni(Map<String, Object> params) {
        try {
            Integer idUtente = (Integer) params.get("idUtente");
            if (idUtente == null) {
                return new Response(false, "Missing user ID.", null);
            }
            java.util.List<Prenotazione> lista = prenotazioneDAO.findByUtente(idUtente);
            return new Response(true, "Bookings retrieved.", lista);
        } catch (Exception e) {
            return new Response(false, "Error: " + e.getMessage(), null);
        }
    }

    private Response handlePrenotazioniGestore(Map<String, Object> params) {
        try {
            Integer idGestore = (Integer) params.get("idGestore");
            if (idGestore == null) {
                return new Response(false, "Missing manager ID.", null);
            }
            java.util.List<Prenotazione> lista = prenotazioneDAO.findByGestore(idGestore);
            return new Response(true, "Bookings retrieved.", lista);
        } catch (Exception e) {
            return new Response(false, "Error: " + e.getMessage(), null);
        }
    }

    private Response handleEliminaPrenotazione(Map<String, Object> params) {
        try {
            Integer idPrenotazione = (Integer) params.get("idPrenotazione");
            if (idPrenotazione == null) {
                return new Response(false, "Missing booking ID.", null);
            }
            boolean ok = prenotazioneDAO.elimina(idPrenotazione);
            if (ok)
                return new Response(true, "Booking deleted.", true);
            return new Response(false, "Error deleting booking.", null);
        } catch (Exception e) {
            return new Response(false, "Error: " + e.getMessage(), null);
        }
    }

    private Response handleModificaPrenotazione(Map<String, Object> params) {
        try {
            Integer idUtente = (Integer) params.get("idUtente");
            Integer idPrenotazione = (Integer) params.get("idPrenotazione");
            java.time.LocalDateTime data = (java.time.LocalDateTime) params.get("dataPrenotazione");
            Number postiObj = (Number) params.get("posti");

            if (idUtente == null || idPrenotazione == null) {
                return new Response(false, "Missing required parameters.", null);
            }
            if (data == null) {
                return new Response(false, "Missing booking date.", null);
            }
            int posti = (postiObj != null) ? postiObj.intValue() : 1;
            if (posti < 1) {
                return new Response(false, "Invalid number of people.", null);
            }

            Prenotazione p = new Prenotazione();
            p.setId(idPrenotazione);
            p.setIdUtente(idUtente);
            p.setDataPrenotazione(data);
            p.setPosti(posti);
            p.setStato(params.get("stato") != null ? (String) params.get("stato") : "CONFERMATA");

            Prenotazione aggiornata = prenotazioneDAO.aggiorna(p);
            if (aggiornata != null) {
                return new Response(true, "Booking updated.", aggiornata);
            }
            return new Response(false, "Unable to update booking.", null);
        } catch (Exception e) {
            return new Response(false, "Error: " + e.getMessage(), null);
        }
    }

    private Response handleRistorantiVicini(Map<String, Object> params) {
        try {
            String luogo = (String) params.get("luogo");
            if (luogo == null) {
                return new Response(false, "Missing location.", null);
            }

            params.put("limite", 20); // Limita a 20 risultati

            List<Ristorante> risultati = ristoranteDAO.cerca(params);
            return new Response(true, "Nearby restaurants.", risultati);
        } catch (Exception e) {
            return new Response(false, "Error: " + e.getMessage(), null);
        }
    }

    private Response handleModificaUtente(Map<String, Object> params) {
        try {
            Integer idUtente = (Integer) params.get("idUtente");
            String nome = (String) params.get("nome");
            String cognome = (String) params.get("cognome");
            String email = (String) params.get("email");
            String password = (String) params.get("password");

            if (idUtente == null || nome == null || cognome == null || email == null) {
                return new Response(false, "Missing required parameters.", null);
            }

            Utente existing = utenteDAO.findByEmail(email);
            if (existing != null && existing.getId() != idUtente) {
                return new Response(false, "The email entered is already in use.", null);
            }

            Utente u = new Utente();
            u.setId(idUtente);
            u.setNome(nome);
            u.setCognome(cognome);
            u.setEmail(email);

            if (password != null && !password.trim().isEmpty()) {
                u.setPasswordHash(BCrypt.hashpw(password, BCrypt.gensalt()));
            }

            Utente aggiornato = utenteDAO.modifica(u);
            if (aggiornato != null) {
                return new Response(true, "Profile updated successfully.", aggiornato);
            }
            return new Response(false, "Error updating profile.", null);
        } catch (Exception e) {
            return new Response(false, "Error: " + e.getMessage(), null);
        }
    }

    private Response handleEliminaUtente(Map<String, Object> params) {
        try {
            Integer idUtente = (Integer) params.get("idUtente");
            if (idUtente == null) {
                return new Response(false, "Missing user ID.", null);
            }

            boolean eliminato = utenteDAO.elimina(idUtente);
            if (eliminato) {
                return new Response(true, "Account deleted.", true);
            }
            return new Response(false, "Error deleting account.", null);
        } catch (Exception e) {
            return new Response(false, "Error: " + e.getMessage(), null);
        }
    }

    private void chiudi() {
        try {
            if (ois != null)
                ois.close();
            if (oos != null)
                oos.close();
            if (socket != null)
                socket.close();
            System.out.println("Connessione client chiusa.");
        } catch (IOException e) {
            System.err.println("Errore durante la chiusura della connessione: " + e.getMessage());
        }
    }
}
