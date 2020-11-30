package biliruben.tools;

import java.io.File;

import biliruben.files.FileHandler;
import biliruben.files.FileScanner;
import biliruben.files.Scanner;

import com.biliruben.util.GetOpts;
import com.biliruben.util.OptionLegend;

public class PurgeFiles {

	private static final String OPT_FILE_NAME = "file";
	private static final String OPT_RECURSIVE = "r";
	private static final boolean READ_ONLY = true;
	private static String _fileName;
	private static boolean _recurs;

	/**
	 * Given a filename filter, this application will delete any file or directory
	 * that matches the filter.  Optional parameters can be passed to instruct it to
	 * go in recursively
	 * @param args
	 */
	public static void main(String[] args) {
		init(args);
		purgeFiles();
	}
	
	private static void purgeFiles() {
		Scanner scanner = new FileScanner(_recurs);
		File scanFile = new File(_fileName);
		scanner.scan(scanFile);
	}
	
	private static void init(String[] args) {
		GetOpts opts = new GetOpts(PurgeFiles.class);
		
		OptionLegend legend = new OptionLegend(OPT_FILE_NAME);
		legend.setRequired(true);
		legend.setDescription("Filename, directory name, or filename pattern to scan");
		opts.addLegend(legend);
		
		legend = new OptionLegend(OPT_RECURSIVE);
		legend.setRequired(false);
		legend.setFlag(true);
		opts.addLegend(legend);
		
		opts.parseOpts(args);
		
		_fileName = opts.getStr(OPT_FILE_NAME);
		String recurs = opts.getStr(OPT_RECURSIVE);
		if (recurs != null) {
			_recurs = Boolean.valueOf(recurs);
		}
	}

	static class PurgeFileHandler implements FileHandler {

		public void handleFile(File file) {
			if (READ_ONLY) {
				System.err.print("RO: Not deleting file " + file);
			} else {
				// DELETE it!
				// get some loggin' in here!
				file.delete();
			}
		}
		
	}
}
