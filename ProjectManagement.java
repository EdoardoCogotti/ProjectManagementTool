import java.io.*;
import java.nio.file.*;
import javafx.application.Application;
import javafx.event.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.*;


public class ProjectManagement extends Application  {
    
    // stili CSS
    private final String CssFontText ="-fx-font-size: 19px; -fx-font-family: \"Courier New\";";
    private final String CssBold = " -fx-font-weight: bold;";
    private final String CssFontColor = "-fx-background-color: #cd7f32;-fx-background-radius: 6; -fx-padding:3;";
    private final String cssBoxStyle = "-fx-border-color: black; -fx-border-width: 2; -fx-padding: 3px;";
    
    private GestoreBasiDati gbd = GestoreBasiDati.getGestoreBasiDiDati();
    private final GestorePromo gp = new GestorePromo();
    private final SocketServerLog socketLog= new SocketServerLog();
    private CountdownTimer timer;
    private FrameStatistiche statFrame;
        
    private boolean occupato = false;
    private int selIndex=0, selTable=0;
    private int myIndex = -1, myTable=-1;
    
    private TableView<TaskDaCompletare> tbIceBox, tbInProgress, tbTesting;
    private Pane iceBox, inProgress, testing;
    private VBox form;
    private HBox stat;
    private Label titolo,nomeTag, descrizioneTag, minutiTag;
    private Label timerPlaceholder = new Label();
    private TextField nomeCampo;
    private TextField descrizioneCampo;
    private TextField minuteCampo;
    private Button submit, next, cancel;
    private GridPane grid;
    
    public void start(Stage stage) {
        
        allestisciIceBox();
        allestisciInProgress();
        allestisciTesting();
        allestisciForm();
        
        if(!gp.setMyTask())
           System.out.println("MyTask non settato");
   
        grid = new GridPane(); 
        grid.setPadding(new Insets(10,10,10,10));
        grid.setVgap(2);
        grid.setHgap(4);
        grid.setStyle("-fx-background-image: url('file:../../myfiles/3gwzpys0.bmp')");
        
        titolo = new Label("Project Management");   
        titolo.setStyle(CssFontText+CssBold+CssFontColor);
        
        grid.add(titolo, 0, 0, 4, 1);
        GridPane.setConstraints(iceBox, 0, 1);
        GridPane.setConstraints(inProgress, 0, 2);
        GridPane.setConstraints(testing, 0, 3);
        GridPane.setConstraints(form, 1, 0, 1, 4);
        
        grid.getChildren().addAll(iceBox, inProgress, testing, form);
        
        stat = new HBox();
        statFrame = new FrameStatistiche(stat);
        grid.add(stat, 0, 4, 3, 1);
        
        Group root = new Group(grid);
        Scene scene = new Scene(root, 1023, 730);

        stage.setTitle("Project Management");
        stage.setScene(scene);
        stage.show();
        
        // Caching events
        stage.setOnCloseRequest((WindowEvent ew)-> { salvaStatoFormTxt(); }); 
        caricaStatoFormTxt();
    }
    
    private void allestisciIceBox(){ 
        iceBox = new Pane(); 
        tbIceBox = new TableView<>();
        
        Label iceBoxNome  = new Label("IceBox");
        
        iceBox.getChildren().addAll(iceBoxNome, tbIceBox);
        
        iceBoxNome.layoutXProperty().bind(iceBox.widthProperty().subtract(iceBoxNome.widthProperty()).divide(2));
        iceBoxNome.setStyle(CssFontText+CssBold);
        tbIceBox.setLayoutY(25);
        
        allestisciTable(tbIceBox,0);
    }
    
    private void allestisciInProgress(){ 
        inProgress = new Pane(); 
        tbInProgress = new TableView<>();
        
        Label inProgressNome  = new Label("InProgress");
        
        inProgress.getChildren().addAll(inProgressNome, tbInProgress);
        
        inProgressNome.layoutXProperty().bind(inProgress.widthProperty().subtract(inProgressNome.widthProperty()).divide(2));
        inProgressNome.setStyle(CssFontText+CssBold);
        tbInProgress.setLayoutY(25);
        
        allestisciTable(tbInProgress,1);
    }
   
