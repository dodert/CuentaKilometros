package dodert.cuentakilometros3;

import android.os.Environment;
import android.util.Log;
import android.util.Xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Created by doder on 27/03/2016.
 */
public class TrakingSaver {
    final String _folderName = "DistanceTracker";
    final String _fileNamePrefix = "DT_";
    private String _fileNameFull = "";
    private File _file;

    public TrakingSaver(String fileName) {

        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmssSSS");
        Date outputDate = new Date();
        String adsasd = format.format(outputDate);

        _fileNameFull = _fileNamePrefix + adsasd + "_" + fileName;

    }

    public File getFile() {
        return _file;
    }

    public void CreateAndInitilizaFile() {
        if (isExternalStorageWritable()) {
            _file = getAlbumStorageDir();
            CreateKMLFIle(_file);

        }
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public File getAlbumStorageDir() {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), _folderName);
        if (!file.mkdirs()) {
            Log.e("log file test", "Directory not created");
        }

        File newfile = new File(file, _fileNameFull + ".kml");


        return newfile;
    }

    public void addLine(/*File file, */String coordinates, String time) throws ParserConfigurationException, IOException, SAXException, TransformerException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(_file);

        NodeList b = document.getElementsByTagName("gx:Track");
        Node a = b.item(0);
        Element when = document.createElement("when");
        Element coord = document.createElement("gx:coord");

        when.appendChild(document.createTextNode(time));
        coord.appendChild(document.createTextNode(coordinates));

        a.appendChild(when);
        a.appendChild(coord);

        DOMSource source = new DOMSource(document);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        StreamResult result = new StreamResult(_file);
        transformer.transform(source, result);

    }
   /* private void CreateKMLFIle2(File file)
    {
        Document doc = new Document();
        LocalSocketAddress.Namespace sNS = Namespace.getNamespace("someNS", "someNamespace");
        Element element = new Element("SomeElement", sNS);
        element.setAttribute("someKey", "someValue", Namespace.getNamespace("someONS", "someOtherNamespace"));
        Element element2 = new Element("SomeElement", Namespace.getNamespace("someNS", "someNamespace"));
        element2.setAttribute("someKey", "someValue", sNS);
        element.addContent(element2);
        doc.addContent(element);
    }*/


    private void CreateKMLFIle(File file) {
        //TrakingSaver asd = new TrakingSaver();
        //File file = asd.getAlbumStorageDir("traks");
        try {

            OutputStream os = new FileOutputStream(file);
            XmlSerializer xmlSerializer = Xml.newSerializer();

            StringWriter writer = new StringWriter();
            xmlSerializer.setOutput(writer);
            xmlSerializer.startDocument("UTF-8", true);
            xmlSerializer.startTag(null, "kml");
            xmlSerializer.attribute(null, "xmlns", "http://www.opengis.net/kml/2.2");
            xmlSerializer.attribute(null, "xmlns:gx", "http://www.google.com/kml/ext/2.2");
            xmlSerializer.startTag(null, "Folder");
            xmlSerializer.startTag(null, "Placemark");

            xmlSerializer.startTag(null, "gx:Track");

            /*xmlSerializer.startTag(null, "gx:when");
            xmlSerializer.text("2010-05-28T02:02:09Z");
            xmlSerializer.endTag(null, "gx:when");

            xmlSerializer.startTag(null, "gx:coord");
            xmlSerializer.text("-122.207881 37.371915 156.000000");
            xmlSerializer.endTag(null, "gx:coord");*/

            xmlSerializer.endTag(null, "gx:Track");
            xmlSerializer.endTag(null, "Placemark");
            xmlSerializer.endTag(null, "Folder");
            xmlSerializer.endTag(null, "kml");




            /*xmlSerializer.text("GPS device");
            xmlSerializer.endTag(null, "name");
            xmlSerializer.startTag(null, "Snippet");
            xmlSerializer.text("Created by Benja");
            xmlSerializer.endTag(null, "Snippet");



            xmlSerializer.text("asdadada benja");
            xmlSerializer.endTag(null, "userName");
            xmlSerializer.startTag(null, "password");
            xmlSerializer.text("adasd testest");
            xmlSerializer.endTag(null, "password");
            xmlSerializer.endTag(null, "userData");*/
            xmlSerializer.endDocument();
            xmlSerializer.flush();
            String dataWrite = writer.toString();
            os.write(dataWrite.getBytes());
            os.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}

