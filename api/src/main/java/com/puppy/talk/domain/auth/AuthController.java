package com.puppy.talk.domain.auth;

import com.puppy.talk.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    // private final AuthService authService;

    /**
     * 사용자 로그인
     */
    /*
    @PostMapping("/login")
    @Operation(summary = "사용자 로그인", description = "사용자명과 패스워드로 로그인하여 JWT 토큰을 발급받습니다.")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for username: {}", request.username());
        
        Optional<AuthService.AuthResult> result = authService.login(request.username(), request.password());
        
        if (result.isEmpty()) {
            log.warn("Login failed for username: {}", request.username());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new AuthResponse(null, null, "Invalid username or password"));
        }

        AuthService.AuthResult authResult = result.get();
        return ResponseEntity.ok(new AuthResponse(
            authResult.token(),
            UserResponse.from(authResult.user()),
            "Login successful"
        ));
    }
    */

    /**
     * 사용자 회원가입
     */
    /*
    @PostMapping("/register")
    @Operation(summary = "사용자 회원가입", description = "새 사용자 계정을 생성하고 JWT 토큰을 발급받습니다.")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration attempt for username: {}", request.username());
        
        Optional<AuthService.AuthResult> result = authService.register(
            request.username(), 
            request.email(), 
            request.password()
        );
        
        if (result.isEmpty()) {
            log.warn("Registration failed for username: {}", request.username());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new AuthResponse(null, null, "Registration failed. Username or email may already exist."));
        }

        AuthService.AuthResult authResult = result.get();
        return ResponseEntity.ok(new AuthResponse(
            authResult.token(),
            UserResponse.from(authResult.user()),
            "Registration successful"
        ));
    }
    */

    /**
     * 토큰 유효성 검증
     */
    /*
    @PostMapping("/validate")
    @Operation(summary = "토큰 검증", description = "JWT 토큰의 유효성을 검증하고 사용자 정보를 반환합니다.")
    public ResponseEntity<AuthResponse> validateToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new AuthResponse(null, null, "Missing or invalid authorization header"));
        }

        String token = authHeader.substring(7);
        var userOpt = authService.validateToken(token);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new AuthResponse(null, null, "Invalid or expired token"));
        }

        return ResponseEntity.ok(new AuthResponse(
            token,
            UserResponse.from(userOpt.get()),
            "Token is valid"
        ));
    }
    */

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
