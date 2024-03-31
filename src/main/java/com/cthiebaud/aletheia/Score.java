package com.cthiebaud.aletheia;

import io.vertx.core.json.JsonObject;

public class Score {
    private String sessionId;
    private String pseudo;
    private String level;
    private long elapsed;
    private int erred;
    private int revealed;
    private String symbol;
    private boolean scrambled;
    private String when;

    public Score() {
    }

    public Score(String sessionId, String pseudo, String level, long elapsed, int erred, int revealed, String symbol,
            boolean scrambled,
            String when) {
        this.sessionId = sessionId;
        this.pseudo = pseudo;
        this.level = level;
        this.elapsed = elapsed;
        this.erred = erred;
        this.revealed = revealed;
        this.symbol = symbol;
        this.scrambled = scrambled;
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

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public long getElapsed() {
        return elapsed;
    }

    public void setElapsed(long elapsed) {
        this.elapsed = elapsed;
    }

    public int getErred() {
        return erred;
    }

    public void setErred(int erred) {
        this.erred = erred;
    }

    public int getRevealed() {
        return revealed;
    }

    public void setRevealed(int revealed) {
        this.revealed = revealed;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public boolean isScrambled() {
        return scrambled;
    }

    public void setScrambled(boolean scrambled) {
        this.scrambled = scrambled;
    }

    public String getWhen() {
        return when;
    }

    public void setWhen(String when) {
        this.when = when;
    }

    public Boolean getVictory() {
        return this.erred == 0 && this.revealed == 32;
    }

    public JsonObject toJson() {
        return new JsonObject()
                .put("pseudo", pseudo)
                .put("level", level)
                .put("elapsed", elapsed)
                .put("erred", erred)
                .put("revealed", revealed)
                .put("symbol", symbol)
                .put("scrambled", scrambled);
    }

    public static Score fromJson(JsonObject json) {
        String sessionId = json.getString("sessionId");
        String pseudo = json.getString("pseudo");
        String level = json.getString("level");
        long elapsed = json.getLong("elapsed");
        int erred = json.getInteger("erred");
        int revealed = json.getInteger("revealed");
        String symbol = json.getString("symbol");
        boolean scrambled = json.getBoolean("scrambled");
        String when = json.getString("when");
        return new Score(sessionId, pseudo, level, elapsed, erred, revealed, symbol, scrambled, when);
    }
}
