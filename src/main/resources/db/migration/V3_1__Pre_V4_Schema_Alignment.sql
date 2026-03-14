-- Pre-V4 schema alignment for older production databases
-- Purpose:
--   Some production tables were likely created by older Hibernate schemas, so V4 fails
--   before V5 can repair them. This migration safely adds the missing columns that V4 needs.

DO $$
BEGIN
    IF EXISTS (SELECT 1
               FROM information_schema.tables
               WHERE table_name = 'classified_ads') THEN

        IF NOT EXISTS (SELECT 1
                       FROM information_schema.columns
                       WHERE table_name = 'classified_ads' AND column_name = 'title') THEN
            ALTER TABLE classified_ads ADD COLUMN title VARCHAR(255);
        END IF;

        IF NOT EXISTS (SELECT 1
                       FROM information_schema.columns
                       WHERE table_name = 'classified_ads' AND column_name = 'description') THEN
            ALTER TABLE classified_ads ADD COLUMN description TEXT;
        END IF;

        IF NOT EXISTS (SELECT 1
                       FROM information_schema.columns
                       WHERE table_name = 'classified_ads' AND column_name = 'created_at') THEN
            ALTER TABLE classified_ads ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
        END IF;

        IF NOT EXISTS (SELECT 1
                       FROM information_schema.columns
                       WHERE table_name = 'classified_ads' AND column_name = 'active') THEN
            ALTER TABLE classified_ads ADD COLUMN active BOOLEAN DEFAULT true;
        END IF;

    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1
               FROM information_schema.tables
               WHERE table_name = 'travel_posts') THEN

        IF NOT EXISTS (SELECT 1
                       FROM information_schema.columns
                       WHERE table_name = 'travel_posts' AND column_name = 'created_at') THEN
            ALTER TABLE travel_posts ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
        END IF;

        IF NOT EXISTS (SELECT 1
                       FROM information_schema.columns
                       WHERE table_name = 'travel_posts' AND column_name = 'active') THEN
            ALTER TABLE travel_posts ADD COLUMN active BOOLEAN DEFAULT true;
        END IF;

        IF NOT EXISTS (SELECT 1
                       FROM information_schema.columns
                       WHERE table_name = 'travel_posts' AND column_name = 'departure_date') THEN
            ALTER TABLE travel_posts ADD COLUMN departure_date DATE;
        END IF;

    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1
               FROM information_schema.tables
               WHERE table_name = 'rentals') THEN

        IF NOT EXISTS (SELECT 1
                       FROM information_schema.columns
                       WHERE table_name = 'rentals' AND column_name = 'created_at') THEN
            ALTER TABLE rentals ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
        END IF;

        IF NOT EXISTS (SELECT 1
                       FROM information_schema.columns
                       WHERE table_name = 'rentals' AND column_name = 'active') THEN
            ALTER TABLE rentals ADD COLUMN active BOOLEAN DEFAULT true;
        END IF;

    END IF;
END $$;

-- Backfill travel_posts.departure_date from legacy travel_date if present
DO $$
BEGIN
    IF EXISTS (SELECT 1
               FROM information_schema.tables
               WHERE table_name = 'travel_posts')
       AND EXISTS (SELECT 1
                   FROM information_schema.columns
                   WHERE table_name = 'travel_posts' AND column_name = 'travel_date')
       AND EXISTS (SELECT 1
                   FROM information_schema.columns
                   WHERE table_name = 'travel_posts' AND column_name = 'departure_date') THEN

        UPDATE travel_posts
           SET departure_date = travel_date
         WHERE departure_date IS NULL
           AND travel_date IS NOT NULL;

    END IF;
END $$;

-- Backfill boolean flags
UPDATE classified_ads
   SET active = true
 WHERE active IS NULL;

UPDATE travel_posts
   SET active = true
 WHERE active IS NULL;

UPDATE rentals
   SET active = true
 WHERE active IS NULL;
