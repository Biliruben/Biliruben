/**
 * 
 */
package biliruben.threads;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * Utility class that manages a work queue of runnables. The queue is processed
 * by feeding working threads each runnable item. The ThreadRunner limits the amount
 * of concurrent threads to launch. The ThreadRunner also provides some amount
 * of control allowing the ThreadGroup to stop processing on the work queue.
 * 
 * Note: once ThreadRunner is started, the calling thread must shutdown
 * ThreadRunner or the workthreads will wait indefinitely.
 * 
 * @see ThreadRunner#shutDown()
 * @author trey.kirk
 * @since 1.5
 * 
 */
public class ThreadRunner {

    public interface TRRunnable extends Runnable {
        public void pause();

        public void resume();

        public void shutdown();
    }

    /**
     * Specialized Thread that continually polls the work queue for work. If the
     * work queue is empty, it will wait and poll again either when notified, or
     * when the wait duration expires. After a wait period, it will only poll
     * again if the calling ThreadRunner is still active.
     * 
     * @author trey.kirk
     * @see ThreadRunner#setSleepTime(int)
     * @see ThreadRunner#isActivated()
     */
    public class TRThread extends Thread {

        private TRRunnable task;

        private ThreadRunner tr;

        private boolean activated;

        private boolean running;

        /**
         * Constructor specifying the ThreadRunner to monitor, the ThreadGroup
         * to be assigned to, and the name of this thread.
         * 
         * @param tr
         *            ThreadRunner to monitor. When this
         *            ThreadMonitor.isActivated returns false, these TRThread
         *            will stop polling for work.
         * @param threads
         *            ThreadGroup to assign the new TRThread to.
         * @param name
         *            Name of the thread.
         */
        protected TRThread(ThreadRunner tr, ThreadGroup threads, String name) {
            super(threads, name);
            this.tr = tr;
            activated = true;
        }

        public boolean isRunning() {
            return running;
        }

        public void pauseRunnable() {
            if (task != null)
                task.pause();
        }

        public void resumeRunnable() {
            if (task != null)
                task.resume();
        }

        public void shutdown() {
            if (task != null)
                task.shutdown();
        }

        /**
         * ThreadRunner will call the TRThread's run method when the thread is
         * created.
         */
        public void run() {

            // Need to sychronize around workItems

            // This thread also has an activated boolean that's true on
            // construction. Only when the queue is
            // empty and tr.isActivated() is false will this be set to false
            // (and thuse shutdown the thread.
            while (activated) {

                synchronized (workItems) {
                    task = tr.poll();
                }
                if (task != null) {
                    // Got a Runnable, run it.
                    try {
                        running = true;
                        task.run();
                        running = false;
                    } catch (RuntimeException e) {
                        // apparently we could get a thread leak if we don't
                        // catch these.
                        // TODO: Log it sometime.
                        System.out.println(this.getName()
                                + " encountered the following error: ");
                        e.printStackTrace();
                        running = false;
                    }
                } else {
                    // Didn't get any work
                    if (tr.isActivated()) {
                        // tr is still activated, so wait a little while and
                        // try again.
                        try {
                            Thread.sleep(tr.getSleepTime());
                        } catch (InterruptedException e) {
                            // ignored
                        }
                    } else {
                        // tr is not activated and there wasn't any work.
                        // Our work is done here.
                        activated = false;
                    }
                }

            }
        }
    }

    /**
     * When the ThreadRunner is active, the threads may empty the queue leaving
     * no work for other threads. Instead of letting the thread die because it
     * has no work to do, the thread waits for the duration and then checks to
     * see if there is new work in the queue. This sets the default sleep time.
     * 
     * @see ThreadRunner#setSleepTime(int)
     */
    public static final int DEFAULT_SLEEP_TIME = 125;

    /**
     * The ThreadRunner default constructor uses this value as the default
     * number of threads to create.
     * 
     * @see ThreadRunner#ThreadRunner(int)
     * @see ThreadRunner#ThreadRunner(int, int)
     */
    public static final int DEFAULT_THREADS = 20;

    /**
     * As work is added to the work queue, ThreadRunner will report the current
     * number of items added to the queue and the current size of the queue.
     * This should provide a rough idea of the progress of the threads' work.
     */
    public static final int DEFAULT_REPORT_INCREMENT = 100;

    /**
     * Default base thread name
     */
    public static final String DEFAULT_BASE_NAME = "TRThread";

    private static final String PROP_TOTAL_THREADS = "threads";
    private static final String PROP_THREAD_WORK_LOAD = "maxWork";
    private static final String PROP_THREAD_REPORT_INCR = "reportIncr";
    private static final String PROP_THREAD_SLEEP = "sleep";

