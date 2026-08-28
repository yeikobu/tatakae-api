package fit.tatakae.domain.entity;

import fit.tatakae.domain.exception.FraudulentSessionException;
import fit.tatakae.domain.valueobject.RepsCount;
import fit.tatakae.domain.valueobject.SessionTimeframe;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

public class TrainingSession {
    private final String id;
    private final User user;
    private final Exercise exercise;
    private final RepsCount reps;
    private final SessionTimeframe timeframe;
    private final Clock clock;

    public TrainingSession(User user, Exercise exercise, int reps, Instant start, Instant end, Clock clock) {
        this(UUID.randomUUID().toString(), user, exercise, reps, start, end, clock);
    }

    // Reconstitution constructor: used when the session's identity already exists (e.g. loaded from a repository).
    public TrainingSession(String id, User user, Exercise exercise, int reps, Instant start, Instant end, Clock clock) {
        this.id = id;
        this.user = user;
        this.exercise = exercise;
        this.reps = new RepsCount(reps);
        this.timeframe = new SessionTimeframe(start, end);
        this.clock = clock;

        if (this.reps.value() > exercise.getExerciseMaxRepsAllowedPerMinute()) {
            throw new FraudulentSessionException("Registered exercise max reps are greater than exercise max reps allowed per minute");
        }
    }

    public boolean isForExercise(Exercise otherExercise) {
        return this.exercise == otherExercise;
    }

    // True when this session ranks above the other: more reps, or same reps but registered earlier.
    public boolean outperforms(TrainingSession other) {
        if (this.reps.value() != other.reps.value()) {
            return this.reps.value() > other.reps.value();
        }
        return other.timeframe.start().isAfter(this.timeframe.start());
    }

    // MARK: - Getters
    public String getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Exercise getExercise() {
        return exercise;
    }

    public int getReps() {
        return reps.value();
    }

    public Instant getStart() {
        return timeframe.start();
    }

    public Instant getEnd() {
        return timeframe.end();
    }

    public Clock getClock() {
        return clock;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof TrainingSession other)) {
            return false;
        }
        return this.id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
