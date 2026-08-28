package fit.tatakae.domain.entity;

public enum Exercise {
    PUSH_UP(84),
    PIKE_PUSH_UP(45),
    PULL_UP(77),
    DIP(119),
    SQUAT(104);

    private final int maxRepsPerMinute;

    Exercise(int maxRepsPerMinute) {
        this.maxRepsPerMinute = maxRepsPerMinute;
    }

    public int getExerciseMaxRepsAllowedPerMinute() {
        return maxRepsPerMinute;
    }
}
