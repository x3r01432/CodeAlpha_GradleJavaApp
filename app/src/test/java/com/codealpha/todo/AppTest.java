package com.codealpha.todo;

import org.junit.jupiter.api.*;
import java.net.URI;
import java.net.http.*;
import static org.junit.jupiter.api.Assertions.*;

class AppTest {
    static App app; static HttpClient http = HttpClient.newHttpClient(); static String base;

    @BeforeAll static void up() throws Exception {
        app = new App(0, new TodoService()); app.start();
        base = "http://localhost:" + app.port();
    }
    @AfterAll static void down() { app.stop(); }

    HttpResponse<String> call(String method, String path, String body) throws Exception {
        var b = HttpRequest.newBuilder(URI.create(base + path));
        b.method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
        return http.send(b.build(), HttpResponse.BodyHandlers.ofString());
    }

    @Test void health() throws Exception {
        assertEquals(200, call("GET", "/health", null).statusCode());
    }
    @Test void crudFlow() throws Exception {
        var c = call("POST", "/todos", "{\"title\":\"Learn Gradle\"}");
        assertEquals(201, c.statusCode());
        assertTrue(c.body().contains("Learn Gradle"));
        String id = c.body().replaceAll(".*\"id\":(\\d+).*", "$1");
        assertEquals(200, call("GET", "/todos", null).statusCode());
        assertEquals(200, call("GET", "/todos/" + id, null).statusCode());
        assertTrue(call("PUT", "/todos/" + id, null).body().contains("\"done\":true"));
        assertEquals(204, call("DELETE", "/todos/" + id, null).statusCode());
        assertEquals(404, call("GET", "/todos/" + id, null).statusCode());
        assertEquals(404, call("DELETE", "/todos/" + id, null).statusCode());
        assertEquals(404, call("PUT", "/todos/" + id, null).statusCode());
    }
    @Test void badInput() throws Exception {
        assertEquals(400, call("POST", "/todos", "{}").statusCode());
        assertEquals(400, call("POST", "/todos", "not json").statusCode());
        assertEquals(400, call("POST", "/todos", "x".repeat(5000)).statusCode());
        assertEquals(400, call("GET", "/todos/abc", null).statusCode());
        assertEquals(405, call("PATCH", "/todos", null).statusCode());
        assertEquals(405, call("PATCH", "/todos/1", null).statusCode());
    }
}
