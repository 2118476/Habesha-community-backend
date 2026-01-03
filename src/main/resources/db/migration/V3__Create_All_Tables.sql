-- Create all required tables for Habesha Community Backend
-- This migration is SAFE for existing databases - checks column existence before creating indexes

-- Classified Ads table (uses poster_id, not user_id)
CREATE TABLE IF NOT EXISTS classified_ads (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2),
    category VARCHAR(100),
    location VARCHAR(255),
    contact_info VARCHAR(500),
    poster_id BIGINT,
    featured BOOLEAN DEFAULT false,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add foreign key constraint if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_classified_ads_poster' 
        AND table_name = 'classified_ads'
    ) THEN
        ALTER TABLE classified_ads ADD CONSTRAINT fk_classified_ads_poster 
        FOREIGN KEY (poster_id) REFERENCES users(id) ON DELETE CASCADE;
    END IF;
END $$;

-- Ad Photos table
CREATE TABLE IF NOT EXISTS ad_photo (
    id BIGSERIAL PRIMARY KEY,
    ad_id BIGINT NOT NULL,
    filename VARCHAR(255),
    path VARCHAR(500),
    content_type VARCHAR(100),
    file_size BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add foreign key constraint if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_ad_photo_ad' 
        AND table_name = 'ad_photo'
    ) THEN
        ALTER TABLE ad_photo ADD CONSTRAINT fk_ad_photo_ad 
        FOREIGN KEY (ad_id) REFERENCES classified_ads(id) ON DELETE CASCADE;
    END IF;
END $$;

-- Ad Comments table (uses author_id, not user_id)
CREATE TABLE IF NOT EXISTS ad_comments (
    id BIGSERIAL PRIMARY KEY,
    ad_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    parent_id BIGINT,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add foreign key constraints if they don't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_ad_comments_ad' 
        AND table_name = 'ad_comments'
    ) THEN
        ALTER TABLE ad_comments ADD CONSTRAINT fk_ad_comments_ad 
        FOREIGN KEY (ad_id) REFERENCES classified_ads(id) ON DELETE CASCADE;
    END IF;
    
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_ad_comments_author' 
        AND table_name = 'ad_comments'
    ) THEN
        ALTER TABLE ad_comments ADD CONSTRAINT fk_ad_comments_author 
        FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE;
    END IF;
    
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_ad_comments_parent' 
        AND table_name = 'ad_comments'
    ) THEN
        ALTER TABLE ad_comments ADD CONSTRAINT fk_ad_comments_parent 
        FOREIGN KEY (parent_id) REFERENCES ad_comments(id) ON DELETE CASCADE;
    END IF;
END $$;

-- Ad Likes table
CREATE TABLE IF NOT EXISTS ad_likes (
    id BIGSERIAL PRIMARY KEY,
    ad_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(ad_id, user_id)
);

-- Add foreign key constraints if they don't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_ad_likes_ad' 
        AND table_name = 'ad_likes'
    ) THEN
        ALTER TABLE ad_likes ADD CONSTRAINT fk_ad_likes_ad 
        FOREIGN KEY (ad_id) REFERENCES classified_ads(id) ON DELETE CASCADE;
    END IF;
    
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_ad_likes_user' 
        AND table_name = 'ad_likes'
    ) THEN
        ALTER TABLE ad_likes ADD CONSTRAINT fk_ad_likes_user 
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
    END IF;
END $$;

-- Travel Posts table
CREATE TABLE IF NOT EXISTS travel_posts (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    departure_location VARCHAR(255),
    destination VARCHAR(255),
    departure_date DATE,
    return_date DATE,
    price DECIMAL(10,2),
    available_seats INTEGER,
    user_id BIGINT,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add foreign key constraint if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_travel_posts_user' 
        AND table_name = 'travel_posts'
    ) THEN
        ALTER TABLE travel_posts ADD CONSTRAINT fk_travel_posts_user 
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
    END IF;
END $$;

-- Rentals table (uses owner_id, not user_id)
CREATE TABLE IF NOT EXISTS rentals (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    location VARCHAR(255),
    price_per_month DECIMAL(10,2),
    bedrooms INTEGER,
    bathrooms INTEGER,
    property_type VARCHAR(100),
    available_from DATE,
    owner_id BIGINT,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add foreign key constraint if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_rentals_owner' 
        AND table_name = 'rentals'
    ) THEN
        ALTER TABLE rentals ADD CONSTRAINT fk_rentals_owner 
        FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE;
    END IF;
