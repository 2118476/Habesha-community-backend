-- Store a cover image for each service offer directly in the database,
-- mirroring how rental/ad/home-swap photo blobs are kept (survives Render's
-- ephemeral disk). Both columns are nullable so existing rows keep working.

ALTER TABLE service_offers ADD COLUMN IF NOT EXISTS image_data BYTEA;
ALTER TABLE service_offers ADD COLUMN IF NOT EXISTS image_content_type VARCHAR(100);
