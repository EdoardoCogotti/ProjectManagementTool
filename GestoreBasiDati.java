import java.sql.*;
import java.text.DateFormat;
import java.util.Date;
import java.text.SimpleDateFormat;
import javafx.collections.*;

public class GestoreBasiDati {

    private static GestoreParametriConfigurazioneXML gestoreParametriConfig;
    private static GestoreBasiDati gbd;
    
    private ObservableList<TaskCompletate> olCompletate;
    private ObservableList<TaskDaCompletare> olDaCompletareIceBox;
    private ObservableList<TaskDaCompletare> olDaCompletareInProgress;
    private ObservableList<TaskDaCompletare> olDaCompletareTesting;
    private ObservableList<TaskCompletate> statistiche;

    private static String indirizzoIPDatabase;
    private static int portaDatabase;
    private static String usernameDatabase;
    private static String passwordDatabase;
    private static int topPieChart;
    private static int giorniUltimeTaskTabella;
    
    //Singleton Pattern
    private GestoreBasiDati() {
        try {
            System.out.println("Parametri di configurazione caricati correttamente.");
            gestoreParametriConfig = new GestoreParametriConfigurazioneXML("C:\\prg\\myapps\\ProjectManagement\\myfiles\\ParametriConfigurazione.xml", 
                    "C:\\prg\\myapps\\ProjectManagement\\myfiles\\ValidaParametriConfigurazione.xsd");
            gestoreParametriConfig.leggiFileDiConfigurazione();
        } catch (Exception e) {
            System.out.println("Errore durante la lettura del file di configurazione " + e.getMessage());
            System.exit(0);
        }    
        setParam();
    }
    
    public static GestoreBasiDati getGestoreBasiDiDati(){
        if(gbd==null){
            gbd = new GestoreBasiDati();
        }
        return gbd;    
    }
    
    
    public void setParam(){
        indirizzoIPDatabase = GestoreParametriConfigurazioneXML.parametri.indirizzoIPDatabase;
        portaDatabase= GestoreParametriConfigurazioneXML.parametri.portaDatabase;
        usernameDatabase = GestoreParametriConfigurazioneXML.parametri.usernameDatabase;
        passwordDatabase = GestoreParametriConfigurazioneXML.parametri.passwordDatabase;
        topPieChart = GestoreParametriConfigurazioneXML.parametri.topPieChart;
        giorniUltimeTaskTabella = GestoreParametriConfigurazioneXML.parametri.giorniUltimeTaskTabella;

        olDaCompletareIceBox = FXCollections.observableArrayList(); 
        olDaCompletareInProgress = FXCollections.observableArrayList(); 
        olDaCompletareTesting = FXCollections.observableArrayList(); 
    }
    
    public void caricaTaskDaCompletare(int stadio){
        ObservableList<TaskDaCompletare> aux = getOlDaCompletare(stadio);
        try ( Connection co = DriverManager.getConnection("jdbc:mysql://"+indirizzoIPDatabase+":"+portaDatabase+"/lavagna", usernameDatabase, passwordDatabase);  
            Statement st = co.createStatement(); 
        ) {  
            String query = "SELECT nomeutente, nometask, duratatask, descrizione FROM taskdacompletare WHERE fase=" + Integer.toString(stadio);
            ResultSet rs = st.executeQuery(query);    
            while (rs.next()) 
                aux.add(new TaskDaCompletare(rs.getString("nomeutente"),
                      rs.getString("nometask"), rs.getString("descrizione"), rs.getInt("duratatask"), stadio));
            co.close();
        } catch (SQLException e) {System.err.println(e.getMessage());}        
    }   
 
    public void caricaTaskCompletate(){
        olCompletate = FXCollections.observableArrayList();
         try ( Connection co = DriverManager.getConnection("jdbc:mysql://"+indirizzoIPDatabase+":"+portaDatabase+"/lavagna", usernameDatabase, passwordDatabase); 
            Statement st = co.createStatement(); 
        ) { 
            ResultSet rs = st.executeQuery("SELECT nomeutente, nometask,duratatask, datacompletamento, completata FROM taskcompletate "
                    + "WHERE DataCompletamento > curdate() - INTERVAL "+giorniUltimeTaskTabella + " DAY OR 1=1"); 
            while (rs.next()) 
              olCompletate.add(new TaskCompletate(rs.getString("nomeutente"), rs.getInt("duratatask"),
                      rs.getString("datacompletamento"),rs.getString("nometask"),-1, rs.getBoolean("completata"))); //-1 perchè non mi interessa Tot
            co.close();
        } catch (SQLException e) {System.err.println(e.getMessage());}     
    }
    
    public void insMyTask(String user, String nomeTask, String des, int durata){
   
    try ( Connection co = DriverManager.getConnection("jdbc:mysql://"+indirizzoIPDatabase+":"+portaDatabase+"/lavagna", usernameDatabase, passwordDatabase);  
            Statement st = co.createStatement(); 
            PreparedStatement ps = co.prepareStatement("INSERT INTO taskdacompletare(NomeUtente, NomeTask, Descrizione, DurataTask,Fase)"
                    + " VALUES ( ?, ? ,?,?, 0)");
        ) { 
            ps.setString(1,user);
            ps.setString(2,nomeTask);
            ps.setString(3, des);
            ps.setInt(4, durata);
            ps.executeUpdate(); 
            gbd.getOlDaCompletare(0).add(new TaskDaCompletare(user,nomeTask,des,durata, 0));
            co.close();
        } catch (SQLException e) {System.err.println(e.getMessage());}   
    }       
    
