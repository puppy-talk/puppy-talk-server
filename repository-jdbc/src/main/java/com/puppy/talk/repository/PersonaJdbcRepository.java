package com.puppy.talk.repository;

import com.puppy.talk.infrastructure.PersonaRepository;
import com.puppy.talk.model.persona.Persona;
import com.puppy.talk.model.persona.PersonaIdentity;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PersonaJdbcRepository implements PersonaRepository {

    private static final RowMapper<Persona> PERSONA_ROW_MAPPER = (rs, rowNum) -> new Persona(
        PersonaIdentity.of(rs.getLong("id")),
        rs.getString("name"),
        rs.getString("description"),
        rs.getString("personality_traits"),
        rs.getString("ai_prompt_template"),
        rs.getBoolean("is_active")
    );

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Optional<Persona> findByIdentity(PersonaIdentity identity) {
        String sql = "SELECT id, name, description, personality_traits, ai_prompt_template, is_active FROM personas WHERE id = ?";
        List<Persona> personas = jdbcTemplate.query(sql, PERSONA_ROW_MAPPER, identity.id());
        return personas.isEmpty() ? Optional.empty() : Optional.of(personas.get(0));
    }

    @Override
    public List<Persona> findAll() {
        String sql = "SELECT id, name, description, personality_traits, ai_prompt_template, is_active FROM personas";
        return jdbcTemplate.query(sql, PERSONA_ROW_MAPPER);
    }

    @Override
    public List<Persona> findByIsActive(boolean isActive) {
        String sql = "SELECT id, name, description, personality_traits, ai_prompt_template, is_active FROM personas WHERE is_active = ?";
        return jdbcTemplate.query(sql, PERSONA_ROW_MAPPER, isActive);
    }

    @Override
    public Persona save(Persona persona) {
        if (persona.identity().id() == null) {
            return insert(persona);
        } else {
            return update(persona);
        }
    }

    @Override
    public void deleteByIdentity(PersonaIdentity identity) {
        String sql = "DELETE FROM personas WHERE id = ?";
        jdbcTemplate.update(sql, identity.id());
    }

    private Persona insert(Persona persona) {
        String sql = "INSERT INTO personas (name, description, personality_traits, ai_prompt_template, is_active) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, persona.name());
            ps.setString(2, persona.description());
            ps.setString(3, persona.personalityTraits());
            ps.setString(4, persona.aiPromptTemplate());
            ps.setBoolean(5, persona.isActive());
            return ps;
        }, keyHolder);

        Long generatedId = keyHolder.getKey().longValue();
        return new Persona(
            PersonaIdentity.of(generatedId),
            persona.name(),
            persona.description(),
            persona.personalityTraits(),
            persona.aiPromptTemplate(),
            persona.isActive()
        );
    }

    private Persona update(Persona persona) {
        String sql = "UPDATE personas SET name = ?, description = ?, personality_traits = ?, ai_prompt_template = ?, is_active = ? WHERE id = ?";
        jdbcTemplate.update(sql, persona.name(), persona.description(),
            persona.personalityTraits(), persona.aiPromptTemplate(), persona.isActive(),
            persona.identity().id());
        return persona;
    }
}