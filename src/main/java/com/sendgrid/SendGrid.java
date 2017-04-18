package com.sendgrid;

import com.sendgrid.helpers.mail.objects.ContentType;

import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Class SendGrid allows for quick and easy access to the SendGrid API.
 */
public class SendGrid {
    private static final String VERSION = "3.0.0";
    private static final String USER_AGENT = "sendgrid/" + VERSION + ";java";

    private String apiKey;
    private String host;
    private String version;
    private Client client;
    private Map<String, String> requestHeaders;

    /**
     * @param apiKey is your SendGrid API Key: https://app.sendgrid.com/settings/api_keys
     */
    public SendGrid(String apiKey) {
        this.apiKey = apiKey;
        this.client = new Client();
        initializeSendGrid();
    }

    /**
     * @param apiKey is your SendGrid API Key: https://app.sendgrid.com/settings/api_keys
     * @param test   is true if you are unit testing
     */
    public SendGrid(String apiKey, Boolean test) {
        this.apiKey = apiKey;
        this.client = new Client(test);
        initializeSendGrid();
    }

    /**
     * @param apiKey is your SendGrid API Key: https://app.sendgrid.com/settings/api_keys
     * @param client the Client to use (allows to customize its configuration)
     */
    public SendGrid(String apiKey, Client client) {
        this.apiKey = apiKey;
        this.client = client;
        initializeSendGrid();
    }

    /**
     * @param apiKey              is your SendGrid API Key: https://app.sendgrid.com/settings/api_keys
     * @param closeableHttpClient pass your own tuned client to be used
     *                            <p>Example</p>
     *                            <pre>
     *                                 PoolingHttpClientConnectionManager poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager();
     *                                 poolingHttpClientConnectionManager.setMaxTotal(10_000);
     *                                 poolingHttpClientConnectionManager.setDefaultMaxPerRoute(10_000);
     *                                 RequestConfig defaultRequestConfig = RequestConfig.custom()
     *                                 .setSocketTimeout(5000)
     *                                 .setConnectTimeout(5000)
     *                                 .setConnectionRequestTimeout(5000)
     *                                 .build();
     *                                 CloseableHttpClient httpClient = HttpClients.custom()
     *                                 .setDefaultRequestConfig(defaultRequestConfig)
     *                                 .setConnectionManager(poolingHttpClientConnectionManager)
     *                                 .build();
     *
     *                                    new SendGrid("xxxx", httpclient);
     *                            </pre>
     */
    public SendGrid(String apiKey, CloseableHttpClient closeableHttpClient) {
        this.apiKey = apiKey;
        this.client = new Client(closeableHttpClient);
        initializeSendGrid();
    }

    public void initializeSendGrid() {
        this.host = "api.sendgrid.com";
        this.version = "v3";
        this.requestHeaders = new HashMap<>();
        this.requestHeaders.put("Authorization", "Bearer " + this.apiKey);
        this.requestHeaders.put("User-agent", USER_AGENT);
        this.requestHeaders.put("Accept", ContentType.APPLICATION_JSON);
    }

    public String getLibraryVersion() {
        return VERSION;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Map<String, String> getRequestHeaders() {
        return this.requestHeaders;
    }

    public Map<String, String> addRequestHeader(String key, String value) {
        this.requestHeaders.put(key, value);
        return getRequestHeaders();
    }

    public Map<String, String> removeRequestHeader(String key) {
        this.requestHeaders.remove(key);
        return getRequestHeaders();
    }

    public String getHost() {
        return this.host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Class makeCall makes the call to the SendGrid API, override this method for testing.
     */
    public Response makeCall(Request request) throws IOException {
        return this.client.api(request);
    }

    /**
     * Class api sets up the request to the SendGrid API, this is main interface.
     */
    public Response api(Request request) throws IOException {
        Request req = new Request();
        req.method = request.method;
        req.baseUri = this.host;
        req.endpoint = "/" + version + "/" + request.endpoint;
        req.body = request.body;
        req.headers = this.requestHeaders;
        req.queryParams = request.queryParams;

        return makeCall(req);
    }

}
