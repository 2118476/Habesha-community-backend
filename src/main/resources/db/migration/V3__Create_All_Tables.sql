-- Create all required tables for Habesha Community Backend
-- This migration ensures all entity tables exist with proper PostgreSQL types

-- Classified Ads table
CREATE TABLE IF NOT EXISTS classified_ads (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2),
    category VARCHAR(100),
    location VARCHAR(255),
    contact_info VARCHAR(500),
    user_id BIGINT,
    featured BOOLEAN DEFAULT false,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Ad Photos table
CREATE TABLE IF NOT EXISTS ad_photo (
    id BIGSERIAL PRIMARY KEY,
    ad_id BIGINT NOT NULL,
    filename VARCHAR(255),
    path VARCHAR(500),
    content_type VARCHAR(100),
    file_size BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ad_id) REFERENCES classified_ads(id) ON DELETE CASCADE
);

-- Ad Comments table
CREATE TABLE IF NOT EXISTS ad_comments (
    id BIGSERIAL PRIMARY KEY,
    ad_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    parent_id BIGINT,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ad_id) REFERENCES classified_ads(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_id) REFERENCES ad_comments(id) ON DELETE CASCADE
);

-- Ad Likes table
CREATE TABLE IF NOT EXISTS ad_likes (
    id BIGSERIAL PRIMARY KEY,
    ad_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ad_id) REFERENCES classified_ads(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE(ad_id, user_id)
);

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
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Rentals table
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
    user_id BIGINT,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Rental Photos table
CREATE TABLE IF NOT EXISTS rental_photo (
    id BIGSERIAL PRIMARY KEY,
    rental_id BIGINT NOT NULL,
    filename VARCHAR(255),
    path VARCHAR(500),
    content_type VARCHAR(100),
    file_size BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (rental_id) REFERENCES rentals(id) ON DELETE CASCADE
);

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
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Home Swap Photos table
CREATE TABLE IF NOT EXISTS home_swap_photo (
    id BIGSERIAL PRIMARY KEY,
    home_swap_id BIGINT NOT NULL,
    filename VARCHAR(255),
    path VARCHAR(500),
    content_type VARCHAR(100),
    file_size BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (home_swap_id) REFERENCES home_swap(id) ON DELETE CASCADE
);

