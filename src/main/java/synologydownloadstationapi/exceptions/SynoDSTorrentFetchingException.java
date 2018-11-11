package synologydownloadstationapi.exceptions;

/**
 * Created by Romain on 28/10/2018.
 */
public class SynoDSTorrentFetchingException extends SynoDSAPIException {

	private static final long serialVersionUID = 1L;

    public SynoDSTorrentFetchingException(String id, String urlString) {
        super("Error fetching torrent with id : " + id + " at URL : " + urlString);
    }
}
