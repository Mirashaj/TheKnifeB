package theknife.shared;

import java.io.Serializable;

/**
 * Oggetto serializzabile che rappresenta la risposta del server al client,
 * contenente l'esito e il payload dei dati.
 * 
 * @author Mirashaj Erik 760453 VA
 * @author GorchynskYi Igor 757184 VA
 * @author Kabuka Dan Mumanga 757708 VA
 * @author Mujeci Lorenzo 757597 VA
 */
public class Response implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean successo;
    private String messaggio;
    private Object payload;

    public Response() {
    }

    public Response(boolean successo, String messaggio, Object payload) {
        this.successo = successo;
        this.messaggio = messaggio;
        this.payload = payload;
    }

    public boolean isSuccesso() {
        return successo;
    }

    public void setSuccesso(boolean successo) {
        this.successo = successo;
    }

    public String getMessaggio() {
        return messaggio;
    }

    public void setMessaggio(String messaggio) {
        this.messaggio = messaggio;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }
}
