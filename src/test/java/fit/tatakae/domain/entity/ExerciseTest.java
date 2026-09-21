package fit.tatakae.domain.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ExerciseTest {

    @ParameterizedTest
    @CsvSource({
            "INCLINE_PUSH_UP, 112",
            "DECLINED_PIKE_PUSH_UP, 32",
            "CHIN_UP, 82",
            "AUSTRALIAN_PULL_UP, 96",
            "BURPEES, 50",
            "CRUNCH, 120"
    })
    public void shouldCapEachNewlyCountedExercise(String name, int cap) {
        // Act
        Exercise exercise = Exercise.valueOf(name);

        // Assert
        assertEquals(cap, exercise.getExerciseMaxRepsAllowedPerMinute());
    }

    @ParameterizedTest
    @CsvSource({"PLANK", "HANDSTAND_PUSH_UP", "MUSCLE_UP", "BACKFLIP"})
    public void shouldRejectANameTheDetectorDoesNotCount(String name) {
        // Act and Assert
        assertThrows(IllegalArgumentException.class, () -> Exercise.valueOf(name));
    }

    @Test
    public void shouldKeepChinUpDistinctFromPullUp() {
        // Assert
        assertEquals(82, Exercise.CHIN_UP.getExerciseMaxRepsAllowedPerMinute());
        assertEquals(77, Exercise.PULL_UP.getExerciseMaxRepsAllowedPerMinute());
    }
}
