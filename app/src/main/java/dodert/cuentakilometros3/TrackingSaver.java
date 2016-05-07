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
 * Created by dodert on 27/03/2016.
 */
public class TrackingSaver {
    private static TrackingSaver instance = null;

    public static final int GEOMETRY_GX_TRACK = 1;
    public static final int GEOMETRY_GX_MULTYTRACK = 2;
    //
    final String _folderName = "DistanceTracker";
    final String _fileNamePrefix = "DT_";
    private String _fileNameFull = "";
    private File _file;

    public static TrackingSaver GetInstance() {
        if (instance == null) {
            instance = new TrackingSaver();
        }
        return instance;
    }

    public TrackingSaver() {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmssSSS");
        Date outputDate = new Date();
        String timeString = format.format(outputDate);
        _fileNameFull = _fileNamePrefix + timeString;

        if (isExternalStorageWritable()) {
            _file = getAlbumStorageDir();
            CreateKMLFIle(GEOMETRY_GX_TRACK);

        }
    }

    public File getFile() {
        return _file;
    }

    /*public void CreateAndInitilizaFile() {
        if (isExternalStorageWritable()) {
            _file = getAlbumStorageDir();
            CreateKMLFIle(_file);

        }
    }*/

    /* Checks if external storage is available for read and write */
    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    private File getAlbumStorageDir() {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), _folderName);
        if (!file.mkdirs()) {
            Log.e("log file test", "Directory not created");
        }

        File newfile = new File(file, _fileNameFull + ".kml");

        return newfile;
    }

    /*private void AddNewTrack() throws ParserConfigurationException, IOException, SAXException, TransformerException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(_file);

        NodeList b = document.getElementsByTagName("gx:MultiTrack");
        Node a = b.item(0);
        Element track = document.createElement("gx:Track");


        track.appendChild(document.createTextNode(time));

        a.appendChild(track);

        DOMSource source = new DOMSource(document);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        StreamResult result = new StreamResult(_file);
        transformer.transform(source, result);
    }*/

    public void addTrackLine(/*File file, */String coordinates, String time) throws ParserConfigurationException, IOException, SAXException, TransformerException {
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

    public void addCommentLine(String text) {
       /*
        FileWriter fw = null;
        try {
            fw = new FileWriter(_file);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.append("<!--" + text + "-->");
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
*/
    }

    private void CreateKMLFIle(int type) {
        switch (type) {
            case GEOMETRY_GX_TRACK:
                CreateKMLFIleForGX_TRACK();
                break;
            case GEOMETRY_GX_MULTYTRACK:
                break;
            default:
                throw new RuntimeException("Unknown type: " + type);
        }

    }

    private void CreateKMLFileCommonStart(XmlSerializer xmlSerializer) throws IOException {
        xmlSerializer.startDocument("UTF-8", true);
        xmlSerializer.startTag(null, "kml");
        xmlSerializer.attribute(null, "xmlns", "http://www.opengis.net/kml/2.2");
        xmlSerializer.attribute(null, "xmlns:gx", "http://www.google.com/kml/ext/2.2");
        xmlSerializer.startTag(null, "Document");
        xmlSerializer.startTag(null, "Folder");
        xmlSerializer.startTag(null, "name");
        xmlSerializer.text("Tracks");
        xmlSerializer.endTag(null, "name");
        xmlSerializer.startTag(null, "Placemark");
        xmlSerializer.startTag(null, "name");
        xmlSerializer.text(_fileNameFull);
        xmlSerializer.endTag(null, "name");
    }

    private void CreateKMLFileCommonEnd(XmlSerializer xmlSerializer) throws IOException {
        xmlSerializer.endTag(null, "Placemark");
        xmlSerializer.endTag(null, "Folder");
        xmlSerializer.endTag(null, "Document");
        xmlSerializer.endTag(null, "kml");
        xmlSerializer.endDocument();
        xmlSerializer.flush();
    }

    private void CreateKMLFIleForGX_MULTYTRACK()
    {
        try {
            OutputStream os = new FileOutputStream(_file);
            XmlSerializer xmlSerializer = Xml.newSerializer();

            StringWriter writer = new StringWriter();
            xmlSerializer.setOutput(writer);

            CreateKMLFileCommonStart(xmlSerializer);

            xmlSerializer.startTag(null, "gx:MultiTrack");
            xmlSerializer.startTag(null, "gx:interpolate");
            xmlSerializer.text("0");
            xmlSerializer.endTag(null, "gx:interpolate");
            xmlSerializer.startTag(null, "gx:altitudeMode");
            xmlSerializer.text("absolute");
            xmlSerializer.endTag(null, "gx:altitudeMode");
            xmlSerializer.endTag(null, "gx:MultiTrack");


            CreateKMLFileCommonEnd(xmlSerializer);

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

    private void CreateKMLFIleForGX_TRACK() {
        try {

            OutputStream os = new FileOutputStream(_file);
            XmlSerializer xmlSerializer = Xml.newSerializer();

            StringWriter writer = new StringWriter();
            xmlSerializer.setOutput(writer);
            CreateKMLFileCommonStart(xmlSerializer);

            xmlSerializer.startTag(null, "gx:Track");
            xmlSerializer.startTag(null, "gx:altitudeMode");
            xmlSerializer.text("absolute");
            xmlSerializer.endTag(null, "gx:altitudeMode");
            xmlSerializer.endTag(null, "gx:Track");

            CreateKMLFileCommonEnd(xmlSerializer);

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

