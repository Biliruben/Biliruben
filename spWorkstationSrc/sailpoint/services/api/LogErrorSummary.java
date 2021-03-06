package sailpoint.services.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class LogErrorSummary extends AbstractTraceAspectLogAnalyzer {
	
	private Map<String,Stack<String[]>> _threads;
	private List<String> _errors;
	private int _propNameMaxLength = 10;
	
	/**
	 * Default constructor uses a default layout pattern
	 */
	public LogErrorSummary() {
		this ((String)null);
	}
	
	public LogErrorSummary(String layoutPattern) {
		super (layoutPattern);
		_threads = new HashMap<String, Stack<String[]>>();
		_errors = new ArrayList<String>();
	}
	
	/**
	 * For each log event, test if has an 'Entering' value and capture the method signature information.  Otherwise, test
	 * if it is an ERROR and create the summary information for that error.
	 */
	@Override
	public void addLogEvent(String logEvent) {
		super.addLogEvent(logEvent);
		String thread = getThread();
		List<String> methodSig = getMethodSignature();
		String[] bundle = new String[2];
		// each method is stored in a Map for each known thread
		Stack<String[]> methodStack = _threads.get(thread);
		if (isEntering()) {
			String methodName = methodSig.get(0) + ":" + methodSig.get(1);
			String formattedMethodSig = formatMethodSig(methodSig);
			bundle[0] = methodName;
			bundle[1] = formattedMethodSig;

			if (methodStack == null) {
				methodStack = new Stack<String[]>();
				_threads.put(thread, methodStack);
			}
			methodStack.push(bundle);
		} else if (isExiting()) {
			// exiting, pop off the stack and see ifn it matches
			String methodName = methodSig.get(0) + ":" + methodSig.get(1);
			boolean match = false;
			String thatMethod = null;
			while (!match && !methodStack.isEmpty()) {
				String[] next = methodStack.pop();
				thatMethod = next[0];
				if (thatMethod.equals(methodName)) {
					match = true;
				}
			}
			// this is mostly an 'unwinding' activity.  Not much to actually do with the stack
		} else if (isError()) {
			// found an error!
			// iterate the stack and output earliest method call to latest followed by error msg
			StringBuffer buff = new StringBuffer();
			for (int i = 0; methodStack != null && i < methodStack.size(); i++) {
				String[] next = methodStack.get(i);
				buff.append(next[0] + " (");
				if (next[1] != null && !next[1].equals("")) {
					buff.append("\n");
				}
				buff.append(next[1] + " )\n\n");
			}
			buff.append(logEvent + "\n\n----------------------------------------------------\n\n");
			_errors.add(buff.toString());
		}
		
	}

	/*
	 * Tests if the log event is an error
	 */
	private boolean isError() {
		String priority = getPriority();
		// hmmm, may need to come back to this. I wonder if it could ever be other formats ('ERROR' vs 'ERR' vs 'E')
		if (Log4jPatternConverter.PRIORITY_ERROR.equals(priority)) {
			return true;
		}
		return false;
	}

	/*
	 * Converts the method signature list into a pretty summary.
	 */
	private String formatMethodSig(List<String> methodSig) {
		/* something like:
		 * \tparamName: <-- normalized \s --> paramValue\n
		 */
		StringBuffer buff = new StringBuffer();
		for (int i = 2; i < methodSig.size(); i += 2) {
			String propName = methodSig.get(i);
			String propValue = methodSig.get(i + 1);
			String formatted = String.format("%1$-" + _propNameMaxLength + "s", propName);
			buff.append("\t" + formatted + " : " + propValue + "\n");
		}
		if (buff.length() > 0) {
			buff.delete(buff.length() - 1, buff.length());
		}
		return buff.toString();
	}

	/**
	 * Returns a String of the pretty error messages we've built
	 */
	public String compileSummary() {
		StringBuffer out = new StringBuffer();
		for (String nextError : _errors) {
			out.append(nextError + "\n");
		}
		return out.toString();
	}

}
