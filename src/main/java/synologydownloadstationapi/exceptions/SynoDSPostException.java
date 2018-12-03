package synologydownloadstationapi.exceptions;

import java.util.Map;

/**
 * Created by Romain on 03/11/2018.
 */
public class SynoDSPostException extends SynoDSAPIException {

    private static final long serialVersionUID = 1L;

    public SynoDSPostException(String url) {
        super("Error posting to url : " + url);
    }

    public SynoDSPostException(String url, Map<String, String> parameters) {
        super("Error posting to url : " + url + " with parameters : " + parameters.toString());
    }
}
