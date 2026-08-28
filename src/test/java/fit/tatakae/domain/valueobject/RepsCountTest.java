package fit.tatakae.domain.valueobject;

import fit.tatakae.domain.exception.InconsistentSessionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

public class RepsCountTest {

    @Test
    public void shouldExposeValueWhenRepsAreValid() {
        // Arrange
        int reps = 15;

        // Act
        RepsCount repsCount = new RepsCount(reps);

        // Assert
        assertEquals(reps, repsCount.value());
    }

    // Prueba que las repeticiones no sean inconsistentes, es decir, iguales o inferiores a cero
    @ParameterizedTest
    @ValueSource(ints = {0, -1, -50})
    public void shouldThrowExceptionWhenRepsAreZeroOrNegative(int invalidReps) {
        // Arrange
        // Act
        // Assert
        assertThrows(InconsistentSessionException.class, () -> {
            new RepsCount(invalidReps);
        });
    }
}
