package it.ciopper90.unimorelogin.Farcade;

import it.ciopper90.unimorelogin.Exceptions.LoginException;
import it.ciopper90.unimorelogin.Exceptions.LogoutException;
import it.ciopper90.unimorelogin.Model.Authentication;
import it.ciopper90.unimorelogin.Model.Data;


public class FacadeController {
	/**
	 * @author Copelli Alberto
	 */
	private static FacadeController instance;


	private FacadeController() {

	}

	public void login(Data data, String ip, String mac) throws LoginException {
		try {
			Authentication.Authenticate(data, ip, mac);
		} catch (LoginException e) {
			throw new LoginException("FacadeController " + e.getMessage());
		}
	}

	public void logout() throws LogoutException {
		try {
			Authentication.logout();
		} catch (LogoutException e) {
			throw new LogoutException("FacadeController " + e.getMessage());
		}
	}

	/** Restituisce l'unica istanza di FacadeController */
	public static FacadeController getInstance() {
		if (instance == null) {
			instance = new FacadeController();
		}
		return instance;
	}
}
