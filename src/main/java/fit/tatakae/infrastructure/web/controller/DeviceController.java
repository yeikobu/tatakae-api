package fit.tatakae.infrastructure.web.controller;

import fit.tatakae.application.usecase.RegisterDeviceTokenUseCase;
import fit.tatakae.application.usecase.UnregisterDeviceTokenUseCase;
import fit.tatakae.domain.exception.AuthenticationRequiredException;
import fit.tatakae.infrastructure.web.dto.RegisterDeviceRequest;
import fit.tatakae.infrastructure.web.dto.UnregisterDeviceRequest;
import fit.tatakae.infrastructure.web.security.SecurityContextHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/devices")
@Tag(name = "Devices", description = "APNs device tokens for ranking pushes")
public class DeviceController {

    private final RegisterDeviceTokenUseCase registerDeviceTokenUseCase;
    private final UnregisterDeviceTokenUseCase unregisterDeviceTokenUseCase;

    public DeviceController(RegisterDeviceTokenUseCase registerDeviceTokenUseCase,
                            UnregisterDeviceTokenUseCase unregisterDeviceTokenUseCase) {
        this.registerDeviceTokenUseCase = registerDeviceTokenUseCase;
        this.unregisterDeviceTokenUseCase = unregisterDeviceTokenUseCase;
    }

    @PutMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Register this device for ranking pushes")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Token stored"),
            @ApiResponse(responseCode = "400", description = "Token is not 64 hex characters"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public void register(@Valid @RequestBody RegisterDeviceRequest request) {
        registerDeviceTokenUseCase.execute(currentUserId(), request.token(), request.sandbox());
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Forget this device token")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Token removed, or it was not this athlete's"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public void unregister(@Valid @RequestBody UnregisterDeviceRequest request) {
        unregisterDeviceTokenUseCase.execute(currentUserId(), request.token());
    }

    private static String currentUserId() {
        if (!SecurityContextHelper.isAuthenticated()) {
            throw new AuthenticationRequiredException("Authentication required: provide a valid Bearer access token");
        }
        return SecurityContextHelper.getAuthenticatedUserId();
    }
}
