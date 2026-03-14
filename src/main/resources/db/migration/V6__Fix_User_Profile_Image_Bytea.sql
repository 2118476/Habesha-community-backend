-- Fix users.profile_image so PostgreSQL uses BYTEA instead of OID large objects

DO $$
DECLARE
    profile_image_data_type text;
BEGIN
    IF EXISTS (SELECT 1
               FROM information_schema.tables
               WHERE table_name = 'users') THEN

        IF NOT EXISTS (SELECT 1
                       FROM information_schema.columns
                       WHERE table_name = 'users'
                         AND column_name = 'profile_image') THEN
            ALTER TABLE users ADD COLUMN profile_image BYTEA;
        ELSE
            SELECT data_type
              INTO profile_image_data_type
              FROM information_schema.columns
             WHERE table_name = 'users'
               AND column_name = 'profile_image';

            -- If old schema created profile_image as oid, convert it to bytea
            IF profile_image_data_type = 'oid' THEN
                ALTER TABLE users ADD COLUMN profile_image_tmp BYTEA;

                UPDATE users
                   SET profile_image_tmp = lo_get(profile_image)
                 WHERE profile_image IS NOT NULL;

                ALTER TABLE users DROP COLUMN profile_image;
                ALTER TABLE users RENAME COLUMN profile_image_tmp TO profile_image;
            END IF;
        END IF;

    END IF;
END $$;

-- Ensure profile_image_type column exists
DO $$
BEGIN
    IF EXISTS (SELECT 1
               FROM information_schema.tables
               WHERE table_name = 'users') THEN

        IF NOT EXISTS (SELECT 1
                       FROM information_schema.columns
                       WHERE table_name = 'users'
                         AND column_name = 'profile_image_type') THEN
            ALTER TABLE users ADD COLUMN profile_image_type VARCHAR(100);
        END IF;

    END IF;
END $$;
