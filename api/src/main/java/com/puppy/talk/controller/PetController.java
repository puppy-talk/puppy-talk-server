package com.puppy.talk.controller;

import com.puppy.talk.model.pet.PersonaIdentity;
import com.puppy.talk.model.user.UserIdentity;
import com.puppy.talk.service.PetRegistrationService;
import com.puppy.talk.service.dto.PetRegistrationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pets")
@RequiredArgsConstructor
public class PetController {

    private final PetRegistrationService petRegistrationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PetCreateResponse> createPet(@RequestBody PetCreateRequest request) {
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

        return ApiResponse.ok(response, "Pet registered successfully");
    }
}