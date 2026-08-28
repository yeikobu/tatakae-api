package fit.tatakae.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

@Schema(name = "ErrorResponse", description = "Unified error contract returned by every failing endpoint")
public record ErrorResponse(

        @Schema(example = "User ghost was not found") String message,
        @Schema(example = "RESOURCE_NOT_FOUND") String code,
        @Schema(example = "404") int status,
        @Schema(example = "/api/v1/users/ghost") String path,
        Instant timestamp,
        @Schema(description = "Field level detail, only present on validation errors") List<String> details) {

    public static ErrorResponse of(String message, String code, int status, String path) {
        return new ErrorResponse(message, code, status, path, Instant.now(), null);
    }

    public static ErrorResponse of(String message, String code, int status, String path, List<String> details) {
        return new ErrorResponse(message, code, status, path, Instant.now(), details);
    }
}
