package com.cthiebaud.unique.name;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public enum GreekGenerator implements IGenerator {

    INSTANCE;

    private List<String> adjectives;
    private List<String> greekNames;

    GreekGenerator() {
        adjectives = loadDictionary("adjectives.txt");
        greekNames = loadDictionary("greeks.txt");
    }

    private List<String> loadDictionary(String filename) {
        List<String> dictionary = new ArrayList<>();
        try (InputStream inputStream = getClass().getResourceAsStream(filename);
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.matches("\\p{L}+")) { // Allow accented characters
                    dictionary.add(line);
                } else {
                    System.out.println("rejected " + line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dictionary;
    }

    public String generateSessionId() {
        Random random = new Random();
        String adjective = getRandomElement(adjectives);
        String greekName = getRandomElement(greekNames);
        String numberString = String.format("%02d", random.nextInt(100)); // Generate number string from 00 to 99
        String uniqueName = adjective + "-" + greekName + "-" + numberString;
        return uniqueName.toLowerCase();
    }

    private String getRandomElement(List<String> list) {
        Random random = new Random();
        return list.get(random.nextInt(list.size()));
    }

    public static void main(String[] args) {
        System.out.println(GreekGenerator.INSTANCE.generateSessionId());
    }
}
