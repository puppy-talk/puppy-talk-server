package com.puppytalk.pet;

import com.puppytalk.pet.dto.request.PetCreateCommand;
import com.puppytalk.pet.dto.request.PetCreateRequest;
import com.puppytalk.pet.dto.request.PetDeleteCommand;
import com.puppytalk.pet.dto.request.PetGetQuery;
import com.puppytalk.pet.dto.request.PetListQuery;
import com.puppytalk.pet.dto.response.PetResponse;
import com.puppytalk.pet.dto.response.PetResult;
import com.puppytalk.pet.dto.response.PetsResponse;
import com.puppytalk.pet.dto.response.PetsResult;
import com.puppytalk.support.ApiResponse;
import com.puppytalk.support.ApiSuccessMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Pet", description = "반려동물 관리 API")
@RestController
@RequestMapping("/api/pets")
public class PetController {

    private final PetFacade petFacade;

    public PetController(PetFacade petFacade) {
        this.petFacade = petFacade;
    }

    @Operation(summary = "반려동물 생성", description = "새로운 반려동물을 생성합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "반려동물 생성 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createPet(
        @Parameter(description = "반려동물 생성 요청 정보", required = true)
        @Valid @RequestBody PetCreateRequest request
    ) {
        PetCreateCommand command = PetCreateCommand.of(
            request.ownerId(),
            request.name(),
            request.personaId()
        );

        petFacade.createPet(command);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(null, ApiSuccessMessage.PET_CREATE_SUCCESS.getMessage()));
    }

    @Operation(summary = "반려동물 목록 조회", description = "사용자의 모든 반려동물 목록을 조회합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "반려동물 목록 조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<PetsResponse>> getUserPets(
        @Parameter(description = "반려동물 소유자 ID", required = true, example = "1")
        @RequestParam Long ownerId
    ) {

        PetListQuery query = PetListQuery.of(ownerId);
        PetsResult result = petFacade.getUserPets(query);

        return ResponseEntity.ok(
            ApiResponse.success(
                PetsResponse.from(result),
                ApiSuccessMessage.PET_LIST_SUCCESS.getMessage())
        );
    }

    @Operation(summary = "반려동물 상세 조회", description = "특정 반려동물의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "반려동물 상세 조회 성공",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "접근 권한 없음"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "반려동물을 찾을 수 없음")
    })
    @GetMapping("/{petId}")
    public ResponseEntity<ApiResponse<PetResponse>> getPet(
        @Parameter(description = "반려동물 ID", required = true, example = "1")
        @PathVariable Long petId,
        @Parameter(description = "반려동물 소유자 ID", required = true, example = "1")
        @RequestParam Long ownerId
    ) {

        PetGetQuery query = PetGetQuery.of(petId, ownerId);
        PetResult petResult = petFacade.getPet(query);
        PetResponse response = PetResponse.from(petResult);

        return ResponseEntity.ok(
            ApiResponse.success(response, ApiSuccessMessage.PET_DETAIL_SUCCESS.getMessage()));
    }


    @Operation(summary = "반려동물 삭제", description = "반려동물을 삭제합니다. (소프트 삭제)")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "반려동물 삭제 성공",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "접근 권한 없음"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "반려동물을 찾을 수 없음")
    })
    @DeleteMapping("/{petId}")
    public ResponseEntity<ApiResponse<Void>> deletePet(
        @Parameter(description = "반려동물 ID", required = true, example = "1")
        @PathVariable Long petId,
        @Parameter(description = "반려동물 소유자 ID", required = true, example = "1")
        @RequestParam Long ownerId) {

        PetDeleteCommand command = PetDeleteCommand.of(petId, ownerId);
        petFacade.deletePet(command);

        return ResponseEntity.ok(
            ApiResponse.success(null, ApiSuccessMessage.PET_DELETE_SUCCESS.getMessage()));
    }
}