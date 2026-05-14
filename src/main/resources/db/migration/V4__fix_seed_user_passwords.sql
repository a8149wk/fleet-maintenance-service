-- =====================================================
-- V4: Repair seed user passwords.
-- The BCrypt hash embedded in V2__sample_data.sql does NOT
-- actually decode to "password123" (verified with
-- BCryptPasswordEncoder.matches -> false). Replace it with
-- a freshly-generated BCrypt hash that DOES verify against
-- "password123" so the seeded accounts (admin / manager /
-- finance / client1 / mechanic1) can log in.
-- =====================================================

UPDATE users
SET password = '$2a$10$TXB1Iw9yXLraSgy57hEn.OWSSNoyKkmN4Ec.w17.X0QV855l8q4sG'
WHERE username IN ('admin', 'manager', 'finance', 'client1', 'mechanic1');
