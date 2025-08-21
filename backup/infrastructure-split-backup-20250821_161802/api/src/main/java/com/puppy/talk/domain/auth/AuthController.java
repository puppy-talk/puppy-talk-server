package com.puppy.talk.domain.auth;

import com.puppy.talk.auth.AuthService;
import com.puppy.talk.global.support.ApiResponse;
import com.puppy.talk.global.support.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 사용자 인증 관련 API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "인증", description = "사용자 로그인/회원가입 API")
public class AuthController {

    private final AuthService authService;

    /**
     * 사용자 로그인
     */
    @PostMapping("/login")
    @Operation(summary = "사용자 로그인", description = "사용자명과 패스워드로 로그인하여 JWT 토큰을 발급받습니다.")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for username: {}", request.username());
        
        // TODO: AuthService.login 메소드가 구현되면 활성화
        // var result = authService.login(request.username(), request.password());
        
        // 임시 구현 - 실제 구현 시 제거
        log.warn("Login endpoint not fully implemented yet");
        throw new UnsupportedOperationException("Login functionality not yet implemented");
        
        /*
        if (result.isEmpty()) {
            log.warn("Login failed for username: {}", request.username());
            return ApiResponse.error("Invalid username or password", ErrorCode.AUTHENTICATION_FAILED);
        }

        AuthService.AuthResult authResult = result.get();
        return ApiResponse.ok(
            new AuthResponse(
                authResult.token(),
                UserResponse.from(authResult.user()),
                "Login successful"
            ),
            "Login successful"
        );
        */
    }

    /**
     * 사용자 회원가입
     */
    @PostMapping("/register")
    @Operation(summary = "사용자 회원가입", description = "새 사용자 계정을 생성하고 JWT 토큰을 발급받습니다.")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration attempt for username: {}", request.username());
        
        // TODO: AuthService.register 메소드가 구현되면 활성화
        // var result = authService.register(request.username(), request.email(), request.password());
        
        // 임시 구현 - 실제 구현 시 제거
        log.warn("Register endpoint not fully implemented yet");
        throw new UnsupportedOperationException("Registration functionality not yet implemented");
        
        /*
        if (result.isEmpty()) {
            log.warn("Registration failed for username: {}", request.username());
            return ApiResponse.error(
                "Registration failed. Username or email may already exist.",
                ErrorCode.DUPLICATE_USERNAME
            );
        }

        AuthService.AuthResult authResult = result.get();
        return ApiResponse.ok(
            new AuthResponse(
                authResult.token(),
                UserResponse.from(authResult.user()),
                "Registration successful"
            ),
            "Registration successful"
        );
        */
    }

    /**
     * 토큰 유효성 검증
     */
    @PostMapping("/validate")
    @Operation(summary = "토큰 검증", description = "JWT 토큰의 유효성을 검증하고 사용자 정보를 반환합니다.")
    public ApiResponse<AuthResponse> validateToken(
        @RequestHeader("Authorization") String authHeader
    ) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Invalid authorization header format");
            return ApiResponse.error(
                "Missing or invalid authorization header",
                ErrorCode.AUTHENTICATION_FAILED
            );
        }

        String token = authHeader.substring(7);
        
        // TODO: AuthService.validateToken 메소드가 구현되면 활성화
        // var userOpt = authService.validateToken(token);
        
        // 임시 구현 - 실제 구현 시 제거
        log.warn("Token validation endpoint not fully implemented yet");
        throw new UnsupportedOperationException("Token validation functionality not yet implemented");
        
        /*
        if (userOpt.isEmpty()) {
            log.warn("Token validation failed");
            return ApiResponse.error(
                "Invalid or expired token",
                ErrorCode.AUTHENTICATION_FAILED
            );
        }

        return ApiResponse.ok(
            new AuthResponse(
                token,
                UserResponse.from(userOpt.get()),
                "Token is valid"
            ),
            "Token is valid"
        );
        */
    }

    /**
     * 로그인 요청 DTO
     */
    public record LoginRequest(
        @NotBlank(message = "Username is required")
        String username,
        
        @NotBlank(message = "Password is required")
        String password
    ) {}

    /**
     * 회원가입 요청 DTO
     */
    public record RegisterRequest(
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        String username,
        
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,
        
        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        String password
    ) {}

    /**
     * 인증 응답 DTO
     */
    public record AuthResponse(
        String token,
        UserResponse user,
        String message
    ) {}

    /**
     * 사용자 정보 응답 DTO
     */
    public record UserResponse(
        Long id,
        String username,
        String email
    ) {
        public static UserResponse from(com.puppy.talk.user.User user) {
            return new UserResponse(
                user.identity().id(),
                user.username(),
                user.email()
            );
        }
    }
}
