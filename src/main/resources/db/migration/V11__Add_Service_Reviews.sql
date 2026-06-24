-- ============================================================================
-- V11: Service reviews.
-- A user reviews a service provider (one review per reviewer per provider).
-- Eligibility (genuine two-way chat) is enforced in the application layer.
-- ============================================================================
CREATE TABLE IF NOT EXISTS service_reviews (
    id          BIGSERIAL PRIMARY KEY,
    provider_id BIGINT      NOT NULL,
    reviewer_id BIGINT      NOT NULL,
    rating      INTEGER     NOT NULL,
    comment     VARCHAR(2000),
    created_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP,
    CONSTRAINT uq_service_review_provider_reviewer UNIQUE (provider_id, reviewer_id)
);

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints
                   WHERE constraint_name = 'fk_service_reviews_provider'
                     AND table_name = 'service_reviews') THEN
        ALTER TABLE service_reviews ADD CONSTRAINT fk_service_reviews_provider
            FOREIGN KEY (provider_id) REFERENCES users(id) ON DELETE CASCADE;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints
                   WHERE constraint_name = 'fk_service_reviews_reviewer'
                     AND table_name = 'service_reviews') THEN
        ALTER TABLE service_reviews ADD CONSTRAINT fk_service_reviews_reviewer
            FOREIGN KEY (reviewer_id) REFERENCES users(id) ON DELETE CASCADE;
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_service_reviews_provider ON service_reviews(provider_id);
