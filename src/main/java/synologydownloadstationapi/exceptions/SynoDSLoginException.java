package synologydownloadstationapi.exceptions;

/**
 * Created by Romain on 28/10/2018.
 */
public class SynoDSLoginException extends SynoDSAPIException {

	private static final long serialVersionUID = 1L;

    public SynoDSLoginException(String username) {
        super("Cannot connect to Synology Download Station with user : " + username);
    }
}