    private void allestisciTesting(){ 
        testing = new Pane(); 
        tbTesting = new TableView<>();
        
        Label testingNome  = new Label("Testing");
        
        testing.getChildren().addAll(testingNome, tbTesting);
        
        testingNome.layoutXProperty().bind(testing.widthProperty().subtract(testingNome.widthProperty()).divide(2));
        testingNome.setStyle(CssFontText+CssBold);
        tbTesting.setLayoutY(25);

        allestisciTable(tbTesting,2);
    }
    
    private void allestisciForm(){
        
        nomeTag = new Label("Nome task");
        descrizioneTag = new Label("Descrizione");
        minutiTag = new Label("Minuti:");
        nomeCampo = new TextField();
        descrizioneCampo = new TextField();
        minuteCampo = new TextField();
        submit = new Button();
        submit.setText("Submit");
        submit.setLayoutY(120);
        submit.setOnAction((ActionEvent ae)->{ 
            int min; 
           
            try{ min = Integer.parseInt(minuteCampo.getText()); }
            catch(NumberFormatException e){min=5;}
            
            if(!gp.getOccupato()){ 
                occupato = true;
                form.getChildren().remove(timerPlaceholder);
                timer = new CountdownTimer(min, 1, gp);
                timer.setTimer(form);
                timerPlaceholder = timer.getTimerLabel();
                gp.insertTask(0 , nomeCampo.getText(), descrizioneCampo.getText(),min);
            }
            else{
                System.out.println("Hai già una task");
                gp.changeMyTask(nomeCampo.getText(), descrizioneCampo.getText());
            }
            socketLog.inviaEvento(TipoMessaggioLog.CLICK_PULSANTE_SUBMIT, gp.myName());
        });
       
        next = new Button("Next");
        next.setLayoutY(120);
        next.setOnAction((ActionEvent ae)->{ 
            if(gp.nextTask()){
                form.getChildren().remove(timerPlaceholder);
                occupato = false;
            }
            socketLog.inviaEvento(TipoMessaggioLog.CLICK_PULSANTE_NEXT, gp.myName());
        });
        
        cancel = new Button("Cancel");
        
        cancel.setOnAction((ActionEvent ae)->{ 
            if(myTable==-1) return;
            TableView<TaskDaCompletare> aux = getTable(myTable);
            int n = aux.getSelectionModel().getSelectedIndex();
            
            if(n==myIndex && myTable==selTable){
                gbd.cancellaRigaDaCompletare(aux.getSelectionModel().getSelectedItem().getNomeutente());
                aux.getItems().removeAll(aux.getSelectionModel().getSelectedItem());
                //gestoreParametriConfig.leggiFileDiConfigurazione();
                form.getChildren().remove(timerPlaceholder);
                socketLog.inviaEvento(TipoMessaggioLog.CLICK_PULSANTE_DELETE, gp.myName());
                occupato = false;
                gp.setLibero();
            }
        });
                
        nomeTag.setStyle(CssFontText+CssBold+CssFontColor);
        descrizioneTag.setStyle(CssFontText+CssBold+CssFontColor);
        minutiTag.setStyle(CssFontText+CssBold+CssFontColor);
        
        form = new VBox();
        form.setSpacing(10);
        form.getChildren().addAll(nomeTag,nomeCampo,descrizioneTag,
            descrizioneCampo,submit, next, cancel, minutiTag, minuteCampo);
        form.setStyle(cssBoxStyle);
        
    }
    
