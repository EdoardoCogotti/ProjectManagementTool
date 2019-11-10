
import javafx.beans.property.*;

    public class TaskCompletate { 
        
        private final SimpleStringProperty Nomeutente;
        private final SimpleIntegerProperty Duratatask;
        private final SimpleStringProperty Nometask;
        private final SimpleStringProperty Datatask;
        private final SimpleIntegerProperty Tot;
        private final SimpleBooleanProperty Completata;

        public TaskCompletate(String i, int d, String data, String n,int t,boolean c){
         
            Nomeutente = new SimpleStringProperty(i);
            Duratatask = new SimpleIntegerProperty(d);
            Datatask = new SimpleStringProperty(data);
            Nometask = new SimpleStringProperty(n);
            Tot = new SimpleIntegerProperty(t);
            Completata = new SimpleBooleanProperty(c);
        }

        public int getTot(){ return Tot.get();}
        public String getNomeutente() { return Nomeutente.get(); }
        public int getDuratatask() { return Duratatask.get(); }
        public String getDatatask() { return Datatask.get(); }
        public String getNometask() { return Nometask.get(); }
        public boolean getCompletata() { return Completata.get();}
    }