package fit.tatakae.infrastructure.web.dto;

import fit.tatakae.domain.entity.Gender;
import fit.tatakae.domain.entity.PrivacyLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(name = "CreateUserRequest", description = "Payload used to register a new athlete")
public record CreateUserRequest(

        @Schema(description = "Unique handle that identifies the athlete, stored in lower case", example = "yeikobu")
        @NotBlank(message = "username is required")
        @Size(max = 30, message = "username must be at most 30 characters long")
        @Pattern(regexp = "^[A-Za-z0-9._]+$",
                message = "username must use only letters, digits, dots and underscores")
        String username,

        @Schema(description = "ISO country code used by the local leaderboard", example = "cl")
        @NotBlank(message = "country is required")
        String country,

        @Schema(description = "Whether the athlete shows up on public leaderboards", example = "PUBLIC")
        @NotNull(message = "privacyLevel is required")
        PrivacyLevel privacyLevel,

        @Schema(description = "Category used by men and women leaderboards", example = "MALE")
        @NotNull(message = "gender is required")
        Gender gender) {
}
