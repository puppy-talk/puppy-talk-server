package com.puppy.talk.repository;

import com.puppy.talk.infrastructure.pet.PetRepository;
import com.puppy.talk.model.pet.PersonaIdentity;
import com.puppy.talk.model.pet.Pet;
import com.puppy.talk.model.pet.PetIdentity;
import com.puppy.talk.model.user.UserIdentity;
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
public class PetJdbcRepository implements PetRepository {

    private static final RowMapper<Pet> PET_ROW_MAPPER = (rs, rowNum) -> new Pet(
        PetIdentity.of(rs.getLong("id")),
        UserIdentity.of(rs.getLong("user_id")),
        PersonaIdentity.of(rs.getLong("persona_id")),
        rs.getString("name"),
        rs.getString("breed"),
        rs.getInt("age"),
        rs.getString("profile_image_url")
    );
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Optional<Pet> findByIdentity(PetIdentity identity) {
        String sql = "SELECT id, user_id, persona_id, name, breed, age, profile_image_url FROM pets WHERE id = ?";
        List<Pet> pets = jdbcTemplate.query(sql, PET_ROW_MAPPER, identity.id());
        return pets.isEmpty() ? Optional.empty() : Optional.of(pets.get(0));
    }

    @Override
    public List<Pet> findAll() {
        String sql = "SELECT id, user_id, persona_id, name, breed, age, profile_image_url FROM pets";
        return jdbcTemplate.query(sql, PET_ROW_MAPPER);
    }

    @Override
    public List<Pet> findByUserId(UserIdentity userId) {
        String sql = "SELECT id, user_id, persona_id, name, breed, age, profile_image_url FROM pets WHERE user_id = ?";
        return jdbcTemplate.query(sql, PET_ROW_MAPPER, userId.id());
    }

    @Override
    public Pet save(Pet pet) {
        if (pet.identity().id() == null) {
            return insert(pet);
        } else {
            return update(pet);
        }
    }

    @Override
    public void deleteByIdentity(PetIdentity identity) {
        String sql = "DELETE FROM pets WHERE id = ?";
        jdbcTemplate.update(sql, identity.id());
    }

    private Pet insert(Pet pet) {
        String sql = "INSERT INTO pets (user_id, persona_id, name, breed, age, profile_image_url) VALUES (?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, pet.userId().id());
            ps.setLong(2, pet.personaId().id());
            ps.setString(3, pet.name());
            ps.setString(4, pet.breed());
            ps.setInt(5, pet.age());
            ps.setString(6, pet.profileImageUrl());
            return ps;
        }, keyHolder);

        Long generatedId = keyHolder.getKey().longValue();
        return new Pet(
            PetIdentity.of(generatedId),
            pet.userId(),
            pet.personaId(),
            pet.name(),
            pet.breed(),
            pet.age(),
            pet.profileImageUrl()
        );
    }

    private Pet update(Pet pet) {
        String sql = "UPDATE pets SET user_id = ?, persona_id = ?, name = ?, breed = ?, age = ?, profile_image_url = ? WHERE id = ?";
        jdbcTemplate.update(sql, pet.userId().id(), pet.personaId().id(),
            pet.name(), pet.breed(), pet.age(), pet.profileImageUrl(),
            pet.identity().id());
        return pet;
    }
}