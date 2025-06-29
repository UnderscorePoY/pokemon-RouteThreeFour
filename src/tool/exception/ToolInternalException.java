package tool.exception;


/**
 * An exception when an internal exception is raised. 
 * Not necessarily linked to parsing a file or executing its content, but rather to some safety internal checks.
 */
public class ToolInternalException extends Exception {
	private static final long serialVersionUID = 1L;
	private static final int callerIdx = 1; // Used for automatic extracting the faulty method
	
	/*
	public ToolInternalException(Method caller, Object faultyParameter, String message) {
		super(String.format("In %s : received '%s'. %s", caller == null ? "?": caller.getName(), faultyParameter.toString(), message));
	}
	*/
	
	private ToolInternalException(Throwable thr, Object faultyParameter, String message) {
		super(String.format(
				"In %s.%s%s%s",
				thr.getStackTrace()[callerIdx].getClassName(), 
				thr.getStackTrace()[callerIdx].getMethodName(), 
				(faultyParameter == null) ? "": String.format(" : received '%s'.", faultyParameter.toString()), 
				(message == null) ? "" : String.format(" %s", message)
		));
	}
	
	public ToolInternalException(Object faultyParameter, String message) {
		this(new Throwable(), faultyParameter, message);
	}
	/*
	public ToolInternalException(String str, Object faultyParameter, String message) {
		this(faultyParameter, message);
	}
	*/
}
