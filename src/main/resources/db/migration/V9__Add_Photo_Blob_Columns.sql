-- Store image bytes in the database so photos survive Render's ephemeral disk.
-- Both columns are nullable so existing rows without blobs still work.

ALTER TABLE rental_photo ADD COLUMN IF NOT EXISTS image_data BYTEA;
ALTER TABLE rental_photo ADD COLUMN IF NOT EXISTS content_type VARCHAR(100);

ALTER TABLE home_swap_photo ADD COLUMN IF NOT EXISTS image_data BYTEA;
-- home_swap_photo already has content_type column, skip if exists

ALTER TABLE ad_photo ADD COLUMN IF NOT EXISTS image_data BYTEA;
ALTER TABLE ad_photo ADD COLUMN IF NOT EXISTS content_type VARCHAR(100);
