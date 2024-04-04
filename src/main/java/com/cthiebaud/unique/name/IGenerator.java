package com.cthiebaud.unique.name;

public interface IGenerator {
    String generateSessionId();

    // The static keyword is redundant when used with enum declarations.
    /* static */ enum GeneratorType {
        GREEK,
        KUEGLER
    }

    static IGenerator get(GeneratorType type) {
        switch (type) {
            case GREEK:
                return GreekGenerator.INSTANCE;
            case KUEGLER:
                return KueglerGenerator.INSTANCE;
            default:
                throw new IllegalArgumentException("Invalid generator type: " + type);
        }
    }

    public static void main(String[] args) {
        // Access the singleton instance and generate session ID
        String sessionId = IGenerator.get(IGenerator.GeneratorType.GREEK).generateSessionId();
        System.out.println(sessionId);
    }
}