    private static final int DEFAULT_THREAD_WORK = Integer.MAX_VALUE;

    /*
     * The backing queue.
     */
    private LinkedList<TRRunnable> workItems;

    private int workload;

    /*
     * Controlling boolean. It's set to 'true' when ThreadRunner is constructed
     * and should be set to false when it's time to shut it down.
     */
    protected boolean activated = false;

    /*
     * The work threads are inserted into this ThreadGroup. This may provide
     * some bit of additional control for future upgrades.
     */
    private ThreadGroup threads;

    private int statsIn = 0;

    private int reportIncrement = DEFAULT_REPORT_INCREMENT;

    private int sleepTime = DEFAULT_SLEEP_TIME;

    private String _baseName;

    private boolean _verbose;

    private int _capacity;

    private boolean started = false;

    /**
     * Default constructor using the default number of threads. This constructor
     * sets the queue size to double the number of default threads.
     * 
     * @see ThreadRunner#DEFAULT_THREADS
     */
    public ThreadRunner() {
        this(DEFAULT_THREADS);
    }

    /**
     * Constructor to build a ThreadRunner using a specified properties file.  Properties not
     * specified will use ThreadRunner defaults (as opposed to defaults the caller has implemented
     * assuming no other configuration action is taken on this object)
     * @param propertiesFile
     * 			Filename containing properties to define how these threads are to be run
     */
    public ThreadRunner (String propertiesFile, String baseName) {
        this();
        _baseName = baseName;
        readProperties(propertiesFile);

    }

    /**
     * Constructs a ThreadRunner and defines the number of work threads to
     * create. This constructor sets the queue size to Integer.MAX_VALUE.
     * 
     * @param capacity
     *            Number of concurrent threads to use.
     * @throws IllegalStateException
     *             if capacity is less than 1
     */
    public ThreadRunner(int capacity) {
        this(capacity, DEFAULT_THREAD_WORK);
    }

    /**
     * Constructions a ThreadRunner and define the number of work threads to
     * create. This constructor also allows the caller to specify the size of
     * the work queue. The work queue size will default to the same value of
     * capacity if a value of less than 1 is provided.
     * 
     * @param capacity
     *            Number of concurrent threads to use.
     * @param workload
     *            Size of the queue.
     * @throws IllegalStateException
     *             if capacity is less than 1
     */
    public ThreadRunner(int capacity, int workload) {
        this (capacity, workload, DEFAULT_BASE_NAME);
    }

    /**
     * Constructions a ThreadRunner and define the number of work threads to
     * create. This constructor also allows the caller to specify the size of
     * the work queue. The work queue size will default to the same value of
     * capacity if a value of less than 1 is provided.  The caller may also
     * specify the threads' baseName which in turn defines the properties that
     * will apply to these threads.
     * 
     * @param capacity
     *            Number of concurrent threads to use.
     * @param workload
     *            Size of the queue.
     * @param baseName
     * 			  Base name for working threads and thread group           
     * @throws IllegalStateException
     *             if capacity is less than 1
     */
    public ThreadRunner (int capacity, int workload, String baseName) {
        if (capacity < 1) {
            throw new IllegalStateException("Capacity cannot be less than 1!");
        }

        if (workload < 1) {
            workload = capacity;
        }
        this.workload = workload;
        // Setup our work queue first. Enabling the ThreadRunner and
        // constructing the worker threads before creating a work queue 
        // will lead to an NPE
        workItems = new LinkedList<TRRunnable>();
        _baseName = baseName;
        threads = new ThreadGroup(_baseName + "-WorkItems");
        _capacity = capacity;
    }

    private void startThreads(int capacity) {
        if (!started) {
            activated = true;
            started  = true;
            for (int i = 0; i < capacity; i++) {
                // create our new specialized threads.
                TRThread t = new TRThread(this, threads, _baseName + i);
                t.start();
            }
        }
    }

