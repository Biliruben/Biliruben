package biliruben.gps;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.sourceforge.gpstools.dem.HgtElevationModel;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import biliruben.gps.api.ElevationSource;
import biliruben.gps.api.GPXSource;

/**
 * This class gets the poly lines from gmaps-pedo.com and returns the values
 * in something a little more globally useful, probably GPX
 * @author trey.kirk
 *
 */
public class GmapsPedoAdapter implements GPXSource {

	private static final String URL_GMAPS_PEDO = "http://www.gmap-pedometer.com/gp/ajaxRoute/get";
	private static final String CREATOR = "Biliruben.com";
	private static final String MAP_NAME = "name";
	private static final String DEFAULT_ROUTE_NAME = "Gmaps Pedometer Route";
	private static final String BASE_GMAPS_URL = "http://www.gmap-pedometer.com";
	private static final String MAP_POLY_LINE = "polyline";
	private static final String POLY_MAP_LONG = "long";
	private static final String POLY_MAP_LATT = "lat";
	private static final String POLY_MAP_ELEV = "elev";
	private static final String POLY_DILIM = "a";
	private static final Object MAP_ELEV = "elev";
	private Map<String, String> _dataMap;
	private String _rid;
	private List<Map<String, String>> _polyList;
	private Document _doc;
	private ElevationSource _eModel;

	public static void main (String[] args) throws IOException {
		GmapsPedoAdapter adapter = new GmapsPedoAdapter("5280019");
		//GmapsPedoAdapter adapter = new GmapsPedoAdapter("5281039");
		adapter.readData(true);
		String gpx = adapter.getGpx();
		System.out.println(gpx);
		File output = new File("c:/temp/5280019_route.gpx");
		FileOutputStream fos = new FileOutputStream(output);
		fos.write(gpx.getBytes());
		fos.flush();
		fos.close();
	}


	public GmapsPedoAdapter (String rId) {
		_rid = rId;
	}

	public void setElevationModel (ElevationSource elevationModel) {
		_eModel = elevationModel;
	}

	public void readData() throws IOException {
		readData(false);
	}

	public void readData(boolean withElevation) throws IOException {

		URL url;
		URLConnection urlConn;
		DataOutputStream printout;
		String urlSrc = URL_GMAPS_PEDO;
		url = new URL(urlSrc);
		urlConn = url.openConnection();
		urlConn.setDoInput(true);
		urlConn.setDoOutput(true);
		urlConn.setUseCaches(false);
		urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
		urlConn.setRequestProperty("Content-Length", "11");
		urlConn.setRequestProperty("Connection", "keep-alive");
		urlConn.setRequestProperty("Keep-Alive", "115");
		printout = new DataOutputStream(urlConn.getOutputStream());
		String content="rId=" + URLEncoder.encode(_rid, "UTF-8");
		printout.writeBytes(content);
		printout.flush();
		printout.close();

		String str;
		StringBuffer buff = new StringBuffer();
		DataInputStream dis = new DataInputStream(urlConn.getInputStream());		
		BufferedReader input = new BufferedReader(new InputStreamReader(dis));

		while (null != ((str = input.readLine()))) {
			buff.append(str);
		}
		input.close();
		_dataMap = parseDataMap(buff.toString());
		// while we're here, let's setup the polyline-lists
		parsePolyLists();

		if (withElevation) {
			extractElevation();
		}
	}

	private void extractElevation() throws IOException {
		if (_eModel == null) {
			// roll our own
			_eModel = new GPSDingsElevationAdapter(new HgtElevationModel());
		}
		// any setup for eModel?
		for (Map<String, String> poly : _polyList) {
			String longStr = poly.get(POLY_MAP_LONG);
			String latStr = poly.get(POLY_MAP_LATT);
			BigDecimal lon = new BigDecimal(longStr);
			BigDecimal lat = new BigDecimal(latStr);
			BigDecimal elevation;
			elevation = _eModel.getElevation(lat, lon);
			poly.put(POLY_MAP_ELEV, String.valueOf(elevation));
		}
	}

