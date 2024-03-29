package com.cthiebaud.aletheia;

import io.vertx.core.json.JsonObject;

public class Score {
    private String pseudo;
    private String level;
    private long elapsed;
    private int erred;
    private int revealed;
    private String symbol;
    private boolean scrambled;

    public Score(String pseudo, String level, long elapsed, int erred, int revealed, String symbol, boolean scrambled) {
        this.pseudo = pseudo;
        this.level = level;
        this.elapsed = elapsed;
        this.erred = erred;
        this.revealed = revealed;
        this.symbol = symbol;
        this.scrambled = scrambled;
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
        String pseudo = json.getString("pseudo");
        String level = json.getString("level");
        long elapsed = json.getLong("elapsed");
        int erred = json.getInteger("erred");
        int revealed = json.getInteger("revealed");
        String symbol = json.getString("symbol");
        boolean scrambled = json.getBoolean("scrambled");
        return new Score(pseudo, level, elapsed, erred, revealed, symbol, scrambled);
    }
}
