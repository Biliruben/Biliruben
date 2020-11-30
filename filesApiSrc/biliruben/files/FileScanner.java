package biliruben.files;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import biliruben.threads.ThreadRunner;

public class FileScanner implements Scanner {

    private FilenameFilter _filter;
    private List<FileHandler> _handlers;
    private boolean _recurse;
    //private Set<File> _scanned;
    private ThreadRunner _runner;

    public FileScanner() {
        this (false);
    }

    public FileScanner(boolean recurse) {
        this (recurse, null);
    }

    public FileScanner(boolean recurse, ThreadRunner runner) {
        _runner = runner;
        //_scanned = new HashSet<File>();
        _recurse = recurse;
    }

    public void addHandler(FileHandler handler) {
        if (_handlers == null) {
            _handlers = new ArrayList<FileHandler>();
        }
        _handlers.add(handler);
    }

    private void handle(File file) {
        for (FileHandler handler : _handlers) {
            handler.handleFile(file);
        }
    }

    public void scan(File scanFile) {
        scan(scanFile, true);
    }
    
    public Runnable getRunnable(File scanFile, boolean listContents) {
        // if scanFile is a directory, return a Runnable that scans it. If it's
        // not a directory (just a file), return null
        final File finalScanFile = scanFile;
        final boolean recurse = listContents;
        if (finalScanFile.isDirectory()) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    if (finalScanFile.exists() && finalScanFile.isDirectory() && recurse) {
                        // get listing, recurse
                        File[] files = finalScanFile.listFiles(_filter);
                        if (files != null) {
                            for (File file : files) {
                                scan(file, _recurse);
                            }
                        }
                        if (_recurse) {
                            files = finalScanFile.listFiles();

                            if (files != null) {
                                for (File listFile : files) {
                                    if (listFile.isDirectory()) {
                                        scan(listFile);
                                    }
                                }
                            }
                        }
                    } else if (finalScanFile.isFile()) {
                        if (_filter == null || (_filter != null && _filter.accept(finalScanFile.getParentFile(), finalScanFile.getName()))) {
                            // in the filter
                            handle(finalScanFile);
                        }
                    } else {
                        String directoryName = finalScanFile.getParent();
                        File directory = null;
                        if (directoryName != null) {
                            directory = new File(directoryName);
                        } else {
                            directory = new File(".");
                        }
                        _filter = new DosFileNameFilter(finalScanFile.getName());
                        File[] files = directory.listFiles(_filter);
                        if (files != null) {
                            for (File listFile : files) {
                                scan(listFile);
                            }
                        }

                        if (_recurse) {
                            files = directory.listFiles();

                            if (files != null) {
                                for (File listFile : files) {
                                    if (listFile.isDirectory()) {
                                        scan(listFile);
                                    }
                                }
                            }
                        }
                    }
                }
            };
            return runnable;
        }
        return null;
    }

    public void scan(File scanFile, boolean listContents) {
        if (_handlers == null) {
            // noooooo
            throw new NullPointerException ("No FileHandlers have been specified for scanner!  Cannot scan yet");
        }
        /* not thread safe
        if (_scanned.contains(scanFile)) {
            // don't scan twice
            return;
        }
        _scanned.add(scanFile);
        */
        
        Runnable runnable = getRunnable(scanFile, listContents);
        // if we got a runnable, give it to the runner or run it. If we got null, this is a file
        // and the handler just needs to handle it
        if (_runner != null && runnable != null) {
            _runner.add(runnable);
        } else if (runnable != null) {
            runnable.run();
        } else {
            // null runnable, just handle it
            handle(scanFile);
        }

        /*
		 				if (f.isFile() && f.exists()) {
					_log.debug("File: " + f);
					// I got a file, add it
					fileList.add(f);
				} else if (f.isDirectory() && f.exists()) {
					_log.debug("Directory: " + f);
					// I got a directory, add the contents
					File[] contents = f.listFiles();
					_log.debug("Directory contents: " + contents);
					for (File content : contents) {
						if (content.isFile()) {
							fileList.add(content);
						}
					}
				} else {
					// Didn't get a file, didn't get a directory.  Try a FilenameFilter
					_log.debug("Filter: " + f);
					String directoryName = f.getParent();
					_log.debug("Direcotry: " + directoryName);
					File directory = null;
					if (directoryName != null) {
						directory = new File(directoryName);
					} else {
						directory = new File(".");
					}
					FilenameFilter filenameFilter = new DosFileNameFilter(f.getName());
					File[] files = directory.listFiles(filenameFilter);
					_log.debug("Files from filter: " + files);
					if (files != null) {
						for (File listFile : files) {
							fileList.add(listFile);
						}
					}
				}

         */

    }

    public static void main (String[] args) {

        FileHandler h = new FileHandler() {

            public void handleFile(File file) {
                System.out.println(file.toString());
            }

        };

        String filter = "c:\\temp\\*.xml";
        Scanner s = new FileScanner(true);
        s.addHandler(h);
        s.scan(new File (filter));
    }
}
