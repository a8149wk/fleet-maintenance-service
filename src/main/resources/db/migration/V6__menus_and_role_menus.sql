-- =====================================================
-- V6: Navigation menus + role -> menu mapping.
--
-- Lets the sidebar render menu items per user based on their roles
-- (which the admin UI can edit at runtime). Seed data here matches
-- the previous hardcoded navbar so behaviour is identical on first
-- migration; admins can then reshape it via the UI.
-- =====================================================

CREATE TABLE IF NOT EXISTS menus (
    id           BIGSERIAL PRIMARY KEY,
    code         VARCHAR(60)  NOT NULL UNIQUE,
    label        VARCHAR(100) NOT NULL,
    icon         VARCHAR(60),
    path         VARCHAR(200),
    section      VARCHAR(50),
    sort_order   INTEGER      NOT NULL DEFAULT 0,
    is_active    BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_menus_section_sort ON menus(section, sort_order);

CREATE TABLE IF NOT EXISTS role_menus (
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    menu_id BIGINT NOT NULL REFERENCES menus(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, menu_id)
);

CREATE INDEX IF NOT EXISTS idx_role_menus_menu ON role_menus(menu_id);

-- ---- Seed menu items (matches the previously hardcoded sidebar) ----
INSERT INTO menus (code, label, icon, path, section, sort_order) VALUES
    ('dashboard',  'Dashboard',     'fas fa-house',          '/',           'main',       10),
    ('workorders', 'Work Orders',   'fas fa-clipboard-list', '/workorders', 'main',       20),
    ('vehicles',   'Vehicles',      'fas fa-car',            '/vehicles',   'main',       30),
    ('inventory',  'Inventory',     'fas fa-boxes-stacked',  '/inventory',  'operations', 40),
    ('invoices',   'Invoices',      'fas fa-file-invoice',   '/invoices',   'operations', 50),
    ('clients',    'Clients',       'fas fa-building',       '/clients',    'operations', 60),
    ('workshops',  'Workshops',     'fas fa-wrench',         '/workshops',  'operations', 70),
    ('mechanics',  'Mechanics',     'fas fa-user-gear',      '/mechanics',  'operations', 80),
    ('reports',    'Reports',       'fas fa-chart-line',     '/reports',    'analytics',  90),
    ('admin_users','Users',         'fas fa-users',          '/admin/users','administration', 100),
    ('admin_roles','Roles & Menus', 'fas fa-user-shield',    '/admin/roles','administration', 110),
    ('api_docs',   'API Sandbox',   'fas fa-code',           '/swagger-ui.html', 'administration', 120)
ON CONFLICT (code) DO NOTHING;

-- ---- Role mappings (granted on insert; admins can edit later) ----
-- ADMIN sees everything.
INSERT INTO role_menus (role_id, menu_id)
SELECT r.id, m.id
FROM roles r
CROSS JOIN menus m
WHERE r.name = 'ADMIN'
ON CONFLICT DO NOTHING;

-- MANAGER: operations + analytics, no admin/api-docs.
INSERT INTO role_menus (role_id, menu_id)
SELECT r.id, m.id
FROM roles r
JOIN menus m ON m.code IN (
    'dashboard','workorders','vehicles','inventory','invoices',
    'clients','workshops','mechanics','reports'
)
WHERE r.name = 'MANAGER'
ON CONFLICT DO NOTHING;

-- FINANCE: dashboard + invoices + reports only.
INSERT INTO role_menus (role_id, menu_id)
SELECT r.id, m.id
FROM roles r
JOIN menus m ON m.code IN ('dashboard', 'invoices', 'reports')
WHERE r.name = 'FINANCE'
ON CONFLICT DO NOTHING;

-- MECHANIC: dashboard + work orders + inventory.
INSERT INTO role_menus (role_id, menu_id)
SELECT r.id, m.id
FROM roles r
JOIN menus m ON m.code IN ('dashboard', 'workorders', 'inventory')
WHERE r.name = 'MECHANIC'
ON CONFLICT DO NOTHING;

-- CLIENT: dashboard + work orders + vehicles + invoices (their own).
INSERT INTO role_menus (role_id, menu_id)
SELECT r.id, m.id
FROM roles r
JOIN menus m ON m.code IN ('dashboard', 'workorders', 'vehicles', 'invoices')
WHERE r.name = 'CLIENT'
ON CONFLICT DO NOTHING;
