package tool.exception.master;

import tool.Constants;

public abstract class MasterException extends Exception {
	private static final long serialVersionUID = 1L;

	/**
	 * Returns a general master parsing exception. 
	 * Style consistency: the message expects a starting lowercase, a final punctuation and no final line break.
	 */
	public MasterException(String message) {
		super(String.format("MASTER: %s%s", message, Constants.endl));
	}
}
