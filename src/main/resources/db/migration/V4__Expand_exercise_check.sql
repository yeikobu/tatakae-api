-- Camera-counted rep exercises beyond the original five. Plank stays out:
-- the ranking orders by reps, and a hold is measured in seconds.
--
-- Databases created by Hibernate (this local volume was Flyway-baselined at v1,
-- so V0 never ran) name the check training_sessions_exercise_check.
-- Databases created by V0 name it chk_training_sessions_exercise.
ALTER TABLE training_sessions DROP CONSTRAINT IF EXISTS chk_training_sessions_exercise;
ALTER TABLE training_sessions DROP CONSTRAINT IF EXISTS training_sessions_exercise_check;

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
