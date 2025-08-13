package com.puppy.talk.pet;

import com.puppy.talk.support.ApiResponse;
import com.puppy.talk.pet.dto.request.PetCreateRequest;
import com.puppy.talk.pet.dto.response.PetCreateResponse;
import com.puppy.talk.user.UserIdentity;
import com.puppy.talk.dto.PetRegistrationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/pets")
@RequiredArgsConstructor
public class PetController {

    private final PetRegistrationService petRegistrationService;

    @PostMapping
    public ApiResponse<PetCreateResponse> createPet(@Valid @RequestBody PetCreateRequest request) {
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