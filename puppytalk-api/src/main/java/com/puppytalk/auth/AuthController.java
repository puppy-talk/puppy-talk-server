package com.puppytalk.auth;

import com.puppytalk.auth.dto.request.LoginCommand;
import com.puppytalk.auth.dto.request.LogoutCommand;
import com.puppytalk.auth.dto.response.ActiveTokensResult;
import com.puppytalk.auth.dto.response.TokenResult;
import com.puppytalk.auth.dto.request.LoginRequest;
import com.puppytalk.auth.dto.response.ActiveTokensResponse;
import com.puppytalk.auth.dto.response.TokenResponse;
import jakarta.servlet.http.HttpServletRequest;
import com.puppytalk.user.User;
import com.puppytalk.user.UserId;
import com.puppytalk.support.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 인증 관련 REST API 컨트롤러
 */
@Tag(name = "Authentication", description = "인증 API")
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final AuthenticationFacade authenticationFacade;
    
    public AuthController(AuthenticationFacade authenticationFacade) {
        this.authenticationFacade = authenticationFacade;
    }
    
    @Operation(summary = "로그인", description = "사용자명과 비밀번호로 로그인하고 JWT 토큰을 발급받습니다.")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginCommand command = new LoginCommand(request.username(), request.password());
        TokenResult result = authenticationFacade.login(command);
        TokenResponse response = TokenResponse.from(result);
        
        return ResponseEntity.ok(ApiResponse.success("로그인이 완료되었습니다.", response));
    }
    
    
    @Operation(summary = "로그아웃", description = "현재 토큰을 무효화하고 로그아웃합니다.")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        User currentUser = getCurrentUser(request);
        String accessToken = extractAccessToken(request);
        
        LogoutCommand command = LogoutCommand.singleLogout(currentUser.getId(), accessToken);
        authenticationFacade.logout(command);
        
        return ResponseEntity.ok(ApiResponse.<Void>success("로그아웃되었습니다.", null));
    }
    
    @Operation(summary = "전체 로그아웃", description = "모든 디바이스에서 로그아웃합니다.")
    @PostMapping("/logout-all")
    public ResponseEntity<ApiResponse<Void>> logoutAll(HttpServletRequest request) {
        User currentUser = getCurrentUser(request);
        
        LogoutCommand command = LogoutCommand.logoutAll(currentUser.getId());
        authenticationFacade.logout(command);
        
        return ResponseEntity.ok(ApiResponse.<Void>success("모든 디바이스에서 로그아웃되었습니다.", null));
    }
    
    @Operation(summary = "활성 세션 조회", description = "사용자의 모든 활성 세션을 조회합니다.")
    @GetMapping("/sessions")
    public ResponseEntity<ApiResponse<ActiveTokensResponse>> getActiveSessions(HttpServletRequest request) {
        User currentUser = getCurrentUser(request);
        
        ActiveTokensResult result = authenticationFacade.getActiveTokens(currentUser.getId());
        ActiveTokensResponse response = ActiveTokensResponse.from(result);
        
        return ResponseEntity.ok(ApiResponse.success("활성 세션을 조회했습니다.", response));
    }
    
    private User getCurrentUser(HttpServletRequest request) {
        return (User) request.getAttribute("currentUser");
    }
    
    private String extractAccessToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new IllegalArgumentException("Authorization header not found");
    }
}