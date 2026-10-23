package com.codealpha.todo;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/** Minimal REST API for to-dos using the JDK HTTP server. */
public class App {
    private static final Gson GSON = new Gson();
    private static final int MAX_BODY = 4096;
    private final TodoService service;
    private final HttpServer server;

    public App(int port, TodoService service) throws IOException {
        this.service = service;
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/health", ex -> send(ex, 200, Map.of("status", "UP")));
        server.createContext("/todos", this::handleTodos);
    }

    public void start() { server.start(); }
    public void stop() { server.stop(0); }
    public int port() { return server.getAddress().getPort(); }

    void handleTodos(HttpExchange ex) throws IOException {
        try {
            String path = ex.getRequestURI().getPath().replaceAll("/+$", "");
            String method = ex.getRequestMethod();
            if (path.equals("/todos")) {
                switch (method) {
                    case "GET" -> send(ex, 200, service.list());
                    case "POST" -> {
                        JsonObject body = GSON.fromJson(readBody(ex), JsonObject.class);
                        String title = body != null && body.has("title") && !body.get("title").isJsonNull()
                                ? body.get("title").getAsString() : null;
                        send(ex, 201, service.create(title));
                    }
                    default -> send(ex, 405, error("method not allowed"));
                }
                return;
            }
            long id = Long.parseLong(path.substring("/todos/".length()));
            switch (method) {
                case "GET" -> service.get(id).ifPresentOrElse(t -> ok(ex, t), () -> notFound(ex));
                case "PUT" -> service.complete(id).ifPresentOrElse(t -> ok(ex, t), () -> notFound(ex));
                case "DELETE" -> { if (service.delete(id)) send(ex, 204, null); else notFound(ex); }
                default -> send(ex, 405, error("method not allowed"));
            }
        } catch (IllegalArgumentException | JsonParseException | IllegalStateException | UnsupportedOperationException e) {
            send(ex, 400, error(e instanceof NumberFormatException ? "invalid id" : safe(e)));
        } catch (StringIndexOutOfBoundsException e) {
            send(ex, 404, error("not found"));
        }
    }

    private static String safe(Exception e) {
        return e instanceof JsonParseException || e instanceof IllegalStateException
                || e instanceof UnsupportedOperationException ? "invalid JSON body" : e.getMessage();
    }

    private static String readBody(HttpExchange ex) throws IOException {
        try (InputStream in = ex.getRequestBody()) {
            byte[] b = in.readNBytes(MAX_BODY + 1);
            if (b.length > MAX_BODY) throw new IllegalArgumentException("body too large");
            return new String(b, StandardCharsets.UTF_8);
        }
    }

    private static Map<String, String> error(String msg) { return Map.of("error", msg); }
    private static void ok(HttpExchange ex, Object o) { sendUnchecked(ex, 200, o); }
    private static void notFound(HttpExchange ex) { sendUnchecked(ex, 404, error("not found")); }

    private static void sendUnchecked(HttpExchange ex, int code, Object o) {
        try { send(ex, code, o); } catch (IOException e) { throw new java.io.UncheckedIOException(e); }
    }

    static void send(HttpExchange ex, int code, Object body) throws IOException {
        if (body == null) { ex.sendResponseHeaders(code, -1); ex.close(); return; }
        byte[] out = GSON.toJson(body).getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json");
        ex.sendResponseHeaders(code, out.length);
        ex.getResponseBody().write(out);
        ex.close();
    }

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        App app = new App(port, new TodoService());
        app.start();
        System.out.println("To-do API listening on http://localhost:" + app.port());
    }
}
