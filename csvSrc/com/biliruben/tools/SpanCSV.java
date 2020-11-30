package com.biliruben.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.biliruben.util.GetOpts;
import com.biliruben.util.OptionLegend;
import com.biliruben.util.OptionParseException;
import com.biliruben.util.csv.CSVRecord;
import com.biliruben.util.csv.CSVSource;
import com.biliruben.util.csv.CSVSource.CSVType;
import com.biliruben.util.csv.CSVSourceImpl;
import com.biliruben.util.csv.CSVUtil;

public class SpanCSV {

	private static final String OPT_OUTPUT = "output";
	private static final String OPT_DELIM2 = "delim2";
	private static final String OPT_DELIM = "delim";
	private static final String OPT_FIELD = "field";
	private static final String OPT_FILE = "file";
	private static GetOpts _opts;
	private static String _file;
	private static String _delim;
	private static String _delim2;
	private static List<String> _mfFields;
	private static OutputStream _output;
	private static String[] _targetFields;


	/**
	 * Takes CSV input and spans multi-valued fields into distinct fields.  Inputs
	 * to this tool need to include:<br>
	 * <li>a source CSV file</li>
	 * <li>list of multi-valued fields</li>
	 * <li>a primary delimiter (optional: commas are default)</li>
	 * <li>a secondary delimiter (optional: semi-colons are default)</li>
	 * <li>an output file (optional: stdout is default)</li>
	 * <br>
	 * Output CSV needs to be in the same field order as the input CSV
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// gather options (init)
		init(args);

		// learn field order
		CSVSource src = new CSVSourceImpl(new File(_file), CSVType.WithHeader);
		src.setDelim(_delim.charAt(0));
		src.trimValues(true);
		_targetFields = src.getFields();
		CSVRecord outRecord = createOutputRecord((Map<String, String>) null); 
		outRecord.setIncludeFieldNames(true);
		// output the header
		CSVUtil.exportToCsv(outRecord, _output);

		// read CSV, output to target CSV
		Iterator<Map<String, String>> it = src.iterator();
		while (it.hasNext()) {
			Map<String, String> line = it.next();


			// splitup our stuff
			Map<String, String[]> splitFields = splitFields (line);
			Map<String, String> lastValues = new HashMap<String, String>();
			boolean cont = true; 
			int i = 0;

			while (cont) {
				cont = false;  // trip-wire flag
				Map<String, String> copy = new HashMap<String, String>(line);
				for (String field : splitFields.keySet()) {
					String[] nextArry = splitFields.get(field);
					if (nextArry != null && nextArry.length > i) {
						cont = true; // trip the wire, keep the party going
						copy.put(field, nextArry[i]);
						lastValues.put(field, nextArry[i]);
					} else {
						copy.put(field, lastValues.get(field)); // still gotta populate
					}
				}
				i++;
				if (cont) {
					// spew it
					CSVRecord nextRecord = createOutputRecord(copy);
					CSVUtil.exportToCsv(nextRecord, _output);
				}
			}
		}


	}



	private static Map<String, String[]> splitFields(Map<String, String> line) throws IOException {
		Map<String, String[]> map = new HashMap<String, String[]>();
		for (String f : _mfFields) {
			// for each mf field, get a CSV of that field's values
			String values = line.get(f);
			if (values != null) {
				CSVSource valSrc = new CSVSourceImpl(values, CSVType.WithOutHeader);
				valSrc.setDelim(_delim2.charAt(0));
				valSrc.trimValues(true);
				String[] splitVal = valSrc.getNextLine();
				map.put(f, splitVal);
			} else {
				map.put(f, new String[0]);
			}
		}
		return map;
	}



	private static CSVRecord createOutputRecord(Map<String, String> map) {
		CSVRecord rec = new CSVRecord(_targetFields);
		rec.setIncludeFieldNames(false);
		rec.setDelimiter(_delim);
		
		if (map != null) {
			rec.addLine(map);
		}
		return rec;
	}



	private static void init(String[] args) throws FileNotFoundException {
		_opts = new GetOpts(SpanCSV.class);

		OptionLegend legend = new OptionLegend(OPT_FILE);
		legend.setRequired(true);
		legend.setDescription("Inbound CSV file with multi-valued fields in one line");
		_opts.addLegend(legend);

		legend = new OptionLegend(OPT_FIELD);
		legend.setMulti(true);
		legend.setRequired(true);
		legend.setDescription("Field(s) with delimited values that need to be spanned");
		_opts.addLegend(legend);

		legend = new OptionLegend(OPT_DELIM);
		legend.setRequired(false);
		legend.setDescription("Primary delimiter");
		legend.setDefaultValue(",");
		_opts.addLegend(legend);

		legend = new OptionLegend(OPT_DELIM2);
		legend.setRequired(false);
		legend.setDescription("Secondary delimter, used to span multi-valued fields.  May not be same value as primary delimiter");
		legend.setDefaultValue(";");
		_opts.addLegend(legend);

		legend = new OptionLegend(OPT_OUTPUT);
		legend.setRequired(false);
		legend.setDescription("Output file name.  If none is provided, output is sent to STDOUT");
		_opts.addLegend(legend);

		_opts.parseOpts(args);

		// Fetch and validate options
		_file = _opts.getStr(OPT_FILE);
		_delim = _opts.getStr(OPT_DELIM);
		_delim2 = _opts.getStr(OPT_DELIM2);
		_mfFields = _opts.getList(OPT_FIELD);
		String output = _opts.getStr(OPT_OUTPUT);

		if (_delim.equals(_delim2)) { // whoop! whoop!
			throw new OptionParseException("Primary and secondary delimiters may not be the same: " + _delim + " and " + _delim2, _opts, true);
		}

		if (_delim.length() != 1 || _delim2.length() != 1) {
			throw new OptionParseException("Delimiter must be a single character, primary: " + _delim + ", secondary: " + _delim2, _opts, true);
		}

		if (output == null) {
			_output = System.out;
		} else {
			_output = new FileOutputStream(output);
		}
	}

}