-- Service Offers table
CREATE TABLE IF NOT EXISTS service_offers (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(100),
    price DECIMAL(10,2),
    location VARCHAR(255),
    delivery_type VARCHAR(50), -- 'IN_PERSON', 'ONLINE', 'BOTH'
    user_id BIGINT,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Service Bookings table
CREATE TABLE IF NOT EXISTS service_bookings (
    id BIGSERIAL PRIMARY KEY,
    service_offer_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    booking_date TIMESTAMP,
    status VARCHAR(50) DEFAULT 'PENDING',
    notes TEXT,
    total_amount DECIMAL(10,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (service_offer_id) REFERENCES service_offers(id) ON DELETE CASCADE,
    FOREIGN KEY (customer_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Events table
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
    user_id BIGINT,
    featured BOOLEAN DEFAULT false,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Friend Requests table
CREATE TABLE IF NOT EXISTS friend_requests (
    id BIGSERIAL PRIMARY KEY,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING',
    message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE(sender_id, receiver_id)
);

-- Messages table
CREATE TABLE IF NOT EXISTS messages (
    id BIGSERIAL PRIMARY KEY,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    message_type VARCHAR(50) DEFAULT 'TEXT',
    read_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE
);

-- User Sessions table
CREATE TABLE IF NOT EXISTS user_sessions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    session_token VARCHAR(500) UNIQUE NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_accessed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- User Reports table
CREATE TABLE IF NOT EXISTS user_report (
    id BIGSERIAL PRIMARY KEY,
    reporter_id BIGINT NOT NULL,
    reported_user_id BIGINT NOT NULL,
    reason VARCHAR(255),
    description TEXT,
    status VARCHAR(50) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (reporter_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (reported_user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- User Blocks table
CREATE TABLE IF NOT EXISTS user_block (
    id BIGSERIAL PRIMARY KEY,
    blocker_id BIGINT NOT NULL,
    blocked_user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (blocker_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (blocked_user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE(blocker_id, blocked_user_id)
);

-- Contact Requests table
CREATE TABLE IF NOT EXISTS contact_request (
    id BIGSERIAL PRIMARY KEY,
    requester_id BIGINT NOT NULL,
    target_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING',
    message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (requester_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (target_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE(requester_id, target_id, type, status)
);

-- Payments table
CREATE TABLE IF NOT EXISTS payments (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    stripe_payment_intent_id VARCHAR(255),
    amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'GBP',
    status VARCHAR(50) DEFAULT 'PENDING',
    payment_type VARCHAR(100),
    reference_id BIGINT,
    reference_type VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Password Reset Tokens table
CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(500) UNIQUE NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Account Deletion Requests table
CREATE TABLE IF NOT EXISTS account_deletion_requests (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    reason TEXT,
    status VARCHAR(50) DEFAULT 'PENDING',
    scheduled_deletion_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_classified_ads_user_id ON classified_ads(user_id);
CREATE INDEX IF NOT EXISTS idx_classified_ads_category ON classified_ads(category);
CREATE INDEX IF NOT EXISTS idx_classified_ads_active ON classified_ads(active);
CREATE INDEX IF NOT EXISTS idx_classified_ads_featured ON classified_ads(featured);

CREATE INDEX IF NOT EXISTS idx_ad_photo_ad_id ON ad_photo(ad_id);
CREATE INDEX IF NOT EXISTS idx_ad_comments_ad_id ON ad_comments(ad_id);
CREATE INDEX IF NOT EXISTS idx_ad_comments_user_id ON ad_comments(user_id);
CREATE INDEX IF NOT EXISTS idx_ad_likes_ad_id ON ad_likes(ad_id);
CREATE INDEX IF NOT EXISTS idx_ad_likes_user_id ON ad_likes(user_id);

CREATE INDEX IF NOT EXISTS idx_travel_posts_user_id ON travel_posts(user_id);
CREATE INDEX IF NOT EXISTS idx_travel_posts_departure_date ON travel_posts(departure_date);
CREATE INDEX IF NOT EXISTS idx_travel_posts_active ON travel_posts(active);

CREATE INDEX IF NOT EXISTS idx_rentals_user_id ON rentals(user_id);
CREATE INDEX IF NOT EXISTS idx_rentals_location ON rentals(location);
CREATE INDEX IF NOT EXISTS idx_rentals_active ON rentals(active);
CREATE INDEX IF NOT EXISTS idx_rental_photo_rental_id ON rental_photo(rental_id);

CREATE INDEX IF NOT EXISTS idx_home_swap_user_id ON home_swap(user_id);
CREATE INDEX IF NOT EXISTS idx_home_swap_active ON home_swap(active);
CREATE INDEX IF NOT EXISTS idx_home_swap_photo_home_swap_id ON home_swap_photo(home_swap_id);

CREATE INDEX IF NOT EXISTS idx_service_offers_user_id ON service_offers(user_id);
CREATE INDEX IF NOT EXISTS idx_service_offers_category ON service_offers(category);
CREATE INDEX IF NOT EXISTS idx_service_offers_active ON service_offers(active);

CREATE INDEX IF NOT EXISTS idx_service_bookings_service_offer_id ON service_bookings(service_offer_id);
CREATE INDEX IF NOT EXISTS idx_service_bookings_customer_id ON service_bookings(customer_id);
CREATE INDEX IF NOT EXISTS idx_service_bookings_status ON service_bookings(status);

CREATE INDEX IF NOT EXISTS idx_events_user_id ON events(user_id);
CREATE INDEX IF NOT EXISTS idx_events_event_date ON events(event_date);
CREATE INDEX IF NOT EXISTS idx_events_active ON events(active);
CREATE INDEX IF NOT EXISTS idx_events_featured ON events(featured);

CREATE INDEX IF NOT EXISTS idx_friend_requests_sender_id ON friend_requests(sender_id);
CREATE INDEX IF NOT EXISTS idx_friend_requests_receiver_id ON friend_requests(receiver_id);
CREATE INDEX IF NOT EXISTS idx_friend_requests_status ON friend_requests(status);

CREATE INDEX IF NOT EXISTS idx_messages_sender_id ON messages(sender_id);
CREATE INDEX IF NOT EXISTS idx_messages_receiver_id ON messages(receiver_id);
CREATE INDEX IF NOT EXISTS idx_messages_created_at ON messages(created_at);

CREATE INDEX IF NOT EXISTS idx_user_sessions_user_id ON user_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_user_sessions_expires_at ON user_sessions(expires_at);

CREATE INDEX IF NOT EXISTS idx_payments_user_id ON payments(user_id);
CREATE INDEX IF NOT EXISTS idx_payments_status ON payments(status);
CREATE INDEX IF NOT EXISTS idx_payments_stripe_payment_intent_id ON payments(stripe_payment_intent_id);

CREATE INDEX IF NOT EXISTS idx_password_reset_tokens_user_id ON password_reset_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_password_reset_tokens_expires_at ON password_reset_tokens(expires_at);