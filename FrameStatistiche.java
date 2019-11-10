
import javafx.collections.*;
import javafx.event.ActionEvent;
import javafx.geometry.Side;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;


public class FrameStatistiche {
  
    GestoreBasiDati gbd = GestoreBasiDati.getGestoreBasiDiDati();
    private final TableView<TaskCompletate> tb = new TableView<>(); 
    private SocketServerLog socketLog = new SocketServerLog();
    private GestoreParametriConfigurazioneXML gestoreParametriConfig = new GestoreParametriConfigurazioneXML(
            "C:\\prg\\myapps\\ProjectManagement\\myfiles\\ParametriConfigurazione.xml", 
            "C:\\prg\\myapps\\ProjectManagement\\myfiles\\ValidaParametriConfigurazione.xsd"); 
    
    private static PieChart diagrammaATorta;
    private static ObservableList<PieChart.Data> datiDiagrammaATorta;
    
    FrameStatistiche(HBox s){
    
        tb.setMinWidth(430);
        tb.setMaxHeight(150);
        tb.setEditable(true);
        
        TableColumn nomeutenteCol = new TableColumn("NOME UTENTE");
        nomeutenteCol.setCellValueFactory(new PropertyValueFactory<>("Nomeutente"));
        TableColumn duratataskCol = new TableColumn("DURATA TASK");
        duratataskCol.setCellValueFactory(new PropertyValueFactory<>("Duratatask"));
        TableColumn completataCol = new TableColumn("COMPLETATA");
        completataCol.setCellValueFactory(new PropertyValueFactory<>("Completata"));  
        TableColumn datataskCol = new TableColumn("DATA TASK");
        datataskCol.setCellValueFactory(new PropertyValueFactory<>("Datatask"));   
        
        gbd.caricaTaskCompletate();
        tb.setItems(gbd.getOlCompletate());     
        tb.getColumns().addAll(nomeutenteCol, duratataskCol, datataskCol, completataCol);   
       
        // Differente colore della riga in base al valore della colonna compleataCol
        completataCol.setCellFactory(column -> {
            return new TableCell<TaskCompletate, Boolean>() {
                @Override
                protected void updateItem(Boolean item, boolean empty) {
                    super.updateItem(item, empty);

                    setText(empty ? "" : getItem().toString());

                    TableRow<String> currentRow = getTableRow();
                    
                    if (!isEmpty()) {
                        try{
                            if(item.equals(true))        
                                currentRow.setStyle("-fx-background-color:#68f442");
                            if(item.equals(false))
                                currentRow.setStyle("-fx-background-color:#f75916");
                        }
                        catch(NullPointerException e){ }
                    }
                }
            };
        });
        
        Button cancel = new Button("Cancel");
        
        cancel.setOnAction((ActionEvent ae)->{ 
            String username = tb.getSelectionModel().getSelectedItem().getNomeutente();
            String taskname = tb.getSelectionModel().getSelectedItem().getNometask();
            
            gbd.cancellaRiga(username, taskname);
            tb.getItems().remove(tb.getSelectionModel().getSelectedItem());
            gestoreParametriConfig.leggiFileDiConfigurazione();
            socketLog.inviaEvento(TipoMessaggioLog.CLICK_PULSANTE_DELETE, gestoreParametriConfig.parametri.nickname);
        });
        
        tb.setOnMouseClicked((MouseEvent event)->{         
            socketLog.inviaEvento(TipoMessaggioLog.SELEZIONE_RIGA, gestoreParametriConfig.parametri.nickname);
        });

        s.setSpacing(10);
        s.getChildren().addAll(cancel, tb);  
        allestisciTorta(s);
    }
    
    private void allestisciTorta(HBox s){
            ObservableList<TaskCompletate> listaStatistiche = gbd.ottieniStatistiche();

            datiDiagrammaATorta = FXCollections.observableArrayList();
            listaStatistiche.stream().forEach((task) -> {
                
                String titolo = task.getNomeutente() + "(" + task.getTot() + " Task)";
                datiDiagrammaATorta.add(new PieChart.Data(titolo, task.getTot()));
            });
            
            diagrammaATorta = new PieChart(datiDiagrammaATorta);
            diagrammaATorta.setTitle("Statistiche Task");
            diagrammaATorta.setStyle("-fx-font-size: 16px; -fx-font-family: \"Courier New\";"
            + " -fx-font-weight: bold;");
            diagrammaATorta.setMaxHeight(120);
            diagrammaATorta.setLegendSide(Side.RIGHT);
            diagrammaATorta.setLegendVisible(true);
            diagrammaATorta.setLabelsVisible(false);
            
            s.getChildren().add(diagrammaATorta);
    }  
}
