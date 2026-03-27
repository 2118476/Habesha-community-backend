-- Mark all existing users as email-verified so they are not locked out.
-- Only new registrations after this migration will require verification.
UPDATE users SET email_verified = true WHERE email_verified = false OR email_verified IS NULL;
