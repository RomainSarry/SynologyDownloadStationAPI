package synologydownloadstationapi.exceptions;

/**
 * Created by Romain on 11/11/2018.
 */
public abstract class SynoDSAPIException extends Exception {
    public SynoDSAPIException(String message) {
        super(message);
    }
}
