package com.puppy.talk.controller;

import com.puppy.talk.model.pet.PersonaIdentity;
import com.puppy.talk.model.user.UserIdentity;
import com.puppy.talk.service.PetRegistrationService;
import com.puppy.talk.service.dto.PetRegistrationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;

@RestController
@RequestMapping("/api/pets")
@RequiredArgsConstructor
public class PetController {

    private final PetRegistrationService petRegistrationService;

    @PostMapping
    public ResponseEntity<ApiResponse<PetCreateResponse>> createPet(@Valid @RequestBody PetCreateRequest request) {
        PetRegistrationResult result = petRegistrationService.registerPet(
            UserIdentity.of(request.userId()),
            PersonaIdentity.of(request.personaId()),
            request.name(),
            request.breed(),
            request.age(),
            request.profileImageUrl()
        );

        PetCreateResponse response = PetCreateResponse.of(
            result.pet(),
            result.chatRoom().identity().id()
        );

        URI location = URI.create(String.format("/api/pets/%d", result.pet().identity().id()));
        return ResponseEntity
            .created(location)
            .body(ApiResponse.ok(response, "Pet registered successfully"));
    }
}