	private Map<String, String> parseDataMap (String fromString) {
		/* sample string:
		 * centerX=18.95553589&centerY=69.64607319&zl=14&zv=2&fl=m-e-h-0-1&
		 * polyline=69.65190129a18.95499945a69.65058000a18.95347000a69.64995000a18.95286000a69.64934000a18.95210000a69.64843000a18.95112000a69.64833000a18.95108000a69.64825000a18.95115000a69.64825000a18.95115000a69.64821000a18.95124000a69.64815000a18.95151000a69.64785000a18.95365000a69.64764000a18.95437000a69.64756000a18.95472000a69.64681579a18.95659268a69.64762552a18.95892620a69.64806582a18.95990252a69.64804716a18.96129727a69.64914788a18.96208048a69.64953965a18.96140456a69.65027839a18.96019220a69.65105068a18.96023512a69.65112903a18.95916224a69.65114000a18.95908000a69.65114000a18.95908000a69.65209000a18.96011000a69.65209000a18.96011000a69.65240000a18.96044000a69.65282000a18.96121000a69.65274000a18.96191000a69.65269000a18.96294000a69.65261000a18.96417000a69.65262000a18.96468000a69.65262000a18.96468000a69.65266000a18.96488000a69.65274000a18.96506000a69.65279000a18.96511000a69.65288000a18.96511000a69.65306000a18.96492000a69.65316000a18.96495000a69.65360000a18.96547000a69.65406000a18.96614000a69.65448000a18.96676000a69.65454000a18.96683000a69.65454000a18.96683000a69.65462000a18.96552000a69.65484000a18.96379000a69.65471000a18.96378000a69.65471000a18.96378000a69.65438000a18.96422000a69.65388000a18.96641000a69.65127000a18.97815000a69.65010000a18.98324000a69.64982000a18.98423000a69.64944000a18.98508000a69.64911000a18.98559000a69.64877000a18.98591000a69.64870000a18.98598000a69.64861000a18.98594000a69.64841000a18.98600000a69.64792000a18.98608000a69.64779000a18.98616000a69.64764000a18.98614000a69.64739000a18.98602000a69.64706000a18.98574000a69.64618000a18.98471000a69.64553000a18.98374000a69.64509000a18.98296000a69.64433000a18.98129000a69.64398000a18.98038000a69.64373000a18.97964000a69.64354000a18.97932000a69.64334000a18.97913000a69.64332000a18.97911000a69.64332000a18.97911000a69.64329000a18.97893000a69.64317000a18.97886000a69.64310000a18.97898000a69.64307000a18.97914000a69.64282000a18.97923000a69.64266000a18.97940000a69.64234000a18.97938000a69.64227000a18.97932000a69.64198000a18.97872000a69.64183000a18.97816000a69.64177000a18.97819000a69.64174000a18.97815000a69.64171000a18.97799000a69.64173000a18.97782000a69.64165000a18.97735000a69.64145000a18.97621000a69.64135000a18.97580000a69.64113000a18.97515000a69.64100000a18.97484000a69.64056000a18.97402000a69.63982000a18.97289000a69.63922000a18.97123000a69.63878000a18.97019000a69.63852000a18.96969000a69.63782000a18.96860000a69.63768000a18.96831000a69.63721000a18.96714000a69.63701000a18.96674000a69.63691000a18.96641000a69.63689000a18.96629000a69.63689000a18.96629000a69.63655000a18.96652000a69.63645000a18.96654000a69.63629000a18.96648000a69.63606000a18.96620000a69.63528000a18.96490000a69.63465000a18.96356000a69.63324000a18.96093000a69.63277000a18.96004000a69.63245000a18.95953000a69.63245000a18.95953000a69.63255000a18.95887000a69.63255000a18.95887000a69.63248000a18.95863000a69.63242000a18.95852000a69.63208000a18.95821000a69.63091000a18.95696000a69.63084000a18.95680000a69.63081000a18.95638000a69.63081000a18.95638000a69.63063000a18.95626000a69.63045000a18.95586000a69.63016000a18.95507000a69.62998000a18.95470000a69.62979000a18.95439000a69.62956000a18.95414000a69.62889000a18.95384000a69.62843000a18.95352000a69.62671000a18.95146000a69.62646000a18.95112000a69.62505000a18.94944000a69.62474000a18.94905000a69.62462000a18.94883000a69.62390000a18.94809000a69.62305000a18.94704000a69.62251000a18.94647000a69.62174000a18.94589000a69.62150000a18.94564000a69.62030000a18.94473000a69.62021000a18.94461000a69.61948000a18.94407000a69.61912000a18.94374000a69.61899000a18.94339000a69.61842000a18.94321000a69.61753000a18.94281000a69.61550000a18.94224000a69.61481000a18.94193000a69.61450000a18.94174000a69.61381000a18.94146000a69.61304000a18.94136000a69.61220000a18.94106000a69.61143000a18.94069000a69.61018000a18.94000000a69.60923000a18.93952000a69.60821000a18.93954000a69.60797000a18.93944000a69.60763000a18.93939000a69.60749000a18.93923000a69.60744000a18.93904000a69.60744000a18.93904000a69.60718000a18.93922000a69.60222000a18.93773000a69.60128000a18.93753000a69.60066000a18.93755000a69.59827000a18.93773000a69.59739000a18.93796000a69.59448000a18.93897000a69.59269000a18.93919000a69.58892000a18.93998000a69.59257000a18.93921000a69.59448000a18.93897000a69.59739000a18.93796000a69.59827000a18.93773000a69.60089000a18.93754000a69.60128000a18.93753000a69.60222000a18.93773000a69.60700000a18.93918000a69.60718000a18.93922000a69.60744000a18.93904000a69.60744000a18.93904000a69.60749000a18.93923000a69.60763000a18.93939000a69.60797000a18.93944000a69.60821000a18.93954000a69.60923000a18.93952000a69.61220000a18.94106000a69.61304000a18.94136000a69.61381000a18.94146000a69.61450000a18.94174000a69.61481000a18.94193000a69.61550000a18.94224000a69.61750000a18.94280000a69.61824000a18.94311000a69.61842000a18.94321000a69.61899000a18.94339000a69.61912000a18.94374000a69.61948000a18.94407000a69.62150000a18.94564000a69.62174000a18.94589000a69.62230000a18.94630000a69.62271000a18.94667000a69.62305000a18.94704000a69.62390000a18.94809000a69.62462000a18.94883000a69.62474000a18.94905000a69.62505000a18.94944000a69.62835000a18.95343000a69.62862000a18.95368000a69.62889000a18.95384000a69.62956000a18.95414000a69.62979000a18.95439000a69.62998000a18.95470000a69.63016000a18.95507000a69.63045000a18.95586000a69.63063000a18.95626000a69.63081000a18.95638000a69.63081000a18.95638000a69.63084000a18.95680000a69.63091000a18.95696000a69.63208000a18.95821000a69.63242000a18.95852000a69.63248000a18.95863000a69.63255000a18.95887000a69.63255000a18.95887000a69.63245000a18.95953000a69.63245000a18.95953000a69.63277000a18.96004000a69.63399000a18.96233000a69.63465000a18.96356000a69.63528000a18.96490000a69.63584000a18.96586000a69.63606000a18.96620000a69.63629000a18.96648000a69.63645000a18.96654000a69.63656000a18.96651000a69.63696000a18.96624000a69.63710000a18.96620000a69.63737000a18.96641000a69.63754000a18.96643000a69.63767000a18.96632000a69.63776000a18.96612000a69.63776000a18.96612000a69.63865000a18.96759000a69.63880000a18.96780000a69.63890000a18.96785000a69.64062000a18.97030000a69.64125000a18.97133000a69.64158000a18.97204000a69.64183000a18.97266000a69.64221000a18.97381000a69.64244000a18.97467000a69.64272000a18.97586000a69.64300000a18.97733000a69.64300000a18.97733000a69.64313000a18.97890000a69.64307000a18.97914000a69.64307000a18.97934000a69.64309000a18.97947000a69.64316000a18.97956000a69.64324000a18.98034000a69.64338000a18.98091000a69.64348000a18.98123000a69.64369000a18.98164000a69.64405000a18.98214000a69.64434000a18.98246000a69.64458000a18.98264000a69.64497000a18.98280000a69.64566000a18.98290000a69.64584000a18.98288000a69.64649000a18.98271000a69.64845000a18.98200000a69.64868000a18.98201000a69.64894000a18.98213000a69.64917000a18.98238000a69.64937000a18.98280000a69.64954000a18.98339000a69.64965000a18.98394000a69.65002000a18.98606000a69.65011000a18.98684000a69.65014000a18.98753000a69.65012000a18.98817000a69.65003000a18.98955000a69.65003000a18.99071000a69.65011000a18.99284000a69.65032000a18.99544000a69.65049000a18.99804000a69.65067000a18.99962000a69.65073000a19.00007000a69.65086000a19.00076000a69.65109000a19.00183000a69.65127000a19.00251000a69.65144000a19.00305000a69.65185000a19.00422000a69.65234000a19.00539000a69.65268000a19.00610000a69.65361000a19.00782000a69.65399000a19.00844000a69.65432000a19.00889000a69.65425000a19.00996000a69.65406000a19.01155000a69.65252000a19.01017000a69.64972000a19.00789000a69.64951000a19.00763000a69.64939000a19.00742000a69.64906000a19.00665000a69.64902000a19.00638000a69.64902000a19.00593000a69.64908000a19.00555000a69.64923000a19.00492000a69.64936000a19.00457000a69.64957000a19.00422000a69.65014000a19.00368000a69.65014000a19.00368000a69.65015000a19.00208000a69.65012000a19.00000000a69.65016000a18.99937000a69.65034000a18.99822000a69.65032000a18.99802000a69.65028000a18.99788000a69.65019000a18.99788000a69.64957000a18.99840000a69.64957000a18.99840000a69.64921000a18.99776000a69.64884000a18.99701000a69.64866000a18.99657000a69.64848000a18.99604000a69.64834000a18.99531000a69.64823000a18.99430000a69.64820000a18.99377000a69.64816000a18.99207000a69.64819000a18.99027000a69.64833000a18.98836000a69.64838000a18.98770000a69.64857000a18.98652000a69.64864000a18.98612000a69.64864000a18.98612000a69.64870000a18.98598000a69.64911000a18.98559000a69.64934000a18.98527000a69.64965000a18.98465000a69.64982000a18.98423000a69.65010000a18.98324000a69.65090000a18.97977000a69.65438000a18.96422000a69.65471000a18.96378000a69.65484000a18.96379000a69.65484000a18.96379000a69.65483000a18.96389000a69.65462000a18.96552000a69.65454000a18.96683000a69.65454000a18.96683000a69.65450000a18.96677000a69.65431000a18.96654000a69.65395000a18.96596000a69.65360000a18.96547000a69.65316000a18.96495000a69.65306000a18.96492000a69.65288000a18.96511000a69.65279000a18.96511000a69.65274000a18.96506000a69.65266000a18.96488000a69.65262000a18.96468000a69.65262000a18.96468000a69.65261000a18.96417000a69.65263000a18.96376000a69.65268000a18.96316000a69.65274000a18.96191000a69.65281000a18.96127000a69.65282000a18.96121000a69.65282000a18.96121000a69.65240000a18.96044000a69.65209000a18.96011000a69.65209000a18.96011000a69.65114000a18.95908000a69.65106000a18.95979000a69.65104322a18.96027803a69.65024481a18.96023512a69.64937175a18.96158695a69.64939000a18.96122000a69.64930000a18.96042000a69.64930000a18.96027000a69.64944000a18.95856000a69.64959000a18.95592000a69.64959000a18.95585000a69.64959000a18.95585000a69.64857000a18.95473000a69.64781000a18.95381000a69.64696000a18.95289000a69.64685000a18.95273000a69.64609000a18.95141000a69.64540000a18.94967000a69.64540000a18.94967000a69.64289000a18.94316000a69.64246000a18.94213000a69.64233000a18.94208000a69.64207000a18.94186000a69.64166000a18.94134000a69.64068000a18.93954000a69.64007000a18.93851000a69.63923000a18.93690000a69.63820000a18.93513000a69.63779000a18.93450000a69.63670000a18.93310000a69.63648000a18.93245000a69.63602000a18.93007000a69.63588000a18.92959000a69.63567000a18.92912000a69.63545000a18.92880000a69.63496000a18.92829000a69.63469000a18.92789000a69.63395000a18.92632000a69.63376000a18.92614000a69.63341000a18.92598000a69.63321000a18.92569000a69.63285000a18.92486000a69.63198000a18.92269000a69.62994000a18.91775000a69.62986000a18.91751000a69.62983000a18.91729000a69.62985000a18.91699000a69.62990000a18.91665000a69.63019000a18.91540000a69.63066000a18.91354000a69.63088000a18.91287000a69.63117000a18.91232000a69.63137000a18.91212000a69.63176000a18.91189000a69.63195000a18.91172000a69.63243000a18.91095000a69.63303000a18.91020000a69.63303000a18.91020000a69.63318000a18.90991000a69.63332000a18.90954000a69.63393000a18.90759000a69.63419000a18.90693000a69.63436000a18.90661000a69.63559000a18.90490000a69.63690000a18.90316000a69.63729000a18.90270000a69.63771000a18.90242000a69.63863000a18.90210000a69.63904000a18.90182000a69.64039000a18.90085000a69.64075000a18.90065000a69.64118000a18.90059000a69.64331000a18.90078000a69.64411000a18.90077000a69.64458000a18.90059000a69.64850000a18.89874000a69.64880000a18.89869000a69.64909000a18.89875000a69.64933000a18.89895000a69.64956000a18.89925000a69.65133000a18.90208000a69.65171000a18.90258000a69.65199000a18.90283000a69.65234000a18.90299000a69.65467000a18.90361000a69.65511000a18.90376000a69.65532000a18.90388000a69.65568000a18.90426000a69.65773000a18.90735000a69.65793000a18.90761000a69.65814000a18.90781000a69.65846000a18.90798000a69.65879000a18.90801000a69.66099000a18.90768000a69.66148000a18.90766000a69.66170000a18.90771000a69.66367000a18.90880000a69.66522000a18.90979000a69.66556000a18.91018000a69.66572000a18.91044000a69.66726000a18.91344000a69.66726000a18.91344000a69.66730000a18.91344000a69.66760000a18.91393000a69.66766000a18.91396000a69.66769000a18.91402000a69.66771000a18.91427000a69.66837000a18.91537000a69.66901000a18.91666000a69.66920000a18.91713000a69.66931000a18.91745000a69.66940000a18.91785000a69.66984000a18.92054000a69.66984000a18.92054000a69.66988000a18.92053000a69.66994000a18.92061000a69.67000000a18.92091000a69.66995000a18.92124000a69.66995000a18.92124000a69.67001000a18.92155000a69.67001000a18.92155000a69.67012000a18.92089000a69.67134000a18.91884000a69.67136000a18.91857000a69.67139000a18.91852000a69.67226000a18.91788000a69.67226000a18.91788000a69.67222000a18.91606000a69.67213000a18.91417000a69.67202000a18.91256000a69.67219000a18.91084000a69.67236000a18.90982000a69.67267000a18.90876000a69.67280000a18.90840000a69.67301000a18.90800000a69.67327000a18.90768000a69.67371000a18.90741000a69.67476000a18.90723000a69.67528000a18.90683000a69.67556000a18.90656000a69.67632000a18.90542000a69.67749000a18.90328000a69.67824000a18.90239000a69.67872000a18.90219000a69.67872000a18.90219000a69.67876000a18.90250000a69.67885000a18.90283000a69.67889000a18.90308000a69.67893000a18.90357000a69.67893000a18.90410000a69.67893000a18.90410000a69.68027000a18.90504000a69.68051000a18.90526000a69.68065000a18.90560000a69.68086000a18.90639000a69.68103000a18.90663000a69.68311000a18.90769000a69.68311000a18.90769000a69.68314000a18.90771000a69.68391000a18.90816000a69.68411000a18.90850000a69.68442000a18.90883000a69.68460000a18.90912000a69.68442000a18.90883000a69.68411000a18.90850000a69.68391000a18.90816000a69.68311000a18.90769000a69.68322000a18.90672000a69.68336000a18.90576000a69.68336000a18.90576000a69.68337000a18.90556000a69.68347000a18.90483000a69.68348000a18.90476000a69.68348000a18.90476000a69.68347000a18.90455000a69.68317000a18.90364000a69.68315000a18.90348000a69.68302000a18.90317000a69.68285000a18.90280000a69.68264000a18.90243000a69.68224000a18.90195000a69.68070000a18.90088000a69.68019000a18.90061000a69.67972000a18.90062000a69.67950000a18.90073000a69.67918000a18.90101000a69.67918000a18.90101000a69.67910000a18.90101000a69.67878000a18.90122000a69.67870000a18.90112000a69.67864000a18.90114000a69.67859000a18.90123000a69.67855000a18.90140000a69.67844000a18.90168000a69.67827000a18.90192000a69.67844000a18.90168000a69.67855000a18.90140000a69.67855000a18.90140000a69.67853000a18.90153000a69.67855000a18.90175000a69.67861000a18.90192000a69.67869000a18.90196000a69.67872000a18.90219000a69.67872000a18.90219000a69.67824000a18.90239000a69.67749000a18.90328000a69.67632000a18.90542000a69.67556000a18.90656000a69.67528000a18.90683000a69.67476000a18.90723000a69.67371000a18.90741000a69.67327000a18.90768000a69.67301000a18.90800000a69.67280000a18.90840000a69.67267000a18.90876000a69.67236000a18.90982000a69.67219000a18.91084000a69.67202000a18.91256000a69.67213000a18.91417000a69.67222000a18.91606000a69.67226000a18.91788000a69.67226000a18.91788000a69.67139000a18.91852000a69.67136000a18.91857000a69.67134000a18.91884000a69.67012000a18.92089000a69.67001000a18.92155000a69.66993000a18.92170000a69.66993000a18.92170000a69.66985000a18.92138000a69.66985000a18.92138000a69.66976000a18.92132000a69.66971000a18.92108000a69.66971000a18.92089000a69.66976000a18.92061000a69.66946000a18.91864000a69.66924000a18.91761000a69.66907000a18.91712000a69.66890000a18.91673000a69.66799000a18.91493000a69.66766000a18.91443000a69.66758000a18.91442000a69.66753000a18.91424000a69.66755000a18.91405000a69.66728000a18.91356000a69.66726000a18.91344000a69.66620000a18.91135000a69.66556000a18.91018000a69.66531000a18.90987000a69.66503000a18.90964000a69.66367000a18.90880000a69.66180000a18.90775000a69.66148000a18.90766000a69.66126000a18.90767000a69.65879000a18.90801000a69.65846000a18.90798000a69.65814000a18.90781000a69.65793000a18.90761000a69.65773000a18.90735000a69.65568000a18.90426000a69.65541000a18.90396000a69.65524000a18.90382000a69.65498000a18.90371000a69.65249000a18.90304000a69.65192000a18.90278000a69.65171000a18.90258000a69.65133000a18.90208000a69.64944000a18.89908000a69.64909000a18.89875000a69.64880000a18.89869000a69.64850000a18.89874000a69.64458000a18.90059000a69.64411000a18.90077000a69.64331000a18.90078000a69.64118000a18.90059000a69.64075000a18.90065000a69.64039000a18.90085000a69.63863000a18.90210000a69.63783000a18.90237000a69.63741000a18.90260000a69.63714000a18.90286000a69.63690000a18.90316000a69.63514000a18.90551000a69.63436000a18.90661000a69.63419000a18.90693000a69.63398000a18.90745000a69.63330000a18.90961000a69.63318000a18.90991000a69.63296000a18.91031000a69.63243000a18.91095000a69.63204000a18.91160000a69.63204000a18.91160000a69.63176000a18.91189000a69.63137000a18.91212000a69.63117000a18.91232000a69.63101000a18.91260000a69.63083000a18.91300000a69.63066000a18.91354000a69.63019000a18.91540000a69.62990000a18.91665000a69.62983000a18.91722000a69.62984000a18.91742000a69.62990000a18.91766000a69.63078000a18.91982000a69.63321000a18.92569000a69.63341000a18.92598000a69.63376000a18.92614000a69.63395000a18.92632000a69.63469000a18.92789000a69.63496000a18.92829000a69.63545000a18.92880000a69.63567000a18.92912000a69.63582000a18.92945000a69.63600000a18.92999000a69.63608000a18.93035000a69.63648000a18.93245000a69.63670000a18.93310000a69.63779000a18.93450000a69.63820000a18.93513000a69.63923000a18.93690000a69.64000000a18.93838000a69.64068000a18.93954000a69.64166000a18.94134000a69.64207000a18.94186000a69.64233000a18.94208000a69.64246000a18.94213000a69.64289000a18.94316000a69.64540000a18.94967000a69.64540000a18.94967000a69.64578000a18.94851000a69.64578000a18.94851000a69.64646000a18.95036000a69.64724000a18.95171000a69.64799000a18.95265000a69.64871000a18.95343000a69.64973000a18.95464000a69.65183000a18.95646000
		 * &elev=0a1a0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0
		 * &rId=5280019&rdm=0
		 * &pta=0,1,1,1,0,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,0,1,0,1,1,1,1,1,1,1,1,1,1,0,0,1,1,1,0,1,1,1,0,1,1,1,1,1,0,1,1,0,1,1,1,1,1,1,1,1,1,1,1,1,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,1,1,1,1,1,1,1,1,1,1,1,1,1,0,1,1,1,1,1,1,1,1,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,1,1,1,1,1,1,1,0,1,1,1,1,1,1,1,1,1,1,0,1,1,1,0,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,1,1,1,1,1,1,1,1,1,1,1,1,1,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,1,1,1,1,1,1,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,1,1,1,1,1,1,1,1,1,1,0,1,0,1,1,1,0,1,0,1,1,1,1,1,1,1,1,1,1,1,0,1,1,1,1,1,1,1,1,1,1,1,0,1,1,1,0,1,1,1,1,1,1,1,1,0,0,1,1,1,0,1,1,1,0,1,1,1,0,1,1,1,1,1,1,1,1,1,1,1,0,1,1,1,1,1,0,0,0,0,0,1,1,1,1,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,1,1,1,1,1,0,1,1,1,1,1,1,1,1,1,1,1,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,1,1,1,1,1,1,1,1,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,1,1,1,1,1,1,1,1,1,0,1,1,1,0,1,1,1,0,1,1,1,1,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0&distance=26.8896
		 * &show_name_description=t&name=&description=&
		 */
		String[] tokenPairs = fromString.split("&"); // what if a data token is '&'? Probably comes in as '&amp;'?
		Map<String, String> dataMap = new HashMap<String, String>();
		for (String tokenPair : tokenPairs) {
			String[] tokens = tokenPair.split("=", 2);
			dataMap.put(tokens[0], tokens[1]);
		}

		return dataMap;
	}

