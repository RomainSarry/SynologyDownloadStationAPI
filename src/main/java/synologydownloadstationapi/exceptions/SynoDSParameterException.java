package synologydownloadstationapi.exceptions;

import java.util.Map;

/**
 * Created by Romain on 02/11/2018.
 */
public class SynoDSParameterException extends SynoDSAPIException {

    private static final long serialVersionUID = 1L;

    public SynoDSParameterException(String parameter) {
        super("Error encoding parameter : " + parameter);
    }
}
