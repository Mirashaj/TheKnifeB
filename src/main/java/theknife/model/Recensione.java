package theknife.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Modello che rappresenta una recensione scritta da un cliente per un
 * ristorante.
 * 
 * @author Mirashaj Erik 760453 VA
 * @author GorchynskYi Igor 757184 VA
 * @author Kabuka Dan Mumanga 757708 VA
 * @author Mujeci Lorenzo 757597 VA
 */
public class Recensione implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int idRistorante;
    private int idUtente;
    private String nomeUtente;
    private int stelle;
    private String testo;
    private LocalDateTime dataInserimento;
    private RispostaRecensione risposta;
    private String nomeRistorante;

    public Recensione() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdRistorante() {
        return idRistorante;
    }

    public void setIdRistorante(int idRistorante) {
        this.idRistorante = idRistorante;
    }

    public int getIdUtente() {
        return idUtente;
    }

    public void setIdUtente(int idUtente) {
        this.idUtente = idUtente;
    }

    public String getNomeUtente() {
        return nomeUtente;
    }

    public void setNomeUtente(String nomeUtente) {
        this.nomeUtente = nomeUtente;
    }

    public String getNomeRistorante() {
        return nomeRistorante;
    }

    public void setNomeRistorante(String nomeRistorante) {
        this.nomeRistorante = nomeRistorante;
    }

    public int getStelle() {
        return stelle;
    }

    public void setStelle(int stelle) {
        this.stelle = stelle;
    }

    public String getTesto() {
        return testo;
    }

    public void setTesto(String testo) {
        this.testo = testo;
    }

    public LocalDateTime getDataInserimento() {
        return dataInserimento;
    }

    public void setDataInserimento(LocalDateTime dataInserimento) {
        this.dataInserimento = dataInserimento;
    }

    public RispostaRecensione getRisposta() {
        return risposta;
    }

    public void setRisposta(RispostaRecensione risposta) {
        this.risposta = risposta;
    }

    @Override
    public String toString() {
        return "Recensione{" +
                "id=" + id +
                ", idRistorante=" + idRistorante +
                ", idUtente=" + idUtente +
                ", stelle=" + stelle +
                '}';
    }
}
