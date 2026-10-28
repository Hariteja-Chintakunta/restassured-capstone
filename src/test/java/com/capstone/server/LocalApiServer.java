package com.capstone.server;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public final class LocalApiServer {

    private static final Gson GSON = new Gson();
    private static final Type JSON_OBJECT = new TypeToken<Map<String, Object>>() { }.getType();
    private static final Object STATE_LOCK = new Object();
    private static final Map<Integer, Map<String, Object>> USERS = new LinkedHashMap<>();
    private static final Map<Integer, Map<String, Object>> POSTS = new LinkedHashMap<>();
    private static final Map<Integer, List<Map<String, Object>>> COMMENTS = new LinkedHashMap<>();
    private static final AtomicInteger NEXT_USER_ID = new AtomicInteger(1000);
    private static final AtomicInteger NEXT_POST_ID = new AtomicInteger(2000);
    private static HttpServer server;
    private static ExecutorService executor;

    private LocalApiServer() {
    }

    public static synchronized void start() {
        if (server != null) {
            return;
        }
        resetData();
        try {
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            executor = Executors.newCachedThreadPool(task -> {
                Thread thread = new Thread(task, "local-fake-api");
                thread.setDaemon(true);
                return thread;
            });
            server.setExecutor(executor);
            server.createContext("/", LocalApiServer::handle);
            server.start();
        } catch (IOException e) {
            server = null;
            if (executor != null) {
                executor.shutdownNow();
                executor = null;
            }
            throw new IllegalStateException("Could not start the local fake API server", e);
        }
    }

    public static synchronized void stop() {
        if (server == null) {
            return;
        }
        server.stop(0);
        server = null;
        executor.shutdownNow();
        executor = null;
    }

    public static synchronized String baseUri() {
        if (server == null) {
            throw new IllegalStateException("The local fake API server has not been started");
        }
        return "http://127.0.0.1:" + server.getAddress().getPort();
    }

    private static void resetData() {
        synchronized (STATE_LOCK) {
            USERS.clear();
            POSTS.clear();
            COMMENTS.clear();
            NEXT_USER_ID.set(1000);
            NEXT_POST_ID.set(2000);

            USERS.put(1, user(1, "Local QA User", "local.user@example.test", "male", "active"));
            POSTS.put(1, post(1, 1, "Local test post", "Seeded post for API tests"));
            COMMENTS.put(1, List.of(comment(1, 1, "Local test comment", "commenter@example.test")));
        }
    }

    private static void handle(HttpExchange exchange) throws IOException {
        synchronized (STATE_LOCK) {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            try {
                if (path.equals("/health") && method.equals("GET")) {
                    send(exchange, 200, Map.of("status", "UP"));
                } else if (isUsersPath(path)) {
                    handleUsers(exchange, method, userPath(path));
                } else if (isPostsPath(path)) {
                    handlePosts(exchange, method, postPath(path));
                } else if (path.startsWith("/products")) {
                    handleProducts(exchange, method, path);
                } else {
                    notFound(exchange);
                }
            } catch (JsonParseException | IllegalArgumentException e) {
                send(exchange, 400, Map.of("message", "Invalid request payload"));
            } catch (RuntimeException e) {
                System.err.println("Local API server error: " + e.getMessage());
                send(exchange, 500, Map.of("message", "Local API server error"));
            }
        }
    }

    private static void handleUsers(HttpExchange exchange, String method, String path) throws IOException {
        if (path.isEmpty() && method.equals("GET")) {
            send(exchange, 200, new ArrayList<>(USERS.values()));
            return;
        }
        if (path.isEmpty() && method.equals("POST")) {
            Map<String, Object> input = requestBody(exchange);
            int id = NEXT_USER_ID.incrementAndGet();
            Map<String, Object> created = user(
                    id, requiredString(input, "name"), requiredString(input, "email"),
                    optionalString(input, "gender", "male"), optionalString(input, "status", "active"));
            USERS.put(id, created);
            send(exchange, 201, created);
            return;
        }

        String[] segments = path.split("/");
        if (segments.length == 2 && segments[1].equals("posts") && method.equals("POST")) {
            int userId = parseId(segments[0]);
            if (!USERS.containsKey(userId)) {
                notFound(exchange);
                return;
            }
            Map<String, Object> input = requestBody(exchange);
            int postId = NEXT_POST_ID.incrementAndGet();
            Map<String, Object> created = post(
                    postId, userId, requiredString(input, "title"), requiredString(input, "body"));
            POSTS.put(postId, created);
            COMMENTS.put(postId, new ArrayList<>());
            send(exchange, 201, created);
            return;
        }

        if (segments.length != 1) {
            notFound(exchange);
            return;
        }
        int id = parseId(segments[0]);
        Map<String, Object> existing = USERS.get(id);
        if (existing == null) {
            notFound(exchange);
            return;
        }
        switch (method) {
            case "GET" -> send(exchange, 200, existing);
            case "PUT", "PATCH" -> {
                Map<String, Object> input = requestBody(exchange);
                existing.putAll(input);
                existing.put("id", id);
                send(exchange, 200, existing);
            }
            case "DELETE" -> sendEmpty(exchange, 204, USERS.remove(id));
            default -> methodNotAllowed(exchange);
        }
    }

    private static void handlePosts(HttpExchange exchange, String method, String path) throws IOException {
        if (path.isEmpty() && method.equals("GET")) {
            send(exchange, 200, new ArrayList<>(POSTS.values()));
            return;
        }
        String[] segments = path.split("/");
        if (segments.length == 2 && segments[1].equals("comments") && method.equals("GET")) {
            int postId = parseId(segments[0]);
            if (!POSTS.containsKey(postId)) {
                notFound(exchange);
                return;
            }
            send(exchange, 200, COMMENTS.getOrDefault(postId, List.of()));
            return;
        }
        if (segments.length != 1) {
            notFound(exchange);
            return;
        }
        int postId = parseId(segments[0]);
        Map<String, Object> existing = POSTS.get(postId);
        if (existing == null) {
            notFound(exchange);
            return;
        }
        switch (method) {
            case "GET" -> send(exchange, 200, existing);
            case "PUT", "PATCH" -> {
                Map<String, Object> input = requestBody(exchange);
                existing.putAll(input);
                existing.put("id", postId);
                send(exchange, 200, existing);
            }
            case "DELETE" -> sendEmpty(exchange, 204, POSTS.remove(postId));
            default -> methodNotAllowed(exchange);
        }
    }

    private static void handleProducts(HttpExchange exchange, String method, String path) throws IOException {
        if (!method.equals("GET")) {
            methodNotAllowed(exchange);
            return;
        }
        List<Map<String, Object>> products = products();
        if (path.equals("/products")) {
            send(exchange, 200, Map.of("products", products, "total", products.size(),
                    "skip", 0, "limit", products.size()));
            return;
        }
        if (path.equals("/products/search")) {
            String query = queryParameter(exchange, "q").toLowerCase(Locale.ROOT);
            List<Map<String, Object>> matches = products.stream()
                    .filter(product -> product.get("title").toString().toLowerCase(Locale.ROOT).contains(query))
                    .toList();
            send(exchange, 200, Map.of("products", matches, "total", matches.size(),
                    "skip", 0, "limit", matches.size()));
            return;
        }
        String productId = path.substring("/products/".length());
        if (productId.contains("/")) {
            notFound(exchange);
            return;
        }
        int id = parseId(productId);
        products.stream()
                .filter(product -> ((Number) product.get("id")).intValue() == id)
                .findFirst()
                .ifPresentOrElse(
                        product -> sendUnchecked(exchange, 200, product),
                        () -> sendUnchecked(exchange, 404, Map.of("message", "Not found")));
    }

    private static List<Map<String, Object>> products() {
        return List.of(
                Map.of("id", 1, "title", "Essence Mascara Lash Princess", "price", 9.99),
                Map.of("id", 2, "title", "Apple iPhone Charger", "price", 19.99),
                Map.of("id", 3, "title", "Universal Phone Case", "price", 12.50));
    }

    private static boolean isUsersPath(String path) {
        return path.equals("/users") || path.startsWith("/users/")
                || path.equals("/public/v2/users") || path.startsWith("/public/v2/users/");
    }

    private static String userPath(String path) {
        return removeLeadingSlash(path.startsWith("/public/v2")
                ? path.substring("/public/v2/users".length())
                : path.substring("/users".length()));
    }

    private static boolean isPostsPath(String path) {
        return path.equals("/posts") || path.startsWith("/posts/")
                || path.equals("/public/v2/posts") || path.startsWith("/public/v2/posts/");
    }

    private static String postPath(String path) {
        return removeLeadingSlash(path.startsWith("/public/v2")
                ? path.substring("/public/v2/posts".length())
                : path.substring("/posts".length()));
    }

    private static String removeLeadingSlash(String path) {
        return path.startsWith("/") ? path.substring(1) : path;
    }

    private static Map<String, Object> user(
            int id, String name, String email, String gender, String status) {
        Map<String, Object> user = new LinkedHashMap<>();
        user.put("id", id);
        user.put("name", name);
        user.put("email", email);
        user.put("gender", gender);
        user.put("status", status);
        return user;
    }

    private static Map<String, Object> post(int id, int userId, String title, String body) {
        Map<String, Object> post = new LinkedHashMap<>();
        post.put("id", id);
        post.put("user_id", userId);
        post.put("title", title);
        post.put("body", body);
        return post;
    }

    private static Map<String, Object> comment(int id, int postId, String body, String email) {
        Map<String, Object> comment = new LinkedHashMap<>();
        comment.put("id", id);
        comment.put("post_id", postId);
        comment.put("name", "Local comment");
        comment.put("email", email);
        comment.put("body", body);
        return comment;
    }

    private static Map<String, Object> requestBody(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, Object> input = GSON.fromJson(body, JSON_OBJECT);
        if (input == null) {
            throw new JsonParseException("Request body must be a JSON object");
        }
        return input;
    }

    private static String requiredString(Map<String, Object> input, String key) {
        Object value = input.get(key);
        if (!(value instanceof String text) || text.isBlank()) {
            throw new JsonParseException("Missing required field: " + key);
        }
        return text;
    }

    private static String optionalString(Map<String, Object> input, String key, String defaultValue) {
        Object value = input.get(key);
        return value instanceof String text ? text : defaultValue;
    }

    private static int parseId(String value) {
        try {
            int id = Integer.parseInt(value);
            if (id < 1) {
                throw new NumberFormatException("ID must be positive");
            }
            return id;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Resource ID must be a positive integer", e);
        }
    }

    private static String queryParameter(HttpExchange exchange, String name) {
        String query = exchange.getRequestURI().getRawQuery();
        if (query == null) {
            return "";
        }
        for (String part : query.split("&")) {
            String[] pair = part.split("=", 2);
            if (URLDecoder.decode(pair[0], StandardCharsets.UTF_8).equals(name)) {
                return pair.length == 2 ? URLDecoder.decode(pair[1], StandardCharsets.UTF_8) : "";
            }
        }
        return "";
    }

    private static void send(HttpExchange exchange, int status, Object body) throws IOException {
        byte[] response = GSON.toJson(body).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, response.length);
        try (var output = exchange.getResponseBody()) {
            output.write(response);
        }
    }

    private static void sendUnchecked(HttpExchange exchange, int status, Object body) {
        try {
            send(exchange, status, body);
        } catch (IOException e) {
            throw new IllegalStateException("Could not write local API response", e);
        }
    }

    private static void sendEmpty(HttpExchange exchange, int status, Object removed) throws IOException {
        if (removed == null) {
            notFound(exchange);
            return;
        }
        exchange.sendResponseHeaders(status, -1);
        exchange.close();
    }

    private static void notFound(HttpExchange exchange) throws IOException {
        send(exchange, 404, Map.of("message", "Not found"));
    }

    private static void methodNotAllowed(HttpExchange exchange) throws IOException {
        send(exchange, 405, Map.of("message", "Method not allowed"));
    }
}
