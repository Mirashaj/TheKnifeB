package theknife.shared;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Oggetto serializzabile che rappresenta una richiesta inviata dal client al
 * server, contenente il tipo di operazione e i parametri.
 * 
 * @author Mirashaj Erik 760453 VA
 * @author GorchynskYi Igor 757184 VA
 * @author Kabuka Dan Mumanga 757708 VA
 * @author Mujeci Lorenzo 757597 VA
 */
public class Request implements Serializable {
    private static final long serialVersionUID = 1L;

    private String tipo;
    private Map<String, Object> parametri;

    public Request(String tipo) {
        this.tipo = tipo;
        this.parametri = new HashMap<>();
    }

    public Request(String tipo, Map<String, Object> parametri) {
        this.tipo = tipo;
        this.parametri = parametri != null ? parametri : new HashMap<>();
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Map<String, Object> getParametri() {
        return parametri;
    }

    public void setParametri(Map<String, Object> parametri) {
        this.parametri = parametri;
    }

    public void addParametro(String chiave, Object valore) {
        if (this.parametri == null) {
            this.parametri = new HashMap<>();
        }
        this.parametri.put(chiave, valore);
    }
}
