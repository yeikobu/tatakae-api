-- The column keeps the casing the athlete typed. Uniqueness stays case-insensitive,
-- so YeikoBu and yeikobu cannot both exist.
CREATE UNIQUE INDEX uk_users_username_ci ON users (LOWER(username));
