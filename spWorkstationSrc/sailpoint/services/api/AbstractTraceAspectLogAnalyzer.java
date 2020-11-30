package sailpoint.services.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sailpoint.services.api.Log4jPatternConverter.Identifier;

/**
 * Implementation of {@link LogAnalyzer} for Log4j logging derived from AspectJ's Tracing Aspect.
 * This aspect injects Enter / Exit tracing for each method allowing one to gain information about
 * the method's inputs, outputs, and timing.  This abstract class offers utility methods that compiles
 * that information.<br>
 * <br>
 * Currently, only timing gathering is implemented.<br>
 * <br>
 * Leverages {@link Log4jPatternConverter} to parse the log event messages.
 * @author trey.kirk
 *
 */
public abstract class AbstractTraceAspectLogAnalyzer implements LogAnalyzer {

	/**
	 * Default thread name when one is not available
	 */
	public static final String DEFAULT_THREAD_NAME = "?";

	// 12 hours in milliseconds form
	private static final int TWELVE_HOURS = 1000 * 60 * 60 * 12;


	/**
	 * Default Log4j LayoutPattern
	 */
	public static final String DEFAULT_LAYOUT_PATTERN = "%d{ISO8601} %5p %t %c{4}:%L - %m%n";

	private Log4jPatternConverter _converter;
	private String _layoutPattern;
	private String _message; 

	/**
	 * This attribute enables the behavior of 'correcting' date values.  Correction may need to occur when
	 * the LayoutPattern is one that results in ambiguous time stamps.  That is, time stamps that don't specify
	 * a date or even a 24-hour designation.<br>
	 * <br>
	 * This algorithm assumes that a log file is constructed sequentially and thus all dates are in order from earliest
	 * to latest.  Therefore, when any date is parsed that is earlier than the previous date, a correction is made
	 * to increment the date by 12 hours, and then again by another 12 hours if required.  This algorithm assumes
	 * that when date values are actually present, they will always be in a form that includes day, month, and year.
	 * Thus, no further correction is anticipated after incrementing by a maximum of 24 hours.  However, a warning
	 * will be displayed should such a situation arise.<br>
	 * <br>
	 * Set this value to 'false' to disable this correction scheme.
	 */
	public boolean autoCorrectDates = true;

	private Date _lastDate;

	private long _dateAdjustment = 0;


	/**
	 * Constructor that takes in a single argument
	 * @param layoutPattern The Log4j LayoutPattern used to parse the inbound logfile
	 */
	public AbstractTraceAspectLogAnalyzer(String layoutPattern) {
		if (layoutPattern != null) {
			_layoutPattern = layoutPattern;
		} else {
			_layoutPattern = DEFAULT_LAYOUT_PATTERN;
		}
		_converter = new Log4jPatternConverter(_layoutPattern);
	}

	/**
	 * Default constructor that uses {@link AbstractTraceAspectLogAnalyzer#DEFAULT_LAYOUT_PATTERN}
	 */
	public AbstractTraceAspectLogAnalyzer() {
		this ((String)null);
	}

	/**
	 * Offers next log event to parse, analyze
	 */
	public void addLogEvent(String logEvent) {
		_message = null;
		_converter.setLogEvent(logEvent);
	}

	/**
	 * Extracts the 'Message' token of the log event
	 * @return
	 */
	protected String parseMsg() {
		if (_message == null) {
			_message = _converter.parseToken(Log4jPatternConverter.Identifier.MESSAGE);
		}
		return _message;
	}
	
	public String getPriority() {
		return _converter.parseToken(Identifier.PRIORITY);
	}

