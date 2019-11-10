import java.io.Serializable;
import javafx.scene.control.Label;

    public class TaskDaCompletare implements Serializable{ //13
      
        private String Nomeutente;
        private String NomeTask;
        private int DurataTask;
        //private int Stadio;
        private String Descrizione;
        private int fase;
        public Label postIt;

        public TaskDaCompletare(String i, String t, String des, int d, int s) {
            Nomeutente = i;
            NomeTask = t;
            Descrizione = des;
            DurataTask = d;
            //Stadio = s;
            fase = s;
            postIt = new Label(i+"\n"+t);
        }

        public String getNomeutente() { return Nomeutente; }
        public String getNomeTask() { return NomeTask; }
        public String getDescrizione(){ return Descrizione;}
        //public int getStadio() { return Stadio; }
        public int getDurataTask() { return DurataTask; }
        
        public Label getLabel() { return postIt;}
        public int getFase(){ return fase;}
        public void setFase(int i){ fase=i; }
        public void setNomeTask(String n){NomeTask=n;}
    }