
import java.io.Serializable;

public class ParametriConfigurazione implements Serializable{
    
    public String nickname;
    
    public int portaServerDiLog;
    public String indirizzoIPServerDiLog;
    
    public String indirizzoIPDatabase;
    public int portaDatabase;
    public String usernameDatabase;
    public String passwordDatabase;
    
    public int topPieChart;
    public int giorniUltimeTaskTabella;
}
