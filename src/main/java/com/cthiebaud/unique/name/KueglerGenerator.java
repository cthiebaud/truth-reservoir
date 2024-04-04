package com.cthiebaud.unique.name;

import com.github.kkuegler.HumanReadableIdGenerator;
import com.github.kkuegler.PermutationBasedHumanReadableIdGenerator;

public enum KueglerGenerator implements IGenerator {
    INSTANCE; // Singleton instance

    private final HumanReadableIdGenerator idGen;

    // Private constructor to initialize the id generator
    private KueglerGenerator() {
        idGen = new PermutationBasedHumanReadableIdGenerator();
    }

    // Method to generate session ID
    public String generateSessionId() {
        return idGen.generate();
    }

    public static void main(String[] args) {
        // Access the singleton instance and generate session ID
        String sessionId = KueglerGenerator.INSTANCE.generateSessionId();
        System.out.println(sessionId);
    }
}
