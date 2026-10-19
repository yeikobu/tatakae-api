-- Knee and decline push-ups join the ranking. The CHECK names every value by
-- hand, so it is rebuilt with the two new ones; V4 stays as it shipped.
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
        'CRUNCH',
        'KNEE_PUSH_UP',
        'DECLINE_PUSH_UP'
    ));
