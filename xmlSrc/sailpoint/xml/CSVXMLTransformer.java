package sailpoint.xml;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.xml.sax.SAXException;

import com.biliruben.util.csv.CSVRecord;
import com.biliruben.util.csv.CSVUtil;

public class CSVXMLTransformer extends FilteringXmlTransformer {
	
	protected static final String KEY_CDATA = "CDATA";
	private static final String KEY_ELEMENT = "element";
	CSVRecord _record = new CSVRecord();
	private OutputStream _out;

	public CSVXMLTransformer(List<String> elementFilter, MatchMode matchMode, OutputStream out) {
		super(elementFilter, matchMode);
		_out = out;
	}

	public CSVXMLTransformer(int startLevel, int stopLevel, OutputStream out) {
		super(startLevel, stopLevel);
		_out = out;
	}

	@Override
	public void handleOutput(XMLDataLine dataLine) throws IOException {
		Map<String, String> attrs = dataLine.getAttributes();
		String cdata = dataLine.getCData();
		if (cdata != null) {
			attrs.put(KEY_CDATA, cdata);
		}
		attrs.put(KEY_ELEMENT, dataLine.getElement());
		_record.addLine(attrs);
	}

	@Override
	public void endDocument() throws SAXException {
		super.endDocument();
		// Output the data at the end of the document
		try {
			CSVUtil.exportToCsv(_record, _out);
		} catch (IOException e) {
			throw new SAXException(e);
		}
	}
}
