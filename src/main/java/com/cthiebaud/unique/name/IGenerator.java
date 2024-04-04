package com.cthiebaud.unique.name;

public interface IGenerator {
    NameOnSteroids getNameOnSteroids();

    String generateSessionId(String name);

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
}
