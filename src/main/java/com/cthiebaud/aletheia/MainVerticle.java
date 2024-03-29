package com.cthiebaud.aletheia;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MainVerticle extends AbstractVerticle {

    private Map<String, List<Score>> scores = new HashMap<>();

    @Override
    public void start(Promise<Void> startPromise) {
        initializeScores();

        Router router = Router.router(vertx);
        router.get("/best").handler(this::handleGetBest);
        router.get("/").handler(this::handleGet);
        router.post("/").handler(this::handlePost);
        router.delete("/").handler(this::handleDelete);

        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8888, http -> {
                    if (http.succeeded()) {
                        System.out.println("Server started on port 8888");
                        startPromise.complete();
                    } else {
                        startPromise.fail(http.cause());
                    }
                });
    }

    private void initializeScores() {
        String pseudo = "christophet60";
        String level = "achilles";
        long elapsed = 19000;
        int erred = 0;
        int revealed = 32;
        String symbol = "canonical";
        boolean scrambled = false;

        Score initialScore = new Score(pseudo, level, elapsed, erred, revealed, symbol, scrambled);

        List<Score> initialScores = new ArrayList<>();
        initialScores.add(initialScore);

        scores.put(pseudo, initialScores);
    }

    /*
     * 
     * private void handleRequest(RoutingContext routingContext) {
     * HttpServerRequest request = routingContext.request();
     * 
     * switch (request.method().toString()) {
     * case "GET":
     * if (request.params().contains("pseudo")) {
     * handleGetByPseudo(request);
     * } else {
     * handleGet(request);
     * }
     * break;
     * case "POST":
     * handlePost(request);
     * break;
     * case "DELETE":
     * handleDelete(request);
     * break;
     * default:
     * request.response()
     * .setStatusCode(405)
     * .end("Method Not Allowed");
     * break;
     * }
     * }
     */

    private void handleGet(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();

        request.response()
                .putHeader("content-type", "application/json")
                .end(Json.encode(scores));
    }

    private void handlePost(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();

        request.bodyHandler(buffer -> {
            JsonObject json = buffer.toJsonObject();
            String pseudo = json.getString("pseudo");
            Score newScore = Score.fromJson(json);

            List<Score> pseudoScores = scores.computeIfAbsent(pseudo, k -> new ArrayList<>());
            pseudoScores.add(newScore);

            request.response()
                    .putHeader("content-type", "application/json")
                    .end(Json.encode(newScore));
        });
    }

    private void handleDelete(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();

        String pseudo = request.getParam("pseudo");
        if (pseudo != null && scores.containsKey(pseudo)) {
            scores.remove(pseudo);
            request.response()
                    .putHeader("content-type", "text/plain")
                    .end("Scores for pseudo " + pseudo + " deleted");
        } else {
            request.response()
                    .setStatusCode(404)
                    .end("Scores not found for pseudo " + pseudo);
        }
    }

    private void handleGetByPseudo(HttpServerRequest request) {
        String pseudo = request.getParam("pseudo");
        List<Score> pseudoScores = scores.getOrDefault(pseudo, new ArrayList<>());

        if (!pseudoScores.isEmpty()) {
            request.response()
                    .putHeader("content-type", "application/json")
                    .end(Json.encode(pseudoScores));
        } else {
            request.response()
                    .setStatusCode(404)
                    .end("Scores not found for pseudo " + pseudo);
        }
    }

    private void handleGetBest(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();

        String pseudo = request.getParam("pseudo");
        String level = request.getParam("level");
        Boolean scrambled = Optional.ofNullable(request.getParam("scrambled")).map(Boolean::parseBoolean).orElse(null);
        String symbol = request.getParam("symbol");

        Optional<Score> bestScore = scores.entrySet().stream()
                .filter(entry -> pseudo == null || entry.getKey().equals(pseudo))
                .flatMap(entry -> entry.getValue().stream())
                .filter(score -> (level == null || score.getLevel().equals(level)) &&
                        (symbol == null || score.getSymbol().equals(symbol)) &&
                        (score.isScrambled() == scrambled) &&
                        (score.getRevealed() == 32))
                .min(Comparator.comparingLong(Score::getElapsed));

        if (bestScore.isPresent()) {
            request.response()
                    .putHeader("content-type", "application/json")
                    .end(Json.encode(bestScore.get()));
        } else {
            request.response()
                    .setStatusCode(404)
                    .end("Best score not found with the given criteria");
        }
    }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new MainVerticle());
    }
}
