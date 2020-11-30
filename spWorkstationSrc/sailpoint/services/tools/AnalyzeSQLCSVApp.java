package sailpoint.services.tools;

import java.io.File;
import java.io.IOException;

import sailpoint.services.api.idx.AnalyzeSQLCSV;

import com.biliruben.util.GetOpts;
import com.biliruben.util.OptionLegend;
import com.biliruben.util.csv.CSVSource;
import com.biliruben.util.csv.CSVSourceImpl;
import com.biliruben.util.csv.CSVSource.CSVType;

public class AnalyzeSQLCSVApp {
	
	private static GetOpts _opts;
	private static String OPT_DELIM = "delim";

	private static String OPT_INFILE = "inFile";
	private static String OPT_OUTDIR = "outDir";
	private static String OPT_PARENT_COLUMN = "parent";
	private static void init(String[] args) {
		_opts = new GetOpts(AnalyzeSQLCSVApp.class);

		OptionLegend legend = new OptionLegend(OPT_INFILE);
		legend.setRequired(true);
		legend.setDescription("Delimited file to parse");
		_opts.addLegend(legend);

		legend = new OptionLegend(OPT_OUTDIR);
		legend.setRequired(false);
		legend.setDescription("Out directory to create resuling xml files");
		legend.setIsHidden(true);
		_opts.addLegend(legend);

		legend = new OptionLegend(OPT_PARENT_COLUMN);
		legend.setRequired(false);
		legend.setDescription("Column name of the parent ids.");
		legend.setDefaultValue("PARENT");
		_opts.addLegend(legend);

		legend = new OptionLegend(OPT_DELIM);
		legend.setRequired(false);
		legend.setDescription("Delimter string to parse the file.");
		legend.setDefaultValue("	");
		_opts.addLegend(legend);

		_opts.parseOpts(args);
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		init(args);

		String fileName = _opts.getStr(OPT_INFILE);
		CSVSource csv = new CSVSourceImpl(new File(fileName), CSVType.WithHeader);

		AnalyzeSQLCSV analyzer = new AnalyzeSQLCSV(csv);
		analyzer.setDelim(_opts.getStr(OPT_DELIM).charAt(0));
		analyzer.setParentColumn(_opts.getStr(OPT_PARENT_COLUMN));
		analyzer.analyze();
	}


}
