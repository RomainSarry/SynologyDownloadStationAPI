package synologydownloadstationapi.exceptions;

/**
 * Created by Romain on 28/10/2018.
 */
public class SynoDSURLException extends SynoDSAPIException {

	private static final long serialVersionUID = 1L;

    public SynoDSURLException(String urlString) {
        super("Cannot connect to Synology Download Station with URL : " + urlString);
    }
}
