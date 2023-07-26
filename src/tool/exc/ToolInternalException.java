package tool.exc;

import java.lang.reflect.Method;

/**
 * An exception when an internal exception is raised. 
 * Not necessarily linked to parsing a file or executing its content, but rather to some safety internal checks.
 */
public class ToolInternalException extends Exception {
	private static final long serialVersionUID = 1L;
	
	public ToolInternalException(Method caller, Object faultyParameter, String message) {
		super(String.format("In %s : received '%s'. %s", caller == null ? "?": caller.getName(), faultyParameter.toString(), message));
	}
}
