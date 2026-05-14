-- =====================================================
-- V9: Partner workshop + parts supplier portals,
-- external estimate approval workflow, and notifications.
-- =====================================================

CREATE TABLE IF NOT EXISTS user_workshops (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    workshop_id  BIGINT NOT NULL REFERENCES workshops(id) ON DELETE CASCADE,
    UNIQUE (user_id, workshop_id)
);

CREATE INDEX IF NOT EXISTS idx_user_workshops_workshop ON user_workshops(workshop_id);

CREATE TABLE IF NOT EXISTS work_order_supplier_users (
    id              BIGSERIAL PRIMARY KEY,
    work_order_id   BIGINT NOT NULL REFERENCES work_orders(id) ON DELETE CASCADE,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE (work_order_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_wosu_user ON work_order_supplier_users(user_id);

ALTER TABLE work_orders ADD COLUMN IF NOT EXISTS estimate_approval_status VARCHAR(30) NOT NULL DEFAULT 'NOT_REQUIRED';
ALTER TABLE work_orders ADD COLUMN IF NOT EXISTS estimate_submitted_by BIGINT REFERENCES users(id);
ALTER TABLE work_orders ADD COLUMN IF NOT EXISTS estimate_reviewed_by BIGINT REFERENCES users(id);
ALTER TABLE work_orders ADD COLUMN IF NOT EXISTS estimate_reviewed_at TIMESTAMP;
ALTER TABLE work_orders ADD COLUMN IF NOT EXISTS estimate_approval_remark TEXT;

UPDATE work_orders SET estimate_approval_status = 'NOT_REQUIRED'
WHERE estimate_approval_status IS NULL;

-- ---- Roles ----
INSERT INTO roles (name, description) VALUES
    ('ROLE_PARTNER_WORKSHOP', 'Partner workshop portal — view assigned work orders and submit estimates'),
    ('ROLE_PARTS_SUPPLIER', 'Spare parts supplier portal — view assigned work orders and submit estimates')
ON CONFLICT (name) DO NOTHING;

-- ---- Seed portal users (password: password123, same BCrypt as V4) ----
INSERT INTO users (username, email, password, full_name, phone, is_active) VALUES
    ('partner1', 'partner1@autoprima.com', '$2a$10$TXB1Iw9yXLraSgy57hEn.OWSSNoyKkmN4Ec.w17.X0QV855l8q4sG',
     'Auto Prima Partner Portal', '081234560003', true),
    ('supplier1', 'supplier1@parts.local', '$2a$10$TXB1Iw9yXLraSgy57hEn.OWSSNoyKkmN4Ec.w17.X0QV855l8q4sG',
     'Parts Supplier One', '081000000099', true)
ON CONFLICT (username) DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u CROSS JOIN roles r
WHERE u.username = 'partner1' AND r.name = 'ROLE_PARTNER_WORKSHOP'
ON CONFLICT (user_id, role_id) DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u CROSS JOIN roles r
WHERE u.username = 'supplier1' AND r.name = 'ROLE_PARTS_SUPPLIER'
ON CONFLICT (user_id, role_id) DO NOTHING;

INSERT INTO user_workshops (user_id, workshop_id)
SELECT u.id, 3 FROM users u WHERE u.username = 'partner1'
ON CONFLICT (user_id, workshop_id) DO NOTHING;

INSERT INTO app_settings (setting_key, setting_value)
VALUES ('ESTIMATE_APPROVER_ROLES', 'ROLE_ADMIN,ROLE_MANAGER,ROLE_FINANCE')
ON CONFLICT (setting_key) DO NOTHING;

-- ---- Navigation menus ----
INSERT INTO menus (code, label, icon, path, section, sort_order) VALUES
    ('portal_partner', 'Partner portal', 'fas fa-handshake', '/partner/workorders', 'main', 25),
    ('portal_supplier', 'Supplier portal', 'fas fa-cubes', '/supplier/workorders', 'main', 26),
    ('approvals_estimates', 'Estimate approvals', 'fas fa-clipboard-check', '/approvals/estimates', 'operations', 45),
    ('admin_approval_setup', 'Approval setup', 'fas fa-sliders', '/admin/approval-settings', 'administration', 118)
ON CONFLICT (code) DO NOTHING;

INSERT INTO role_menus (role_id, menu_id)
SELECT r.id, m.id FROM roles r JOIN menus m ON m.code IN ('portal_partner', 'dashboard')
WHERE r.name = 'ROLE_PARTNER_WORKSHOP'
ON CONFLICT (role_id, menu_id) DO NOTHING;

INSERT INTO role_menus (role_id, menu_id)
SELECT r.id, m.id FROM roles r JOIN menus m ON m.code IN ('portal_supplier', 'dashboard')
WHERE r.name = 'ROLE_PARTS_SUPPLIER'
ON CONFLICT (role_id, menu_id) DO NOTHING;

INSERT INTO role_menus (role_id, menu_id)
SELECT r.id, m.id FROM roles r JOIN menus m ON m.code = 'approvals_estimates'
WHERE r.name IN ('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_FINANCE')
ON CONFLICT (role_id, menu_id) DO NOTHING;

INSERT INTO role_menus (role_id, menu_id)
SELECT r.id, m.id FROM roles r JOIN menus m ON m.code = 'admin_approval_setup'
WHERE r.name = 'ROLE_ADMIN'
ON CONFLICT (role_id, menu_id) DO NOTHING;
