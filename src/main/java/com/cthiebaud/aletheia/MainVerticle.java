package com.cthiebaud.aletheia;

import static com.cthiebaud.unique.name.IGenerator.GeneratorType.*;

import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.cthiebaud.unique.name.IGenerator;
import com.cthiebaud.unique.name.NameOnSteroids;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

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

    private DatabaseReference scoresRef;
    private DatabaseReference usersRef;

    private Map<String, Boolean> userExistenceCache = new HashMap<>();

    private void findAvailableUserSession(RoutingContext ctx) {
        HttpServerRequest request = ctx.request();
        String name = request.getParam("name");
        NameOnSteroids nos = IGenerator.get(MYTHOS).getNameOnSteroids(name);
        String sessionId = IGenerator.get(MYTHOS).generateSessionId(nos.getName());
        usersRef.orderByChild("sessionId").equalTo(sessionId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        boolean exists = dataSnapshot.exists();
                        if (exists) {
                            // If session ID exists, try again recursively
                            findAvailableUserSession(ctx);
                        } else {
                            // If session ID doesn't exist, return it as JSON
                            try {
                                JsonObject sessionIdJson = new JsonObject()
                                        .put("sessionId", sessionId)
                                        .put("name", nos.getName())
                                        .put("didascalia", nos.getDidascalia())
                                        .put("description", nos.getDescription());
                                ctx.response()
                                        .putHeader("Content-Type", "application/json")
                                        .end(sessionIdJson.encode());
                            } catch (Exception e) {
                                e.printStackTrace();
                                // Handle exception if needed
                                ctx.fail(500); // Internal server error
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Handle onCancelled event if needed
                        ctx.fail(500); // Internal server error
                    }
                });
    }

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
                .allowedHeader("*") // Allow all headers
        ;

        router.route().handler(corsHandler);
        router.route().failureHandler(this::handleFailure);
        router.get("/user-session").handler(this::findAvailableUserSession);
        router.get("/bests").handler(this::handleGetBests);
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
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.getApplicationDefault())
                .setProjectId("aletheia-8c78f")
                .setDatabaseUrl("https://aletheia-8c78f-default-rtdb.europe-west1.firebasedatabase.app")
                .build();

        FirebaseApp.initializeApp(options);

        // Get reference to the "scores" node in Firebase
        this.scoresRef = FirebaseDatabase.getInstance().getReference("scores");
        this.usersRef = FirebaseDatabase.getInstance().getReference("users");
    }

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
            // Check if the request contains a session ID header
            String sessionId = routingContext.request().getHeader("Session-Id");

            // No need to generate or set a session ID cookie here

            // Store the sessionId in the routing context for later retrieval
            routingContext.put("sessionId", sessionId);
            handler.handle(routingContext);

        } catch (Exception e) {
            // Log the error
            e.printStackTrace();
            // Return detailed error response
            request.response()
                    .setStatusCode(500)
                    .putHeader("content-type", "application/json")
                    .end(Json.encode(new JsonObject().put("error", e.getMessage())));
        }
    }

    private void handlePost(RoutingContext routingContext) {
        handleRequest(routingContext, ctx -> {
            HttpServerRequest request = ctx.request();
            request.bodyHandler(buffer -> {
                JsonObject json = buffer.toJsonObject();
                String sessionId = routingContext.get("sessionId");
                json.put("sessionId", sessionId);

                // Check if the user existence status is cached
                if (!userExistenceCache.containsKey(sessionId)) {
                    // Check if the user already exists in Firebase
                    usersRef.orderByChild("sessionId").equalTo(sessionId)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    boolean exists = dataSnapshot.exists();

                                    if (exists) {
                                        userExistenceCache.put(sessionId, exists); // Cache the existence status
                                    } else {
                                        // User does not exist, push to Firebase
                                        User newUser = User.fromJson(json);
                                        usersRef.push().setValue(newUser, (databaseError, databaseReference) -> {
                                            if (databaseError == null) {
                                                userExistenceCache.put(sessionId, exists); // Cache the existence status
                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    // Handle onCancelled event if needed
                                }
                            });
                }

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
            String sessionId = request.getParam("sessionId");

            Query pseudoOrSessionIdQuery;
            if (sessionId != null) {
                pseudoOrSessionIdQuery = scoresRef.orderByChild("sessionId").equalTo(sessionId);
            } else if (pseudo != null) {
                pseudoOrSessionIdQuery = scoresRef.orderByChild("pseudo").equalTo(pseudo);
            } else {
                sendJsonErrorResponse(request.response(), 400,
                        "[Pseudo or sessionId] parameter is required for delete operation");
                return;
            }
            pseudoOrSessionIdQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        int totalSnapshots = (int) dataSnapshot.getChildrenCount();
                        AtomicInteger deletionCount = new AtomicInteger(0);
                        AtomicBoolean isErrorOccurred = new AtomicBoolean(false);

                        for (DataSnapshot scoreSnapshot : dataSnapshot.getChildren()) {
                            scoreSnapshot.getRef().removeValue(new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError,
                                        DatabaseReference databaseReference) {
                                    if (databaseError != null) {
                                        // Handle error
                                        System.err.println("Error deleting score: " + databaseError.getMessage());
                                        isErrorOccurred.set(true);
                                    }

                                    // Increment deletion count
                                    int count = deletionCount.incrementAndGet();

                                    // Check if all deletions are completed
                                    if (count == totalSnapshots) {
                                        if (isErrorOccurred.get()) {
                                            sendJsonErrorResponse(request.response(), 500,
                                                    "Error occurred during deletion");
                                        } else {
                                            request.response()
                                                    .putHeader("content-type", "text/plain")
                                                    .end(Json.encode(new JsonObject().put("msg",
                                                            "Scores for sessionId=='" + sessionId + "' OR pseudo=='"
                                                                    + pseudo
                                                                    + "' deleted")));
                                        }
                                    }
                                }
                            });
                        }
                    } else {
                        sendJsonErrorResponse(request.response(), 404, "Scores not found for pseudo " + pseudo);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    sendJsonErrorResponse(request.response(), 500,
                            "Failed to delete scores for pseudo " + pseudo + ": " + databaseError.getMessage());
                }
            });
        });

    }

    private void handleGetByPseudo(RoutingContext routingContext) {
        handleRequest(routingContext, ctx -> {
            HttpServerRequest request = ctx.request();
            String pseudo = request.getParam("pseudo");

            if (pseudo == null) {
                // Fetch all scores
                scoresRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<Score> scores = new ArrayList<>();
                        for (DataSnapshot scoreSnapshot : dataSnapshot.getChildren()) {
                            Score score = scoreSnapshot.getValue(Score.class);
                            scores.add(score);
                        }
                        if (!scores.isEmpty()) {
                            request.response()
                                    .putHeader("content-type", "application/json")
                                    .end(Json.encode(scores));
                        } else {
                            request.response()
                                    .setStatusCode(204)
                                    .end();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        sendJsonErrorResponse(request.response(), 500,
                                "Failed to fetch scores: " + databaseError.getMessage());
                    }
                });
            } else {
                // Fetch scores for specific pseudo
                scoresRef.orderByChild("pseudo").equalTo(pseudo)
                        .addListenerForSingleValueEvent(new ValueEventListener() {

                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                List<Score> scores = new ArrayList<>();
                                for (DataSnapshot scoreSnapshot : dataSnapshot.getChildren()) {
                                    Score score = scoreSnapshot.getValue(Score.class);
                                    scores.add(score);
                                }
                                if (!scores.isEmpty()) {
                                    request.response()
                                            .putHeader("content-type", "application/json")
                                            .end(Json.encode(scores));
                                } else {
                                    request.response()
                                            .setStatusCode(204)
                                            .end();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                sendJsonErrorResponse(request.response(), 500, "Failed to fetch scores for pseudo "
                                        + pseudo + ": " + databaseError.getMessage());
                            }

                        });
            }
        });
    }

    private void handleGetBests(RoutingContext routingContext) {
        handleRequest(routingContext, ctx -> {
            HttpServerRequest request = ctx.request();

            Query query = scoresRef.orderByChild("victory").equalTo(true);

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Map<String, Score> bestScores = new HashMap<>();

                    for (DataSnapshot scoreSnapshot : dataSnapshot.getChildren()) {
                        Score score = scoreSnapshot.getValue(Score.class);

                        String level = score.getLevel();
                        boolean scrambled = score.isScrambled();
                        String key = level + "-" + scrambled;

                        // Check if the current score is better than the previously found best score
                        if (!bestScores.containsKey(key) || score.getElapsed() < bestScores.get(key).getElapsed()) {
                            bestScores.put(key, score);
                        }
                    }

                    if (!bestScores.isEmpty()) {
                        // Convert the map of best scores to a list
                        List<Score> bestScoresList = new ArrayList<>(bestScores.values());

                        request.response()
                                .putHeader("content-type", "application/json")
                                .end(Json.encode(bestScoresList));
                    } else {
                        request.response()
                                .setStatusCode(204)
                                .end();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    sendJsonErrorResponse(request.response(), 500,
                            "Failed to fetch best scores: " + databaseError.getMessage());
                }
            });
        });
    }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new MainVerticle());
    }
}
