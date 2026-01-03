-- Migration to add profile image columns to users table
-- This ensures the columns exist for existing databases

-- Add profile_image column if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'users' AND column_name = 'profile_image') THEN
        ALTER TABLE users ADD COLUMN profile_image BYTEA;
    END IF;
END $$;

-- Add profile_image_type column if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'users' AND column_name = 'profile_image_type') THEN
        ALTER TABLE users ADD COLUMN profile_image_type VARCHAR(100);
    END IF;
END $$;

-- Add profile_image_url column if it doesn't exist (for external URLs)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'users' AND column_name = 'profile_image_url') THEN
        ALTER TABLE users ADD COLUMN profile_image_url TEXT;
    END IF;
END $$;