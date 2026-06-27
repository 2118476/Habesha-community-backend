-- Trust & Safety / moderation console support.

-- 1) Make reports generic: a report can target a USER or a piece of content
--    (RENTAL, SERVICE, EVENT, AD, TRAVEL, HOMESWAP, REVIEW). target_id still
--    holds the *owner* user so moderators can act on the person too.
ALTER TABLE user_report ADD COLUMN IF NOT EXISTS content_type VARCHAR(32);
ALTER TABLE user_report ADD COLUMN IF NOT EXISTS content_id   BIGINT;
UPDATE user_report SET content_type = 'USER' WHERE content_type IS NULL;
UPDATE user_report SET content_id = target_id WHERE content_id IS NULL;

-- 2) Reason shown to a suspended/banned user (and kept for the record).
ALTER TABLE users ADD COLUMN IF NOT EXISTS suspension_reason VARCHAR(500);

-- 3) Audit log: every admin/moderator action, for accountability.
CREATE TABLE IF NOT EXISTS audit_logs (
    id          BIGSERIAL PRIMARY KEY,
    actor_id    BIGINT,
    actor_name  VARCHAR(255),
    action      VARCHAR(64)  NOT NULL,
    target_type VARCHAR(48),
    target_id   BIGINT,
    detail      VARCHAR(2000),
    created_at  TIMESTAMP    NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_audit_logs_created_at ON audit_logs (created_at DESC);
