package tool.exception.config;

import tool.Constants;

public class ConfigException extends Exception {
	private static final long serialVersionUID = 1L;

	/**
	 * Returns a general config parsing exception. 
	 * Style consistency: the message expects a starting lowercase, a final punctuation and no final line break.
	 */
	public ConfigException(String message) {
		super(String.format("CONFIG: %s%s", message, Constants.endl));
	}
}
