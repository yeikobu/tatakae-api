package fit.tatakae.infrastructure.web.controller;

import fit.tatakae.application.usecase.FindOrCreateUserByAppleSubUseCase;
import fit.tatakae.application.usecase.GetUserUseCase;
import fit.tatakae.domain.entity.User;
import fit.tatakae.infrastructure.web.dto.AppleSignInRequest;
import fit.tatakae.infrastructure.web.dto.AppleSignInResponse;
import fit.tatakae.infrastructure.web.dto.UserResponse;
import fit.tatakae.infrastructure.web.security.SecurityContextHelper;
import fit.tatakae.infrastructure.web.security.jwt.AppleJwtClaims;
import fit.tatakae.infrastructure.web.security.jwt.AppleJwtValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Sign in with Apple authentication endpoints")
public class AuthController {

    private final AppleJwtValidator appleJwtValidator;
    private final FindOrCreateUserByAppleSubUseCase findOrCreateUserByAppleSubUseCase;
    private final GetUserUseCase getUserUseCase;

    public AuthController(AppleJwtValidator appleJwtValidator,
                         FindOrCreateUserByAppleSubUseCase findOrCreateUserByAppleSubUseCase,
                         GetUserUseCase getUserUseCase) {
        this.appleJwtValidator = appleJwtValidator;
        this.findOrCreateUserByAppleSubUseCase = findOrCreateUserByAppleSubUseCase;
        this.getUserUseCase = getUserUseCase;
    }

    @PostMapping("/apple")
    @Operation(summary = "Sign in with Apple - bootstrap session",
               description = "Validates Apple identity token and finds or creates the athlete. " +
                           "Returns athlete info and whether this call created the account. " +
                           "Stateless: subsequent calls use the same identity token as Bearer.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authentication successful, athlete found or created"),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "401", description = "Invalid or expired Apple identity token")
    })
    public AppleSignInResponse signInWithApple(@Valid @RequestBody AppleSignInRequest request) {
        // Validate Apple identity token (iss, aud, exp, signature)
        AppleJwtClaims claims = appleJwtValidator.validate(request.identityToken(), request.nonce());

        // Find or create athlete linked to Apple sub
        boolean existed = findOrCreateUserByAppleSubUseCase.exists(claims.sub());
        User user = findOrCreateUserByAppleSubUseCase.execute(claims.sub());
        boolean created = !existed;

        return AppleSignInResponse.from(user, created);
    }

    @GetMapping("/me")
    @Operation(summary = "Get authenticated athlete info",
               description = "Returns the profile of the currently authenticated athlete. " +
                           "Requires Bearer token (the same Apple identity token used in /auth/apple).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Athlete info returned"),
            @ApiResponse(responseCode = "401", description = "Not authenticated or invalid token")
    })
    public UserResponse me() {
        String authenticatedUserId = SecurityContextHelper.getAuthenticatedUserId();
        User user = getUserUseCase.execute(authenticatedUserId);
        return UserResponse.from(user);
    }
}
