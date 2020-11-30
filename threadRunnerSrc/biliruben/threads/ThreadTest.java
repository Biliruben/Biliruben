/**
 * 
 */
package biliruben.threads;

/**
 * @author trey.kirk
 *
 */
public class ThreadTest implements Runnable {
	// The limit to place on the work queue
	private static final int MAX_WORK_LOAD = 10000;
	// The total number of work items to submit
	private static final int TOTAL_WORK_ITEMS = 1000;
	// Simulate work by sleeping the work thread
	private static final long WORK_THREAD_DELAY = 100;
	long delay;
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		// TODO Auto-generated method stub
try {
		Thread.sleep(delay);
} catch (Exception e) {
	// whatever
}
	}
	
	public ThreadTest (long delay) {
		this.delay = delay;
	}

	public static void main(String[] args) {
		ThreadRunner runner = new ThreadRunner(10,MAX_WORK_LOAD);
		runner.setReportIncrement(100);
		
		// test prop file
		runner.readProperties("ThreadTest.cfg");
		
		for (int i = 0; i < TOTAL_WORK_ITEMS; i++) {
			runner.add(new ThreadTest(WORK_THREAD_DELAY));
			try {
			//Thread.sleep(10);
			} catch (Exception e) {
				// whtaeve
			}
		}
		
		runner.shutDown();
	}
}
