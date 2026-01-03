-- Safe schema alignment migration
-- This migration ensures schema matches entity definitions without breaking existing data
-- It's designed to work with databases that may have been created by Hibernate ddl-auto

-- First, ensure all required columns exist in existing tables
-- Add missing columns to classified_ads if they don't exist
DO $$
BEGIN
    -- Ensure poster_id column exists (ClassifiedAd uses poster_id, not user_id)
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'classified_ads' AND column_name = 'poster_id') THEN
        -- If user_id exists, rename it to poster_id
        IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'classified_ads' AND column_name = 'user_id') THEN
            ALTER TABLE classified_ads RENAME COLUMN user_id TO poster_id;
        ELSE
            -- Add poster_id column if neither exists
            ALTER TABLE classified_ads ADD COLUMN poster_id BIGINT;
        END IF;
    END IF;
    
    -- Ensure other required columns exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'classified_ads' AND column_name = 'contact_info') THEN
        ALTER TABLE classified_ads ADD COLUMN contact_info VARCHAR(500);
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'classified_ads' AND column_name = 'featured') THEN
        ALTER TABLE classified_ads ADD COLUMN featured BOOLEAN DEFAULT false;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'classified_ads' AND column_name = 'active') THEN
        ALTER TABLE classified_ads ADD COLUMN active BOOLEAN DEFAULT true;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'classified_ads' AND column_name = 'updated_at') THEN
        ALTER TABLE classified_ads ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
    END IF;
END $$;

-- Fix ad_comments table to use author_id instead of user_id
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'ad_comments') THEN
        -- Ensure author_id column exists
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'ad_comments' AND column_name = 'author_id') THEN
            -- If user_id exists, rename it to author_id
            IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'ad_comments' AND column_name = 'user_id') THEN
                ALTER TABLE ad_comments RENAME COLUMN user_id TO author_id;
            ELSE
                -- Add author_id column if neither exists
                ALTER TABLE ad_comments ADD COLUMN author_id BIGINT NOT NULL DEFAULT 1;
            END IF;
        END IF;
        
        -- Ensure other required columns exist
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'ad_comments' AND column_name = 'updated_at') THEN
            ALTER TABLE ad_comments ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
        END IF;
    END IF;
END $$;

-- Fix rentals table to use owner_id instead of user_id
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'rentals') THEN
        -- Ensure owner_id column exists
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'rentals' AND column_name = 'owner_id') THEN
            -- If user_id exists, rename it to owner_id
            IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'rentals' AND column_name = 'user_id') THEN
                ALTER TABLE rentals RENAME COLUMN user_id TO owner_id;
            ELSE
                -- Add owner_id column if neither exists
                ALTER TABLE rentals ADD COLUMN owner_id BIGINT;
            END IF;
        END IF;
        
        -- Ensure other required columns exist
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'rentals' AND column_name = 'price_per_month') THEN
            ALTER TABLE rentals ADD COLUMN price_per_month DECIMAL(10,2);
        END IF;
        
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'rentals' AND column_name = 'bedrooms') THEN
            ALTER TABLE rentals ADD COLUMN bedrooms INTEGER;
        END IF;
        
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'rentals' AND column_name = 'bathrooms') THEN
            ALTER TABLE rentals ADD COLUMN bathrooms INTEGER;
        END IF;
        
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'rentals' AND column_name = 'property_type') THEN
            ALTER TABLE rentals ADD COLUMN property_type VARCHAR(100);
        END IF;
        
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'rentals' AND column_name = 'available_from') THEN
            ALTER TABLE rentals ADD COLUMN available_from DATE;
        END IF;
        
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'rentals' AND column_name = 'active') THEN
            ALTER TABLE rentals ADD COLUMN active BOOLEAN DEFAULT true;
        END IF;
        
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'rentals' AND column_name = 'updated_at') THEN
            ALTER TABLE rentals ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
        END IF;
    END IF;
END $$;

