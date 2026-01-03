-- Initial schema creation for Habesha Community Backend
-- This migration ensures all required tables exist with proper PostgreSQL types

-- Create users table if it doesn't exist
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    username VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(255),
    city VARCHAR(255),
    profile_image_url TEXT,
    password VARCHAR(255),
    bio VARCHAR(1024),
    banner_image_url TEXT,
    notifications_seen_at TIMESTAMP,
    xp INTEGER DEFAULT 0,
    twitter VARCHAR(255),
    linkedin VARCHAR(255),
    instagram VARCHAR(255),
    reset_password_token VARCHAR(255),
    option_value VARCHAR(255),
    role VARCHAR(50) DEFAULT 'USER',
    active BOOLEAN DEFAULT true,
    frozen BOOLEAN DEFAULT false,
    frozen_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    language VARCHAR(10) DEFAULT 'en',
    ai_assist_enabled BOOLEAN DEFAULT false,
    notifications BOOLEAN DEFAULT true,
    last_login_at TIMESTAMP,
    last_active_at TIMESTAMP,
    profile_image BYTEA,
    profile_image_type VARCHAR(100),
    theme VARCHAR(50),
    density VARCHAR(50),
    font_scale VARCHAR(50),
    reduced_motion BOOLEAN,
    email_visibility VARCHAR(50),
    phone_visibility VARCHAR(50),
    show_online_status BOOLEAN,
    show_last_seen BOOLEAN,
    searchable BOOLEAN,
    mentions_policy VARCHAR(50),
    dm_policy VARCHAR(50),
    notifications_json TEXT
);

-- Create user_badges table if it doesn't exist
CREATE TABLE IF NOT EXISTS user_badges (
    user_id BIGINT NOT NULL,
    badge VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_active ON users(active);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_user_badges_user_id ON user_badges(user_id);