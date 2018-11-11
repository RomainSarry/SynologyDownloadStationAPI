package synologydownloadstationapi.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import synologydownloadstationapi.beans.SynoDSTorrent;
import synologydownloadstationapi.beans.SynoDSTorrentFile;
import synologydownloadstationapi.exceptions.*;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Romain on 11/11/2018.
 */
public class SynoDSAPI {

    private static final String UTF_8 = "UTF-8";

    private String urlString;

    private String sessionId;

    public SynoDSAPI(String urlString, String username, String password) throws Exception {
        this.urlString = urlString + "/webapi";

        CookieManager cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);
        URL url = new URL(urlString + "/auth.cgi?api=SYNO.API.Auth&version=1&method=login&account=" + username + "&passwd=" + password + "&session=DownloadStation&format=cookie");

        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoInput(true);
        connection.setDoOutput(true);

        OutputStream os = connection.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, UTF_8));
        writer.write("username=" + URLEncoder.encode(username, UTF_8) + "&password="
                + URLEncoder.encode(password, UTF_8));
        writer.flush();
        writer.close();
        os.close();

        connection.getContent();

        List<HttpCookie> cookies = cookieManager.getCookieStore().getCookies();
        for (HttpCookie cookie : cookies) {
            if (cookie.getName().equals("id")) {
                sessionId = cookie.getValue();
            }
        }
    }

    public List<SynoDSTorrent> getTorrentsList() throws SynoDSTorrentsFetchingException {
        String url = null;

        try {
            List<SynoDSTorrent> torrents = new ArrayList<>();

            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            url = urlString + "/DownloadStation/task.cgi?api=SYNO.DownloadStation.Task&version=1&method=list&additional=detail,file";
            ArrayNode tasksNode = (ArrayNode) mapper.readTree(getRequest(url)).get("data").get("tasks");
            for (JsonNode taskNode : tasksNode) {
                if (taskNode.get("type").equals("bt")) {
                    SynoDSTorrent torrent = mapper.readValue(taskNode.toString(), SynoDSTorrent.class);
                    JsonNode additionnalNode = taskNode.get("additiionnal");
                    if (additionnalNode.has("file")) {
                        JsonNode fileNode = additionnalNode.get("file");
                        torrent.setFiles(mapper.readValue(fileNode.toString(), new TypeReference<List<SynoDSTorrentFile>>() {}));
                    }
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

            url = urlString + "/DownloadStation/task.cgi?api=SYNO.DownloadStation.Task&version=1&method=getinfo&additional=detail,file&id=" + id;
            JsonNode taskNode = (ArrayNode) mapper.readTree(getRequest(url)).get("data").get("tasks").get(0);
            if (taskNode.get("type").equals("bt")) {
                torrent = mapper.readValue(taskNode.toString(), SynoDSTorrent.class);
                JsonNode additionnalNode = taskNode.get("additiionnal");
                if (additionnalNode.has("file")) {
                    JsonNode fileNode = additionnalNode.get("file");
                    torrent.setFiles(mapper.readValue(fileNode.toString(), new TypeReference<List<SynoDSTorrentFile>>() {}));
                }
            }

            return torrent;
        } catch (Exception e) {
            throw new SynoDSTorrentFetchingException(id, url);
        }
    }

    public void addTorrents(List<String> urls, Map<String, String> parameters) {

    }

    public void deleteTorrents(List<String> ids) {

    }

    public void pauseTorrents(List<String> ids) {

    }

    public void resumeTorrents(List<String> ids) {

    }

    private String getParamsAsString(Map<String, String> parameters) throws SynoDSParametersException {
        try {
            List<NameValuePair> paramsAsNameValuePairs = new LinkedList<>();
            if (parameters != null && !parameters.isEmpty()) {
                for (Map.Entry<String, String> entry : parameters.entrySet()) {
                    paramsAsNameValuePairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
                }
            }

            return URLEncodedUtils.format(paramsAsNameValuePairs, UTF_8);
        } catch (Exception e) {
            throw new SynoDSParametersException(parameters);
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

    private void postRequest(String url, Map<String, String> parameters) throws SynoDSParametersException, SynoDSPostException {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestProperty("Cookie", "id=" + sessionId);
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);

            OutputStream os = connection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, UTF_8));
            writer.write(getParamsAsString(parameters));
            writer.flush();
            writer.close();
            os.close();

            connection.getInputStream();
        } catch (SynoDSParametersException e) {
            throw e;
        } catch (Exception e) {
            if (parameters == null || parameters.isEmpty()) {
                throw new SynoDSPostException(url);
            } else {
                throw new SynoDSPostException(url, parameters);
            }
        }
    }
}
