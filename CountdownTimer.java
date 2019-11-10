import java.util.concurrent.*;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class CountdownTimer{ 
    
    boolean scaduto = false;
    
    Label labelTimer;
    private GestorePromo gpTimer;
    private Integer seconds; //1 perchè non scenda a valori negativi
    private Integer minutes;
    private ScheduledFuture sf;

    public CountdownTimer(int m, int s, GestorePromo gp){
        minutes = m;
        seconds = s;
        gpTimer = new GestorePromo();
        gpTimer = gp;
    }
    
    public void setTimer(VBox p) {
        
        labelTimer= new Label(); 
        
        String m = (minutes>=10) ? minutes.toString() : "0"+minutes.toString();
        labelTimer.setText("Countdown: "+ m +":" + seconds.toString());
        labelTimer.setStyle("-fx-font-weight: bold; -fx-font-size: 18px");  
        
        p.getChildren().add(labelTimer);
        
        // crea un singolo thread Executor
        ScheduledExecutorService e = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = Executors.defaultThreadFactory().newThread(r);
                t.setDaemon(true); //altrimenti chiusa la finestra il programma funziona ancora
                return t;
            }
        });
        
        // ogni secondo chiama runTime per aggiornare Timer
        sf = e.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                   runTime();
                });
            }
        }, 0, 1, TimeUnit.SECONDS);   
        
    }
    
    public Label getTimerLabel(){ return labelTimer; }
    public ScheduledFuture getScheduleFuture(){ return sf; }
    public boolean scaduto(){return scaduto;}
    
    private void runTime() { 
        seconds--;
         
        String m = (minutes>=10) ? minutes.toString() : "0"+minutes.toString();
        String s = (seconds>=10) ? seconds.toString() : "0"+seconds.toString();
        labelTimer.setText("Countdown: "+ m +":" + s); 
        
        if(minutes==0 && seconds==0){
            gpTimer.forzaTask(false);
            scaduto = true;
            sf.cancel(true);
        }
        if(seconds==0){
            minutes--;
            seconds=60;
        }
    }
}