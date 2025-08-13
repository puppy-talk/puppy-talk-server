package com.puppy.talk.pet;

import com.puppy.talk.support.ApiResponse;
import com.puppy.talk.pet.dto.request.PetCreateRequest;
import com.puppy.talk.pet.dto.response.PetCreateResponse;
import com.puppy.talk.user.UserIdentity;
import com.puppy.talk.dto.PetRegistrationResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/pets")
@RequiredArgsConstructor
@Tag(name = "Pet", description = "반려동물 관리 API")
public class PetController {

    private final PetRegistrationService petRegistrationService;

    @PostMapping
    @Operation(summary = "반려동물 등록", description = "새로운 가상 반려동물을 생성하고 전용 채팅방을 만듭니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "반려동물 등록 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
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