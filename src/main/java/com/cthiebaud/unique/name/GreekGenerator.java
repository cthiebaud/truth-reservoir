package com.cthiebaud.unique.name;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

public enum GreekGenerator implements IGenerator {

    INSTANCE;

    private List<String> adjectives;
    private List<NameOnSteroids> greekNames;

    GreekGenerator() {
        adjectives = loadDictionary("adjectives.txt");
        greekNames = loadEntities("entities.yaml");
    }

    // Method to load a dictionary from a file
    private List<String> loadDictionary(String filename) {
        List<String> dictionary = new ArrayList<>(); // Create a list to store dictionary words

        try (
                // Open the input stream to read from the dictionary file
                InputStream inputStream = getClass().getResourceAsStream(filename);
                // Create a buffered reader to efficiently read lines from the input stream
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
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

    // Method to load entities from a YAML file into a list of NameOnSteroids
    public List<NameOnSteroids> loadEntities(String filename) {
        // Create a YAML parser with a custom constructor for NameOnSteroids class
        Yaml yaml = new Yaml(new Constructor(NameOnSteroids.class, new LoaderOptions()));

        // Load the YAML file as an input stream
        InputStream inputStream = getClass().getResourceAsStream("entities.yaml");

        // Parse YAML data into a stream of objects representing YAML documents
        Stream<Object> stream = StreamSupport.stream(yaml.loadAll(inputStream).spliterator(), false);

        // Map each object to NameOnSteroids and collect into a list
        List<NameOnSteroids> entities = stream.map(NameOnSteroids.class::cast).collect(Collectors.toList());

        return entities; // Return the list of loaded entities
    }

    public String generateSessionId(String name) {
        String adjective = getRandomElement(adjectives);
        Random random = new Random();
        String numberString = String.format("%02d", random.nextInt(100)); // Generate number string from 00 to 99
        return (adjective + "-" + name + "-" + numberString).toLowerCase();
    }

    public NameOnSteroids getNameOnSteroids() {
        NameOnSteroids greekName = getRandomElement2(greekNames);
        return greekName;
    }

    private String getRandomElement(List<String> list) {
        Random random = new Random();
        return list.get(random.nextInt(list.size()));
    }

    private NameOnSteroids getRandomElement2(List<NameOnSteroids> list) {
        Random random = new Random();
        return list.get(random.nextInt(list.size()));
    }

    public static void main(String[] args) {
        System.out.println("---");
        IGenerator generator = IGenerator.get(IGenerator.GeneratorType.GREEK);
        NameOnSteroids nos = generator.getNameOnSteroids();
        System.out.println(nos);
        System.out.println(generator.generateSessionId(nos.getName()));
        System.out.println("---");
    }
}
