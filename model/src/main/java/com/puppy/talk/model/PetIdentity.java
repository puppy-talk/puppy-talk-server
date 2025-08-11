package com.puppy.talk.model;

import java.util.Objects;

public class PetIdentity {

    private final Long id;

    public PetIdentity(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Id cannot be null");
        }
        this.id = id;
    }

    public static PetIdentity of(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Id cannot be null");
        }
        return new PetIdentity(id);
    }

    public Long getId() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        PetIdentity that = (PetIdentity) obj;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "PetIdentity{id=" + id + '}';
    }
}