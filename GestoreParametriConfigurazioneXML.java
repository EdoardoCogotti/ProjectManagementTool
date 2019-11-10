

import javax.xml.parsers.*;
import javax.xml.validation.*;
import javax.xml.transform.stream.*;
import javax.xml.transform.dom.*;
import javax.xml.*;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.*;
import org.xml.sax.*;
import java.io.*;
import java.nio.file.*;
import com.thoughtworks.xstream.XStream;


public class GestoreParametriConfigurazioneXML {

    private final String pathFileConfigurazione;
    private final String pathFileXSDConfigurazione;
    public static ParametriConfigurazione parametri;
    
    public GestoreParametriConfigurazioneXML(String pathFileConfig, String pathFileXSD) {
        pathFileConfigurazione = pathFileConfig;
        pathFileXSDConfigurazione = pathFileXSD;
    }
    

     public void leggiFileDiConfigurazione() {
        
        if (validaFileDiConfigurazione()) {
           try{
            XStream xs = new XStream();
            String configurazioniLette = new String(Files.readAllBytes(Paths.get(pathFileConfigurazione)));
            parametri = (ParametriConfigurazione) xs.fromXML(configurazioniLette);
           }catch(Exception e){}
        }
    }

    public boolean validaFileDiConfigurazione() {
        try{
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Document d = db.parse(new File(pathFileConfigurazione));
            Schema s = sf.newSchema(new StreamSource(new File(pathFileXSDConfigurazione)));
            s.newValidator().validate(new DOMSource(d));
        }
        catch (ParserConfigurationException | SAXException | IOException e) {
            if(e instanceof SAXException)
                System.out.println("Errore di validazione: " + e.getMessage());
            else
                System.out.println(e.getMessage());
        }
        return true;
    }
}