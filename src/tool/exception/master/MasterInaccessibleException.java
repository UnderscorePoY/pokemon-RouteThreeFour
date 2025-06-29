package tool.exception.master;


public class MasterInaccessibleException extends MasterException {
	private static final long serialVersionUID = 1L;

	public MasterInaccessibleException(String typeOfFaulty, String faultyFileName, String providedInFileName, String additionalInfo) {
		super(String.format(
				"inaccessible %s file '%s'%s.%s", 
				typeOfFaulty, faultyFileName,
				(providedInFileName == null) ? "" : String.format(" provided in '%s'", providedInFileName),
				(additionalInfo == null) ? "" : String.format(" %s", additionalInfo)
		));
	}
}
