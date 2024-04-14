package com.cthiebaud.unique.name;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public interface IGenerator {
    NameOnSteroids getNameOnSteroids(String name);

    String generateSessionId(String name);

    // The static keyword is redundant when used with enum declarations.
    /* static */ enum GeneratorType {
        MYTHOS,
    }

    static IGenerator get(GeneratorType type) {
        switch (type) {
            case MYTHOS:
                return MythosGenerator.INSTANCE;
            default:
                throw new IllegalArgumentException("Invalid generator type: " + type);
        }
    }

    // Method to load a dictionary from a file
    static List<String> loadDictionary(String filename) {
        // Create a list to store dictionary words
        List<String> dictionary = new ArrayList<>();

        // Open the input stream to read from the dictionary file
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(IGenerator.class.getResourceAsStream(filename)))) {
            String line;
            // Read each line from the file until the end
            while ((line = br.readLine()) != null) {
                // Check if the line contains only letters (allowing accented characters)
                if (line.matches("\\p{L}+")) {
                    // If the line contains only letters, add it to the dictionary
                    dictionary.add(line);
                } else {
                    // If the line contains non-letter characters, print a rejection message
                    System.out.println("rejected " + line);
                }
            }
        } catch (IOException e) {
            // Handle IOException if an error occurs while reading the file
            e.printStackTrace();
        }

        // Return the loaded dictionary
        return dictionary;
    }

    static String getRandomElement(List<String> list) {
        Random random = new Random();
        return list.get(random.nextInt(list.size()));
    }

}