	private void parsePolyLists() {
		// all the data we need is in the map
		String polyLine = _dataMap.get(MAP_POLY_LINE);
		String elevLine = _dataMap.get(MAP_ELEV);
		// polyLine is cooridinate pairs delimited by 'a' (is it always 'a'?) // check pedo-to-gpx java script to see what they do
		String[] polys = polyLine.split(POLY_DILIM);  
		_polyList = new ArrayList<Map<String, String>>();
		for (int i = 0; i < polys.length; i+= 2) {
			// fetch a pair at a time
			String lat = polys[i];
			// better be an i+1
			String lon = polys[i + 1];
			Map<String, String> dataPoint = new HashMap<String, String>();
			dataPoint.put(POLY_MAP_LATT, lat);
			dataPoint.put(POLY_MAP_LONG, lon);
			_polyList.add(dataPoint);
		}
	}

	public Document getGpxDom() throws ParserConfigurationException {
		// I'm partial to just creating the document
		DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbfac.newDocumentBuilder();
		Document doc = db.newDocument();
		//doc.setXmlVersion("1.0");

		// root element
		Element root = doc.createElement(ELEMENT_ROOT_NAME);
		root.setAttribute(ATTRIBUTE_GPX_VERSION, "1.1");
		root.setAttribute(ATTRIBUTE_GPX_CREATOR, CREATOR);
		root.setAttribute("xmlns", "http://www.topografix.com/GPX/1/1");
		root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		root.setAttribute("xsi:schemaLocation", "http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd");
		doc.appendChild(root);

		// we'll create routes
		Element rte = doc.createElement(ELEMENT_RTE);
		root.appendChild(rte); // rte has no attributes

		// name it
		String name = _dataMap.get(MAP_NAME);
		if (name == null || "".equals(name.trim())) {
			name = DEFAULT_ROUTE_NAME;
		}
		Element nameEl = doc.createElement(ELEMENT_NAME);
		rte.appendChild(nameEl);
		Text cdataName = doc.createTextNode(name);
		nameEl.appendChild(cdataName);

		// Use comment node to contain original gmaps-pedo link
		Element cmtEl = doc.createElement(ELEMENT_CMT);
		rte.appendChild(cmtEl);
		CDATASection cdataLink = doc.createCDATASection(BASE_GMAPS_URL + "/r=" + _rid);
		cmtEl.appendChild(cdataLink);

		//TODO: could add a desc, src, link? hmm, use link instead of cmt...

		// on with the datar
		for (Map<String, String> dataPoint : _polyList) {
			Element routePoint = doc.createElement(ELEMENT_RTE_EPT);
			routePoint.setAttribute(ATTRIBUTE_RET_LAT, dataPoint.get(POLY_MAP_LATT));
			routePoint.setAttribute(ATTRIBUTE_RET_LON, dataPoint.get(POLY_MAP_LONG));
			rte.appendChild(routePoint);

			String eleStr = dataPoint.get(POLY_MAP_ELEV);
			if (eleStr != null) {
				Element elevation = doc.createElement(ELEMENT_ELE);
				Text elevationData = doc.createTextNode(eleStr);
				elevation.appendChild(elevationData);
				routePoint.appendChild(elevation);
			}
		}

		_doc = doc;
		return _doc;
	}

	public String getGpx() {
		if (_dataMap == null) {
			// throw or just return null? throw
			throw new NullPointerException("Data map is null!  You must call readData() before you can convert the data.");
		}
		String gpx = null;
		if (_doc == null) {
			try {
				_doc = getGpxDom();
			} catch (ParserConfigurationException e) {
				throw new RuntimeException(e);
			}

		}

		try {
			// TODO: got something in our XML toolbox for this?
			TransformerFactory transfac = TransformerFactory.newInstance();
			transfac.setAttribute("indent-number", 3);
			Transformer trans = transfac.newTransformer();
			trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			trans.setOutputProperty(OutputKeys.INDENT, "yes");

			StringWriter sw = new StringWriter();
			StreamResult result = new StreamResult(sw);
			DOMSource source = new DOMSource(_doc);
			trans.transform(source, result);
			gpx = sw.toString();
		} catch (TransformerConfigurationException e) {
			throw new RuntimeException(e);
		} catch (TransformerException e) {
			throw new RuntimeException(e);
		}

		return gpx;
	}


}
