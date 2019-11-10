
import java.net.*;
import java.io.*;

public class SocketServerLog {
    
    public SocketServerLog(){}
                   
    public void inviaEvento(TipoMessaggioLog evLog, String nickname){
        String ipLog = GestoreParametriConfigurazioneXML.parametri.indirizzoIPServerDiLog;
        int portLog = GestoreParametriConfigurazioneXML.parametri.portaServerDiLog;
        try(
            Socket s = new Socket(ipLog, portLog);
            ObjectOutputStream oout = new ObjectOutputStream(s.getOutputStream()))
        {  
            // invio messaggio al serverLog
            MessaggioLog msg = new MessaggioLog(evLog, nickname);
            oout.writeObject(msg);
        }
        catch(IOException e){e.printStackTrace(); System.exit(1);}
    } 
}
