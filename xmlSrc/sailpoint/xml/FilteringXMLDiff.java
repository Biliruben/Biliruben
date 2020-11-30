package sailpoint.xml;

import java.io.File;
import java.io.FilenameFilter;

import org.apache.oro.io.Perl5FilenameFilter;

import com.biliruben.tools.xml.BreadCrumbHandler;
import com.biliruben.tools.xml.BreadCrumbHandler.MatchMode;

public class FilteringXMLDiff {

	private FilenameFilter _fileFilter;
	private File _dir;
	private MatchMode _mode;
	private String _elementFilter = null;

	public FilteringXMLDiff (File directory, String filePattern) {
		this (directory, new Perl5FilenameFilter(filePattern));
	}

	public FilteringXMLDiff (File directory, FilenameFilter filter) {
		if (filter != null) {
			_fileFilter = filter;
		} else {
			throw new NullPointerException("FilenameFilter cannot be null");
		}
		
		if (directory != null) {
			_dir = directory;
		} else {
			throw new NullPointerException("Directory File cannot be null");
		}
		_mode = BreadCrumbHandler.MatchMode.EQUALS;

	}
	
	public BreadCrumbHandler.MatchMode getMode() {
		return _mode;
	}
	
	public void setMode(BreadCrumbHandler.MatchMode mode) {
		_mode = mode;
	}
	
	public String getFilter() {
		return _elementFilter;
	}
	
	public void setFilter(String filter) {
		_elementFilter = filter;
	}
	
	public FilteringXMLDiff() {
		this (new File("."), new Perl5FilenameFilter("*"));
	}
	
	void doit() {
		File[] files = _dir.listFiles(_fileFilter);
		
	}
	
}
