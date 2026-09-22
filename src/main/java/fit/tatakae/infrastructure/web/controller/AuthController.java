package fit.tatakae.infrastructure.web.controller;

import fit.tatakae.application.usecase.FindOrCreateUserByAppleSubUseCase;
import fit.tatakae.application.usecase.GetUserUseCase;
import fit.tatakae.application.usecase.SessionTokens;
import fit.tatakae.domain.entity.User;
import fit.tatakae.infrastructure.web.dto.AppleSignInRequest;
import fit.tatakae.infrastructure.web.dto.AppleSignInResponse;
import fit.tatakae.infrastructure.web.dto.RefreshTokenRequest;
import fit.tatakae.infrastructure.web.dto.SessionTokenResponse;
import fit.tatakae.infrastructure.web.dto.UserResponse;
import fit.tatakae.infrastructure.web.security.SecurityContextHelper;
import fit.tatakae.infrastructure.web.security.SessionAuthService;
import fit.tatakae.infrastructure.web.security.jwt.AppleJwtClaims;
import fit.tatakae.infrastructure.web.security.jwt.AppleJwtValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Sign in with Apple and Tatakae session tokens")
public class AuthController {

    private final AppleJwtValidator appleJwtValidator;
    private final FindOrCreateUserByAppleSubUseCase findOrCreateUserByAppleSubUseCase;
    private final GetUserUseCase getUserUseCase;
    private final SessionAuthService sessionAuthService;

    public AuthController(AppleJwtValidator appleJwtValidator,
                          FindOrCreateUserByAppleSubUseCase findOrCreateUserByAppleSubUseCase,
                          GetUserUseCase getUserUseCase,
                          SessionAuthService sessionAuthService) {
        this.appleJwtValidator = appleJwtValidator;
        this.findOrCreateUserByAppleSubUseCase = findOrCreateUserByAppleSubUseCase;
        this.getUserUseCase = getUserUseCase;
        this.sessionAuthService = sessionAuthService;
    }

    @PostMapping("/apple")
    @Operation(summary = "Sign in with Apple - bootstrap session",
               description = "Validates Apple identityToken (+ optional nonce), finds or creates the athlete, "
                       + "and returns Tatakae access + refresh session tokens. "
                       + "Subsequent API calls use Authorization: Bearer <accessToken> (not the Apple identity token).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authentication successful, athlete found or created"),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "401", description = "Invalid or expired Apple identity token")
    })
    public AppleSignInResponse signInWithApple(@Valid @RequestBody AppleSignInRequest request) {
        AppleJwtClaims claims = appleJwtValidator.validate(request.identityToken(), request.nonce());

        boolean existed = findOrCreateUserByAppleSubUseCase.exists(claims.sub());
        User user = findOrCreateUserByAppleSubUseCase.execute(claims.sub());
        boolean created = !existed;

        SessionTokens tokens = sessionAuthService.issue(user.getUserId());
        return AppleSignInResponse.from(user, created, tokens);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh session tokens",
               description = "Validates the opaque refresh token, revokes it, and issues a new access + refresh pair.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "New tokens issued"),
            @ApiResponse(responseCode = "401", description = "Invalid, expired, or revoked refresh token")
    })
    public SessionTokenResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        SessionTokens tokens = sessionAuthService.refresh(request.refreshToken());
        return SessionTokenResponse.from(tokens);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Sign out — revoke refresh token",
               description = "Requires Bearer access token. Revokes the refresh token in the body so it cannot be reused.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Refresh token revoked"),
            @ApiResponse(responseCode = "401", description = "Not authenticated or invalid refresh token")
    })
    public void logout(@Valid @RequestBody RefreshTokenRequest request) {
        if (!SecurityContextHelper.isAuthenticated()) {
            throw new fit.tatakae.domain.exception.AuthenticationRequiredException(
                    "Authentication required: provide a valid Bearer access token"
            );
        }
        String userId = SecurityContextHelper.getAuthenticatedUserId();
        sessionAuthService.logout(userId, request.refreshToken());
    }

    @GetMapping("/me")
    @Operation(summary = "Get authenticated athlete info",
               description = "Returns the profile of the currently authenticated athlete. "
                       + "Requires Bearer Tatakae access token (from /auth/apple or /auth/refresh).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Athlete info returned"),
            @ApiResponse(responseCode = "401", description = "Not authenticated or invalid token")
    })
    public UserResponse me() {
        if (!SecurityContextHelper.isAuthenticated()) {
            throw new fit.tatakae.domain.exception.AuthenticationRequiredException(
                "Authentication required: provide a valid Bearer token"
            );
        }

        String authenticatedUserId = SecurityContextHelper.getAuthenticatedUserId();
        User user = getUserUseCase.execute(authenticatedUserId);
        return UserResponse.from(user);
    }
}
