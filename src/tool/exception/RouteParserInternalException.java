package tool.exception;

import java.util.Queue;

import tool.Constants;

/**
 * A exception to be raised when an internal mechanic of the battle parser is faulty. 
 * This should only be raised when the issue is not directly linked to the parsing of custom files, but instead for cases "that shouldn't be encountered" when programming the parser.
 */
public class RouteParserInternalException extends RouteParserException {
	private static final long serialVersionUID = 1L;

	public RouteParserInternalException(String flag, Queue<String> info) {
		super(String.format("in flag '%s'. Please contact the maintainer.%s%s", flag, Constants.endl, info));
	}

}
