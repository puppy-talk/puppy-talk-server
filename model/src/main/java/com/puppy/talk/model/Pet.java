package com.puppy.talk.model;

public class Pet {

    private final PetIdentity identity;
    private final String name;
    private final String breed;
    private final int age;

    public Pet(PetIdentity identity, String name, String breed, int age) {
        if (identity == null) {
            throw new IllegalArgumentException("Identity cannot be null");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        if (breed == null || breed.trim().isEmpty()) {
            throw new IllegalArgumentException("Breed cannot be null or empty");
        }
        if (age < 0) {
            throw new IllegalArgumentException("Age cannot be negative");
        }

        this.identity = identity;
        this.name = name;
        this.breed = breed;
        this.age = age;
    }

    public static Builder builder() {
        return new Builder();
    }

    public PetIdentity getIdentity() {
        return identity;
    }

    public String getName() {
        return name;
    }

    public String getBreed() {
        return breed;
    }

    public int getAge() {
        return age;
    }

    public static class Builder {

        private PetIdentity identity;
        private String name;
        private String breed;
        private int age;

        public Builder identity(PetIdentity identity) {
            this.identity = identity;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder breed(String breed) {
            this.breed = breed;
            return this;
        }

        public Builder age(int age) {
            this.age = age;
            return this;
        }

        public Pet build() {
            if (identity == null) {
                throw new IllegalArgumentException("Identity cannot be null");
            }
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Name cannot be null or empty");
            }
            if (breed == null || breed.trim().isEmpty()) {
                throw new IllegalArgumentException("Breed cannot be null or empty");
            }
            if (age < 0) {
                throw new IllegalArgumentException("Age cannot be negative");
            }

            return new Pet(identity, name, breed, age);
        }
    }
}