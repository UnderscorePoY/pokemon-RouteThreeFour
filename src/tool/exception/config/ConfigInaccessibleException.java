package tool.exception.config;


public class ConfigInaccessibleException extends ConfigException {
	private static final long serialVersionUID = 1L;

	public ConfigInaccessibleException(String typeOfFaulty, String faultyFileName, String providedInFileName, String additionalInfo) {
		super(String.format(
				"inaccessible %s file '%s'%s.%s", 
				typeOfFaulty, faultyFileName,
				(providedInFileName == null) ? "" : String.format(" provided in '%s'", providedInFileName),
				(additionalInfo == null) ? "" : String.format(" %s", additionalInfo)
		));
	}
}