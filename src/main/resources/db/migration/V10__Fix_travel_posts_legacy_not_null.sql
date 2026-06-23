-- ============================================================================
-- V10: Fix travel post creation (every INSERT was failing).
--
-- The original schema (V3) created travel_posts with a NOT NULL "title" column
-- (plus other legacy trip-style columns). The current TravelPost entity models
-- a route instead (origin_city / destination_city / travel_date / message /
-- contact_method) and NEVER sets "title" — so every insert failed with a
-- not-null violation, leaving the table empty while reads still worked.
--
-- Make the legacy, entity-unmanaged columns optional so inserts succeed. The
-- block is idempotent and safe: it only touches columns that actually exist,
-- and DROP NOT NULL on an already-nullable column is a no-op.
-- ============================================================================
DO $$
DECLARE
    col TEXT;
    legacy_cols TEXT[] := ARRAY[
        'title', 'description', 'departure_location', 'destination',
        'departure_date', 'return_date', 'price', 'available_seats'
    ];
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables
               WHERE table_name = 'travel_posts') THEN
        FOREACH col IN ARRAY legacy_cols LOOP
            IF EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_name = 'travel_posts' AND column_name = col) THEN
                EXECUTE format('ALTER TABLE travel_posts ALTER COLUMN %I DROP NOT NULL', col);
            END IF;
        END LOOP;
    END IF;
END $$;
