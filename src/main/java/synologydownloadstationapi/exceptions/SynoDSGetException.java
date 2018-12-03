package synologydownloadstationapi.exceptions;

/**
 * Created by Romain on 03/11/2018.
 */
public class SynoDSGetException extends SynoDSAPIException {

    private static final long serialVersionUID = 1L;

    public SynoDSGetException(String url) {
        super("Error getting from url : " + url);
    }
}