END $$;

-- Rental Photos table
CREATE TABLE IF NOT EXISTS rental_photo (
    id BIGSERIAL PRIMARY KEY,
    rental_id BIGINT NOT NULL,
    filename VARCHAR(255),
    path VARCHAR(500),
    content_type VARCHAR(100),
    file_size BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add foreign key constraint if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_rental_photo_rental' 
        AND table_name = 'rental_photo'
    ) THEN
        ALTER TABLE rental_photo ADD CONSTRAINT fk_rental_photo_rental 
        FOREIGN KEY (rental_id) REFERENCES rentals(id) ON DELETE CASCADE;
    END IF;
END $$;

-- Home Swap table
CREATE TABLE IF NOT EXISTS home_swap (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    location VARCHAR(255),
    available_from DATE,
    available_to DATE,
    property_type VARCHAR(100),
    bedrooms INTEGER,
    bathrooms INTEGER,
    user_id BIGINT,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add foreign key constraint if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_home_swap_user' 
        AND table_name = 'home_swap'
    ) THEN
        ALTER TABLE home_swap ADD CONSTRAINT fk_home_swap_user 
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
    END IF;
END $$;

-- Home Swap Photos table
CREATE TABLE IF NOT EXISTS home_swap_photo (
    id BIGSERIAL PRIMARY KEY,
    home_swap_id BIGINT NOT NULL,
    filename VARCHAR(255),
    path VARCHAR(500),
    content_type VARCHAR(100),
    file_size BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add foreign key constraint if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_home_swap_photo_home_swap' 
        AND table_name = 'home_swap_photo'
    ) THEN
        ALTER TABLE home_swap_photo ADD CONSTRAINT fk_home_swap_photo_home_swap 
        FOREIGN KEY (home_swap_id) REFERENCES home_swap(id) ON DELETE CASCADE;
    END IF;
END $$;

-- Service Offers table (uses provider_id, not user_id)
CREATE TABLE IF NOT EXISTS service_offers (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(100),
    price DECIMAL(10,2),
    location VARCHAR(255),
    delivery_type VARCHAR(50), -- 'IN_PERSON', 'ONLINE', 'BOTH'
    provider_id BIGINT,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add foreign key constraint if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_service_offers_provider' 
        AND table_name = 'service_offers'
    ) THEN
        ALTER TABLE service_offers ADD CONSTRAINT fk_service_offers_provider 
        FOREIGN KEY (provider_id) REFERENCES users(id) ON DELETE CASCADE;
    END IF;
END $$;

-- Service Bookings table (uses service_id and customer_id)
CREATE TABLE IF NOT EXISTS service_bookings (
    id BIGSERIAL PRIMARY KEY,
    service_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    booking_date TIMESTAMP,
    status VARCHAR(50) DEFAULT 'PENDING',
    notes TEXT,
    total_amount DECIMAL(10,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add foreign key constraints if they don't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_service_bookings_service' 
        AND table_name = 'service_bookings'
    ) THEN
        ALTER TABLE service_bookings ADD CONSTRAINT fk_service_bookings_service 
        FOREIGN KEY (service_id) REFERENCES service_offers(id) ON DELETE CASCADE;
    END IF;
    
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_service_bookings_customer' 
        AND table_name = 'service_bookings'
    ) THEN
        ALTER TABLE service_bookings ADD CONSTRAINT fk_service_bookings_customer 
        FOREIGN KEY (customer_id) REFERENCES users(id) ON DELETE CASCADE;
    END IF;
END $$;

-- Events table (uses organizer_id, not user_id)
CREATE TABLE IF NOT EXISTS events (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    location VARCHAR(255),
    event_date TIMESTAMP,
    end_date TIMESTAMP,
    price DECIMAL(10,2),
    max_attendees INTEGER,
    category VARCHAR(100),
    organizer_id BIGINT,
    featured BOOLEAN DEFAULT false,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add foreign key constraint if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_events_organizer' 
        AND table_name = 'events'
    ) THEN
        ALTER TABLE events ADD CONSTRAINT fk_events_organizer 
        FOREIGN KEY (organizer_id) REFERENCES users(id) ON DELETE CASCADE;
    END IF;
END $$;

-- Friend Requests table
CREATE TABLE IF NOT EXISTS friend_requests (
    id BIGSERIAL PRIMARY KEY,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING',
    message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(sender_id, receiver_id)
);

