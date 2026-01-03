-- Production optimizations and final schema adjustments
-- This migration adds any missing constraints and optimizations

-- Ensure proper constraints on users table
ALTER TABLE users 
    ALTER COLUMN email SET NOT NULL,
    ALTER COLUMN username SET NOT NULL;

-- Add constraints if they don't exist
DO $$ 
BEGIN
    -- Add unique constraint on email if it doesn't exist
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'users_email_key' 
        AND table_name = 'users'
    ) THEN
        ALTER TABLE users ADD CONSTRAINT users_email_key UNIQUE (email);
    END IF;

    -- Add unique constraint on username if it doesn't exist
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'users_username_key' 
        AND table_name = 'users'
    ) THEN
        ALTER TABLE users ADD CONSTRAINT users_username_key UNIQUE (username);
    END IF;
END $$;

-- Add missing columns to users table if they don't exist
DO $$ 
BEGIN
    -- Add profile_image column if missing
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'users' AND column_name = 'profile_image'
    ) THEN
        ALTER TABLE users ADD COLUMN profile_image BYTEA;
    END IF;

    -- Add profile_image_type column if missing
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'users' AND column_name = 'profile_image_type'
    ) THEN
        ALTER TABLE users ADD COLUMN profile_image_type VARCHAR(100);
    END IF;

    -- Add profile_image_url column if missing
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'users' AND column_name = 'profile_image_url'
    ) THEN
        ALTER TABLE users ADD COLUMN profile_image_url TEXT;
    END IF;

    -- Add banner_image_url column if missing
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'users' AND column_name = 'banner_image_url'
    ) THEN
        ALTER TABLE users ADD COLUMN banner_image_url TEXT;
    END IF;

    -- Add notifications_json column if missing
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'users' AND column_name = 'notifications_json'
    ) THEN
        ALTER TABLE users ADD COLUMN notifications_json TEXT;
    END IF;
END $$;

-- Create additional performance indexes
CREATE INDEX IF NOT EXISTS idx_users_created_at ON users(created_at);
CREATE INDEX IF NOT EXISTS idx_users_last_active_at ON users(last_active_at);
CREATE INDEX IF NOT EXISTS idx_users_frozen ON users(frozen);

-- Optimize text search indexes
CREATE INDEX IF NOT EXISTS idx_classified_ads_title_gin ON classified_ads USING gin(to_tsvector('english', title));
CREATE INDEX IF NOT EXISTS idx_classified_ads_description_gin ON classified_ads USING gin(to_tsvector('english', description));

-- Add partial indexes for active records only
CREATE INDEX IF NOT EXISTS idx_classified_ads_active_created_at ON classified_ads(created_at) WHERE active = true;
CREATE INDEX IF NOT EXISTS idx_travel_posts_active_departure_date ON travel_posts(departure_date) WHERE active = true;
CREATE INDEX IF NOT EXISTS idx_rentals_active_created_at ON rentals(created_at) WHERE active = true;

-- Ensure proper data types for PostgreSQL
-- Update any remaining MySQL-specific column types if they exist
DO $$
BEGIN
    -- Check and fix any LONGTEXT columns (should be TEXT in PostgreSQL)
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'users' 
        AND column_name = 'bio' 
        AND character_maximum_length != 1024
    ) THEN
        ALTER TABLE users ALTER COLUMN bio TYPE VARCHAR(1024);
    END IF;
END $$;