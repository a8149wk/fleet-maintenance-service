-- =====================================================
-- V7: Application key/value settings + Branding admin menu.
--
-- "app_settings" stores admin-editable runtime configuration. The
-- first known key is LOGO_PATH (absolute or relative path to the
-- currently-uploaded logo file); future keys may include SITE_TITLE,
-- PRIMARY_COLOR, etc.
-- =====================================================

CREATE TABLE IF NOT EXISTS app_settings (
    setting_key   VARCHAR(80) PRIMARY KEY,
    setting_value TEXT,
    updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Seed the LOGO_PATH row so updates are simple UPDATEs (no upsert needed).
INSERT INTO app_settings (setting_key, setting_value)
VALUES ('LOGO_PATH', NULL)
ON CONFLICT (setting_key) DO NOTHING;

-- Add the Branding admin menu and grant it to ADMIN by default.
INSERT INTO menus (code, label, icon, path, section, sort_order)
VALUES ('admin_branding', 'Branding', 'fas fa-image', '/admin/branding', 'administration', 115)
ON CONFLICT (code) DO NOTHING;

INSERT INTO role_menus (role_id, menu_id)
SELECT r.id, m.id
FROM roles r
JOIN menus m ON m.code = 'admin_branding'
WHERE r.name = 'ADMIN'
ON CONFLICT DO NOTHING;