-- Add foreign key constraints if they don't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_friend_requests_sender' 
        AND table_name = 'friend_requests'
    ) THEN
        ALTER TABLE friend_requests ADD CONSTRAINT fk_friend_requests_sender 
        FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE;
    END IF;
    
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_friend_requests_receiver' 
        AND table_name = 'friend_requests'
    ) THEN
        ALTER TABLE friend_requests ADD CONSTRAINT fk_friend_requests_receiver 
        FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE;
    END IF;
END $$;

-- Messages table (uses sender_id and recipient_id)
CREATE TABLE IF NOT EXISTS messages (
    id BIGSERIAL PRIMARY KEY,
    sender_id BIGINT NOT NULL,
    recipient_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    message_type VARCHAR(50) DEFAULT 'TEXT',
    read_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add foreign key constraints if they don't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_messages_sender' 
        AND table_name = 'messages'
    ) THEN
        ALTER TABLE messages ADD CONSTRAINT fk_messages_sender 
        FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE;
    END IF;
    
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_messages_recipient' 
        AND table_name = 'messages'
    ) THEN
        ALTER TABLE messages ADD CONSTRAINT fk_messages_recipient 
        FOREIGN KEY (recipient_id) REFERENCES users(id) ON DELETE CASCADE;
    END IF;
END $$;

-- User Sessions table
CREATE TABLE IF NOT EXISTS user_sessions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    session_token VARCHAR(500) UNIQUE NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_accessed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add foreign key constraint if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_user_sessions_user' 
        AND table_name = 'user_sessions'
    ) THEN
        ALTER TABLE user_sessions ADD CONSTRAINT fk_user_sessions_user 
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
    END IF;
END $$;

-- User Reports table (uses reporter_id and target_id)
CREATE TABLE IF NOT EXISTS user_report (
    id BIGSERIAL PRIMARY KEY,
    reporter_id BIGINT NOT NULL,
    target_id BIGINT NOT NULL,
    reason VARCHAR(255),
    description TEXT,
    status VARCHAR(50) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add foreign key constraints if they don't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_user_report_reporter' 
        AND table_name = 'user_report'
    ) THEN
        ALTER TABLE user_report ADD CONSTRAINT fk_user_report_reporter 
        FOREIGN KEY (reporter_id) REFERENCES users(id) ON DELETE CASCADE;
    END IF;
    
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_user_report_target' 
        AND table_name = 'user_report'
    ) THEN
        ALTER TABLE user_report ADD CONSTRAINT fk_user_report_target 
        FOREIGN KEY (target_id) REFERENCES users(id) ON DELETE CASCADE;
    END IF;
END $$;

-- User Blocks table (uses blocker_id and blocked_id)
CREATE TABLE IF NOT EXISTS user_block (
    id BIGSERIAL PRIMARY KEY,
    blocker_id BIGINT NOT NULL,
    blocked_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(blocker_id, blocked_id)
);

-- Add foreign key constraints if they don't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_user_block_blocker' 
        AND table_name = 'user_block'
    ) THEN
        ALTER TABLE user_block ADD CONSTRAINT fk_user_block_blocker 
        FOREIGN KEY (blocker_id) REFERENCES users(id) ON DELETE CASCADE;
    END IF;
    
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_user_block_blocked' 
        AND table_name = 'user_block'
    ) THEN
        ALTER TABLE user_block ADD CONSTRAINT fk_user_block_blocked 
        FOREIGN KEY (blocked_id) REFERENCES users(id) ON DELETE CASCADE;
    END IF;
END $$;

-- Contact Requests table
CREATE TABLE IF NOT EXISTS contact_request (
    id BIGSERIAL PRIMARY KEY,
    requester_id BIGINT NOT NULL,
    target_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING',
    message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(requester_id, target_id, type, status)
);

-- Add foreign key constraints if they don't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_contact_request_requester' 
        AND table_name = 'contact_request'
    ) THEN
        ALTER TABLE contact_request ADD CONSTRAINT fk_contact_request_requester 
        FOREIGN KEY (requester_id) REFERENCES users(id) ON DELETE CASCADE;
    END IF;
    
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_contact_request_target' 
        AND table_name = 'contact_request'
    ) THEN
        ALTER TABLE contact_request ADD CONSTRAINT fk_contact_request_target 
        FOREIGN KEY (target_id) REFERENCES users(id) ON DELETE CASCADE;
    END IF;
END $$;