    private void salvaStatoFormTxt(){ 
       try{
            String x = "";
            String name = nomeCampo.getText();
            String des = descrizioneCampo.getText();
            String dur = minuteCampo.getText();
            String indS = Integer.toString(selIndex);
            String indT = Integer.toString(selTable);
            if(!IsAlphaNumeric(name) || !IsAlphaNumericAndPunctuation(des)){
                Exception e = new Exception();
                throw e;
            }
            
            // '*' indica dati separati da '-' ; '#' indica il tempo
            if(!occupato || timer.scaduto())
                x ="*"+x.concat(name).concat("-").concat(des).concat("-").concat(dur)
                        .concat("-").concat(indS).concat("-").concat(indT);     
            else{
                String time = timer.labelTimer.getText();
                time = time.substring(11);
                x = "#"+time;
            }
            Files.write(Paths.get("./myfiles/form.txt"), x.getBytes());
        }
        catch(IOException e){
           System.out.println("Impossibile salvare lo stato del form"
                + "con estensione txt");
        }
        catch(Exception e){ e.printStackTrace(); System.out.println("Solo alfanumerici");} 
    }
   
       
    private void caricaStatoFormTxt(){
        
        String x = "";
        try{
            x = new String(Files.readAllBytes(
                    Paths.get("./myfiles/form.txt")));  
        }
        catch(IOException  e){
            System.out.println("Impossibile prelevare form da file txt");
        }
        if(x.startsWith("*")){ //* dati è # è tempo
            x = x.substring(1);
            String[] Fields = x.split("-");
            if (Fields.length > 5) 
                throw new IllegalArgumentException("String not in correct format");
            if(Fields.length >= 1)
                nomeCampo.setText(Fields[0]);
            if(Fields.length >= 2)
                descrizioneCampo.setText(Fields[1]);
            if(Fields.length >= 3)
                minuteCampo.setText(Fields[2]);
            
            getTable(Integer.parseInt(Fields[4])).getSelectionModel().select(Integer.parseInt(Fields[3]));
 
            occupato = false;
        }
        else if(x.startsWith("#")){
            int min = Integer.parseInt(x.substring(1,3));
            int sec = Integer.parseInt(x.substring(4,6));
            min = (sec==0)?((min==0)?0:min-1):min;
            sec = (sec==0)?60:sec; //00:00 non verrà mai salvato perchè si invia allo scadere del tempo
            timer = new CountdownTimer(min, sec, gp);
            timer.setTimer(form);
            timerPlaceholder = timer.getTimerLabel();
            occupato = true;
        }
    }
    
    private void allestisciTable(TableView aux, int n){
    
        aux.setMinWidth(840); 
        aux.setMaxHeight(150);
        aux.setEditable(true);
        
        TableColumn nomeutenteCol = new TableColumn("NOME UTENTE");
        nomeutenteCol.setCellValueFactory(new PropertyValueFactory<>("Nomeutente"));
        TableColumn nometaskCol = new TableColumn("NOME TASK");
        nometaskCol.setCellValueFactory(new PropertyValueFactory<>("NomeTask"));
        TableColumn duratataskCol = new TableColumn("DURATA TASK");
        duratataskCol.setCellValueFactory(new PropertyValueFactory<>("DurataTask"));  
        TableColumn descrizioneCol = new TableColumn("DESCRIZIONE");
        descrizioneCol.setCellValueFactory(new PropertyValueFactory<>("Descrizione"));   
       
        selTable=n;  
        
        aux.setOnMouseClicked((MouseEvent event)->{
            TaskDaCompletare tdc = (TaskDaCompletare) aux.getSelectionModel().getSelectedItem();        
            selTable=n;            
            selIndex = aux.getSelectionModel().getFocusedIndex();
            // aggiorno posizione della mia task quando la seleziono
            if(tdc.getNomeutente().equals(gp.myName())){
                nomeCampo.setText(tdc.getNomeTask());
                descrizioneCampo.setText(tdc.getDescrizione());
                myTable = n;
                myIndex = aux.getSelectionModel().getSelectedIndex();
            }
            socketLog.inviaEvento(TipoMessaggioLog.SELEZIONE_RIGA, gp.myName());
        });

        gbd.caricaTaskDaCompletare(selTable);
        aux.setItems(gbd.getOlDaCompletare(selTable));     
        aux.getColumns().addAll(nomeutenteCol, nometaskCol, duratataskCol, descrizioneCol);   
    }
    
    private boolean IsAlphaNumeric(String s){
        return s!= null && s.matches("^[a-zA-Z0-9]*$");
    }
    private boolean IsAlphaNumericAndPunctuation(String s){
        return s!= null && s.matches("^[a-zA-Z0-9,.;.'!? ]*$");
    }
    
    private TableView getTable(int n){
        switch(n){
            case 0: return tbIceBox;
            case 1: return tbInProgress;
            case 2: return tbTesting ;
        }
        return null;
    }
}