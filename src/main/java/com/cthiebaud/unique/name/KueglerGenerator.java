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
    public String generateSessionId(String name) {
        return idGen.generate();
    }

    public NameOnSteroids getNameOnSteroids() {
        return NameOnSteroids.nullNameOnSteroids;
    }

    public static void main(String[] args) {
        IGenerator generator = IGenerator.get(IGenerator.GeneratorType.KUEGLER);
        NameOnSteroids nos = generator.getNameOnSteroids();
        System.out.println(nos);
        String sessionId = generator.generateSessionId(nos.getName());
        System.out.println(sessionId);
    }
}
