-- ============================================================================
-- V13: Device push tokens for Firebase Cloud Messaging (FCM).
-- One row per device/browser registered to receive push notifications for a
-- user. Tokens are unique; a token re-registering just updates its owner.
-- ============================================================================
CREATE TABLE IF NOT EXISTS device_tokens (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT       NOT NULL,
    token       VARCHAR(512) NOT NULL,
    platform    VARCHAR(20),
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP,
    CONSTRAINT uq_device_tokens_token UNIQUE (token)
);

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints
                   WHERE constraint_name = 'fk_device_tokens_user'
                     AND table_name = 'device_tokens') THEN
        ALTER TABLE device_tokens ADD CONSTRAINT fk_device_tokens_user
            FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_device_tokens_user ON device_tokens(user_id);
