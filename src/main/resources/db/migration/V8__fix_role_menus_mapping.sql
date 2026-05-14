-- =====================================================
-- V8: Repair the role -> menu mapping seeded by V6 / V7.
--
-- The original V6/V7 INSERTs filtered roles with unprefixed names
-- ('ADMIN', 'MANAGER', ...) but this codebase actually seeds role
-- rows WITH the 'ROLE_' prefix in V2. Result: those INSERTs matched
-- zero rows and the sidebar rendered empty for every user.
--
-- This migration re-runs the same mapping logic but accepts BOTH
-- naming conventions so existing installs are repaired without
-- touching V6/V7 history (Flyway repair() in FlywayConfig keeps
-- their checksums in sync after the in-source body fix).
-- =====================================================

-- ADMIN sees everything.
INSERT INTO role_menus (role_id, menu_id)
SELECT r.id, m.id
FROM roles r
CROSS JOIN menus m
WHERE r.name IN ('ADMIN', 'ROLE_ADMIN')
ON CONFLICT DO NOTHING;

-- MANAGER: operations + analytics.
INSERT INTO role_menus (role_id, menu_id)
SELECT r.id, m.id
FROM roles r
JOIN menus m ON m.code IN (
    'dashboard','workorders','vehicles','inventory','invoices',
    'clients','workshops','mechanics','reports'
)
WHERE r.name IN ('MANAGER', 'ROLE_MANAGER')
ON CONFLICT DO NOTHING;

-- FINANCE: dashboard + invoices + reports only.
INSERT INTO role_menus (role_id, menu_id)
SELECT r.id, m.id
FROM roles r
JOIN menus m ON m.code IN ('dashboard', 'invoices', 'reports')
WHERE r.name IN ('FINANCE', 'ROLE_FINANCE')
ON CONFLICT DO NOTHING;

-- MECHANIC: dashboard + work orders + inventory.
INSERT INTO role_menus (role_id, menu_id)
SELECT r.id, m.id
FROM roles r
JOIN menus m ON m.code IN ('dashboard', 'workorders', 'inventory')
WHERE r.name IN ('MECHANIC', 'ROLE_MECHANIC')
ON CONFLICT DO NOTHING;

-- CLIENT: dashboard + work orders + vehicles + invoices.
INSERT INTO role_menus (role_id, menu_id)
SELECT r.id, m.id
FROM roles r
JOIN menus m ON m.code IN ('dashboard', 'workorders', 'vehicles', 'invoices')
WHERE r.name IN ('CLIENT', 'ROLE_CLIENT')
ON CONFLICT DO NOTHING;
