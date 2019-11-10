
import javafx.collections.ObservableList;

public class GestorePromo {      
    
    private TaskDaCompletare myTask;
    private final GestoreBasiDati gbd = GestoreBasiDati.getGestoreBasiDiDati();
    
    GestorePromo(){}
    
    public boolean setMyTask(){
        myTask = gbd.getMyTask(myName());
        if(myTask==null)
             System.out.println("L'utente " + myName()+ " non ha task da svolgere");
        else
            System.out.println(myTask.getNomeTask());
        return myTask!=null;
    }
    
    public boolean nextTask(){
        if(myTask==null){
            System.out.println("Non hai ancora una task");
            return false;
        }
        int n = myTask.getFase();

        
        if(n<2){
            gbd.getOlDaCompletare(n+1).add(myTask);
            gbd.aggiornaStadio(n+1, myName());
            myTask.setFase(n+1);
            gbd.getOlDaCompletare(n).remove(myTask); 
            return false;
        }
        else{
            return forzaTask(true); // n==2 --> fase testing (last phase)
        }
    }
    
    public void insertTask(int fase,String nomeTask, String des, int durata){
        gbd.insMyTask(myName(), nomeTask, des ,durata);      
        myTask=gbd.getMyTask(myName());
        if(fase!=0)
            gbd.getOlDaCompletare(fase-1).remove(myTask);
    }
    
    public boolean forzaTask(boolean esito){
        if(myTask!=null){
            System.out.println("CERCO DI INVIARE "+myTask.getNomeutente()+" "+myTask.getNomeTask());
            gbd.inviaTaskCompletata(myTask.getNomeutente(), myTask.getDescrizione(),esito);
            gbd.getOlDaCompletare(myTask.getFase()).remove(myTask);
            myTask=null;
            return true;
        }
        myTask=null;
        return false;
    }
    
    public void changeMyTask(String nametask, String des){
       if(myTask==null) 
           return;
       ObservableList aux =  gbd.getOlDaCompletare(myTask.getFase());
       for(int i=0; i<aux.size(); i++){
            if(aux.get(i).equals(myTask)){
               myTask.setNomeTask(nametask);
               aux.set(i, myTask);
            }
       }
       System.out.println("cambioTask");
    }
    
    public String myName(){ return GestoreParametriConfigurazioneXML.parametri.nickname;} //da file config.
    public boolean getOccupato(){ return myTask!=null;}
    public void setLibero(){myTask=null;}
}
