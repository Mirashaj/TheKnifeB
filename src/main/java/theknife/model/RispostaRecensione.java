package theknife.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Modello che rappresenta la risposta di un gestore a una recensione.
 * 
 * @author Mirashaj Erik 760453 VA
 * @author GorchynskYi Igor 757184 VA
 * @author Kabuka Dan Mumanga 757708 VA
 * @author Mujeci Lorenzo 757597 VA
 */
public class RispostaRecensione implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int idRecensione;
    private int idGestore;
    private String testo;
    private LocalDateTime dataRisposta;

    public RispostaRecensione() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdRecensione() {
        return idRecensione;
    }

    public void setIdRecensione(int idRecensione) {
        this.idRecensione = idRecensione;
    }

    public int getIdGestore() {
        return idGestore;
    }

    public void setIdGestore(int idGestore) {
        this.idGestore = idGestore;
    }

    public String getTesto() {
        return testo;
    }

    public void setTesto(String testo) {
        this.testo = testo;
    }

    public LocalDateTime getDataRisposta() {
        return dataRisposta;
    }

    public void setDataRisposta(LocalDateTime dataRisposta) {
        this.dataRisposta = dataRisposta;
    }

    @Override
    public String toString() {
        return "RispostaRecensione{" +
                "id=" + id +
                ", idRecensione=" + idRecensione +
                ", idGestore=" + idGestore +
                '}';
    }
}