-- Payments table (uses payer_id, not user_id)
CREATE TABLE IF NOT EXISTS payments (
    id BIGSERIAL PRIMARY KEY,
    payer_id BIGINT NOT NULL,
    stripe_payment_intent_id VARCHAR(255),
    amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'GBP',
    status VARCHAR(50) DEFAULT 'PENDING',
    payment_type VARCHAR(100),
    reference_id BIGINT,
    reference_type VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add foreign key constraint if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_payments_payer' 
        AND table_name = 'payments'
    ) THEN
        ALTER TABLE payments ADD CONSTRAINT fk_payments_payer 
        FOREIGN KEY (payer_id) REFERENCES users(id) ON DELETE CASCADE;
    END IF;
END $$;

-- Password Reset Tokens table
CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(500) UNIQUE NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add foreign key constraint if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_password_reset_tokens_user' 
        AND table_name = 'password_reset_tokens'
    ) THEN
        ALTER TABLE password_reset_tokens ADD CONSTRAINT fk_password_reset_tokens_user 
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
    END IF;
END $$;

-- Account Deletion Requests table
CREATE TABLE IF NOT EXISTS account_deletion_requests (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    reason TEXT,
    status VARCHAR(50) DEFAULT 'PENDING',
    scheduled_deletion_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add foreign key constraint if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_account_deletion_requests_user' 
        AND table_name = 'account_deletion_requests'
    ) THEN
        ALTER TABLE account_deletion_requests ADD CONSTRAINT fk_account_deletion_requests_user 
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
    END IF;
END $$;

-- SAFE INDEX CREATION - Only create indexes if columns exist
-- This prevents the "column does not exist" error

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
END $$;

-- Ad Photo indexes
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'ad_photo' AND column_name = 'ad_id') THEN
        CREATE INDEX IF NOT EXISTS idx_ad_photo_ad_id ON ad_photo(ad_id);
    END IF;
END $$;

-- Ad Comments indexes (uses author_id)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'ad_comments' AND column_name = 'ad_id') THEN
        CREATE INDEX IF NOT EXISTS idx_ad_comments_ad_id ON ad_comments(ad_id);
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'ad_comments' AND column_name = 'author_id') THEN
        CREATE INDEX IF NOT EXISTS idx_ad_comments_author_id ON ad_comments(author_id);
    END IF;
END $$;

-- Ad Likes indexes
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'ad_likes' AND column_name = 'ad_id') THEN
        CREATE INDEX IF NOT EXISTS idx_ad_likes_ad_id ON ad_likes(ad_id);
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'ad_likes' AND column_name = 'user_id') THEN
        CREATE INDEX IF NOT EXISTS idx_ad_likes_user_id ON ad_likes(user_id);
    END IF;
END $$;

-- Travel Posts indexes
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'travel_posts' AND column_name = 'user_id') THEN
        CREATE INDEX IF NOT EXISTS idx_travel_posts_user_id ON travel_posts(user_id);
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'travel_posts' AND column_name = 'departure_date') THEN
        CREATE INDEX IF NOT EXISTS idx_travel_posts_departure_date ON travel_posts(departure_date);
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'travel_posts' AND column_name = 'active') THEN
        CREATE INDEX IF NOT EXISTS idx_travel_posts_active ON travel_posts(active);
    END IF;
END $$;

-- Rentals indexes (uses owner_id)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'rentals' AND column_name = 'owner_id') THEN
        CREATE INDEX IF NOT EXISTS idx_rentals_owner_id ON rentals(owner_id);
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'rentals' AND column_name = 'location') THEN
        CREATE INDEX IF NOT EXISTS idx_rentals_location ON rentals(location);
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'rentals' AND column_name = 'active') THEN
        CREATE INDEX IF NOT EXISTS idx_rentals_active ON rentals(active);
    END IF;
END $$;

-- Rental Photo indexes
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'rental_photo' AND column_name = 'rental_id') THEN
        CREATE INDEX IF NOT EXISTS idx_rental_photo_rental_id ON rental_photo(rental_id);
    END IF;
END $$;

-- Home Swap indexes
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'home_swap' AND column_name = 'user_id') THEN
        CREATE INDEX IF NOT EXISTS idx_home_swap_user_id ON home_swap(user_id);
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'home_swap' AND column_name = 'active') THEN
        CREATE INDEX IF NOT EXISTS idx_home_swap_active ON home_swap(active);
    END IF;
END $$;

