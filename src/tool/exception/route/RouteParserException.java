package tool.exception.route;

import tool.Constants;
import tool.RouteParser;

/**
 * An exception raised by the Route parser.
 */
public class RouteParserException extends Exception {
	private static final long serialVersionUID = 1L;

	/**
	 * Returns a general route parsing exception. 
	 * Style consistency: the message expects a starting lowercase, a final punctuation and no final line break.
	 */
	public RouteParserException(String message) {
		super(String.format("ERROR ON LINE %d: %s%s", RouteParser.lineNum, message, Constants.endl));
	}
}