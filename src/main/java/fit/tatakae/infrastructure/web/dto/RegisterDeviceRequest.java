package fit.tatakae.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(name = "RegisterDeviceRequest", description = "APNs device token for ranking pushes")
public record RegisterDeviceRequest(
        @Schema(description = "64-character hex device token", example = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef")
        @NotBlank(message = "token is required")
        String token,

        @Schema(description = "True for TatakaeDev (sandbox APNs), false for the App Store build")
        @NotNull(message = "sandbox is required")
        Boolean sandbox) {
}
