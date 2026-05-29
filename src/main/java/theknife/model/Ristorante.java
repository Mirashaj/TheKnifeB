package theknife.model;

import java.io.Serializable;

/**
 * Modello che rappresenta un ristorante con tutti i suoi attributi (nome,
 * indirizzo, coordinate, tipo cucina, ecc.).
 * 
 * @author Mirashaj Erik 760453 VA
 * @author GorchynskYi Igor 757184 VA
 * @author Kabuka Dan Mumanga 757708 VA
 * @author Mujeci Lorenzo 757597 VA
 */
public class Ristorante implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String nome;
    private String nazione;
    private String citta;
    private String indirizzo;
    private double latitudine;
    private double longitudine;
    private double prezzoMedio;
    private boolean delivery;
    private boolean prenotazione;
    private double mediaStelle;
    private int numRecensioni;
    private String tipoCucina;
    private String descrizione;
    private int idGestore;

    public Ristorante() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getNazione() {
        return nazione;
    }

    public void setNazione(String nazione) {
        this.nazione = nazione;
    }

    public String getCitta() {
        return citta;
    }

    public void setCitta(String citta) {
        this.citta = citta;
    }

    public String getIndirizzo() {
        return indirizzo;
    }

    public void setIndirizzo(String indirizzo) {
        this.indirizzo = indirizzo;
    }

    public double getLatitudine() {
        return latitudine;
    }

    public void setLatitudine(double latitudine) {
        this.latitudine = latitudine;
    }

    public double getLongitudine() {
        return longitudine;
    }

    public void setLongitudine(double longitudine) {
        this.longitudine = longitudine;
    }

    public double getPrezzoMedio() {
        return prezzoMedio;
    }

    public void setPrezzoMedio(double prezzoMedio) {
        this.prezzoMedio = prezzoMedio;
    }

    public boolean isDelivery() {
        return delivery;
    }

    public void setDelivery(boolean delivery) {
        this.delivery = delivery;
    }

    public boolean isPrenotazione() {
        return prenotazione;
    }

    public void setPrenotazione(boolean prenotazione) {
        this.prenotazione = prenotazione;
    }

    public double getMediaStelle() {
        return mediaStelle;
    }

    public void setMediaStelle(double mediaStelle) {
        this.mediaStelle = mediaStelle;
    }

    public int getNumRecensioni() {
        return numRecensioni;
    }

    public void setNumRecensioni(int numRecensioni) {
        this.numRecensioni = numRecensioni;
    }

    public String getTipoCucina() {
        return tipoCucina;
    }

    public void setTipoCucina(String tipoCucina) {
        this.tipoCucina = tipoCucina;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public int getIdGestore() {
        return idGestore;
    }

    public void setIdGestore(int idGestore) {
        this.idGestore = idGestore;
    }

    @Override
    public String toString() {
        return "Ristorante{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", citta='" + citta + '\'' +
                ", indirizzo='" + indirizzo + '\'' +
                ", tipoCucina='" + tipoCucina + '\'' +
                ", descrizione='" + descrizione + '\'' +
                '}';
    }
}