	/**
	 * Determines if the log event is an 'Entering method' event
	 * @return
	 */
	public boolean isEntering() {
		String message = parseMsg();
		if (message != null && message.startsWith("Entering ")) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Determines if the log event is an 'Exiting method' event
	 * @return
	 */
	public boolean isExiting () {
		String message = parseMsg();
		if (message != null && message.startsWith("Exiting ")) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Returns an array of String that is the method signature in the following elements:<br>
	 * 0 - The category name<br>
	 * 1 - The method name<br>
	 * each following odd element - parameter name<br>
	 * each subsequent following even element - the parameter's value<br>
	 * @return
	 */
	public List<String> getMethodSignature() {
		List<String> methodSignature = new ArrayList<String>();

		if (!isEntering() && !isExiting()) { // isExiting is irrelevant for getting the method sig.
			return null; // not entering or exiting? nothing to return
		}

		String message = parseMsg();
		String[] tokens = message.split("^Entering |^Exiting ");
		if (tokens.length != 2) {
			return null; // sanity check failed.. why?
		}

		String msg = tokens[1];
		String methodName;
		String className = parseCategory();
		methodSignature.add(className);

		if (isEntering()) {
			Pattern p = Pattern.compile("^(Entering )([\\S]+)\\((.*)\\)", Pattern.DOTALL);
			// the message may have newlines.  ...what to do
			Matcher m = p.matcher(message);
			if (m.matches()) {
				// method is group 2
				methodName = m.group(2);
				methodSignature.add(methodName);
				// signature is group 3
				String signature = m.group(3);
				Pattern sigPattern = Pattern.compile("([a-zA-Z0-9_$?]+) = .+?,?", Pattern.DOTALL);
				Matcher sigMatcher = sigPattern.matcher(signature);
				int lastEnd = 0;
				int start = 0;
				int end = 0;
				while (sigMatcher.find()) {
					String nextGroup = sigMatcher.group();
					start = sigMatcher.start();
					end = sigMatcher.end();
					String paramName = nextGroup.split(" = ")[0];
					if (start != 0) {
						String lastValue = signature.substring(lastEnd - 1,  start - 2);
						methodSignature.add(lastValue);
					}
					methodSignature.add(paramName);
					lastEnd = end;
				}
				if (lastEnd > 0) {
					String lastValue = signature.substring(lastEnd - 1,  signature.length());
					methodSignature.add(lastValue);
				}
			}
		} else {
			methodName = msg.split(" = ")[0];
			methodSignature.add(methodName);
		}

		return methodSignature;
	}

	/**
	 * Extracts the method name from the log event.  Note that while {@link Log4jPatternConverter}
	 * support extracting the Method token, this method will first attempt to extract the method
	 * name as stated in the Message token.  If none is found, it will then defer to finding
	 * the Method token in from the log event.
	 * @see Log4jPatternConverter#parseToken(sailpoint.services.api.Log4jPatternConverter.Identifier, String)
	 */
	public String getMethod() {
		List<String> methodSig = getMethodSignature();
		if (methodSig != null) {
			String base = methodSig.get(0) + ":" + methodSig.get(1) + "(";
			StringBuffer buff = new StringBuffer(base.length() + 1);
			buff.append(base);
			for (int i = 2; i < methodSig.size(); i++) {
				if (i % 2 == 0) {
					// parameter names are even indexes
					buff.append(methodSig.get(i));
					if (i < methodSig.size() - 3) {
						// for the ', '
						buff.append(", ");
					}
				}
			}
			buff.append(")");
			return buff.toString();
		} else {
			return "";
		}

		/*
		if (isEntering() || isExiting()) {
			String message = parseMsg();
			String method = "";
			String[] tokens = message.split("^Entering |^Exiting ");

			if (tokens.length != 2) {
				return method;
			}

			String msg = tokens[1];
			String className = parseCategory();

			if (isEntering()) {
				Pattern p = Pattern.compile("^(Entering )([\\S]+)\\((.*)\\)", Pattern.DOTALL);
				// the message may have newlines.  ...what to do
				Matcher m = p.matcher(message);
				StringBuffer methodBuff = new StringBuffer();
				if (m.matches()) {
					// method is group 2
					methodBuff.append(m.group(2) + "(");
					// signature is group 3
					String signature = m.group(3);
					// Extract the types from the signautre
					Pattern sigPattern = Pattern.compile("([a-zA-Z0-9_$?]+) = .+?,?", Pattern.DOTALL);
					Matcher sigMatcher = sigPattern.matcher(signature);
					boolean first = true;
					while (sigMatcher.find()) {
						String nextGroup = sigMatcher.group();
						String token = nextGroup.split(" = ")[0];
						if (!first) {
							methodBuff.append("," + token);
						} else {
							first = false;
							methodBuff.append(token);
						}
					}
				}

				method = className + ":" + methodBuff.toString() + ")";
			} else {
				method = className + ":" + msg.split(" = ")[0];
			}

			return method;
		} else {
			// the only other place for the method is via the Method identifier from the log4jconverter
			return _converter.parseToken(Log4jPatternConverter.Identifier.METHOD);
		}
		 */

	}

	/**
	 * Extracts the Thread token from the log event.  If no thread is found, {@link AbstractTraceAspectLogAnalyzer#DEFAULT_THREAD_NAME}
	 * will be used
	 * @return
	 */
	public String getThread() {
		String thread = _converter.parseToken(Log4jPatternConverter.Identifier.THREAD);
		if (thread == null) {
			thread  = DEFAULT_THREAD_NAME;
		}
		return thread;
	}

	/**
	 * Extracts the Category token from the log event.  This is commonly the full class name which is derived
	 * commonly by best practices.  Extracting the Class token is not supported from this implementation since
	 * many avoid capturing that token due to performance constraints.
	 * @return
	 */
	public String parseCategory () {
		String className = _converter.parseToken(Log4jPatternConverter.Identifier.CATEGORY);
		return className;
	}

	/**
	 * Extracts the Date based off of the SimpleDatePattern interpreted by the LayoutPattern.  If {@link AbstractTraceAspectLogAnalyzer#autoCorrectDates}
	 * is enabled, ambiguous time stamps will be adjusted as the logfile is scanned.
	 * @return
	 * @see Log4jPatternConverter#parseDate(String)
	 */
	public Date getDate() {
		Date current = _converter.parseDate();
		if (current != null) {
			// previous adjustments need to persist
			current.setTime(current.getTime() + _dateAdjustment);
			if (autoCorrectDates && _lastDate != null) {
				// make a new adjustment
				if (current.before(_lastDate)) {
					current.setTime(current.getTime() + TWELVE_HOURS);
					_dateAdjustment += TWELVE_HOURS;
				}
				// one more time
				if (current.before(_lastDate)) {
					current.setTime(current.getTime() + TWELVE_HOURS);
					_dateAdjustment += TWELVE_HOURS;
				}	
			}
			// warning: adjustments didn't fly
			if (_lastDate != null && current.before(_lastDate)) {
				// TODO: Incorporate better logging
				System.err.println("Current date: " + current + " is before last: " + _lastDate);
				System.err.println("From event: " + _converter.getLogEvent());
			}
			_lastDate = current;
		}
		return current;
	}
}