-- Fix events table to use organizer_id instead of user_id
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'events') THEN
        -- Ensure organizer_id column exists
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'events' AND column_name = 'organizer_id') THEN
            -- If user_id exists, rename it to organizer_id
            IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'events' AND column_name = 'user_id') THEN
                ALTER TABLE events RENAME COLUMN user_id TO organizer_id;
            ELSE
                -- Add organizer_id column if neither exists
                ALTER TABLE events ADD COLUMN organizer_id BIGINT;
            END IF;
        END IF;
        
        -- Ensure other required columns exist
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'events' AND column_name = 'end_date') THEN
            ALTER TABLE events ADD COLUMN end_date TIMESTAMP;
        END IF;
        
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'events' AND column_name = 'max_attendees') THEN
            ALTER TABLE events ADD COLUMN max_attendees INTEGER;
        END IF;
        
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'events' AND column_name = 'featured') THEN
            ALTER TABLE events ADD COLUMN featured BOOLEAN DEFAULT false;
        END IF;
        
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'events' AND column_name = 'active') THEN
            ALTER TABLE events ADD COLUMN active BOOLEAN DEFAULT true;
        END IF;
        
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'events' AND column_name = 'updated_at') THEN
            ALTER TABLE events ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
        END IF;
    END IF;
END $$;

-- Fix service_offers table to use provider_id instead of user_id
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'service_offers') THEN
        -- Ensure provider_id column exists
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'service_offers' AND column_name = 'provider_id') THEN
            -- If user_id exists, rename it to provider_id
            IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'service_offers' AND column_name = 'user_id') THEN
                ALTER TABLE service_offers RENAME COLUMN user_id TO provider_id;
            ELSE
                -- Add provider_id column if neither exists
                ALTER TABLE service_offers ADD COLUMN provider_id BIGINT;
            END IF;
        END IF;
        
        -- Ensure other required columns exist
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'service_offers' AND column_name = 'delivery_type') THEN
            ALTER TABLE service_offers ADD COLUMN delivery_type VARCHAR(50);
        END IF;
        
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'service_offers' AND column_name = 'active') THEN
            ALTER TABLE service_offers ADD COLUMN active BOOLEAN DEFAULT true;
        END IF;
        
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'service_offers' AND column_name = 'updated_at') THEN
            ALTER TABLE service_offers ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
        END IF;
    END IF;
END $$;

-- Fix service_bookings table to use service_id instead of service_offer_id
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'service_bookings') THEN
        -- Ensure service_id column exists
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'service_bookings' AND column_name = 'service_id') THEN
            -- If service_offer_id exists, rename it to service_id
            IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'service_bookings' AND column_name = 'service_offer_id') THEN
                ALTER TABLE service_bookings RENAME COLUMN service_offer_id TO service_id;
            ELSE
                -- Add service_id column if neither exists
                ALTER TABLE service_bookings ADD COLUMN service_id BIGINT NOT NULL DEFAULT 1;
            END IF;
        END IF;
        
        -- Ensure other required columns exist
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'service_bookings' AND column_name = 'booking_date') THEN
            ALTER TABLE service_bookings ADD COLUMN booking_date TIMESTAMP;
        END IF;
        
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'service_bookings' AND column_name = 'status') THEN
            ALTER TABLE service_bookings ADD COLUMN status VARCHAR(50) DEFAULT 'PENDING';
        END IF;
        
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'service_bookings' AND column_name = 'notes') THEN
            ALTER TABLE service_bookings ADD COLUMN notes TEXT;
        END IF;
        
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'service_bookings' AND column_name = 'total_amount') THEN
            ALTER TABLE service_bookings ADD COLUMN total_amount DECIMAL(10,2);
        END IF;
        
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'service_bookings' AND column_name = 'updated_at') THEN
            ALTER TABLE service_bookings ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
        END IF;
    END IF;
END $$;

-- Fix payments table to use payer_id instead of user_id
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'payments') THEN
        -- Ensure payer_id column exists
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'payments' AND column_name = 'payer_id') THEN
            -- If user_id exists, rename it to payer_id
            IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'payments' AND column_name = 'user_id') THEN
                ALTER TABLE payments RENAME COLUMN user_id TO payer_id;
            ELSE
                -- Add payer_id column if neither exists
                ALTER TABLE payments ADD COLUMN payer_id BIGINT NOT NULL DEFAULT 1;
            END IF;
        END IF;
        
        -- Ensure other required columns exist
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'payments' AND column_name = 'stripe_payment_intent_id') THEN
            ALTER TABLE payments ADD COLUMN stripe_payment_intent_id VARCHAR(255);
        END IF;
        
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'payments' AND column_name = 'currency') THEN
            ALTER TABLE payments ADD COLUMN currency VARCHAR(3) DEFAULT 'GBP';
        END IF;
        
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'payments' AND column_name = 'status') THEN
            ALTER TABLE payments ADD COLUMN status VARCHAR(50) DEFAULT 'PENDING';
        END IF;
        
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'payments' AND column_name = 'payment_type') THEN
            ALTER TABLE payments ADD COLUMN payment_type VARCHAR(100);
        END IF;
        
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'payments' AND column_name = 'reference_id') THEN
            ALTER TABLE payments ADD COLUMN reference_id BIGINT;
        END IF;
        
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'payments' AND column_name = 'reference_type') THEN
            ALTER TABLE payments ADD COLUMN reference_type VARCHAR(100);
        END IF;
        
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'payments' AND column_name = 'updated_at') THEN
            ALTER TABLE payments ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
        END IF;
    END IF;
