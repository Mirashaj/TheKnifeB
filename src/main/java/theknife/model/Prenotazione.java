package theknife.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Modello che rappresenta una prenotazione di un tavolo in un ristorante.
 * 
 * @author Mirashaj Erik 760453 VA
 * @author GorchynskYi Igor 757184 VA
 * @author Kabuka Dan Mumanga 757708 VA
 * @author Mujeci Lorenzo 757597 VA
 */
public class Prenotazione implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private int idUtente;
    private int idRistorante;
    private String nomeRistorante;
    private LocalDateTime dataPrenotazione;
    private int posti;
    private String stato;

    public Prenotazione() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdUtente() {
        return idUtente;
    }

    public void setIdUtente(int idUtente) {
        this.idUtente = idUtente;
    }

    public int getIdRistorante() {
        return idRistorante;
    }

    public void setIdRistorante(int idRistorante) {
        this.idRistorante = idRistorante;
    }

    public String getNomeRistorante() {
        return nomeRistorante;
    }

    public void setNomeRistorante(String nomeRistorante) {
        this.nomeRistorante = nomeRistorante;
    }

    public LocalDateTime getDataPrenotazione() {
        return dataPrenotazione;
    }

    public void setDataPrenotazione(LocalDateTime dataPrenotazione) {
        this.dataPrenotazione = dataPrenotazione;
    }

    public int getPosti() {
        return posti;
    }

    public void setPosti(int posti) {
        this.posti = posti;
    }

    public String getStato() {
        return stato;
    }

    public void setStato(String stato) {
        this.stato = stato;
    }

    @Override
    public String toString() {
        return "Prenotazione{id=" + id + ", ristorante='" + nomeRistorante + "', data=" + dataPrenotazione + ", posti="
                + posti + "}";
    }
}
