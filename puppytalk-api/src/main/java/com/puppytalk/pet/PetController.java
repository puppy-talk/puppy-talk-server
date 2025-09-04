package com.puppytalk.pet;

import com.puppytalk.pet.dto.request.PetCreateCommand;
import com.puppytalk.pet.dto.request.PetCreateRequest;
import com.puppytalk.pet.dto.request.PetDeleteCommand;
import com.puppytalk.pet.dto.request.PetGetQuery;
import com.puppytalk.pet.dto.request.PetListQuery;
import com.puppytalk.pet.dto.response.PetResponse;
import com.puppytalk.pet.dto.response.PetResult;
import com.puppytalk.pet.dto.response.PetsResponse;
import com.puppytalk.pet.dto.response.PetListResult;
import com.puppytalk.support.ApiResponse;
import com.puppytalk.support.ApiSuccessMessage;
import com.puppytalk.user.User;
import com.puppytalk.auth.CurrentUser;
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
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Pet", description = "반려동물 관리 API")
@RestController
@RequestMapping("/api/pets")
public class PetController {

    private final PetFacade petFacade;

    public PetController(PetFacade petFacade) {
        this.petFacade = petFacade;
    }

    @Operation(
        summary = "반려동물 생성", 
        description = "새로운 반려동물을 생성합니다. 반려동물은 불변 엔티티로 생성 후 수정되지 않습니다."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201", 
            description = "반려동물 생성 성공",
            content = @Content(schema = @Schema(implementation = PetResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "잘못된 요청 - 필수 필드 누락 또는 유효성 검증 실패"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500", 
            description = "서버 내부 오류"
        )
    })
    @PostMapping
    public ResponseEntity<ApiResponse<PetResponse>> createPet(
        @Parameter(
            description = "반려동물 생성 요청 정보 - 소유자 ID, 이름, 페르소나 포함", 
            required = true
        ) @Valid @RequestBody PetCreateRequest request,
        @CurrentUser User currentUser
    ) {
        Long ownerId = currentUser.getId().value();

        PetCreateCommand command = PetCreateCommand.of(ownerId, request.name(), request.persona());

        petFacade.createPet(command);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(ApiSuccessMessage.PET_CREATE_SUCCESS));
    }

    @Operation(summary = "반려동물 목록 조회", description = "사용자의 모든 반려동물 목록을 조회합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "반려동물 목록 조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<PetsResponse>> getPetList(@CurrentUser User currentUser) {
        PetListQuery query = PetListQuery.of(currentUser.getId().value());
        PetListResult result = petFacade.getPetList(query);

        return ResponseEntity.ok(
            ApiResponse.success(
                PetsResponse.from(result),
                ApiSuccessMessage.PET_LIST_SUCCESS)
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
        @CurrentUser User currentUser
    ) {
        PetGetQuery query = PetGetQuery.of(petId, currentUser.getId().value());
        PetResult petResult = petFacade.getPet(query);
        PetResponse response = PetResponse.from(petResult);

        return ResponseEntity.ok(
            ApiResponse.success(response, ApiSuccessMessage.PET_DETAIL_SUCCESS));
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
        @CurrentUser User currentUser) {
        PetDeleteCommand command = PetDeleteCommand.of(petId, currentUser.getId().value());
        petFacade.deletePet(command);

        return ResponseEntity.ok(
            ApiResponse.success(ApiSuccessMessage.PET_DELETE_SUCCESS)
        );
    }
}
