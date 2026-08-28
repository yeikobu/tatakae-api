package fit.tatakae.domain.valueobject;

import fit.tatakae.domain.exception.InconsistentSessionException;

public record RepsCount(int value) {
    public RepsCount {
        if (value <= 0) {
            throw new InconsistentSessionException("Reps quantity must be greater than 0 to save a training session");
        }
    }
}
