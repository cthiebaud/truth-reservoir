package com.cthiebaud.unique.name;

import com.cthiebaud.mythos.model.Model;
import com.cthiebaud.mythos.model.Model.Actor;

import java.util.List;
import java.util.Random;

public enum MythosGenerator implements IGenerator {
    INSTANCE;

    private List<String> adjectives;

    MythosGenerator() {
        adjectives = GreekGenerator.loadDictionary("adjectives.txt");
    }

    @Override
    public NameOnSteroids getNameOnSteroids(String name) {
        Actor a = null;
        if (name != null) {
            a = Model.INSTANCE.findActorByName(name).orElse(null);
        }
        if (a == null) {
            a = Model.INSTANCE.getRandomActor();
        }
        return new NameOnSteroids(a.getName(), a.getDidascalia(), a.getHtmlDescription());

    }

    @Override
    public String generateSessionId(String name) {
        String adjective = IGenerator.getRandomElement(adjectives);
        Random random = new Random();
        // Generate number string from 00 to 99
        String numberString = String.format("%02d", random.nextInt(100));
        return (adjective + "-" + name + "-" + numberString).toLowerCase();
    }

}
