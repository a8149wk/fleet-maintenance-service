-- =====================================================
-- V5: Rebrand seed data on already-deployed databases.
-- Earlier installs of V2 wrote workshop / mechanic / system
-- setting rows using legacy branded strings. V2 itself has
-- been rewritten in-place to use neutral values, and a
-- FlywayMigrationStrategy bean now runs flyway.repair()
-- before migrate() so the historical checksum is realigned
-- automatically. This V5 patches the live data so existing
-- environments end up with the same neutral values that a
-- fresh install of V2 now produces.
-- =====================================================

UPDATE workshops
SET name  = 'Central Workshop',
    email = 'central@fms.local'
WHERE code = 'WS001';

UPDATE workshops
SET name  = 'Jakarta Branch Workshop',
    email = 'jakarta@fms.local'
WHERE code = 'WS002';

UPDATE mechanics SET email = 'ahmad@fms.local' WHERE code = 'MEC001';
UPDATE mechanics SET email = 'budi@fms.local'  WHERE code = 'MEC002';
UPDATE mechanics SET email = 'cecep@fms.local' WHERE code = 'MEC003';
UPDATE mechanics SET email = 'dedi@fms.local'  WHERE code = 'MEC004';
UPDATE mechanics SET email = 'eko@fms.local'   WHERE code = 'MEC005';

UPDATE system_settings SET setting_value = 'FleetOps'        WHERE setting_key = 'company_name';
UPDATE system_settings SET setting_value = 'info@fms.local'  WHERE setting_key = 'company_email';
