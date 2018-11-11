package synologydownloadstationapi.exceptions;

import java.util.Map;

/**
 * Created by Romain on 02/11/2018.
 */
public class SynoDSParametersException extends SynoDSAPIException {

    private static final long serialVersionUID = 1L;

    public SynoDSParametersException(Map<String, String> parameters) {
        super("Error parsing parameters : " + parameters.toString());
    }
}
