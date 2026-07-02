-- ==============================
-- INSERT ROLES
-- ==============================
INSERT INTO roles (name) VALUES
('ADMIN'),
('LIBRARIAN'),
('MEMBER');

-- ==============================
-- INSERT PERMISSIONS
-- ==============================
INSERT INTO permissions (name) VALUES
('BOOK_READ'),
('BOOK_WRITE'),
('BORROW_WRITE'),
('BORROW_READ'),
('RETURN_WRITE'),
('RETURN_READ'),
('INVENTORY_WRITE'),
('INVENTORY_READ'),
('USER_WRITE'),
('USER_READ');

-- ==============================
-- ADMIN → ALL PERMISSIONS
-- ==============================
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ADMIN';

-- ==============================
-- LIBRARIAN PERMISSIONS
-- ==============================
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.name IN (
                                'BOOK_READ',
                                'BOOK_WRITE',
                                'BORROW_WRITE',
                                'BORROW_READ',
                                'RETURN_WRITE',
                                'RETURN_READ',
                                'INVENTORY_WRITE',
                                'INVENTORY_READ',
                                'USER_READ'
                            )
WHERE r.name = 'LIBRARIAN';

-- ==============================
-- MEMBER PERMISSIONS
-- ==============================
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.name IN (
    'BOOK_READ',
    'BORROW_WRITE',
    'BORROW_READ',
    'RETURN_WRITE',
    'RETURN_READ'
)
WHERE r.name = 'MEMBER';