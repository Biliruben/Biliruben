package biliruben.files;

import java.io.File;

public interface Scanner {
	
	public void addHandler(FileHandler handler);
	
	public void scan(File scanFile);

}
