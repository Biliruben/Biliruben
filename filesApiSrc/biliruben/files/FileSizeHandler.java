package biliruben.files;

import java.io.File;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class FileSizeHandler implements FileHandler {
    
    private Map<String, Set<File>> _sizeMap;
    private boolean _reportFileSizes;
    private long _minSize;
    
    public FileSizeHandler(boolean reportFileSizes) {
        _sizeMap = new HashMap<String, Set<File>>();
        _reportFileSizes = reportFileSizes;
    }
    
    private void addToMap(String path, File file) {
        Set<File> files = _sizeMap.get(path);
        if (files == null) {
            files = new HashSet<File>();
            _sizeMap.put(path, files);
        }
        files.add(file);
    }

    @Override
    public void handleFile(File file) {
        // add two entries: one for the file and one for the parent
        //addToMap(file.getAbsolutePath(), file);
        // dunno if I want to do this here or in the getFileSizes method
        String parent = file.getParent();
        if (parent == null || parent.trim().equals("")) {
            parent = File.pathSeparator;
        }
        addToMap(parent, file);
    }
    
    
    
    public String getFileSizes() {
        // For each file, get its parent path and add this file size to that
        // entry. Also add an entry for this file

        StringBuilder buff = new StringBuilder();
        NumberFormat formater = NumberFormat.getInstance();

        // our map is already built of parent paths and files we iterated. So now
        // for each entry, add the file sizes for the parent path
        
        // at iteration, also sort the keys for readability
        for (String path : new TreeSet<String>(_sizeMap.keySet())) {
            Set<File> files = _sizeMap.get(path);
            long fileSizes = 0L;
            for (File file : files) {
                if (_reportFileSizes) {
                    buff.append(file.getAbsolutePath()).append(":\t").append(formater.format(file.length())).append("\n");
                }
                fileSizes += file.length();
            }
            if (fileSizes >= _minSize) {
                buff.append(path).append(":\t").append(formater.format(fileSizes));
                if (_reportFileSizes) {
                    buff.append("\n------------------------------------");
                }
                buff.append("\n");
            }
        }
        return buff.toString();
    }

    public void setMinSize(long minSize) {
        this._minSize = minSize;
    }

}
