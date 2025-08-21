package com.puppy.talk.domain.pet;

import com.puppy.talk.global.support.ApiResponse;
import com.puppy.talk.domain.pet.dto.request.PetCreateRequest;
import com.puppy.talk.domain.pet.dto.response.PetCreateResponse;
import com.puppy.talk.domain.pet.dto.response.PetResponse;
import com.puppy.talk.pet.dto.PetRegistrationResult;
import com.puppy.talk.pet.dto.PetCreateCommand;
import com.puppy.talk.pet.PetFacade;
import com.puppy.talk.pet.Pet;
import com.puppy.talk.pet.PetIdentity;
import com.puppy.talk.user.UserIdentity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/pets")
@RequiredArgsConstructor
@Tag(name = "Pet", description = "반려동물 관리 API")
public class PetController {

    private final PetFacade petFacade;

    @PostMapping
    @Operation(summary = "반려동물 등록", description = "새로운 가상 반려동물을 생성하고 전용 채팅방을 만듭니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "반려동물 등록 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ApiResponse<PetCreateResponse> createPet(@Valid @RequestBody PetCreateRequest request) {
        log.info("Creating pet: name={}, breed={}, userId={}", 
            request.name(), request.breed(), request.userId());
        
        PetCreateCommand command = PetCreateCommand.of(
            request.userId(),
            request.personaId(),
            request.name(),
            request.breed(),
            request.age(),
            request.profileImageUrl()
        );
        
        PetRegistrationResult result = petFacade.createPet(command);

        PetCreateResponse response = PetCreateResponse.of(
            result.pet(),
            result.chatRoom().identity().id()
        );
        
        log.info("Pet created successfully: petId={}, chatRoomId={}", 
            result.pet().identity().id(), result.chatRoom().identity().id());

        return ApiResponse.ok(response, "Pet registered successfully");
    }

    @GetMapping("/{petId}")
    @Operation(summary = "반려동물 조회", description = "특정 반려동물의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "반려동물을 찾을 수 없음")
    })
    public ApiResponse<PetResponse> getPet(@PathVariable String petId) {
        log.info("Getting pet: petId={}", petId);
        
        Pet pet = petFacade.findPet(new PetIdentity(Long.parseLong(petId)));
        PetResponse response = PetResponse.from(pet);
        
        log.info("Pet retrieved successfully: petId={}, name={}", petId, pet.name());
        return ApiResponse.ok(response, "Pet retrieved successfully");
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "사용자 반려동물 목록 조회", description = "특정 사용자가 소유한 모든 반려동물을 조회합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    public ApiResponse<List<PetResponse>> getUserPets(@PathVariable String userId) {
        log.info("Getting pets for user: userId={}", userId);
        
        List<Pet> pets = petFacade.findUserPets(new UserIdentity(Long.parseLong(userId)));
        List<PetResponse> responses = pets.stream()
            .map(PetResponse::from)
            .collect(Collectors.toList());
        
        log.info("User pets retrieved successfully: userId={}, count={}", userId, pets.size());
        return ApiResponse.ok(responses, "User pets retrieved successfully");
    }

    @GetMapping
    @Operation(summary = "전체 반려동물 목록 조회", description = "시스템에 등록된 모든 반려동물을 조회합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    public ApiResponse<List<PetResponse>> getAllPets() {
        log.info("Getting all pets");
        
        List<Pet> pets = petFacade.findAllPets();
        List<PetResponse> responses = pets.stream()
            .map(PetResponse::from)
            .collect(Collectors.toList());
        
        log.info("All pets retrieved successfully: count={}", pets.size());
        return ApiResponse.ok(responses, "All pets retrieved successfully");
    }

    @DeleteMapping("/{petId}")
    @Operation(summary = "반려동물 삭제", description = "특정 반려동물을 삭제합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "반려동물을 찾을 수 없음")
    })
    public ApiResponse<Void> deletePet(@PathVariable String petId) {
        log.info("Deleting pet: petId={}", petId);
        
        petFacade.deletePet(new PetIdentity(Long.parseLong(petId)));
        
        log.info("Pet deleted successfully: petId={}", petId);
        return ApiResponse.ok(null, "Pet deleted successfully");
    }

    @GetMapping("/user/{userId}/with-personas")
    @Operation(summary = "사용자 반려동물과 페르소나 정보 조회", description = "사용자의 반려동물과 해당 페르소나 정보를 함께 조회합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    public ApiResponse<List<PetWithPersonaResponse>> getUserPetsWithPersonas(@PathVariable String userId) {
        log.info("Getting pets with personas for user: userId={}", userId);
        
        List<PetFacade.PetWithPersonaInfo> petsWithPersonas = petFacade.findUserPetsWithPersonas(new UserIdentity(Long.parseLong(userId)));
        List<PetWithPersonaResponse> responses = petsWithPersonas.stream()
            .map(info -> new PetWithPersonaResponse(
                PetResponse.from(info.pet()),
                PersonaResponse.from(info.persona())
            ))
            .collect(Collectors.toList());
        
        log.info("Pets with personas retrieved successfully: userId={}, count={}", userId, petsWithPersonas.size());
        return ApiResponse.ok(responses, "Pets with personas retrieved successfully");
    }

    /**
     * 펫과 페르소나 정보를 포함한 응답 DTO
     */
    public record PetWithPersonaResponse(
        PetResponse pet,
        PersonaResponse persona
    ) {}

    /**
     * 페르소나 응답 DTO
     */
    public record PersonaResponse(
        Long id,
        String name,
        String description,
        String imageUrl
    ) {
        public static PersonaResponse from(com.puppy.talk.pet.Persona persona) {
            return new PersonaResponse(
                persona.identity().id(),
                persona.name(),
                persona.description(),
                null // imageUrl not available in current Persona model
            );
        }
    }
}