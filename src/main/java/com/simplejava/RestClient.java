package com.simplejava;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

public class RestClient {

    private final HttpClient client;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Duration requestTimeout;
    private final Map<String, String> defaultHeaders;

    public RestClient() {
        this(new ApplicationConfiguration(null));
    }

    public RestClient(ApplicationConfiguration configuration) {
        HttpClient.Builder clientBuilder = HttpClient.newBuilder()
                .version(HttpClient.Version.valueOf(configuration.getStringParam("rest-client.http-version")))
                .connectTimeout(Duration.ofMillis(configuration.getParam("rest-client.connect-timeout-millis")));

        if (configuration.getBooleanParam("rest-client.proxy.enabled")) {
            clientBuilder.proxy(ProxySelector.of(new InetSocketAddress(
                    configuration.getStringParam("rest-client.proxy.host"),
                    configuration.getParam("rest-client.proxy.port"))));
        }

        if (configuration.getBooleanParam("rest-client.authentication.enabled")) {
            String username = configuration.getStringParam("rest-client.authentication.username");
            String password = configuration.getStringParam("rest-client.authentication.password");
            clientBuilder.authenticator(new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password.toCharArray());
                }
            });
        }

        this.client = clientBuilder.build();
        this.requestTimeout = Duration.ofMillis(configuration.getParam("rest-client.request-timeout-millis"));
        this.defaultHeaders = configuration.getStringMapParam("rest-client.headers");
    }

    public String get(String url) {
        return get(url, Map.of());
    }

    public <T> T get(String url, Class<T> responseType) {
        return get(url, Map.of(), responseType);
    }

    public <T> T get(String url, Map<String, String> headers, Class<T> responseType) {
        try {
            return objectMapper.readValue(get(url, headers), responseType);
        } catch (Exception e) {
            throw new RuntimeException("Could not map GET response", e);
        }
    }

    public String get(String url, Map<String, String> headers) {
        try {
            var requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(requestTimeout)
                    .GET();

                Map<String, String> allHeaders = new LinkedHashMap<>(defaultHeaders);
                allHeaders.putAll(headers);
                allHeaders.forEach(requestBuilder::setHeader);

            var response = client.send(
                    requestBuilder.build(),
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new RuntimeException("HTTP error: " + response.statusCode());
            }

            return response.body();
        } catch (Exception e) {
            throw new RuntimeException("GET request failed", e);
        }
    }
}