END $$;

-- Fix messages table to use recipient_id instead of receiver_id
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'messages') THEN
        -- Ensure recipient_id column exists
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'messages' AND column_name = 'recipient_id') THEN
            -- If receiver_id exists, rename it to recipient_id
            IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'messages' AND column_name = 'receiver_id') THEN
                ALTER TABLE messages RENAME COLUMN receiver_id TO recipient_id;
            ELSE
                -- Add recipient_id column if neither exists
                ALTER TABLE messages ADD COLUMN recipient_id BIGINT NOT NULL DEFAULT 1;
            END IF;
        END IF;
        
        -- Ensure other required columns exist
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'messages' AND column_name = 'message_type') THEN
            ALTER TABLE messages ADD COLUMN message_type VARCHAR(50) DEFAULT 'TEXT';
        END IF;
        
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'messages' AND column_name = 'read_at') THEN
            ALTER TABLE messages ADD COLUMN read_at TIMESTAMP;
        END IF;
    END IF;
END $$;

-- Fix user_report table column names
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'user_report') THEN
        -- Ensure target_id column exists (not reported_user_id)
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'user_report' AND column_name = 'target_id') THEN
            -- If reported_user_id exists, rename it to target_id
            IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'user_report' AND column_name = 'reported_user_id') THEN
                ALTER TABLE user_report RENAME COLUMN reported_user_id TO target_id;
            ELSE
                -- Add target_id column if neither exists
                ALTER TABLE user_report ADD COLUMN target_id BIGINT NOT NULL DEFAULT 1;
            END IF;
        END IF;
    END IF;
END $$;

-- Fix user_block table column names
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'user_block') THEN
        -- Ensure blocked_id column exists (not blocked_user_id)
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'user_block' AND column_name = 'blocked_id') THEN
            -- If blocked_user_id exists, rename it to blocked_id
            IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'user_block' AND column_name = 'blocked_user_id') THEN
                ALTER TABLE user_block RENAME COLUMN blocked_user_id TO blocked_id;
            ELSE
                -- Add blocked_id column if neither exists
                ALTER TABLE user_block ADD COLUMN blocked_id BIGINT NOT NULL DEFAULT 1;
            END IF;
        END IF;
    END IF;
END $$;

-- Now create SAFE indexes that only run if the columns actually exist
-- This prevents the "column does not exist" error that was causing the migration to fail

-- Classified Ads indexes (uses poster_id)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'classified_ads' AND column_name = 'poster_id') THEN
        CREATE INDEX IF NOT EXISTS idx_classified_ads_poster_id ON classified_ads(poster_id);
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'classified_ads' AND column_name = 'category') THEN
        CREATE INDEX IF NOT EXISTS idx_classified_ads_category ON classified_ads(category);
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'classified_ads' AND column_name = 'active') THEN
        CREATE INDEX IF NOT EXISTS idx_classified_ads_active ON classified_ads(active);
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'classified_ads' AND column_name = 'featured') THEN
        CREATE INDEX IF NOT EXISTS idx_classified_ads_featured ON classified_ads(featured);
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'classified_ads' AND column_name = 'created_at') THEN
        CREATE INDEX IF NOT EXISTS idx_classified_ads_created_at ON classified_ads(created_at);
    END IF;
END $$;

