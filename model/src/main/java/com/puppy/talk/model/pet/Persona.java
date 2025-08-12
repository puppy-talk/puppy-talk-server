package com.puppy.talk.model.pet;

public record Persona(
    PersonaIdentity identity,
    String name,
    String description,
    String personalityTraits,
    String aiPromptTemplate,
    boolean isActive
) {

    public Persona {
        if (identity == null) {
            throw new IllegalArgumentException("Identity cannot be null");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        if (aiPromptTemplate == null || aiPromptTemplate.trim().isEmpty()) {
            throw new IllegalArgumentException("AI prompt template cannot be null or empty");
        }
    }

    public Persona(
        PersonaIdentity identity,
        String name,
        String description,
        String personalityTraits,
        String aiPromptTemplate
    ) {
        this(identity, name, description, personalityTraits, aiPromptTemplate, true);
    }
}