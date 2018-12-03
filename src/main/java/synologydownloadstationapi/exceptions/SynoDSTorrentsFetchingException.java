package synologydownloadstationapi.exceptions;

/**
 * Created by Romain on 28/10/2018.
 */
public class SynoDSTorrentsFetchingException extends SynoDSAPIException {

	private static final long serialVersionUID = 1L;

    public SynoDSTorrentsFetchingException(String urlString) {
        super("Error fetching torrents at URL : " + urlString);
    }
}
