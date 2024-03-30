package com.cthiebaud.aletheia;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.CorsHandler;

public class MainVerticle extends AbstractVerticle {

    private Map<String, List<Score>> scores = new HashMap<>();
    private DatabaseReference scoresRef;

    @Override
    public void start(Promise<Void> startPromise) {
        // initializeScores();
        try {
            initializeWithDefaultCredentials();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Router router = Router.router(vertx);

        // Configure CORS
        CorsHandler corsHandler = CorsHandler.create()
                .addOrigin("*") // Allow requests from any origin
                .allowedMethod(HttpMethod.GET) // Allow GET requests
                .allowedMethod(HttpMethod.POST) // Allow POST requests
                .allowedMethod(HttpMethod.DELETE) // Allow DELETE requests
                .allowedHeader("*"); // Allow all headers

        router.route().handler(corsHandler);
        router.route().failureHandler(this::handleFailure);
        router.get("/best").handler(this::handleGetBest);
        router.get("/").handler(this::handleGetByPseudo);
        router.post("/").handler(this::handlePost);
        router.delete("/").handler(this::handleDelete);

        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8080, http -> {
                    if (http.succeeded()) {
                        System.out.println("Server started on port 8080");
                        startPromise.complete();
                    } else {
                        startPromise.fail(http.cause());
                    }
                });
    }

    public void initializeWithDefaultCredentials() throws IOException {
        // Initialize Firebase with default credentials
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.getApplicationDefault())
                .setProjectId("aletheia-8c78f")
                .setDatabaseUrl("https://aletheia-8c78f-default-rtdb.europe-west1.firebasedatabase.app")
                .build();

        FirebaseApp.initializeApp(options);

