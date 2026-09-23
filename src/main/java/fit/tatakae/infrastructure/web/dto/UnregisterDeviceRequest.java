package fit.tatakae.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "UnregisterDeviceRequest")
public record UnregisterDeviceRequest(
        @Schema(description = "64-character hex device token")
        @NotBlank(message = "token is required")
        String token) {
}
