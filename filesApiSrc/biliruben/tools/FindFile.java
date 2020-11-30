package biliruben.tools;

import java.io.File;
import java.io.FilenameFilter;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import biliruben.files.DosFileNameFilter;
import biliruben.threads.ThreadRunner;
import biliruben.threads.ThreadRunner.TRRunnable;

import com.biliruben.util.GetOpts;
import com.biliruben.util.OptionLegend;

public class FindFile {
    
    private static class Monitor {
        
        private Queue<String> _queue;
        
        public Monitor() {
            _queue = new LinkedList<String>();
        }
        
        public synchronized void addMessage(String message) {
            _queue.add(message);
        }
        
        public synchronized void addMessages(Collection<String> messages) {
            _queue.addAll(messages);
        }
        
        public synchronized void printMessages(PrintStream out) {
            for (String message : _queue) {
                out.print(message);
            }
            _queue.clear();
        }
        
        public synchronized List<String> getMessages() {
            List<String> messages = new ArrayList<String>();
            messages.addAll(_queue);
            _queue.clear();
            return messages;
        }
    }
    
    private static class Scanner implements TRRunnable {

        private File _file;
        private Monitor _mon;
        private ThreadRunner _runner;
        private FilenameFilter _filter;
        private boolean _pause;
        private boolean _running;
        
        Scanner(FilenameFilter filter, File file, Monitor mon, ThreadRunner runner) {
            _file = file;
            _mon = mon;
            _runner = runner;
            _filter = filter;
            _pause = false;
            _running = true;
        }
        
        @Override
        public void run() {
            List<String> matches = new ArrayList<String>();
            for (File f : _file.listFiles()) {
                if (f.isDirectory() && f.canRead()) {
                    Scanner s = new Scanner(_filter, f, _mon, _runner);
                    _runner.add(s);
                }
                if (_filter.accept(f.getParentFile(), f.getName())) {
                    matches.add(f.getAbsolutePath() + "\n");
                }
            }
            _mon.addMessages(matches);
            _mon.printMessages(System.out);
        }

        @Override
        public void pause() {
            // in practice, no op
            _pause = true;
        }

        @Override
        public void resume() {
            // in practice, no op
            _pause = false;
        }

        @Override
        public void shutdown() {
            // in practice, no op
            _running = false;
        }
        
    }

    private static final String OPT_THREADS = "t";

    private static GetOpts _opts;

    private static Integer _threads;

    private static ArrayList<String> _fileTokens;

    /**
     * Given a dos filter, find matchin files
     * @param args
     * @throws InterruptedException 
     */
    public static void main(String[] args) throws InterruptedException {
        init(args);
        
        // Build a thread pool (ThreadRunner)
        ThreadRunner runner = new ThreadRunner(_threads);
        runner.setReportIncrement(0);
        
        // Create a Runnable that uses a non-recursive FileScanner
        String curr = System.getProperty("user.dir");
        File pwd = new File(curr);
        if (_fileTokens == null || _fileTokens.size() == 0) {
            _fileTokens = new ArrayList<String>();
            _fileTokens.add("*");
        }
        
        Monitor m = new Monitor();
        
        for (String fileToken : _fileTokens) {
            DosFileNameFilter filter = new DosFileNameFilter(fileToken);
            Scanner s = new Scanner(filter, pwd, m, runner);
            runner.add(s);
        }
        
        while (runner.hasWork()) {
            Thread.sleep(250);
        }
        runner.shutDown();

    }
    
    private static void init(String[] args) {
        _opts = new GetOpts(FindFile.class);
        OptionLegend legend = new OptionLegend(OPT_THREADS);
        legend.setDescription("Number of scanner threads to use");
        legend.setDefaultValue("4");
        legend.setRequired(false);
        _opts.addLegend(legend);
        
        _opts.setDescriptionTail("File filter to use (include double-quotes around wildcard characters)");
        _opts.setUsageTail("fileFilter[]");
        
        _opts.parseOpts(args);
        
        _threads = Integer.valueOf(_opts.getStr(OPT_THREADS));
        _fileTokens = _opts.getUnswitchedOptions();
        
    }

}
