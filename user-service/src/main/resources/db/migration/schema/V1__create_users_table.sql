-- =====================================================
-- USER SERVICE - USERS TABLE WITH CHECK CONSTRAINTS
-- =====================================================

CREATE TABLE users (
    id BIGINT PRIMARY KEY,

    email VARCHAR(255) UNIQUE NOT NULL,

    full_name VARCHAR(150),
    phone VARCHAR(20),

    membership_type VARCHAR(50) NOT NULL DEFAULT 'STANDARD',
    membership_status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_membership_type
        CHECK (membership_type IN ('STANDARD', 'PREMIUM', 'STUDENT', 'STAFF')),

    CONSTRAINT chk_membership_status
        CHECK (membership_status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED'))
);

CREATE INDEX idx_users_email ON users(email);

CREATE INDEX idx_users_membership_status ON users(membership_status);