import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
 
public class XMLParser {
 
    DocumentBuilderFactory docFactory;
    DocumentBuilder docBuilder;
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
    }
    
    public void createXMLFromQueriesFile() throws IOException {
        
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
}