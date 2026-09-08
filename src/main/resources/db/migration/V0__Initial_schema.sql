-- Initial schema for tatakae-api
-- Creates users, friendships, and training_sessions tables

-- Users table
CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY,
    username VARCHAR(30) NOT NULL,
    country VARCHAR(255),
    privacy_level VARCHAR(50) NOT NULL,
    gender VARCHAR(50) NOT NULL DEFAULT 'MALE',
    CONSTRAINT uk_users_username UNIQUE (username),
    CONSTRAINT chk_users_privacy_level CHECK (privacy_level IN ('PRIVATE', 'PUBLIC')),
    CONSTRAINT chk_users_gender CHECK (gender IN ('MALE', 'FEMALE', 'UNSPECIFIED'))
);

-- Friendships table
CREATE TABLE friendships (
    id VARCHAR(36) PRIMARY KEY,
    requester_id VARCHAR(36) NOT NULL,
    addressee_id VARCHAR(36) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    responded_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT chk_friendships_status CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED', 'BLOCKED'))
);

-- Indexes for friendships (no foreign keys by design - see architecture docs)
CREATE INDEX idx_friendship_requester ON friendships(requester_id);
CREATE INDEX idx_friendship_addressee ON friendships(addressee_id);

-- Training sessions table
CREATE TABLE training_sessions (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    exercise VARCHAR(50) NOT NULL,
    reps INTEGER NOT NULL,
    started_at TIMESTAMP WITH TIME ZONE NOT NULL,
    ended_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_training_sessions_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT chk_training_sessions_exercise CHECK (exercise IN ('PUSH_UP', 'PIKE_PUSH_UP', 'PULL_UP', 'DIP', 'SQUAT'))
);
