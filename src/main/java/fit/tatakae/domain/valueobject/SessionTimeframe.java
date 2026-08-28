package fit.tatakae.domain.valueobject;

import fit.tatakae.domain.exception.InconsistentSessionException;

import java.time.Duration;
import java.time.Instant;

public record SessionTimeframe(Instant start, Instant end) {
    public SessionTimeframe {
        if (!end.isAfter(start)) {
            throw new InconsistentSessionException("End time must be after start time");
        }
    }

    public Duration duration() {
        return Duration.between(start, end);
    }
}
