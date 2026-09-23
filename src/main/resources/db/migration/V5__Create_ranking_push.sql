-- Device tokens for APNs, the per-board throttle, and the ranking-alert opt-out.
-- Missing preference row means alerts stay on.

CREATE TABLE device_tokens (
    token VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    sandbox BOOLEAN NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_device_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_device_tokens_user_id ON device_tokens(user_id);

CREATE TABLE ranking_push_log (
    user_id VARCHAR(36) NOT NULL,
    scope VARCHAR(16) NOT NULL,
    sent_at TIMESTAMP WITH TIME ZONE NOT NULL,
    PRIMARY KEY (user_id, scope),
    CONSTRAINT fk_ranking_push_log_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_ranking_push_log_scope CHECK (scope IN ('GLOBAL', 'COUNTRY', 'FRIENDS'))
);

CREATE TABLE ranking_alert_preferences (
    user_id VARCHAR(36) PRIMARY KEY,
    enabled BOOLEAN NOT NULL,
    CONSTRAINT fk_ranking_alert_preferences_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
