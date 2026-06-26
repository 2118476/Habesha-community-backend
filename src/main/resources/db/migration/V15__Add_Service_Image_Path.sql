-- Store the Supabase Storage public URL for a service's cover image, mirroring
-- how rental photos and profile avatars are kept on Supabase's CDN instead of
-- as Postgres blobs. Nullable so existing rows keep working; the legacy
-- image_data (BYTEA) column remains as a fallback when Supabase is disabled.

ALTER TABLE service_offers ADD COLUMN IF NOT EXISTS image_path VARCHAR(1024);
