package biliruben.gps.api;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;

public interface GPXSource {

	static public final String ELEMENT_ROOT_NAME = "gpx";
	static public final String ATTRIBUTE_GPX_VERSION = "version";
	static public final String ATTRIBUTE_GPX_CREATOR = "creator";
	static public final String ATTRIBUTE_GPX_SCHEMA_LOCATION = "schemaLocation";
	static public final String ATTRIBUTE_RET_LAT = "lat";
	static public final String ATTRIBUTE_RET_LON = "lon";
	static public final String ELEMENT_METADATA = "metadata";
	static public final String ELEMENT_WPT = "wpt";
	static public final String ELEMENT_RTE = "rte";
	static public final String ELEMENT_TRK = "trk";
	static public final String ELEMENT_EXT = "extensions";
	static public final String ELEMENT_RTE_EPT = "rtept";
	static public final String ELEMENT_NAME = "name";
	static public final String ELEMENT_CMT = "cmt";
	static public final String ELEMENT_ELE = "ele";
	
	// there are many more, add as needed :-)
	public String getGpx();
	
	public Document getGpxDom() throws ParserConfigurationException;
}
