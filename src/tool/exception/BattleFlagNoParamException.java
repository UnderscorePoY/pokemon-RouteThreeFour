package tool.exception;

import tool.Constants;

/**
 * A exception to be raised when a no-paramater battle flag is faulty.
 */
public class BattleFlagNoParamException extends RouteParserException {
	private static final long serialVersionUID = 1L;

	/**
	 * Returns a route parsing exception related to a flag with no parameter, with complementary information. 
	 * Style consistency: the complementary information expects a starting uppercase, a final punctuation and no final line break.
	 */
	public BattleFlagNoParamException(String flag, String complementaryInfo) {
		super(String.format("in flag '%s'.%s", flag, 
				complementaryInfo != null ? String.format("%s%s", Constants.endl, complementaryInfo) : ""));
	}
	
	/**
	 * Returns a route parsing exception related to a flag with no parameter.
	 */
	public BattleFlagNoParamException(String flag) {
		this(flag, null);
	}
}
