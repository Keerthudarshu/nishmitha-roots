-- PostgreSQL conversion of MySQL dump for Neon database
-- This inserts sample users into the PostgreSQL database

-- Note: In PostgreSQL, bit(1) is replaced with BOOLEAN
-- and AUTO_INCREMENT becomes SERIAL

-- Sample users for testing (converted from MySQL dump)
INSERT INTO users (id, created_at, date_of_birth, email, gender, is_active, last_password_change, loyalty_points, member_since, name, password_hash, phone, role, total_orders, updated_at) 
VALUES 
(7, '2025-09-24 10:35:10.000000', NULL, 'admin@gmail.com', NULL, true, '2025-09-24 10:35:10.000000', 0, '2025-09-24', 'Admin', 'Admin@123', '8197277941', 'admin', 0, '2025-09-24 10:35:10.000000'),
(8, '2025-09-24 05:10:28.779408', '2001-07-18', 'nishu@gmail.com', 'Female', true, '2025-09-27 07:33:58.236364', 0, '2025-09-24', 'SS', 'Nishu@123', '8197277941', 'user', 3, '2025-09-29 11:17:10.416344')
ON CONFLICT (email) DO NOTHING;

-- Update sequence to continue from inserted IDs
SELECT setval('users_id_seq', (SELECT MAX(id) FROM users), true);