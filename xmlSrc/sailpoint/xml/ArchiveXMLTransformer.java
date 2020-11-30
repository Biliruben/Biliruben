package sailpoint.xml;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public class ArchiveXMLTransformer extends FilteringXmlTransformer {

	private OutputStream _out;

	public ArchiveXMLTransformer(int startLevel, int stopLevel, OutputStream out) {
		super(startLevel, stopLevel);
		_out = out;
	}
	
	public ArchiveXMLTransformer(List<String> filter, MatchMode matchMode, OutputStream out) {
		super(filter, matchMode);
		_out = out;
	}

	@Override
	public void handleOutput(XMLDataLine dataLine) throws IOException {
		
		StringBuffer buff = new StringBuffer();
		buff.append(dataLine.getElement() + "\n");
		if (dataLine.getCData() != null) {
			buff.append("\tCDATA: " + dataLine.getCData()+ "\n");
		}
		
		Map<String, String> attrs = dataLine.getAttributes();
		for (String key : attrs.keySet()) {
			String value = attrs.get(key);
			buff.append("\t" + key + ": " + value + "\n");
		}

		buff.append("\n");
		_out.write(buff.toString().getBytes());
	}


}

