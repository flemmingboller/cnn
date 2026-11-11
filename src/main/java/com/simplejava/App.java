package com.simplejava;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class App {

    public static int portFromConfig(String[] args) {
        String overridePath = null;
        for (String arg : args) {
            if (arg.startsWith("--config=")) {
                overridePath = arg.substring("--config=".length());
            }
        }
        return new ApplicationConfiguration(overridePath).getParam("server.port");
    }

    public static HttpServer createServer(int port) throws IOException {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.register("GET", "/", () -> new Response(200, "text/plain; charset=UTF-8", "SimpleJava is running"));
        dispatcher.register("GET", "/hello", new HelloController());
        dispatcher.register("GET", "/hello-world", new HelloWorldController());

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", dispatcher::handle);
        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        server.start();
        return server;
    }

    public static void main(String[] args) throws IOException {
        int port = portFromConfig(args);
        System.out.println("SimpleJava is starting...");
        HttpServer server = createServer(port);
        System.out.println("Server started on http://localhost:" + server.getAddress().getPort());
    }

    public static String message() {
        return "Hello, SimpleJava!";
    }
}