-- Other table indexes with column existence checks
DO $$
BEGIN
    -- Ad Comments indexes (uses author_id)
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'ad_comments' AND column_name = 'ad_id') THEN
        CREATE INDEX IF NOT EXISTS idx_ad_comments_ad_id ON ad_comments(ad_id);
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'ad_comments' AND column_name = 'author_id') THEN
        CREATE INDEX IF NOT EXISTS idx_ad_comments_author_id ON ad_comments(author_id);
    END IF;
    
    -- Rentals indexes (uses owner_id)
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'rentals' AND column_name = 'owner_id') THEN
        CREATE INDEX IF NOT EXISTS idx_rentals_owner_id ON rentals(owner_id);
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'rentals' AND column_name = 'location') THEN
        CREATE INDEX IF NOT EXISTS idx_rentals_location ON rentals(location);
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'rentals' AND column_name = 'active') THEN
        CREATE INDEX IF NOT EXISTS idx_rentals_active ON rentals(active);
    END IF;
    
    -- Events indexes (uses organizer_id)
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'events' AND column_name = 'organizer_id') THEN
        CREATE INDEX IF NOT EXISTS idx_events_organizer_id ON events(organizer_id);
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'events' AND column_name = 'event_date') THEN
        CREATE INDEX IF NOT EXISTS idx_events_event_date ON events(event_date);
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'events' AND column_name = 'active') THEN
        CREATE INDEX IF NOT EXISTS idx_events_active ON events(active);
    END IF;
    
    -- Service Offers indexes (uses provider_id)
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'service_offers' AND column_name = 'provider_id') THEN
        CREATE INDEX IF NOT EXISTS idx_service_offers_provider_id ON service_offers(provider_id);
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'service_offers' AND column_name = 'category') THEN
        CREATE INDEX IF NOT EXISTS idx_service_offers_category ON service_offers(category);
    END IF;
    
    -- Payments indexes (uses payer_id)
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'payments' AND column_name = 'payer_id') THEN
        CREATE INDEX IF NOT EXISTS idx_payments_payer_id ON payments(payer_id);
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'payments' AND column_name = 'status') THEN
        CREATE INDEX IF NOT EXISTS idx_payments_status ON payments(status);
    END IF;
    
    -- Travel Posts indexes
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'travel_posts' AND column_name = 'user_id') THEN
        CREATE INDEX IF NOT EXISTS idx_travel_posts_user_id ON travel_posts(user_id);
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'travel_posts' AND column_name = 'departure_date') THEN
        CREATE INDEX IF NOT EXISTS idx_travel_posts_departure_date ON travel_posts(departure_date);
    END IF;
    
    -- Home Swap indexes
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'home_swap' AND column_name = 'user_id') THEN
        CREATE INDEX IF NOT EXISTS idx_home_swap_user_id ON home_swap(user_id);
    END IF;
    
    -- Ad Likes indexes
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'ad_likes' AND column_name = 'ad_id') THEN
        CREATE INDEX IF NOT EXISTS idx_ad_likes_ad_id ON ad_likes(ad_id);
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'ad_likes' AND column_name = 'user_id') THEN
        CREATE INDEX IF NOT EXISTS idx_ad_likes_user_id ON ad_likes(user_id);
    END IF;
    
    -- Friend Requests indexes
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'friend_requests' AND column_name = 'sender_id') THEN
        CREATE INDEX IF NOT EXISTS idx_friend_requests_sender_id ON friend_requests(sender_id);
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'friend_requests' AND column_name = 'receiver_id') THEN
        CREATE INDEX IF NOT EXISTS idx_friend_requests_receiver_id ON friend_requests(receiver_id);
    END IF;
    
    -- Messages indexes (uses recipient_id)
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'messages' AND column_name = 'sender_id') THEN
        CREATE INDEX IF NOT EXISTS idx_messages_sender_id ON messages(sender_id);
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'messages' AND column_name = 'recipient_id') THEN
        CREATE INDEX IF NOT EXISTS idx_messages_recipient_id ON messages(recipient_id);
    END IF;
    
    -- User Sessions indexes
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'user_sessions' AND column_name = 'user_id') THEN
        CREATE INDEX IF NOT EXISTS idx_user_sessions_user_id ON user_sessions(user_id);
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'user_sessions' AND column_name = 'expires_at') THEN
        CREATE INDEX IF NOT EXISTS idx_user_sessions_expires_at ON user_sessions(expires_at);
    END IF;
    
    -- Password Reset Tokens indexes
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'password_reset_tokens' AND column_name = 'user_id') THEN
        CREATE INDEX IF NOT EXISTS idx_password_reset_tokens_user_id ON password_reset_tokens(user_id);
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'password_reset_tokens' AND column_name = 'expires_at') THEN
        CREATE INDEX IF NOT EXISTS idx_password_reset_tokens_expires_at ON password_reset_tokens(expires_at);
    END IF;
END $$;