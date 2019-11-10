

import java.io.Serializable;
import java.sql.Timestamp;


public class MessaggioLog implements Serializable { // 0

    private TipoMessaggioLog tipo;
    private String mittente;
    private String data;

    public MessaggioLog(TipoMessaggioLog t, String m) {
        tipo = t;
        mittente = m;
        
        Timestamp timeStampCorrente = new Timestamp(System.currentTimeMillis());
        data = timeStampCorrente.toString();
    }
    
    public void setMittente(String m) { mittente = m; }
    public void setTipo(TipoMessaggioLog t) { tipo = t; }
    public void setData(String d) { data = d; }
    
    public String getMittente() { return mittente; }
    public TipoMessaggioLog getTipo() {  return tipo; }
    public String getData() { return data; }
    
}