    /**
     * Wraps the worker queue's offer(Object) method by forcing the caller to
     * wait until the queue has space. This throttles the incoming requests to
     * avoid potential memory consumption nightmare. This method will leverage
     * the specified thread wait time for its own wait time.
     * 
     * @param task
     *            Runnable object with a task to complete.
     * @see ThreadRunner#setSleepTime(int)
     */
    public void add(TRRunnable task) {
        boolean added;

        // Need to synchronize on our work queue.
        if (!activated) {
            startThreads (_capacity);
        }

        do {
            added = false;
            if (workItems.size() < workload) {
                synchronized (workItems) {
                    added = workItems.offer(task);
                }
            }
            if (!added) {
                // If we weren't able to add it; it's because the work queue
                // is full. This is where
                // we throttle the caller by forcing it to wait until space
                // frees up.
                try {
                    Thread.sleep(this.getSleepTime());
                } catch (InterruptedException e) {
                    // whatever
                }
            }
        } while (!added);

        // future enhancement may be to incorporate batching, i.e. allowing
        // the queue to build up and then
        // notify our work threads that work is ready to pick up.

        // BTW, it's generally suggested that notifyAll() works better than
        // notify(). Look into it.
        //workItems.notify();

        // This is probably not thread safe.  We've assume a single thread will
        // be updating our ThreadRunner when it's entirely possible the caller could be
        // multiple threads.
        // TODO: determine how, if at all, this degrades performance by including this logic
        // in the sychronized loop
        statsIn++;

        // Report.
        // TODO: use a log object instead.
        int reportIncr = this.getReportIncrement();
        if (reportIncr > 0) {
            if (statsIn % reportIncr == 0) {
                System.out.println(statsIn + " queued.  Queue size: "
                        + workItems.size());
            }
        }


    }

    /**
     * The reportIncrement is used to determine how often to report the status
     * of the work load. For every reportIncrement, a short status of the amount
     * of tasks added to the work queue and the current size of the queue will
     * be returned. A value of 0 will disable any reporting.
     * 
     * @return the reportIncrement
     */
    public int getReportIncrement() {
        return this.reportIncrement;
    }

    /**
     * Returns the time in milliseconds a thread will sleep before checking for
     * work again. A thread will only sleep when no additional work is found in
     * the queue.
     * 
     * @return the sleepTime
     * @see ThreadRunner#setSleepTime(int)
     */
    public int getSleepTime() {
        return this.sleepTime;
    }

    /**
     * @return the threads ThreadGroup containing our worker threads.
     */
    protected ThreadGroup getThreads() {
        return this.threads;
    }

    /**
     * Checks to see if the runner has anything to do.
     * 
     * @return True if any items are in the work queue or any thread
     * 	is currently doing work; false otherwise.
     */
    public boolean hasWork() {
        return (!workItems.isEmpty() || anyThreadRunning());
    }

    public void pause() {
        activated = false;
        Thread[] activeThreads = new Thread[threads.activeCount()];
        threads.enumerate(activeThreads);
        for (Thread t : activeThreads) {
            TRThread thr = (TRThread)t;
            thr.pauseRunnable();
        }
    }

    public void resume() {
        Thread[] activeThreads = new Thread[threads.activeCount()];
        threads.enumerate(activeThreads);
        for (Thread t : activeThreads) {
            TRThread thr = (TRThread)t;
            thr.resumeRunnable();
        }
        activated = true;
    }

    private boolean anyThreadRunning() {
        // An activeCount > 0 is probably enough, but I would rather trust
        // our implementation's indication of execution.
        int active = threads.activeCount();
        Thread[] activeThreads = new Thread[active];
        threads.enumerate(activeThreads);
        for (Thread t : activeThreads) {
            TRThread thr = (TRThread) t;
            if (thr.isRunning()) {
                return true;
            }
        }		
        return false;
    }

    /**
     * 
     * @return The active status of the ThreadRunner. As long as ThreadRunner is
     *         active, its work threads will stay alive waiting for work to
     *         arrive in the queue. Use the shutdown method to instruct the work
     *         queues that no further work is needed.
     * @see ThreadRunner#shutDown()
     */
    public boolean isActivated() {
        return activated;
    }

    /**
     * Work threads will use the poll method to retrieve work.
     * 
     * @return A Runnable object or null if the queue is empty.
     */
    protected TRRunnable poll() {
        // LinkedList is not threadsafe.
        TRRunnable r = null;
        if (activated) { // returns a null runnable if not activated.  threads will try again if their still activated
            synchronized (workItems) {
                r = workItems.poll();
            }
        }
        return r;
    }

    /**
     * The reportIncrement is used to determine how often to report the status
     * of the work load. For every reportIncrement, a short status of the amount
     * of tasks added to the work queue and the current size of the queue will
     * be returned. A value of 0 will disable any reporting.
     * 
     * @param reportIncrement
     *            the reportIncrement to set
     */
    public void setReportIncrement(int reportIncrement) {
        this.reportIncrement = reportIncrement;
    }

    /**
     * Sets the time in milliseconds a thread will sleep before checking for
     * work again. A thread will only sleep when no additional work is found in
     * the queue.
     * 
     * @param sleepTime
     *            the sleepTime to set
     * @see ThreadRunner#getSleepTime()
     */
    public void setSleepTime(int sleepTime) {
        this.sleepTime = sleepTime;
    }

