import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
 
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XMLParser {
 
    DocumentBuilderFactory docFactory;
    DocumentBuilder docBuilder;
    ArrayList<Entry<String,String>> queries;
    
    private static final String QUERIES_PATH = "../queries/topics_MB1-49.txt";
    private static final String QUERIES_XML_PATH = "../queries/topics_MB1-49.xml";
    private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n";
    private static final String ROOT_ELEMENT = "<queries>\r\n";
    private static final String ROOT_ELEMENT_CLOSE = "</queries>";
    
    
    public XMLParser() {
        this.docFactory = DocumentBuilderFactory.newInstance();
        try {
            this.docBuilder = docFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        
        queries = new ArrayList<Entry<String, String>>();
        
        this.fillQueries();
    }
    
    private void fillQueries() {
        try {
            XMLParser.createXMLFromQueriesFile();
            
            Document doc = this.docBuilder.parse(QUERIES_XML_PATH);
            doc.getDocumentElement().normalize();
            
            NodeList titleList = doc.getElementsByTagName("title");
            NodeList nameList = doc.getElementsByTagName("num");
            
            for (int i = 0; i < titleList.getLength(); i++) {
                String title = titleList.item(i).getTextContent();
                String name = nameList.item(i).getTextContent();
                title = title.trim();
                name = name.trim();
                name = name.replaceFirst("^[a-zA-Z]+: [a-zA-Z]+0+(?!$)", "");
                queries.add(new AbstractMap.SimpleEntry<String, String>(name, title));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }

    private static void createXMLFromQueriesFile() throws IOException {
        
        InputStream is = null;
        OutputStream os = null;
        
        File src = new File(QUERIES_PATH);
        File dest = new File(QUERIES_XML_PATH);
        
        try {
            is = new FileInputStream(src);
            os = new FileOutputStream(dest);
            
            byte[] buffer = new byte[1024];
            int length;
           
            os.write(XML_HEADER.getBytes(), 0, XML_HEADER.length());
            os.write(ROOT_ELEMENT.getBytes(), 0, ROOT_ELEMENT.length());
            
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            os.write(ROOT_ELEMENT_CLOSE.getBytes(), 0, ROOT_ELEMENT_CLOSE.length());
            is.close();
            os.close();
        }
    }
    
    public ArrayList<Entry<String, String>> getQueries() {
        ArrayList<Entry<String, String>> toReturn = new ArrayList<Entry<String, String>>();
        
        for (Entry<String, String> entry : this.queries) {
            toReturn.add(new AbstractMap.SimpleEntry<String, String>(entry.getKey(), entry.getValue()));
        }
        return this.queries;
    }
}