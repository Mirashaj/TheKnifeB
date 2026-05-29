package theknife.model;

import java.io.Serializable;

/**
 * Modello che rappresenta il riepilogo statistico di un ristorante (media
 * stelle, numero recensioni).
 * 
 * @author Mirashaj Erik 760453 VA
 * @author GorchynskYi Igor 757184 VA
 * @author Kabuka Dan Mumanga 757708 VA
 * @author Mujeci Lorenzo 757597 VA
 */
public class RiepilogoRistorante implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String nome;
    private double mediaStelle;
    private int numRecensioni;

    public RiepilogoRistorante() {
    }

    public RiepilogoRistorante(int id, String nome, double mediaStelle, int numRecensioni) {
        this.id = id;
        this.nome = nome;
        this.mediaStelle = mediaStelle;
        this.numRecensioni = numRecensioni;
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

    @Override
    public String toString() {
        return "RiepilogoRistorante{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", mediaStelle=" + mediaStelle +
                ", numRecensioni=" + numRecensioni +
                '}';
    }
}
