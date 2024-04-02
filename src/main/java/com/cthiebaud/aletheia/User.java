package com.cthiebaud.aletheia;

import io.vertx.core.json.JsonObject;

public class User {
    private String sessionId;
    private String pseudo;
    private String when;

    public User() {
    }

    public User(String sessionId, String pseudo, String when) {
        this.sessionId = sessionId;
        this.pseudo = pseudo;
        this.when = when;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getPseudo() {
        return pseudo;
    }

    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }

    public String getWhen() {
        return when;
    }

    public void setWhen(String when) {
        this.when = when;
    }

    public JsonObject toJson() {
        return new JsonObject()
                .put("sessionId", sessionId)
                .put("pseudo", pseudo)
                .put("when", when);
    }

    public static User fromJson(JsonObject json) {
        String sessionId = json.getString("sessionId");
        String pseudo = json.getString("pseudo");
        String when = json.getString("when");
        return new User(sessionId, pseudo, when);
    }
}
