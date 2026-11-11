package com.simplejava;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Dispatcher {

    private final Map<String, Map<String, RestController>> routes = new HashMap<>();

    public void register(String method, String path, RestController controller) {
        routes.computeIfAbsent(method.toUpperCase(), ignored -> new HashMap<>())
                .put(path, controller);
    }

    public Response dispatch(String method, String path) {
        RestController controller = routes.getOrDefault(method.toUpperCase(), Map.of()).get(path);
        if (controller == null) {
            return new Response(404, "text/plain; charset=UTF-8", "Not Found");
        }
        return controller.handle();
    }

    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        Response response = dispatch(method, path);
        byte[] bytes = response.body().getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set("Content-Type", response.contentType());
        exchange.sendResponseHeaders(response.statusCode(), bytes.length);

        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(bytes);
        }
    }
}
