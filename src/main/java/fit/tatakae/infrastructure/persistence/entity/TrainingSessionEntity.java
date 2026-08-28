package fit.tatakae.infrastructure.persistence.entity;

import fit.tatakae.domain.entity.Exercise;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "training_sessions")
public class TrainingSessionEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    @Column(name = "exercise", nullable = false)
    private Exercise exercise;

    @Column(name = "reps", nullable = false)
    private int reps;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "ended_at", nullable = false)
    private Instant endedAt;

    protected TrainingSessionEntity() {
    }

    public TrainingSessionEntity(String id, UserEntity user, Exercise exercise, int reps,
                                 Instant startedAt, Instant endedAt) {
        this.id = id;
        this.user = user;
        this.exercise = exercise;
        this.reps = reps;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
    }

    public String getId() {
        return id;
    }

    public UserEntity getUser() {
        return user;
    }

    public Exercise getExercise() {
        return exercise;
    }

    public int getReps() {
        return reps;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getEndedAt() {
        return endedAt;
    }
}
