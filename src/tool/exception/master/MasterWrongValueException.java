package tool.exception.master;

import tool.Constants;
import tool.exception.config.ConfigException;


public class MasterWrongValueException extends ConfigException {
	private static final long serialVersionUID = 1L;

	/**
	 * Style consistency : additionalInfo must start with a capital, have a final punctuation and no final line break.
	 */
	public MasterWrongValueException(String masterFileName, String sectionName, String optionName, String faultyValue, String additionalInfo) {
		super(String.format("invalid %s value in '%s', in section [%s], received '%s'.%s", 
				optionName, masterFileName, sectionName, faultyValue,
				additionalInfo == null ? "" : String.format("%s%s %s", Constants.endl, optionName, additionalInfo)));
	}
}
