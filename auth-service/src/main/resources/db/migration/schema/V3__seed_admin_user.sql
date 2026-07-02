-- Insert default admin user (password = admin123 → BCrypt)
INSERT INTO auth_users (email, password_hash)
VALUES (
    'admin@library.com',
    '$2a$10$A8djK6qIVh3/8894zfW5Zu3twfsC7VDMx.WDNJo3p3Pnl5YWZdu9.'
);

-- Assign ADMIN role
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM auth_users u, roles r
WHERE u.email = 'admin@library.com'
  AND r.name = 'ADMIN';