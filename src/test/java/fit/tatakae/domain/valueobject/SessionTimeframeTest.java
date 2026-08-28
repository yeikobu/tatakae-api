package fit.tatakae.domain.valueobject;

import fit.tatakae.domain.exception.InconsistentSessionException;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

public class SessionTimeframeTest {

    @Test
    public void shouldExposeStartEndAndDurationWhenValid() {
        // Arrange
        Instant start = Instant.parse("2026-07-22T10:00:00Z");
        Instant end = start.plusSeconds(60);

        // Act
        SessionTimeframe timeframe = new SessionTimeframe(start, end);

        // Assert
        assertEquals(start, timeframe.start());
        assertEquals(end, timeframe.end());
        assertEquals(Duration.ofSeconds(60), timeframe.duration());
    }

    @Test
    public void shouldThrowExceptionWhenEndIsBeforeStart() {
        // Arrange
        Instant end = Instant.parse("2026-07-22T10:00:00Z");
        Instant start = end.plusSeconds(60);

        // Act
        // Assert
        assertThrows(InconsistentSessionException.class, () -> {
            new SessionTimeframe(start, end);
        });
    }

    @Test
    public void shouldThrowExceptionWhenStartIsEqualToEnd() {
        // Arrange
        Instant start = Instant.parse("2026-07-22T10:00:00Z");
        Instant end = Instant.parse("2026-07-22T10:00:00Z");

        // Act
        // Assert
        assertThrows(InconsistentSessionException.class, () -> {
            new SessionTimeframe(start, end);
        });
    }
}