-- Home Swap Photo indexes
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'home_swap_photo' AND column_name = 'home_swap_id') THEN
        CREATE INDEX IF NOT EXISTS idx_home_swap_photo_home_swap_id ON home_swap_photo(home_swap_id);
    END IF;
END $$;

-- Service Offers indexes (uses provider_id)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'service_offers' AND column_name = 'provider_id') THEN
        CREATE INDEX IF NOT EXISTS idx_service_offers_provider_id ON service_offers(provider_id);
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'service_offers' AND column_name = 'category') THEN
        CREATE INDEX IF NOT EXISTS idx_service_offers_category ON service_offers(category);
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'service_offers' AND column_name = 'active') THEN
        CREATE INDEX IF NOT EXISTS idx_service_offers_active ON service_offers(active);
    END IF;
END $$;

-- Service Bookings indexes (uses service_id and customer_id)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'service_bookings' AND column_name = 'service_id') THEN
        CREATE INDEX IF NOT EXISTS idx_service_bookings_service_id ON service_bookings(service_id);
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'service_bookings' AND column_name = 'customer_id') THEN
        CREATE INDEX IF NOT EXISTS idx_service_bookings_customer_id ON service_bookings(customer_id);
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'service_bookings' AND column_name = 'status') THEN
        CREATE INDEX IF NOT EXISTS idx_service_bookings_status ON service_bookings(status);
    END IF;
END $$;

-- Events indexes (uses organizer_id)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'events' AND column_name = 'organizer_id') THEN
        CREATE INDEX IF NOT EXISTS idx_events_organizer_id ON events(organizer_id);
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'events' AND column_name = 'event_date') THEN
        CREATE INDEX IF NOT EXISTS idx_events_event_date ON events(event_date);
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'events' AND column_name = 'active') THEN
        CREATE INDEX IF NOT EXISTS idx_events_active ON events(active);
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'events' AND column_name = 'featured') THEN
        CREATE INDEX IF NOT EXISTS idx_events_featured ON events(featured);
    END IF;
END $$;

-- Friend Requests indexes
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'friend_requests' AND column_name = 'sender_id') THEN
        CREATE INDEX IF NOT EXISTS idx_friend_requests_sender_id ON friend_requests(sender_id);
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'friend_requests' AND column_name = 'receiver_id') THEN
        CREATE INDEX IF NOT EXISTS idx_friend_requests_receiver_id ON friend_requests(receiver_id);
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'friend_requests' AND column_name = 'status') THEN
        CREATE INDEX IF NOT EXISTS idx_friend_requests_status ON friend_requests(status);
    END IF;
END $$;

-- Messages indexes (uses sender_id and recipient_id)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'messages' AND column_name = 'sender_id') THEN
        CREATE INDEX IF NOT EXISTS idx_messages_sender_id ON messages(sender_id);
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'messages' AND column_name = 'recipient_id') THEN
        CREATE INDEX IF NOT EXISTS idx_messages_recipient_id ON messages(recipient_id);
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'messages' AND column_name = 'created_at') THEN
        CREATE INDEX IF NOT EXISTS idx_messages_created_at ON messages(created_at);
    END IF;
END $$;

-- User Sessions indexes
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'user_sessions' AND column_name = 'user_id') THEN
        CREATE INDEX IF NOT EXISTS idx_user_sessions_user_id ON user_sessions(user_id);
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'user_sessions' AND column_name = 'expires_at') THEN
        CREATE INDEX IF NOT EXISTS idx_user_sessions_expires_at ON user_sessions(expires_at);
    END IF;
END $$;

-- Payments indexes (uses payer_id)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'payments' AND column_name = 'payer_id') THEN
        CREATE INDEX IF NOT EXISTS idx_payments_payer_id ON payments(payer_id);
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'payments' AND column_name = 'status') THEN
        CREATE INDEX IF NOT EXISTS idx_payments_status ON payments(status);
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'payments' AND column_name = 'stripe_payment_intent_id') THEN
        CREATE INDEX IF NOT EXISTS idx_payments_stripe_payment_intent_id ON payments(stripe_payment_intent_id);
    END IF;
END $$;

-- Password Reset Tokens indexes
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'password_reset_tokens' AND column_name = 'user_id') THEN
        CREATE INDEX IF NOT EXISTS idx_password_reset_tokens_user_id ON password_reset_tokens(user_id);
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'password_reset_tokens' AND column_name = 'expires_at') THEN
        CREATE INDEX IF NOT EXISTS idx_password_reset_tokens_expires_at ON password_reset_tokens(expires_at);
    END IF;
END $$;