package com.cthiebaud.unique.name;

import java.util.List;
import java.util.Random;

public interface IGenerator {
    NameOnSteroids getNameOnSteroids();

    String generateSessionId(String name);

    // The static keyword is redundant when used with enum declarations.
    /* static */ enum GeneratorType {
        GREEK,
        KUEGLER,
        MYTHOS,
    }

    static IGenerator get(GeneratorType type) {
        switch (type) {
            case GREEK:
                return GreekGenerator.INSTANCE;
            case KUEGLER:
                return KueglerGenerator.INSTANCE;
            case MYTHOS:
                return MythosGenerator.INSTANCE;
            default:
                throw new IllegalArgumentException("Invalid generator type: " + type);
        }
    }

    static String getRandomElement(List<String> list) {
        Random random = new Random();
        return list.get(random.nextInt(list.size()));
    }

}
