package com.cthiebaud.unique.name;

import static com.cthiebaud.mythos.model.Model.MODEL;
import com.cthiebaud.mythos.model.Model.Actor;

import java.util.List;
import java.util.Random;

public enum MythosGenerator implements IGenerator {
    INSTANCE;

    private List<String> adjectives;

    MythosGenerator() {
        adjectives = IGenerator.loadDictionary("adjectives.txt");
    }

    @Override
    public NameOnSteroids getNameOnSteroids(String name) {
        Actor a = null;
        if (name != null) {
            a = MODEL.findActorByName(name).orElse(null);
        }
        if (a == null) {
            a = MODEL.getRandomActor();
        }
        return new NameOnSteroids(a.getName(), a.getDidascalia(), a.getHtmlDescription());
    }

    @Override
    public String generateSessionId(String name) {
        String adjective = IGenerator.getRandomElement(adjectives);
        // Generate number string from 00 to 99
        Random random = new Random();
        String numberString = String.format("%02d", random.nextInt(100));
        return (adjective + "-" + name + "-" + numberString).toLowerCase();
    }

    /*
     * mvn exec:java -Dexec.mainClass="com.cthiebaud.unique.name.MythosGenerator"
     * -Dexec.args="Athena" -q
     */
    public static void main(String[] args) {
        System.out.println("---");
        IGenerator generator = IGenerator.get(IGenerator.GeneratorType.MYTHOS);
        NameOnSteroids nos = generator.getNameOnSteroids(args.length > 0 ? args[0] : null);
        System.out.println(nos);
        System.out.println(generator.generateSessionId(nos.getName()));
        System.out.println("---");
    }
}