        // Get reference to the "scores" node in Firebase
        this.scoresRef = FirebaseDatabase.getInstance().getReference("scores");
    }

    /*
     * private void initializeScores() {
     * String pseudo = "christophet60";
     * String level = "achilles";
     * long elapsed = 29999;
     * int erred = 0;
     * int revealed = 32;
     * String symbol = "canonical";
     * boolean scrambled = true;
     * // Get the current date and time
     * LocalDateTime now = LocalDateTime.now();
     * 
     * // Create a formatter for ISO 8601 format
     * DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
     * 
     * // Format the date and time using the formatter
     * String when = now.format(formatter);
     * 
     * Score initialScore = new Score(pseudo, level, elapsed, erred, revealed,
     * symbol, scrambled, when);
     * 
     * List<Score> initialScores = new ArrayList<>();
     * initialScores.add(initialScore);
     * 
     * // scores.put(pseudo, initialScores);
     * }
     */

    private void handleFailure(RoutingContext routingContext) {
        // Log the error
        Throwable failure = routingContext.failure();
        failure.printStackTrace();
        // Return detailed error response
        routingContext.response()
                .setStatusCode(500)
                .putHeader("content-type", "application/json")
                .end(Json.encode(new JsonObject().put("error", failure.getMessage())));
    }

    private void sendJsonErrorResponse(HttpServerResponse response, int statusCode, String errorMessage) {
        JsonObject errorJson = new JsonObject().put("error", errorMessage);
        response.setStatusCode(statusCode)
                .putHeader("content-type", "application/json")
                .end(Json.encode(errorJson));
    }

    private void handleRequest(RoutingContext routingContext, Handler<RoutingContext> handler) {
        HttpServerRequest request = routingContext.request();

        try {
            handler.handle(routingContext);
        } catch (Exception e) {
            // Log the error
            // e.printStackTrace();
            // Return detailed error response
            request.response()
                    .setStatusCode(500)
                    .putHeader("content-type", "application/json")
                    .end(Json.encode(new JsonObject().put("error", e.getMessage())));
        }
    }

    // private void handlePost(RoutingContext routingContext) {
    // handleRequest(routingContext, ctx -> {
    // HttpServerRequest request = ctx.request();
    // request.bodyHandler(buffer -> {
    // JsonObject json = buffer.toJsonObject();
    // // String pseudo = json.getString("pseudo");
    // String pseudo = request.remoteAddress().host();
    // Score newScore = Score.fromJson(json);
    // newScore.setPseudo(pseudo);
    // List<Score> pseudoScores = scores.computeIfAbsent(pseudo, k -> new
    // ArrayList<>());
    // pseudoScores.add(newScore);
    // request.response()
    // .putHeader("content-type", "application/json")
    // .end(Json.encode(newScore));
    // });
    // });
    // }

    private void handlePost(RoutingContext routingContext) {
        handleRequest(routingContext, ctx -> {
            HttpServerRequest request = ctx.request();
            request.bodyHandler(buffer -> {
                JsonObject json = buffer.toJsonObject();
                Score newScore = Score.fromJson(json);
                if (newScore.getWhen() == null) {
                    newScore.setWhen(DateTimeFormatter.ISO_INSTANT.format(Instant.now()));
                }

                // Push new score to Firebase
                scoresRef.push().setValue(newScore, (databaseError, databaseReference) -> {
                    if (databaseError != null) {
                        sendJsonErrorResponse(request.response(), 500,
                                "Failed to store score in database: " + databaseError.getMessage());
                    } else {
                        List<Score> pseudoScores = scores.computeIfAbsent(newScore.getPseudo(), k -> new ArrayList<>());
                        pseudoScores.add(newScore);
                        request.response()
                                .putHeader("content-type", "application/json")
                                .end(Json.encode(newScore));
                    }
                });
            });
        });
    }

    private void handleDelete(RoutingContext routingContext) {
        handleRequest(routingContext, ctx -> {
            HttpServerRequest request = ctx.request();

            String pseudo = request.getParam("pseudo");
            if (pseudo != null && scores.containsKey(pseudo)) {
                scores.remove(pseudo);
                request.response()
                        .putHeader("content-type", "text/plain")
                        .end(Json.encode(new JsonObject().put("msg", "Scores for pseudo " + pseudo + " deleted")));
            } else {
                sendJsonErrorResponse(request.response(), 404, "Scores not found for pseudo " + pseudo);
            }
        });
    }

    private void handleGetByPseudo(RoutingContext routingContext) {
        handleRequest(routingContext, ctx -> {
            HttpServerRequest request = ctx.request();
            String pseudo = request.getParam("pseudo");

            List<Score> ret;
            if (pseudo == null) {
                ret = scores.values().stream()
                        .flatMap(List::stream)
                        .collect(Collectors.toList());
            } else {
                ret = scores.getOrDefault(pseudo, new ArrayList<>());
            }

            if (!ret.isEmpty()) {
                request.response()
                        .putHeader("content-type", "application/json")
                        .end(Json.encode(ret));
            } else {
                request.response()
                        .setStatusCode(204)
                        .end();
            }
        });
    }

    private void handleGetBest(RoutingContext routingContext) {
        handleRequest(routingContext, ctx -> {
            HttpServerRequest request = ctx.request();

            String pseudo = request.getParam("pseudo");
            String level = request.getParam("level");
            Boolean scrambled = Optional.ofNullable(request.getParam("scrambled"))
                    .map(Boolean::parseBoolean)
                    .orElse(null);
            String symbol = request.getParam("symbol");

            Stream<Score> qwe = scores.entrySet().stream()
                    .filter(entry -> pseudo == null || entry.getKey().equals(pseudo))
                    .flatMap(entry -> entry.getValue().stream())
                    .filter(score -> (level == null || score.getLevel().equals(level)) &&
                            (symbol == null || score.getSymbol().equals(symbol)) &&
                            (scrambled == null || score.isScrambled() == scrambled) &&
                            (score.getErred() == 0) &&
                            (score.getRevealed() == 32));

            Optional<Score> bestScore = qwe.min(Comparator.comparingLong(Score::getElapsed));

            if (bestScore.isPresent()) {
                request.response()
                        .putHeader("content-type", "application/json")
                        .end(Json.encode(bestScore.get()));
            } else {
                request.response()
                        .setStatusCode(204)
                        .end();
            }
        });
    }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new MainVerticle());
    }
}
