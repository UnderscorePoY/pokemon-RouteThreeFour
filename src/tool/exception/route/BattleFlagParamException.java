package tool.exception.route;

import tool.Constants;

/**
 * A exception to be raised when a battle flag with a parameter is faulty.
 */
public class BattleFlagParamException extends RouteParserException {
	private static final long serialVersionUID = 1L;

	/**
	 * Returns a route parsing exception related to a flag with a parameter, with complementary information. 
	 * Style consistency: the complementary information expects a starting uppercase, a final punctuation and no final line break.
	 */
	public BattleFlagParamException(String flag, String paramater, String complementaryInfo) {
		super(String.format("in flag '%s', received '%s'.%s", flag, paramater, 
				complementaryInfo != null ? String.format("%s%s", Constants.endl, complementaryInfo) : ""));
	}
	
	/**
	 * Returns a route parsing exception related to a flag with a parameter.
	 */
	public BattleFlagParamException(String flag, String paramater) {
		this(flag, paramater, null);
	}
}