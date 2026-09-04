package fit.tatakae.infrastructure.web.dto;

import fit.tatakae.domain.entity.Gender;
import fit.tatakae.domain.entity.PrivacyLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// The handle can change here: the identity of the athlete is the UUID in the path, not the name.
@Schema(name = "UpdateUserRequest", description = "Profile of an athlete, handle included")
public record UpdateUserRequest(

        @Schema(description = "New public handle, stored in lower case", example = "yeikobu")
        @NotBlank(message = "username is required")
        @Size(max = 30, message = "username must be at most 30 characters long")
        @Pattern(regexp = "^[A-Za-z0-9._]+$",
                message = "username must use only letters, digits, dots and underscores")
        String username,

        @Schema(description = "ISO country code used by the local leaderboard", example = "cl")
        @NotBlank(message = "country is required")
        String country,

        @Schema(description = "Whether the athlete shows up on public leaderboards", example = "PRIVATE")
        @NotNull(message = "privacyLevel is required")
        PrivacyLevel privacyLevel,

        @Schema(description = "Category used by men and women leaderboards", example = "FEMALE")
        @NotNull(message = "gender is required")
        Gender gender) {
}
