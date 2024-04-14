package com.cthiebaud.unique.name;

public class NameOnSteroids {
    private String name;
    private String didascalia;
    private String description;

    // Default constructor
    public NameOnSteroids() {
    }

    // Parameterized constructor
    public NameOnSteroids(String name, String didascalia, String description) {
        this();
        this.name = name.trim();
        this.didascalia = didascalia.trim();
        this.description = description.trim();
    }

    // Getters and setters for all attributes
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDidascalia() {
        return didascalia;
    }

    public void setDidascalia(String didascalia) {
        this.didascalia = didascalia;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // toString method for printing object details
    @Override
    public String toString() {
        return "NameOnSteroids{" +
                "name='" + name + '\'' +
                ", didascalia='" + didascalia + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
