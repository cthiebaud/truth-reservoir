package com.cthiebaud.aletheia;

import io.vertx.core.json.JsonObject;

public class Score {
    private String sessionId;
    private String pseudo;
    private String level;
    private long elapsed;
    private int erred;
    private int unconcealed;
    private String symbol;
    private boolean scrambled;
    private String when;
    private boolean victory;

    public Score() {
    }

    public Score(String sessionId, String pseudo, String level, long elapsed, int erred, int unconcealed, String symbol,
            boolean scrambled,
            String when,
            boolean victory) {
        this.sessionId = sessionId;
        this.pseudo = pseudo;
        this.level = level;
        this.elapsed = elapsed;
        this.erred = erred;
        this.unconcealed = unconcealed;
        this.symbol = symbol;
        this.scrambled = scrambled;
        this.when = when;
        this.victory = victory;
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

    public int getUnconcealed() {
        return unconcealed;
    }

    public void setUnconcealed(int unconcealed) {
        this.unconcealed = unconcealed;
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
        return (this.erred == 0 && this.unconcealed == 32) && this.victory;
    }

    public void setVictory(boolean victory) {
        this.victory = victory;
    }

    public JsonObject toJson() {
        return new JsonObject()
                .put("sessionId", sessionId)
                .put("pseudo", pseudo)
                .put("level", level)
                .put("elapsed", elapsed)
                .put("erred", erred)
                .put("unconcealed", unconcealed)
                .put("symbol", symbol)
                .put("scrambled", scrambled)
                .put("when", when)
                .put("victory", victory);
    }

    public static Score fromJson(JsonObject json) {
        String sessionId = json.getString("sessionId");
        String pseudo = json.getString("pseudo");
        String level = json.getString("level");
        long elapsed = json.getLong("elapsed");
        int erred = json.getInteger("erred");
        int unconcealed = json.getInteger("unconcealed");
        String symbol = json.getString("symbol");
        boolean scrambled = json.getBoolean("scrambled");
        String when = json.getString("when");
        boolean victory = json.getBoolean("victory");
        return new Score(sessionId, pseudo, level, elapsed, erred, unconcealed, symbol, scrambled, when, victory);
    }
}
