package biliruben.files;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import biliruben.threads.ThreadRunner;

/**
 * This handler will create a file copy queue.  It will insert into that queue a copy command
 * containing a source file and a target destination.  One or more threads will be pulling these
 * jobs from that queue and process the file copy.  As we expand on this, some behaviors I want to
 * instill into it are:
 * - define the number of threads at runtime
 * - speficy the ability to only copy oer existing files; only copy files that don't already exist; only overwrite
 *      files when their checksums dont' match
 * @author trey.kirk
 *
 */
public class FileCopyHandler implements FileHandler {

    private static final int DEFAULT_THREADS = 4;
    private static final boolean DEFAULT_REPORT = true;
    private String _source;
    private String _target;
    private boolean _report = DEFAULT_REPORT;
    private ThreadRunner _runner;


    private static class FileCopyRunnable implements Runnable {

        private File _sourceFile;
        private File _targetFile;
        private boolean _report;

        FileCopyRunnable(File sourceFile, File targetFile, boolean report) {
            _sourceFile = sourceFile;
            _targetFile = targetFile;
            _report = report;
        }

        @Override
        public void run() {
            if (_report) {
                System.out.println(_sourceFile + " --> " + _targetFile);
            }
            // work happens here.  For now, just spit out the would-be-ness
            try {
                prepareDestination();
                InputStream in = new FileInputStream(_sourceFile);

                //For Overwrite the file.
                OutputStream out = new FileOutputStream(_targetFile);

                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0){
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();
            } catch (FileNotFoundException e) {
                System.err.println(e.getMessage());
                // would like to log the exception
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
        
        private void prepareDestination() {
            File directory = _targetFile.getParentFile();
            if (!directory.exists()) {
                // we're the worker.  We assume our callers have already determined that it's ok
                // to create the target path
                directory.mkdirs();
            }
        }

    }

    public FileCopyHandler (String sourceDirectory, String targetDirectory, boolean createIfNotExist) throws IOException {
        _source = sourceDirectory;
        _target = targetDirectory;
        testDestination(createIfNotExist); // testing the source is done by way of the scanner that handles us
        _runner = getRunner();
    }
    
    public void setReport(boolean report) {
        _report = report;
    }

    private void testDestination(boolean create) throws IOException {
        File targetDirectory = new File (_target);
        if (!targetDirectory.exists()) {
            if (create) {
                targetDirectory.mkdirs();
            } else {
                throw new IOException("Target directory does not exist! " + _target);
            }
        }
        if (!targetDirectory.isDirectory()) {
            throw new IOException("Target location is not a directory! " + _target);
        }
        if (!targetDirectory.canWrite()) {
            throw new IOException("Target directory cannot be written to! " + _target);
        }

    }

    private ThreadRunner getRunner() {
        ThreadRunner runner = new ThreadRunner(DEFAULT_THREADS);
        runner.setReportIncrement(0); // no reporting
        return runner;
    }

    @Override
    public void handleFile(File file) {
        /*
         With this file, I need to use the source directory to determine the
         relative remaining path.  For example, if the source directory is 'c:\temp\hold'
         and the file being copied is 'c:\temp\hold\mp3s\Tool\rock01.mp3', I need
         distinguish the 'mp3s\Tool\rock01.mp3'

         I suppose a little regex magic is all that's needed: take the source path and replace it with
         the target path. The path separators might make things kludgy
         */

        String sourcePath = file.getPath();  // use abstract paths at all times, easier on the user
        String targetPath = sourcePath.replace(_source, _target);
        File targetFile = new File(targetPath);
        FileCopyRunnable runnable = new FileCopyRunnable(file, targetFile, _report);
        _runner.add(runnable);
    }

    /**
     * Shutsdown the file copy runner.  Shutdown simply means it will destroy the threads only
     * when all work has been completed.
     */
    public void shutdown(boolean immidiate) {
        _runner.shutDown(immidiate);
    }

    public void shutdown() {
        shutdown(false);
    }

    public boolean isActive() {
        return _runner.isActivated();
    }

}
