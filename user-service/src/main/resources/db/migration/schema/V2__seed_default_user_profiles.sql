-- =====================================================
-- DEFAULT ADMIN PROFILE
-- This ID must match auth_users.id from Auth Service.
-- Use only for local/dev seed data.
-- =====================================================

INSERT INTO users (
    id,
    email,
    full_name,
    phone,
    membership_type,
    membership_status
)
VALUES (
    1,
    'admin@library.com',
    'Admin User',
    '9876543210',
    'PREMIUM',
    'ACTIVE'
);