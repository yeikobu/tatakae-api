-- Camera-counted rep exercises beyond the original five. Plank stays out:
-- the ranking orders by reps, and a hold is measured in seconds.
ALTER TABLE training_sessions DROP CONSTRAINT chk_training_sessions_exercise;

ALTER TABLE training_sessions
    ADD CONSTRAINT chk_training_sessions_exercise CHECK (exercise IN (
        'PUSH_UP',
        'PIKE_PUSH_UP',
        'PULL_UP',
        'DIP',
        'SQUAT',
        'INCLINE_PUSH_UP',
        'DECLINED_PIKE_PUSH_UP',
        'CHIN_UP',
        'AUSTRALIAN_PULL_UP',
        'BURPEES',
        'CRUNCH'
    ));
