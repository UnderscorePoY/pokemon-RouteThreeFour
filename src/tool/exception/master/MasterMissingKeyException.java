package tool.exception.master;

import tool.exception.config.ConfigException;


public class MasterMissingKeyException extends ConfigException {
	private static final long serialVersionUID = 1L;

	/**
	 * Style consistency : additionalInfo must start with a capital, have a final punctuation and no final line break.
	 */
	public MasterMissingKeyException(String masterFileName, String sectionName, String optionName, String additionalInfo) {
		super(String.format("missing mandatory key '%s' in '%s', in section [%s].%s",
				optionName, masterFileName, sectionName,
				additionalInfo == null ? "" : additionalInfo));
	}
}
