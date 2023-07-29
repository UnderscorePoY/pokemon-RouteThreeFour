package tool.exception.config;

public class ConfigMissingKeyException extends ConfigException {
	private static final long serialVersionUID = 1L;

	/**
	 * Style consistency : additionalInfo must start with a capital, have a final punctuation and no final line break.
	 */
	public ConfigMissingKeyException(String configFileName, String sectionName, String optionName, String additionalInfo) {
		super(String.format("missing mandatory key '%s' in '%s', in section [%s].%s",
				optionName, configFileName, sectionName,
				additionalInfo == null ? "" : additionalInfo));
	}
}
