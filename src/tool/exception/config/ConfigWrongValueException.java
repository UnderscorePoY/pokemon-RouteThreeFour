package tool.exception.config;

import tool.Constants;

public class ConfigWrongValueException extends ConfigException {
	private static final long serialVersionUID = 1L;

	/**
	 * Style consistency : additionalInfo must start without a capital, have a final punctuation and no final line break.
	 */
	public ConfigWrongValueException(String configFileName, String sectionName, String optionName, String faultyValue, String additionalInfo) {
		super(String.format("invalid %s value in '%s', in section [%s], received '%s'.%s", 
				optionName, configFileName, sectionName, faultyValue,
				additionalInfo == null ? "" : String.format("%s%s %s", Constants.endl, optionName, additionalInfo)));
	}
}
