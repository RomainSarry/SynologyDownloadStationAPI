package synologydownloadstationapi.beans;

import java.util.List;

/**
 * Created by Romain on 11/11/2018.
 */
public class SynoDSTorrent {
    private List<SynoDSTorrentFile> files;

    private String id;

    private String status;

    private String title;

    public List<SynoDSTorrentFile> getFiles() {
        return files;
    }

    public void setFiles(List<SynoDSTorrentFile> files) {
        this.files = files;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
