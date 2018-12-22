package synologydownloadstationapi.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import synologydownloadstationapi.beans.SynoDSTorrent;
import synologydownloadstationapi.beans.SynoDSTorrentFile;
import synologydownloadstationapi.exceptions.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Romain on 11/11/2018.
 */
public class SynoDSAPI {

    private static final String UTF_8 = "UTF-8";

    private String urlString;

    private String sessionId;

    public SynoDSAPI(String urlString, String username, String password) throws SynoDSURLException, SynoDSLoginException, IOException {
        this.urlString = urlString + "/webapi";

        if (urlString == null) {
            throw new SynoDSURLException(urlString);
        }

        CookieManager cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);
        URL url = new URL(this.urlString + "/auth.cgi?api=SYNO.API.Auth&version=2&method=login&account=" + username + "&passwd=" + password + "&session=DownloadStation&format=cookie");

        this.urlString = this.urlString + "/DownloadStation/task.cgi?api=SYNO.DownloadStation.Task";

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setDoInput(true);
        connection.setDoOutput(true);

        connection.getContent();

        List<HttpCookie> cookies = cookieManager.getCookieStore().getCookies();
        for (HttpCookie cookie : cookies) {
            if (cookie.getName().equals("id")) {
                sessionId = cookie.getValue();
            }
        }

        if (sessionId == null) {
            throw new SynoDSLoginException(username);
        }
    }

    public List<SynoDSTorrent> getTorrentList() throws SynoDSTorrentsFetchingException {
        String url = null;

        try {
            List<SynoDSTorrent> torrents = new ArrayList<>();

            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            url = urlString + "&version=1&method=list&additional=detail,file";
            JsonNode tasksNode = mapper.readTree(getRequest(url)).get("data").get("tasks");
            for (JsonNode taskNode : tasksNode) {
                if (taskNode.get("type").asText().equals("bt")) {
                    SynoDSTorrent torrent = mapper.readValue(taskNode.toString(), SynoDSTorrent.class);
                    JsonNode additionalNode = taskNode.get("additional");
                    if (additionalNode.has("file")) {
                        JsonNode fileNode = additionalNode.get("file");
                        torrent.setFiles(mapper.readValue(fileNode.toString(), new TypeReference<List<SynoDSTorrentFile>>() {}));
                    }
                    torrents.add(torrent);
                }
            }

            return torrents;
        } catch (Exception e) {
            throw new SynoDSTorrentsFetchingException(url);
        }
    }

    public SynoDSTorrent getTorrent(String id) throws SynoDSTorrentFetchingException {
        String url = null;

        try {
            SynoDSTorrent torrent = null;

            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            url = urlString + "&version=1&method=getinfo&additional=detail,file&id=" + id;
            JsonNode taskNode = mapper.readTree(getRequest(url)).get("data").get("tasks").get(0);
            if (taskNode.get("type").asText().equals("bt")) {
                torrent = mapper.readValue(taskNode.toString(), SynoDSTorrent.class);
                JsonNode additional = taskNode.get("additional");
                if (additional.has("file")) {
                    JsonNode fileNode = additional.get("file");
                    torrent.setFiles(mapper.readValue(fileNode.toString(), new TypeReference<List<SynoDSTorrentFile>>() {}));
                }
            }

            return torrent;
        } catch (Exception e) {
            throw new SynoDSTorrentFetchingException(id, url);
        }
    }

    public void addTorrents(List<String> urls, String destination) throws SynoDSGetException, SynoDSParameterException {
        String url = urlString + "&version=3&method=create&destination=" + destination + "&uri=" + encodeParameter(String.join(",", urls));
        getRequest(url);
    }

    public void deleteTorrents(List<String> ids) throws SynoDSGetException {
        String url = urlString + "&version=1&method=delete&id=" + String.join(",", ids);
        getRequest(url);
    }

    public void pauseTorrents(List<String> ids) throws SynoDSGetException {
        String url = urlString + "&version=1&method=pause&id=" + String.join(",", ids);
        getRequest(url);
    }

    public void resumeTorrents(List<String> ids) throws SynoDSGetException {
        String url = urlString + "&version=1&method=resume&id=" + String.join(",", ids);
        getRequest(url);
    }

    public void pauseAllTorrents() throws SynoDSTorrentsFetchingException, SynoDSGetException {
        List<String> ids = getTorrentList().stream().map(SynoDSTorrent::getId).collect(Collectors.toList());
        if (!ids.isEmpty()) {
            pauseTorrents(ids);
        }
    }

    public void resumeAllTorrents() throws SynoDSTorrentsFetchingException, SynoDSGetException {
        List<String> ids = getTorrentList().stream().map(SynoDSTorrent::getId).collect(Collectors.toList());
        if (!ids.isEmpty()) {
            resumeTorrents(ids);
        }
    }

    private String getRequest(String url) throws SynoDSGetException {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestProperty("Cookie", "id=" + sessionId);

            BufferedReader br = new BufferedReader(new InputStreamReader((connection.getInputStream())));
            StringBuilder sb = new StringBuilder();
            String output;
            while ((output = br.readLine()) != null) {
                sb.append(output);
            }

            return sb.toString();
        } catch (Exception e) {
            throw new SynoDSGetException(url);
        }
    }

    private String encodeParameter(String parameter) throws SynoDSParameterException {
        try {
            return URLEncoder.encode(parameter, UTF_8);
        } catch (Exception e) {
            throw new SynoDSParameterException(parameter);
        }
    }
}
