package com.puppy.talk.repository;

import com.puppy.talk.infrastructure.PetRepository;
import com.puppy.talk.model.Pet;
import com.puppy.talk.model.PetIdentity;
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

    private static final RowMapper<Pet> PET_ROW_MAPPER = (rs, rowNum) -> Pet.builder()
        .identity(PetIdentity.of(rs.getLong("id")))
        .name(rs.getString("name"))
        .breed(rs.getString("breed"))
        .age(rs.getInt("age"))
        .build();
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Optional<Pet> findByIdentity(PetIdentity identity) {
        String sql = "SELECT id, name, breed, age FROM pets WHERE id = ?";
        List<Pet> pets = jdbcTemplate.query(sql, PET_ROW_MAPPER, identity.getId());
        return pets.isEmpty() ? Optional.empty() : Optional.of(pets.get(0));
    }

    @Override
    public List<Pet> findAll() {
        String sql = "SELECT id, name, breed, age FROM pets";
        return jdbcTemplate.query(sql, PET_ROW_MAPPER);
    }

    @Override
    public Pet save(Pet pet) {
        if (pet.getIdentity().getId() == null) {
            return insert(pet);
        } else {
            return update(pet);
        }
    }

    @Override
    public void deleteByIdentity(PetIdentity identity) {
        String sql = "DELETE FROM pets WHERE id = ?";
        jdbcTemplate.update(sql, identity.getId());
    }

    private Pet insert(Pet pet) {
        String sql = "INSERT INTO pets (name, breed, age) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, pet.getName());
            ps.setString(2, pet.getBreed());
            ps.setInt(3, pet.getAge());
            return ps;
        }, keyHolder);

        Long generatedId = keyHolder.getKey().longValue();
        return Pet.builder()
            .identity(PetIdentity.of(generatedId))
            .name(pet.getName())
            .breed(pet.getBreed())
            .age(pet.getAge())
            .build();
    }

    private Pet update(Pet pet) {
        String sql = "UPDATE pets SET name = ?, breed = ?, age = ? WHERE id = ?";
        jdbcTemplate.update(sql, pet.getName(), pet.getBreed(), pet.getAge(),
            pet.getIdentity().getId());
        return pet;
    }
}