    /**
     * Sets the activated boolean to false. The work threads read this value and
     * stop waiting for work when the switch is set to false. Any tasks left in
     * the work queue will be completed first.
     */
    public void shutDown() {
        shutDown(false);
    }

    /**
     * Sets the activated boolean to false. The work threads read this value and
     * stop waiting for work when the switch is set to false. Also, the work
     * queue is drained of any remaing tasks which are lost.
     * 
     * @param immidiate
     *            Set to true to force the work queue to empty out.
     */
    public void shutDown(boolean immidiate) {
        resume(); // in case there are paused threads
        activated = false;
        started = false;
        if (immidiate) {
            // We've been asked to ensure all threads stop now, so empty the queue.
            // This should ensure our threads end after they've completed their current
            // work.
            synchronized (workItems) {
                workItems.clear();
            }

            // speaking of completing their current work, if it supports a stop, do it
            Thread[] activeThreads = new Thread[threads.activeCount()];
            threads.enumerate(activeThreads);
            for (Thread t : activeThreads) {
                TRThread thr = (TRThread)t;
                thr.shutdown();
            }

            // wakeup all the sleepers.
            threads.interrupt();
        }
    }

    public List<TRRunnable> getWorkItems() {
        List<TRRunnable> runnables = new ArrayList<TRRunnable>();
        for (TRRunnable runnable : workItems) {
            runnables.add(runnable);
        }
        return runnables;
    }

    /**
     * @return the workload
     */
    public int getWorkload() {
        return workItems.size();
    }

    /**
     * @param workload the workload to set
     */
    public void setWorkload(int workload) {
        this.workload = workload;
    }

    public void readProperties (String propFile) {
        try {
            File cfgFile = new File(propFile);

            //if (cfgFile.exists() && cfgFile.canRead() && cfgFile.isFile()) {
            Properties props = new Properties();
            String prop = "";
            try {
                // get number of threads
                props.load(new FileInputStream(cfgFile));
                prop = _baseName + "." + PROP_TOTAL_THREADS;
                String msg = props.getProperty(prop, String.valueOf(DEFAULT_THREADS));
                if (msg != null) {

                    log (prop + ": " + msg);

                    Integer totalThreads = Integer.valueOf(msg);
                    if (totalThreads < 1) {
                        throw new IllegalArgumentException("Error Reading Properties: " + prop + " must be creater than 0!");
                    }
                    if (totalThreads != _capacity) {
                        // Config file overrules the constructor for number of threads to run
                        resetThreads (totalThreads);
                    }

                }

                // get thread max pool
                prop = _baseName + "." + PROP_THREAD_WORK_LOAD;
                msg = props.getProperty(prop, String.valueOf(DEFAULT_THREAD_WORK));
                if (msg != null) {

                    log (prop + ": " + msg);

                    workload = Integer.valueOf(msg);
                }
                if (workload < 1) {
                    throw new IllegalArgumentException("Error Reading Properties: " + prop + " must be creater than 0!");
                }

                // Thread report increment
                prop = _baseName + "." + PROP_THREAD_REPORT_INCR;
                msg = props.getProperty(prop, String.valueOf(DEFAULT_REPORT_INCREMENT));
                if (msg != null) {

                    log (prop + ": " + msg);

                    reportIncrement = Integer.valueOf(msg);
                }

                // Thread sleep
                prop = _baseName + "." + PROP_THREAD_SLEEP;
                msg = props.getProperty(prop, String.valueOf(DEFAULT_SLEEP_TIME));
                if (msg != null) {
                    log (prop + ": " + msg);
                    sleepTime = Integer.valueOf(msg);
                }
                if (sleepTime < 0) {
                    throw new IllegalArgumentException("Error Reading Properties: " + prop + " must be greater than 0!");
                }

            } catch (IOException e) {
                if (_verbose) {
                    log ("Error Reading Properties: " + prop + "\n" + e.getMessage());
                }
            }
            //} else {
            //log ("Error", "Specified properties file cannot be read: " + propFile);
            //}
        } catch (RuntimeException e) {
            // The potential for all this error throwing can leave threads hanging
            this.shutDown(true);
            throw new RuntimeException (e);
        }

    }

    private void resetThreads(Integer totalThreads) {
        _capacity = totalThreads;
        this.shutDown(true);
        while (this.hasWork()) {
            // might have some threads lingering to shutdown
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        startThreads (totalThreads);


    }

    private void log(String string) {
        // TODO Auto-generated method stub

    }

    private void log (Object logLevel, String msg) {
        // TODO: come back and replace logLevel with Log.WARN, etc
    }

    public void clear() {
        synchronized (workItems) {
            workItems.clear();
        }
    }

}
