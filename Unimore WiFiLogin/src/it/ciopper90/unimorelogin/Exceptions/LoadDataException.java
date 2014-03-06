package it.ciopper90.unimorelogin.Exceptions;

public class LoadDataException extends Exception {
	/**
	 * @author Copelli Alberto
	 */
	private static final long serialVersionUID = 1L;
	private String errore;
	private final String EXCEPTION_NAME = "LoadDataException";

	public LoadDataException() {
		super();
		this.errore = "unknown";
	}

	public LoadDataException(String error) {
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
