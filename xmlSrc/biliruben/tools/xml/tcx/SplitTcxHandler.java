package biliruben.tools.xml.tcx;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import biliruben.tools.xml.BreadCrumbHandler;
import biliruben.tools.xml.tcx.object.Activities;
import biliruben.tools.xml.tcx.object.Activity;

/**
 * Splits an incoming TCX file into multiple smaller TCX files with the same content
 * spanned across them.
 * @author trey.kirk
 *
 */
public class SplitTcxHandler extends BreadCrumbHandler {
    
    private String _outputDirPath;
    private String _baseFile;
    private int _activityLimit;
    private int _count;
    private List<Activity> _activities;
    private List<String> _fileNames;

    private boolean _parsing;

    private Activity _current;
    
    private static String HEADER_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>\n" +
            "<TrainingCenterDatabase xmlns=\"http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
            "xsi:schemaLocation=\"http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2 http://www.garmin.com/xmlschemas/TrainingCenterDatabasev2.xsd\">\n";


    public SplitTcxHandler(String baseFileName, String outputDir, int limit) throws IOException {
        this._baseFile = baseFileName;
        this._outputDirPath = outputDir;
        this._activityLimit = limit;
        this._parsing = false;
        this._fileNames = new ArrayList<String>();
        _activities = new ArrayList<Activity>();
        validateOutputDir();
    }
    
    private void validateOutputDir() throws IOException {
        File outputDir = new File(_outputDirPath);
        if (!outputDir.isDirectory() && !outputDir.canWrite()) {
            throw new IOException("Cannot write to directory: " + _outputDirPath);
        }
    }
    
    @Override
    public void startElement(String uri, String localName, String name,
            Attributes attributes) throws SAXException {
        super.startElement(uri, localName, name, attributes);
        if (Activities.ELEMENT_ACTIVITIES.equals(localName)) {
            _parsing = true;
        } else if (Activity.ELEMENT_ACTIVITY.equals(localName)) {
            _current = new Activity(attributes);
            _activities.add(_current);
        } else if (_parsing) {
            _current.getHandler().startElement(uri, localName, name, attributes);
        } else {
            System.out.println("Ignoring Start tag: " + localName);
        }
    }
    
    @Override
    public void endElement(String uri, String localName, String name)
            throws SAXException {
        super.endElement(uri, localName, name);
        if (Activity.ELEMENT_ACTIVITY.equals(localName)) {
            if (_activities.size() % _activityLimit == 0) {
                try {
                    writeActivities();
                } catch (FileNotFoundException e) {
                    throw new SAXException(e);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        } else if (Activities.ELEMENT_ACTIVITIES.equals(name)) {
            _parsing = false;
            if (_activities.size() > 0) {
                try {
                    writeActivities();
                } catch (FileNotFoundException e) {
                    throw new SAXException(e);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        } else if (_parsing) {
            _current.getHandler().endElement(uri, localName, name);
        } else {
            System.out.println("Ignoring End tag: " + localName);
        }
    }
    
    private void writeActivities() throws IOException {
        _count++; // increment the count first
        File f = new File(_outputDirPath + File.separator + _baseFile + "_" + _count + ".tcx");
        FileOutputStream fos = new FileOutputStream(f);
        StringBuilder xml = new StringBuilder();
        xml.append(HEADER_XML);
        Activities a = new Activities(_activities);
        xml.append(a.toXml());
        xml.append("</TrainingCenterDatabase>\n");
        fos.write(xml.toString().getBytes());
        fos.flush();
        fos.close();
        _activities = new ArrayList<Activity>();
        _fileNames.add(f.getAbsolutePath());
    }
    
    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
        _count = 0;
    }
    
    public List<String> getFilesWritten() {
        return _fileNames;
    }
    
    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        super.characters(ch, start, length);
        if (_parsing && _current != null) {
            _current.getHandler().characters(ch, start, length);
        }
    }
}