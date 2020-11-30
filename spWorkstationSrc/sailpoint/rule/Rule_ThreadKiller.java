package sailpoint.rule;

import java.util.List;

public class Rule_ThreadKiller extends GenericRule {

	@Override
	public Object execute() throws Throwable {
		
		String threadName = "QuartzScheduler_Worker-2";
		Thread[] threads = new Thread[Thread.activeCount()];
		Thread.enumerate(threads);
		
		List tList = java.util.Arrays.asList(threads);
		for (Object o : tList) {
			Thread t = (Thread)o;
			String tName = t.getName();
			if (tName != null && tName.compareToIgnoreCase(threadName) == 0) {
				t.stop();
			}
		}

		return null;
	}

}
