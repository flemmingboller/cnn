package com.simplejava;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AppTest {

    @Test
    void messageReturnsGreeting() {
        assertEquals("Hello, SimpleJava!", App.message());
    }

    @Test
    void createServerStartsHttpServer() throws IOException, InterruptedException {
        HttpServer server = App.createServer(0);
        try {
            int port = server.getAddress().getPort();
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + port + "/"))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(200, response.statusCode());
            assertNotNull(response.body());
            assertEquals("SimpleJava is running", response.body());
        } finally {
            server.stop(0);
        }
    }

    @Test
    void helloEndpointReturnsJson() throws IOException, InterruptedException {
        HttpServer server = App.createServer(0);
        try {
            int port = server.getAddress().getPort();
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + port + "/hello"))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(200, response.statusCode());
            assertTrue(response.headers().firstValue("Content-Type").orElse("").contains("application/json"));
            assertTrue(response.body().contains("\"message\":\"hello\""));
        } finally {
            server.stop(0);
        }
    }

    @Test
    void helloWorldEndpointReturnsJson() throws IOException, InterruptedException {
        HttpServer server = App.createServer(0);
        try {
            int port = server.getAddress().getPort();
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + port + "/hello-world"))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(200, response.statusCode());
            assertTrue(response.headers().firstValue("Content-Type").orElse("").contains("application/json"));
            assertEquals("{\"message\":\"Hello, World!\"}", response.body());
        } finally {
            server.stop(0);
        }
    }

    @Test
    void restClientReturnsRawJsonResponse() throws IOException {
        HttpServer server = App.createServer(0);
        try {
            int port = server.getAddress().getPort();
            String response = new RestClient().get("http://localhost:" + port + "/hello-world");

            assertEquals("{\"message\":\"Hello, World!\"}", response);
        } finally {
            server.stop(0);
        }
    }

    @Test
    void restClientMapsJsonPlaceholderUserAndPrintsResponse() {
        JsonPlaceholderUserDto response = new RestClient().get(
                "https://jsonplaceholder.typicode.com/users/1",
                JsonPlaceholderUserDto.class);

        System.out.println("JSONPlaceholder response: " + response);
        assertEquals(1, response.id());
        assertEquals("Leanne Graham", response.name());
        assertEquals("Gwenborough", response.address().city());
        assertEquals("Romaguera-Crona", response.company().name());
    }

    @Test
    void restClientSendsConfiguredAndCustomHeaders() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/headers", exchange -> {
            String body = exchange.getRequestHeaders().getFirst("Accept") + "|"
                    + exchange.getRequestHeaders().getFirst("X-Test");
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        server.start();
        try {
            int port = server.getAddress().getPort();
            String response = new RestClient().get(
                    "http://localhost:" + port + "/headers",
                    Map.of("X-Test", "custom"));

            assertEquals("application/json|custom", response);
        } finally {
            server.stop(0);
        }
    }

    @Test
    void unknownEndpointReturnsNotFound() throws IOException, InterruptedException {
        HttpServer server = App.createServer(0);
        try {
            int port = server.getAddress().getPort();
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + port + "/missing"))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(404, response.statusCode());
            assertTrue(response.body().contains("Not Found"));
        } finally {
            server.stop(0);
        }
    }
}
