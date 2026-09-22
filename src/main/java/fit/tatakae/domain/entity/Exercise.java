package fit.tatakae.domain.entity;

public enum Exercise {
    PUSH_UP(84),
    PIKE_PUSH_UP(45),
    PULL_UP(77),
    DIP(119),
    SQUAT(104),
    // Absolute set ceilings, same rule as the five above: TrainingSession
    // compares reps to this number and does not scale it by the set's duration.
    // Easier than the sibling movement sits a bit higher; harder sits lower.
    // BURPEES matches the published one-minute record (50).
    INCLINE_PUSH_UP(112),
    DECLINED_PIKE_PUSH_UP(32),
    CHIN_UP(82),
    AUSTRALIAN_PULL_UP(96),
    BURPEES(50),
    CRUNCH(120);

    private final int maxRepsPerMinute;

    Exercise(int maxRepsPerMinute) {
        this.maxRepsPerMinute = maxRepsPerMinute;
    }

    public int getExerciseMaxRepsAllowedPerMinute() {
        return maxRepsPerMinute;
    }
}