    public void aggiornaStadio(int f, String user){
        try ( Connection co = DriverManager.getConnection("jdbc:mysql://"+indirizzoIPDatabase+":"+portaDatabase+"/lavagna", usernameDatabase, passwordDatabase);  
            PreparedStatement ps = co.prepareStatement("UPDATE taskdacompletare SET fase=? WHERE nomeutente=? ");
        ) { 
            ps.setInt(1, f);
            ps.setString(2, user);
            ps.executeUpdate();
            System.out.println("Aggiornamento stadi in DB completato");
            co.close();
        } catch (SQLException e) {System.err.println(e.getMessage());}   
    }
    
    public void inviaTaskCompletata(String user, String descrizione, boolean complete){
        TaskDaCompletare tdc;
        try ( Connection co = DriverManager.getConnection("jdbc:mysql://"+indirizzoIPDatabase+":"+portaDatabase+"/lavagna", usernameDatabase, passwordDatabase);  
            Statement st = co.createStatement(); //10
            PreparedStatement ps = co.prepareStatement("INSERT INTO taskcompletate(NomeUtente, NomeTask, DurataTask,"
                    + " DataCompletamento, DescrizioneTask, Completata) VALUES ( ?, ? , ?, ?, ?, ?)");
            PreparedStatement psDelete = co.prepareStatement("DELETE FROM taskdacompletare WHERE nomeutente= \""+user+"\"");
        ) { 
            ResultSet rs = st.executeQuery("SELECT nometask, nomeutente, duratatask, descrizione, fase FROM taskdacompletare WHERE nomeutente = \"" + user + "\""); //11   
            System.out.println("inserimento IN CORSO");
            if(!rs.next()){ //cursore
                System.err.print("Nessuna task sulla lavagna dell'utente "+ user);
                return;
            }
            tdc = new TaskDaCompletare( rs.getString("nomeutente"),
                    rs.getString("nometask"), rs.getString("descrizione"),rs.getInt("duratatask"),rs.getInt("fase"));
            ps.setString(1,user);
            ps.setString(2,tdc.getNomeTask());
            ps.setInt(3,tdc.getDurataTask());
            Date d= new Date();
            DateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            ps.setString(4,f.format(d));
            ps.setString(5, tdc.getDescrizione());
            ps.setBoolean(6, complete);
            ps.executeUpdate();
            psDelete.executeUpdate();
            System.out.println("inserimento completato");
            co.close();
        } catch (SQLException e) {System.err.println(e.getMessage());}   
    }       

    public void cancellaRiga(String user, String task){
        try ( Connection co = DriverManager.getConnection("jdbc:mysql://"+indirizzoIPDatabase+":"+portaDatabase+"/lavagna", usernameDatabase, passwordDatabase);  
            PreparedStatement ps = co.prepareStatement("DELETE FROM taskcompletate "
                    + "WHERE nomeutente=? AND nometask = ? ");
        ) { 
            ps.setString(1, user);
            ps.setString(2, task);
            ps.executeUpdate();
            System.out.println("Eliminazione riga anche in DB completata");
            co.close();
        } catch (SQLException e) {System.err.println(e.getMessage());}   
    }
    
    public void cancellaRigaDaCompletare(String user){
        try ( Connection co = DriverManager.getConnection("jdbc:mysql://"+indirizzoIPDatabase+":"+portaDatabase+"/lavagna", usernameDatabase, passwordDatabase);  
            PreparedStatement ps = co.prepareStatement("DELETE FROM taskdacompletare "
                    + "WHERE nomeutente=?");
        ) { 
            ps.setString(1, user);
            ps.executeUpdate();
            System.out.println("Eliminazione riga anche in DB completata");
            co.close();
        } catch (SQLException e) {System.err.println(e.getMessage());}   
    }
    
    
    public ObservableList ottieniStatistiche(){ 
        
        statistiche = FXCollections.observableArrayList();
        try(    
            Connection co = DriverManager.getConnection("jdbc:mysql://"+indirizzoIPDatabase+":"+portaDatabase+"/lavagna", usernameDatabase, passwordDatabase);  
            Statement st = co.createStatement();
        ){ ResultSet rs = st.executeQuery("SELECT * FROM ( select nomeutente, count(*) as tot"
                + " from taskcompletate group by NomeUtente) as n ORDER BY Tot DESC LIMIT "+ topPieChart);   
         while (rs.next()) {
                statistiche.add(new TaskCompletate(rs.getString("NomeUtente"), -1, "" ,"", rs.getInt("Tot"), true));
            }
            st.close();
            co.close();
        }
        catch(SQLException e ){e.getSQLState();}
        return statistiche;
    }
    
    public TaskDaCompletare getMyTask(String name){
        ObservableList<TaskDaCompletare> aux = null;
        try ( Connection co = DriverManager.getConnection("jdbc:mysql://"+indirizzoIPDatabase+":"+portaDatabase+"/lavagna", usernameDatabase, passwordDatabase);  
            Statement st = co.createStatement(); 
        ) {  
            String query = "SELECT nomeutente, nometask, duratatask, descrizione, fase FROM taskdacompletare WHERE nomeutente= \"" + name + "\"";
            ResultSet rs = st.executeQuery(query); 
            if(rs.next()){
                aux = getOlDaCompletare(rs.getInt("fase"));
                for(int i=0; i<aux.size(); i++)
                    if(aux.get(i).getNomeutente().equals(rs.getString("nomeutente")))
                        return aux.get(i);
            }
            co.close();
        } catch (SQLException e) {System.err.println(e.getMessage());}  
        return null;
    }  
    
    public ObservableList getOlCompletate(){ return olCompletate;}

    public ObservableList getOlDaCompletare(int n){
        switch(n){
            case 0 : return olDaCompletareIceBox;
            case 1 : return olDaCompletareInProgress;
            case 2 : return olDaCompletareTesting;               
        }
        return null;
    }
}


