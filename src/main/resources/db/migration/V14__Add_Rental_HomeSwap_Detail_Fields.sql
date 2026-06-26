-- ============================================================================
-- V14: Richer detail fields for rentals and home swaps so the post forms can
-- capture them and the detail pages stop showing "Not provided". All columns
-- are nullable so existing rows keep working.
-- ============================================================================

-- Rentals
ALTER TABLE rentals ADD COLUMN IF NOT EXISTS bedrooms       INTEGER;
ALTER TABLE rentals ADD COLUMN IF NOT EXISTS bathrooms      INTEGER;
ALTER TABLE rentals ADD COLUMN IF NOT EXISTS deposit        NUMERIC(12, 2);
ALTER TABLE rentals ADD COLUMN IF NOT EXISTS bills_included BOOLEAN;
ALTER TABLE rentals ADD COLUMN IF NOT EXISTS furnished      BOOLEAN;
ALTER TABLE rentals ADD COLUMN IF NOT EXISTS available_from DATE;

-- Home swap
ALTER TABLE home_swap ADD COLUMN IF NOT EXISTS home_type          VARCHAR(40);
ALTER TABLE home_swap ADD COLUMN IF NOT EXISTS bedrooms           INTEGER;
ALTER TABLE home_swap ADD COLUMN IF NOT EXISTS bathrooms          INTEGER;
ALTER TABLE home_swap ADD COLUMN IF NOT EXISTS floor_level        VARCHAR(60);
ALTER TABLE home_swap ADD COLUMN IF NOT EXISTS parking            BOOLEAN;
ALTER TABLE home_swap ADD COLUMN IF NOT EXISTS garden_or_balcony  BOOLEAN;
ALTER TABLE home_swap ADD COLUMN IF NOT EXISTS swap_window        VARCHAR(120);
ALTER TABLE home_swap ADD COLUMN IF NOT EXISTS preferred_location VARCHAR(200);
