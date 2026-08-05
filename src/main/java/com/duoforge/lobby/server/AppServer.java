package com.duoforge.lobby.server;

import com.duoforge.lobby.model.Match;
import com.duoforge.lobby.model.Player;
import com.duoforge.lobby.service.LobbyEngine;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.Executors;

public final class AppServer {
    private static final Map<String, String> MIME_TYPES = Map.of(
            "html", "text/html; charset=utf-8",
            "css", "text/css; charset=utf-8",
            "js", "application/javascript; charset=utf-8",
            "json", "application/json; charset=utf-8",
            "svg", "image/svg+xml"
    );

    private final LobbyEngine engine;
    private final HttpServer server;

    public AppServer(int port, LobbyEngine engine) throws IOException {
        this.engine = engine;
        this.server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
        this.server.createContext("/", new Router());
        this.server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(0);
    }

    private final class Router implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                    cors(exchange);
                    exchange.sendResponseHeaders(204, -1);
                    return;
                }

                String path = exchange.getRequestURI().getPath();
                if (path.startsWith("/api/")) handleApi(exchange, path);
                else serveStatic(exchange, path);
            } catch (IllegalArgumentException exception) {
                sendJson(exchange, 400, "{\"error\":" + Json.quote(exception.getMessage()) + "}");
            } catch (Exception exception) {
                exception.printStackTrace();
                sendJson(exchange, 500, "{\"error\":\"Unexpected server error.\"}");
            }
        }
    }

    private void handleApi(HttpExchange exchange, String path) throws IOException {
        String method = exchange.getRequestMethod().toUpperCase();
        if (method.equals("GET") && path.equals("/api/health")) {
            sendJson(exchange, 200, "{\"status\":\"healthy\",\"product\":\"DuoForge\"}");
            return;
        }
        if (method.equals("GET") && path.equals("/api/state")) {
            sendJson(exchange, 200, Json.state(engine));
            return;
        }
        if (method.equals("GET") && path.startsWith("/api/players/")) {
            int playerId = parsePathId(path, "/api/players/");
            Player player = engine.getPlayer(playerId);
            if (player == null) sendJson(exchange, 404, "{\"error\":\"Player not found.\"}");
            else sendJson(exchange, 200, Json.player(player));
            return;
        }
        if (method.equals("POST") && path.equals("/api/players")) {
            int playerId = Json.requiredInt(readBody(exchange), "playerId");
            Player player = engine.getOrCreatePlayer(playerId);
            sendJson(exchange, 201, "{\"message\":\"Player ready.\",\"player\":" + Json.player(player) + ",\"state\":" + Json.state(engine) + "}");
            return;
        }
        if (method.equals("POST") && path.equals("/api/matchmaking/online")) {
            int playerId = Json.requiredInt(readBody(exchange), "playerId");
            LobbyEngine.MatchmakingResult result = engine.joinOnlineQueue(playerId);
            sendJson(exchange, 200, "{\"message\":" + Json.quote(result.message()) + ",\"matched\":" + result.matched() + ",\"match\":" + Json.match(result.match()) + ",\"state\":" + Json.state(engine) + "}");
            return;
        }
        if (method.equals("DELETE") && path.startsWith("/api/matchmaking/queue/")) {
            int playerId = parsePathId(path, "/api/matchmaking/queue/");
            boolean removed = engine.cancelQueue(playerId);
            sendJson(exchange, 200, "{\"message\":" + Json.quote(removed ? "Queue entry cancelled." : "Player was not queued.") + ",\"state\":" + Json.state(engine) + "}");
            return;
        }
        if (method.equals("POST") && path.equals("/api/matches/friend")) {
            String body = readBody(exchange);
            int playerId = Json.requiredInt(body, "playerId");
            int friendId = Json.requiredInt(body, "friendId");
            Match match = engine.createFriendMatch(playerId, friendId);
            sendJson(exchange, 201, "{\"message\":\"Friend duel completed.\",\"match\":" + Json.match(match) + ",\"state\":" + Json.state(engine) + "}");
            return;
        }
        if (method.equals("POST") && path.equals("/api/reset-demo")) {
            engine.resetWithDemoData();
            sendJson(exchange, 200, "{\"message\":\"Demo arena reset.\",\"state\":" + Json.state(engine) + "}");
            return;
        }
        sendJson(exchange, 404, "{\"error\":\"API route not found.\"}");
    }

    private void serveStatic(HttpExchange exchange, String path) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }
        String resourcePath = path.equals("/") ? "static/index.html" : "static" + path;
        InputStream stream = AppServer.class.getClassLoader().getResourceAsStream(resourcePath);
        if (stream == null && !path.contains(".")) {
            resourcePath = "static/index.html";
            stream = AppServer.class.getClassLoader().getResourceAsStream(resourcePath);
        }
        if (stream == null) {
            byte[] body = "Not found".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(404, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
            return;
        }
        byte[] body = stream.readAllBytes();
        String extension = resourcePath.substring(resourcePath.lastIndexOf('.') + 1);
        exchange.getResponseHeaders().set("Content-Type", MIME_TYPES.getOrDefault(extension, "application/octet-stream"));
        exchange.getResponseHeaders().set("Cache-Control", extension.equals("html") ? "no-cache" : "public, max-age=3600");
        exchange.sendResponseHeaders(200, body.length);
        exchange.getResponseBody().write(body);
        exchange.close();
    }

    private String readBody(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    private int parsePathId(String path, String prefix) {
        try {
            return Integer.parseInt(path.substring(prefix.length()));
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("Invalid player ID in URL.");
        }
    }

    private void sendJson(HttpExchange exchange, int status, String json) throws IOException {
        cors(exchange);
        byte[] body = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.sendResponseHeaders(status, body.length);
        exchange.getResponseBody().write(body);
        exchange.close();
    }

    private void cors(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET,POST,DELETE,OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }
}
