package it.ciopper90.unimorelogin.Exceptions;

public class SaveDataException extends Exception {
	/**
	 * @author Copelli Alberto
	 */
	private static final long serialVersionUID = 1L;
	private String errore;
	private final String EXCEPTION_NAME = "SaveDataException";

	public SaveDataException() {
		super();
		this.errore = "unknown";
	}

	public SaveDataException(String error) {
		super(error);
		this.errore = error;
	}

	@Override
	public String getMessage() {
		return this.errore;
	}

	@Override
	public String toString() {
		return EXCEPTION_NAME + this.errore;
	}
}